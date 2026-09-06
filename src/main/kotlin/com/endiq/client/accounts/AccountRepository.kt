package com.endiq.client.accounts

import com.endiq.client.config.JsonFiles
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.nio.file.Path
import java.util.UUID

/** Stores account names/UUIDs and a PUBLIC OAuth client ID, never passwords or tokens. */
class AccountRepository(private val path: Path) {
    val profiles = mutableListOf<AccountProfile>()
    var microsoftClientId: String = ""
    fun load() {
        val json = JsonFiles.read(path)
        microsoftClientId = json.get("microsoftClientId")?.asString.orEmpty()
        profiles.clear()
        for (entry in json.getAsJsonArray("accounts") ?: JsonArray()) runCatching {
            val item=entry.asJsonObject
            val kind=AccountProfile.Kind.valueOf(item["kind"].asString)
            if(kind!=AccountProfile.Kind.LAUNCHER) {
                val profile=AccountProfile(UUID.fromString(item["id"].asString),item["name"].asString,kind)
                require(profile.name.matches(Regex("[A-Za-z0-9_]{3,16}")))
                require(kind!=AccountProfile.Kind.OFFLINE || AccountProfile.offline(profile.name).id==profile.id)
                if(profiles.none { it.id==profile.id }) profiles.add(profile)
            }
        }
    }
    fun remember(profile: AccountProfile) {
        profiles.removeAll { it.id==profile.id };if(profile.kind!=AccountProfile.Kind.LAUNCHER) profiles.add(profile)
        save()
    }
    fun remove(id: UUID) { profiles.removeAll { it.id==id };save() }
    fun save() {
        val accounts=JsonArray()
        profiles.filter { it.kind!=AccountProfile.Kind.LAUNCHER }.forEach { profile ->
            accounts.add(JsonObject().apply { addProperty("id",profile.id.toString());addProperty("name",profile.name);addProperty("kind",profile.kind.name) })
        }
        JsonFiles.write(path,JsonObject().apply { addProperty("schema",1);addProperty("microsoftClientId",microsoftClientId);add("accounts",accounts) })
    }
}
