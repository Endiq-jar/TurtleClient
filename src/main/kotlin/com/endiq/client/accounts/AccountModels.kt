package com.endiq.client.accounts

import java.nio.charset.StandardCharsets
import java.util.UUID

/** Only this non-secret metadata is eligible for persistence. */
data class AccountProfile(val id: UUID, val name: String, val kind: Kind) {
    enum class Kind { LAUNCHER, MICROSOFT, OFFLINE }
    companion object {
        fun offline(name: String): AccountProfile {
            require(name.matches(Regex("[A-Za-z0-9_]{3,16}"))) { "Use 3–16 letters, numbers, or underscores." }
            return AccountProfile(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(StandardCharsets.UTF_8)),name,Kind.OFFLINE)
        }
    }
}
/** Intentionally NOT a data class: no generated toString/copy exposing bearer tokens. */
class AccountCredentials(val profile: AccountProfile, private val token: String, val expiresAt: Long,
                         val xuid: String? = null, val clientId: String? = null) {
    fun tokenForSession() = token
    fun usable(now: Long = System.currentTimeMillis()) = profile.kind != AccountProfile.Kind.MICROSOFT || now + 30_000 < expiresAt
    override fun toString() = "AccountCredentials(redacted)"
}
class AccountProblem(message: String) : Exception(message)

/** A saved Microsoft row must never hide the session supplied by the launcher. */
object AccountSelection {
    fun rows(saved:List<AccountProfile>,launcher:AccountProfile)=
        (listOf(launcher)+saved).distinctBy { it.id }

    fun credentials(profile:AccountProfile,launcher:AccountCredentials,cached:Map<UUID,AccountCredentials>,
                    now:Long=System.currentTimeMillis()):AccountCredentials? = when {
        profile.kind==AccountProfile.Kind.LAUNCHER && profile.id==launcher.profile.id ->
            cached[profile.id]?.takeIf { it.usable(now) } ?: launcher
        profile.kind==AccountProfile.Kind.OFFLINE -> AccountCredentials(profile,"0",Long.MAX_VALUE)
        else -> cached[profile.id]?.takeIf { it.usable(now) }
    }
}
