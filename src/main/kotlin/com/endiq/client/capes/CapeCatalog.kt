package com.endiq.client.capes

import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Base64
import java.util.UUID
import java.util.regex.Pattern

/**
 * Where capes come from.
 *
 * Every parse here works on plain text or JSON and is covered by unit tests that
 * never open a socket; only [Transport] touches the network, and it is injected so
 * the download path is testable too. The three sources are the ones a client can
 * actually resolve: the player's own Mojang profile cape, the player's LabyMod
 * cape, and NameMC's public index of official capes.
 */
object CapeCatalog {
    /** NameMC's cape index: one page listing every cape it knows, with display names. */
    const val NAME_MC_INDEX = "https://namemc.com/capes"

    /** A page of capes is small; this bounds both the list and the total download. */
    const val MAX_CATALOGUE = 64
    const val MAX_TEXTURE_BYTES = 512 * 1024
    private const val USER_AGENT = "TurtleClient/1.0 (+https://github.com/Endiq-jar/TurtleClient)"

    private val CAPE_LINK: Pattern = Pattern.compile("/cape/([0-9a-f]{16})")
    private val LABEL: Pattern = Pattern.compile("(?:alt|title)=\"([^\"]{1,48})\"")
    private val TAG: Pattern = Pattern.compile("<[^>]*>")
    private val COUNT: Pattern = Pattern.compile("[\\d,\\s.]+")

    /** One cape NameMC lists: its texture hash and the name shown beside it. */
    data class Reference(val hash: String, val name: String)

    fun isHash(value: String) = value.length == 16 && value.all { it in '0'..'9' || it in 'a'..'f' }

    /**
     * NameMC serves cape textures from a content-addressed path built from the hash.
     * Example: 8c05ef3c54870d04 -> https://texture.namemc.com/8c/05/8c05ef3c54870d04.png
     */
    fun nameMcTexture(hash: String): String {
        require(isHash(hash)) { "Not a cape texture hash: $hash" }
        return "https://texture.namemc.com/${hash.substring(0, 2)}/${hash.substring(2, 4)}/$hash.png"
    }

    /**
     * Cape references in document order. The index page is HTML, not an API, so the
     * name is taken from the anchor's own text and only falls back to a hash label
     * when NameMC changes its markup. Unparsable input yields an empty list, never a
     * crash.
     */
    fun nameMcCapes(html: String): List<Reference> {
        val found = linkedMapOf<String, String>()
        val links = CAPE_LINK.matcher(html)
        while (links.find() && found.size < MAX_CATALOGUE) {
            val hash = links.group(1) ?: continue
            if (!found.containsKey(hash)) found[hash] = anchorText(html, links.end())
        }
        return found.map { (hash, name) -> Reference(hash, name.ifBlank { "Cape ${hash.take(6)}" }) }
    }

    /**
     * The visible label of the anchor whose href ended at [from]: text first, attributes second.
     * [from] sits inside the opening tag, so the rest of that tag is skipped before any
     * text is read -- otherwise the trailing `">` of the attribute leaks into the name.
     */
    private fun anchorText(html: String, from: Int): String {
        val open = html.indexOf('>', from)
        val start = if (open in 0..from + 400) open + 1 else from
        val end = (html.indexOf("</a>", start).takeIf { it in 0..start + 1200 } ?: start + 900).coerceAtMost(html.length)
        val body = html.substring(start.coerceAtMost(html.length), end)
        val words = unescape(TAG.matcher(body).replaceAll(" ")).split(Regex("\\s+"))
            .filter { it.isNotBlank() && !COUNT.matcher(it).matches() }
        if (words.isNotEmpty()) return words.joinToString(" ").take(48)
        val attributes = LABEL.matcher(html.substring(from, end))
        return if (attributes.find()) unescape(attributes.group(1)) else ""
    }

    private fun unescape(value: String) = value.replace("&amp;", "&").replace("&#39;", "'").replace("&#8217;", "’")
        .replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ").trim()

    /** Mojang's session server answers with a base64 texture blob that may or may not hold a cape. */
    fun mojangProfile(uuid: UUID) = "https://sessionserver.mojang.com/session/minecraft/profile/$uuid"

    /** The profile's base64 texture blob holds the cape URL when the account has a cape. */
    fun mojangCapeUrl(profileJson: String): String? = runCatching {
        val properties = JsonParser.parseString(profileJson).asJsonObject["properties"]?.asJsonArray
        val encoded = mutableListOf<String>()
        if (properties != null) for (property in properties) {
            runCatching { property.asJsonObject["value"].asString }.getOrNull()?.let { encoded += it }
        }
        val decoded = encoded.mapNotNull { runCatching { String(Base64.getDecoder().decode(it), Charsets.UTF_8) }.getOrNull() }
            .mapNotNull { runCatching { JsonParser.parseString(it).asJsonObject }.getOrNull() }
        val textures = decoded.firstOrNull { it["textures"]?.isJsonObject == true }?.getAsJsonObject("textures")
        val cape = textures?.get("CAPE")?.takeIf { it.isJsonObject }?.asJsonObject
        val url = cape?.get("url")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
        url?.let { secure(it) }
    }.getOrNull()

    /** LabyMod answers with the cape PNG itself, or 404 when the account has none. */
    fun labymodCape(uuid: UUID) = "https://dl.labymod.net/capes/$uuid"

    /** Mojang publishes http texture URLs; only https is fetched. */
    fun secure(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        return when (uri.scheme?.lowercase()) {
            "https" -> url
            "http" -> "https" + url.removePrefix("http")
            else -> null
        }
    }

    /** The only network seam. Returns the body for a 2xx response, or null for anything else. */
    fun interface Transport {
        fun get(url: String): ByteArray?
    }

    object Http : Transport {
        private val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        override fun get(url: String): ByteArray? = runCatching {
            val request = HttpRequest.newBuilder(URI(url)).timeout(Duration.ofSeconds(15))
                .header("User-Agent", USER_AGENT).header("Accept", "*/*").GET().build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
            val body = response.body()
            if (body == null || response.statusCode() !in 200..299 || body.size > MAX_TEXTURE_BYTES) null else body
        }.getOrNull()
    }
}

/** Convenience for the JSON/HTML sources, which are text. */
fun CapeCatalog.Transport.text(url: String): String? = get(url)?.toString(Charsets.UTF_8)
