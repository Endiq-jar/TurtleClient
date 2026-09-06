package com.endiq.client.config

import com.endiq.client.modules.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.fabricmc.loader.api.FabricLoader

/** Independent codec, shared by persistence and regression tests. */
object ModulePreferenceCodec {
    fun encode(modules:List<Module>)=JsonObject().apply {
        addProperty("schema",1)
        add("modules",JsonObject().apply { modules.forEach { mod ->
            add(mod.name,JsonObject().apply {
                addProperty("enabled",mod.enabled);addProperty("favorite",mod.favorited);addProperty("key",mod.key)
                add("settings",JsonObject().apply { mod.settings.forEach { setting ->
                    when(setting) {
                        is BoolSetting->addProperty(setting.name,setting.value)
                        is SliderSetting->addProperty(setting.name,setting.value)
                        is DropdownSetting->addProperty(setting.name,setting.selected)
                        is TextSetting->addProperty(setting.name,setting.value)
                        is ColorSetting->add(setting.name,JsonArray().apply { listOf(setting.r,setting.g,setting.b,setting.a).forEach { add(it) } })
                        is ActionSetting->Unit
                    }
                } })
            })
        } })
    }
    fun restore(modules:List<Module>,json:JsonObject) {
        val saved=json.getAsJsonObject("modules")?:return
        for(mod in modules) runCatching {
            val value=saved.getAsJsonObject(mod.name)?:return@runCatching
            value.get("favorite")?.let { mod.favorited=it.asBoolean }
            value.get("key")?.asInt?.let { mod.key=if(it==-1 || it in 32..348)it else -1 }
            val settings=value.getAsJsonObject("settings")
            for(setting in mod.settings) runCatching { settings?.get(setting.name)?.let { apply(setting,it) } }
            value.get("enabled")?.asBoolean?.let { if(it)mod.enable() else mod.disable() }
        }
    }
    private fun apply(setting:Setting,value:JsonElement) {
        when(setting) {
            is BoolSetting->setting.value=value.asBoolean
            is SliderSetting->value.asFloat.takeIf { it.isFinite() }?.let { setting.value=it.coerceIn(setting.min,setting.max) }
            is DropdownSetting->setting.selected=value.asInt.coerceIn(0,(setting.options.size-1).coerceAtLeast(0))
            is TextSetting->setting.value=value.asString.filterNot { it.isISOControl() }.take(setting.limit)
            is ColorSetting->{ val c=value.asJsonArray;require(c.size()==4);setting.r=c[0].asInt.coerceIn(0,255);setting.g=c[1].asInt.coerceIn(0,255);setting.b=c[2].asInt.coerceIn(0,255);setting.a=c[3].asInt.coerceIn(0,255) }
            is ActionSetting->Unit
        }
    }
}
object ModulePreferences {
    private val path get()=FabricLoader.getInstance().configDir.resolve("turtle-client/modules.json")
    private var ready=false
    private var saved=""
    private var nextCheck=0L
    var lastError:String?=null
        private set
    fun initialize() {
        runCatching { ModulePreferenceCodec.restore(ModuleManager.modules,JsonFiles.read(path)) }
            .onFailure { lastError="Preferences could not be loaded; defaults are active." }
        saved=ModulePreferenceCodec.encode(ModuleManager.modules).toString();ready=true
    }
    fun tick() { val now=System.currentTimeMillis();if(now>=nextCheck) { nextCheck=now+1000;save() } }
    fun save() {
        if(!ready)return
        val json=ModulePreferenceCodec.encode(ModuleManager.modules);val content=json.toString()
        if(saved==content)return
        runCatching { JsonFiles.write(path,json);saved=content;lastError=null }
            .onFailure { lastError="Could not save preferences. Check access to the config folder." }
    }
}
