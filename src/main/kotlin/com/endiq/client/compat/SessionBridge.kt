package com.endiq.client.compat

import com.endiq.client.accounts.AccountCredentials
import com.endiq.client.accounts.AccountProfile
import com.endiq.client.accounts.AccountProblem
import com.mojang.authlib.minecraft.UserApiService
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.net.Proxy
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture

//? if >=26.1 {
/*typealias GameSession = net.minecraft.client.User
*///?} else if >=1.20.2 {
typealias GameSession = net.minecraft.client.session.Session
//?} else {
/*typealias GameSession = net.minecraft.client.util.Session
*///?}

/**
 * A transaction, not just a username edit. Rebuild account-scoped services before
 * touching Minecraft, then swap on its client thread while disconnected.
 * Field discovery uses JVM types/signatures, never unmapped Yarn string names.
 */
object SessionBridge {
    class Prepared internal constructor(internal val updates: List<Pair<Field,Any?>>,val profile:AccountProfile) : AutoCloseable {
        override fun close() { updates.mapNotNull { it.second as? AutoCloseable }.distinct().forEach { runCatching { it.close() } } }
        override fun toString() = "Prepared account switch (redacted)"
    }
    private fun fields()=MinecraftClient::class.java.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
    private fun sessionField()=fields().single { it.type==GameSession::class.java }.apply { isAccessible=true }
    fun launcher():AccountCredentials {
        val mc=MinecraftClient.getInstance();val session=sessionField().get(mc) as GameSession
//? if >=26.1 {
/*        val name=session.name;val id=session.profileId;val token=session.accessToken
*///?} else if >=1.20.2 {
        val name=session.username;val id=session.uuidOrNull ?: UUID.nameUUIDFromBytes("OfflinePlayer:${session.username}".toByteArray());val token=session.accessToken
//?} else {
/*        val name=session.username;val id=session.profile.id ?: AccountProfile.offline(name).id;val token=session.accessToken
*///?}
        val profile=AccountProfile(id,name,AccountProfile.Kind.LAUNCHER)
        return AccountCredentials(profile,token,Long.MAX_VALUE)
    }
    private fun gameSession(credentials:AccountCredentials):GameSession {
        val p=credentials.profile;val token=credentials.tokenForSession()
        val xuid=Optional.ofNullable(credentials.xuid);val clientId=Optional.ofNullable(credentials.clientId)
//? if >=26.1 {
/*        return GameSession(p.name,p.id,token,xuid,clientId)
*///?} else if >=1.21.10 {
/*        return GameSession(p.name,p.id,token,xuid,clientId)
*///?} else if >=1.20.2 {
        return GameSession(p.name,p.id,token,xuid,clientId,if(p.kind==AccountProfile.Kind.OFFLINE)GameSession.AccountType.LEGACY else GameSession.AccountType.MSA)
//?} else {
/*        return GameSession(p.name,p.id.toString(),token,xuid,clientId,if(p.kind==AccountProfile.Kind.OFFLINE)GameSession.AccountType.LEGACY else GameSession.AccountType.MSA)
*///?}
    }
    private fun instanceFields(value:Any):List<Any> = runCatching {
        value.javaClass.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }.mapNotNull { f -> f.isAccessible=true;f.get(value) }
    }.getOrDefault(emptyList())
    private fun arguments(types:Array<Class<*>>, candidates:List<Any>):Array<Any>? {
        val args=mutableListOf<Any>()
        for(type in types) args+=candidates.firstOrNull { type.isInstance(it) } ?: return null
        return args.toTypedArray()
    }
    /** Only constructors/factories that explicitly depend on account/auth services qualify. */
    private fun construct(type:Class<*>, candidates:List<Any>, dependency:(Class<*>)->Boolean):Any? {
        for(method in type.declaredMethods.filter { Modifier.isStatic(it.modifiers) && it.returnType==type && it.parameterTypes.any(dependency) }.sortedByDescending { it.parameterCount }) {
            val args=arguments(method.parameterTypes,candidates)?:continue
            method.isAccessible=true;return method.invoke(null,*args)
        }
        for(constructor in type.declaredConstructors.filter { it.parameterTypes.any(dependency) }.sortedByDescending { it.parameterCount }) {
            val args=arguments(constructor.parameterTypes,candidates)?:continue
            constructor.isAccessible=true;return constructor.newInstance(*args)
        }
        return null
    }
    fun prepare(credentials:AccountCredentials):Prepared {
        if(!credentials.usable()) throw AccountProblem("This sign-in expired. Sign in again through Microsoft.")
        val mc=MinecraftClient.getInstance()
        val offline=credentials.profile.kind==AccountProfile.Kind.OFFLINE || credentials.tokenForSession() in listOf("", "0")
        val session=gameSession(credentials)
//? if >=26.1 {
/*        val proxy=mc.proxy
*///?} else {
        val proxy=mc.networkProxy
//?}
        val offlineFactory=YggdrasilAuthenticationService::class.java.methods.firstOrNull { it.name=="createOffline" && it.parameterCount==1 }
        val auth=if(offline && offlineFactory!=null)offlineFactory.invoke(null,proxy) as YggdrasilAuthenticationService else YggdrasilAuthenticationService(proxy)
        val api=if(offline)UserApiService.OFFLINE else auth.createUserApiService(credentials.tokenForSession())
        // No OFFLINE_PROPERTIES fallback on online failure: parental/account restrictions must not be bypassed.
        val fetchProperties=UserApiService::class.java.methods.firstOrNull { it.name=="fetchProperties" && it.parameterCount==0 }
        val properties=fetchProperties?.invoke(api)
        val candidates=mutableListOf<Any>(session,api,auth,mc,mc.runDirectory,mc.runDirectory.toPath())
        val changes=linkedMapOf<Field,Any?>()
        val all=fields().onEach { it.isAccessible=true }
        val oldByType=all.associateWith { it.get(mc) }
        val dependency:(Class<*>)->Boolean = { it==UserApiService::class.java || it==GameSession::class.java || it==YggdrasilAuthenticationService::class.java || it.name=="com.mojang.authlib.yggdrasil.FriendsService" }
        changes[sessionField()]=session
        changes[all.single { it.type==UserApiService::class.java }]=api
        // A fresh session service is used for profile validation and modern Services bundles.
        val sessionService=auth.createMinecraftSessionService();candidates+=sessionService
        auth.javaClass.methods.firstOrNull { it.name=="createFriendsService" && it.parameterCount==1 }?.let { candidates+=it.invoke(auth,credentials.tokenForSession()) }
        // Services, social manager, telemetry, key-pair manager, reporting context and
        // (26.2) remote friends handler are rebuilt from their typed dependencies.
        repeat(3) {
            for(field in all) {
                if(field in changes || field.type.isPrimitive || field.type.name.startsWith("java."))continue
                val old=oldByType[field]
                // Preserve independent context (e.g. report environment), never an old
                // account-bound manager. Dependencies are resolved in later passes.
                val context=(old?.let(::instanceFields)?:emptyList()).filter { value ->
                    !dependency(value.javaClass) && !value.javaClass.name.startsWith("java.") &&
                        value.javaClass.declaredConstructors.none { c -> c.parameterTypes.any(dependency) }
                }
                val pool=candidates+context
                val value=construct(field.type,pool,dependency)?:continue
                changes[field]=value;candidates.add(0,value)
            }
        }
        for(field in all.filter { it.type==CompletableFuture::class.java }) {
            val resultType=(field.genericType as? ParameterizedType)?.actualTypeArguments?.singleOrNull() as? Class<*>?:continue
            if(properties!=null && resultType.isInstance(properties)) changes[field]=CompletableFuture.completedFuture(properties)
            else if(resultType.name=="com.mojang.authlib.yggdrasil.ProfileResult") {
                val method=sessionService.javaClass.methods.firstOrNull { it.name=="fetchProfile" && it.parameterCount==2 }
                val profile=if(offline)null else method?.invoke(sessionService,credentials.profile.id,true)
                if(!offline && profile==null)throw AccountProblem("Minecraft could not load the signed account profile.")
                changes[field]=CompletableFuture.completedFuture(profile)
            }
        }
        // Old clients retain profile-property maps rather than a ProfileResult future.
        for(field in all.filter { it.type.name=="com.mojang.authlib.properties.PropertyMap" }) {
            changes[field]=field.type.getDeclaredConstructor().newInstance()
        }
        // A stale social/key/telemetry service is an error, never a partial success.
        for(field in all) {
            if(field in changes)continue
            val requiresAccount=field.type.declaredConstructors.any { c -> c.parameterTypes.any(dependency) } ||
                field.type.declaredMethods.any { m -> Modifier.isStatic(m.modifiers) && m.returnType==field.type && m.parameterTypes.any(dependency) }
            if(requiresAccount)throw AccountProblem("This game version could not rebuild an account service. The current account is unchanged.")
        }
        return Prepared(changes.toList(),credentials.profile)
    }
    fun commit(prepared:Prepared) {
        val mc=MinecraftClient.getInstance()
        if(mc.world!=null)throw AccountProblem("Disconnect from your world before switching accounts.")
        val old=prepared.updates.map { (field,_) -> field to field.get(mc) }
        try {
            prepared.updates.forEach { (field,value) -> field.set(mc,value) }
            java.lang.invoke.VarHandle.fullFence()
        } catch(failure:Throwable) {
            old.asReversed().forEach { (field,value) -> runCatching { field.set(mc,value) } }
            throw AccountProblem("Account switching failed. The previous account was restored.")
        }
        // Stop old account-bound workers after a successful atomic switch.
        for((field,value) in old) {
            if(value is CompletableFuture<*>)value.cancel(false)
            if(value is AutoCloseable && !field.type.name.startsWith("java."))runCatching { value.close() }
        }
//? if >=26.1 {
/*        runCatching {
            val social=mc.playerSocialManager
            if(social.isFriendListEnabled) {
                prepared.updates.firstOrNull { it.first.type.name=="net.minecraft.client.gui.screens.social.RemoteFriendListUpdateHandler" }?.second?.let { it.javaClass.getMethod("start").invoke(it) }
            }
            mc.updateTitle()
        }
*///?} else {
        runCatching { mc.updateWindowTitle() }
//?}
    }
}
