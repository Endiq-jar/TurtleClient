package com.endiq.client.accounts

import com.endiq.client.compat.*
import net.fabricmc.loader.api.FabricLoader
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

/** Main-thread account model; blocking authentication runs off the render thread. */
object Accounts {
    private val workers=Executors.newFixedThreadPool(2) { runnable -> Thread(runnable,"Turtle account worker").apply { isDaemon=true } }
    private val generation=AtomicInteger()
    private var task:Future<*>?=null
    private var cancellation:MicrosoftLogin.Cancellation?=null
    private val credentials=mutableMapOf<UUID,AccountCredentials>()
    private lateinit var launcher:AccountCredentials
    val repository=AccountRepository(FabricLoader.getInstance().configDir.resolve("turtle-client/accounts.json"))
    var active:AccountProfile?=null
        private set
    var busy=false
        private set
    var message=""
        private set
    var challenge:MicrosoftLogin.DeviceChallenge?=null
        private set
    var lastAdded:UUID?=null
        private set
    fun initialize() {
        if(::launcher.isInitialized)return
        launcher=SessionBridge.launcher();active=launcher.profile
        runCatching { repository.load() }.onFailure { message="Account list could not be loaded. The launcher account is still available." }
    }
    fun profiles():List<AccountProfile> { initialize();return (repository.profiles+launcher.profile).distinctBy { it.id } }
    fun ready(profile:AccountProfile)=when(profile.kind) {
        AccountProfile.Kind.OFFLINE -> true
        AccountProfile.Kind.LAUNCHER -> true
        AccountProfile.Kind.MICROSOFT -> credentials[profile.id]?.usable()==true
    }
    fun addOffline(name:String):AccountProfile {
        val profile=AccountProfile.offline(name.trim());repository.remember(profile);lastAdded=profile.id
        message="Offline profile added. It cannot authenticate to online-mode servers.";return profile
    }
    fun forget(profile:AccountProfile) {
        check(profile.id!=active?.id) { "Switch away before removing the active account" }
        check(profile.id!=launcher.profile.id) { "The launcher account cannot be removed" }
        credentials.remove(profile.id);repository.remove(profile.id);message="Account forgotten on this device."
    }
    fun canForget(profile:AccountProfile?)=profile!=null && profile.id!=active?.id && profile.id!=launcher.profile.id && !busy
    fun setClientId(value:String) {
        if(!MicrosoftLogin.validClientId(value.trim()))throw AccountProblem("Use the public application (client) ID from your Microsoft app registration.")
        repository.microsoftClientId=value.trim();repository.save();message="Microsoft application ID saved. No client secret is needed."
    }
    fun cancel() {
        generation.incrementAndGet();cancellation?.cancel();task?.cancel(true);challenge=null;busy=false
    }
    fun signIn() {
        cancel();val id=generation.incrementAndGet();val cancel=MicrosoftLogin.Cancellation();cancellation=cancel
        val clientId=repository.microsoftClientId;busy=true;message="Requesting an official Microsoft sign-in code...";lastAdded=null
        task=workers.submit {
            try {
                val auth=MicrosoftLogin();val device=auth.begin(clientId)
                publish(id) { challenge=device;message="Open Microsoft, enter the code, then return here." }
                val verified=auth.complete(clientId,device,cancel)
                publish(id) {
                    credentials[verified.profile.id]=verified
                    val saved=runCatching { repository.remember(verified.profile) }.isSuccess
                    lastAdded=verified.profile.id;challenge=null;busy=false
                    message=if(saved)"Signed in as ${verified.profile.name}. Select Use account to switch." else "Signed in for this session, but the account name could not be saved."
                }
            } catch(_:CancellationException) { /* Explicit cancellation does not become an error toast. */ }
            catch(_:InterruptedException) { Thread.currentThread().interrupt() }
            catch(error:Exception) { publish(id) { busy=false;challenge=null;message=if(error is AccountProblem)error.message.orEmpty() else "Sign-in could not complete. Check your connection and application setup." } }
        }
    }
    fun use(profile:AccountProfile) {
        if(busy)return
        if(MinecraftClient.getInstance().world!=null) { message="Disconnect from your world before switching accounts.";return }
        val value=credentials[profile.id] ?: when {
            profile.id==launcher.profile.id -> launcher
            profile.kind==AccountProfile.Kind.OFFLINE -> AccountCredentials(profile,"0",Long.MAX_VALUE)
            else -> { message="Sign in to this Microsoft account again. Tokens are not saved to disk.";return }
        }
        val id=generation.incrementAndGet();busy=true;message="Preparing account services..."
        task=workers.submit {
            try {
                val prepared=SessionBridge.prepare(value)
                MinecraftClient.getInstance().execute {
                    if(id!=generation.get() || MinecraftClient.getInstance().currentScreen !is com.endiq.client.gui.AccountsScreen) {
                        prepared.close();if(id==generation.get())busy=false;return@execute
                    }
                    try { SessionBridge.commit(prepared);active=profile;message="Active account: ${profile.name}${if(profile.kind==AccountProfile.Kind.OFFLINE)" (offline)" else ""}" }
                    catch(error:Exception) { prepared.close();message=if(error is AccountProblem)error.message.orEmpty() else "Switch failed; the current account was kept." }
                    finally { busy=false }
                }
            } catch(error:Exception) {
                publish(id) { busy=false;message=if(error is AccountProblem)error.message.orEmpty() else "Could not prepare this account. The current account is unchanged." }
            }
        }
    }
    private fun publish(id:Int,action:()->Unit) { MinecraftClient.getInstance().execute { if(id==generation.get())action() } }
    fun shutdown() { cancel();credentials.clear();workers.shutdownNow() }
}
