package com.endiq.client.gui

import com.endiq.client.gui.components.ModuleIcons
import com.google.gson.JsonParser
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import java.security.MessageDigest
import javax.imageio.ImageIO
import kotlin.test.*

class ModuleArtworkTest {
    private val loader=javaClass.classLoader
    private fun node(name:String):ClassNode=loader.getResourceAsStream("$name.class")!!.use {
        ClassNode().also { node->ClassReader(it).accept(node,ClassReader.SKIP_DEBUG) }
    }
    @Test fun everyRegisteredBuiltinHasADistinctIconAndCatalogEntry() {
        // Inspect NEW instructions instead of initializing modules that register game callbacks.
        val init=node("com/endiq/client/modules/ModuleManager").methods.single { it.name=="init" }
        val registered=init.instructions.toArray().filterIsInstance<TypeInsnNode>()
            .filter { it.opcode==Opcodes.NEW && it.desc.startsWith("com/endiq/client/modules/impl/") }
            .map { it.desc.substringAfterLast('/') }.toSet()
        assertEquals(registered,ModuleIcons.keys.keys)
        assertEquals(52,registered.size)
        val catalog=loader.getResourceAsStream("assets/turtle-client/module-icons.json")!!.bufferedReader().use {
            JsonParser.parseReader(it).asJsonObject["modules"].asJsonArray
        }
        assertEquals(registered,catalog.map { it.asJsonObject["class"].asString }.toSet())
        val hashes=mutableSetOf<String>()
        for(entry in catalog) {
            val spec=entry.asJsonObject;val path=ModuleIcons.path(spec["class"].asString)
            assertEquals(spec["texture"].asString,path)
            val bytes=loader.getResourceAsStream("assets/turtle-client/$path")!!.use { it.readBytes() }
            val image=ImageIO.read(bytes.inputStream())
            assertEquals(32,image.width);assertEquals(32,image.height)
            assertTrue(image.colorModel.hasAlpha());assertEquals(0,image.getRGB(0,0) ushr 24)
            val digest=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
            assertTrue(hashes.add(digest),"Reused generic icon for ${spec["class"]}")
            assertTrue(bytes.size<3000,"Oversized module icon: $path")
        }
        assertEquals("textures/gui/icons/grid.png",ModuleIcons.path("ExtensionModule","all"))
        assertEquals("textures/gui/icons/hud.png",ModuleIcons.path("ExtensionModule","hud"))
    }
    @Test fun bothLibraryCardsAndSettingsUseTheModuleSpecificResolver() {
        for(screen in listOf("ClickGui","settings/ModSettingsGui")) {
            val calls=node("com/endiq/client/gui/$screen").methods.flatMap { it.instructions.toArray().toList() }
                .filterIsInstance<MethodInsnNode>()
            assertTrue(calls.any { it.owner=="com/endiq/client/gui/components/TurtleTheme" && it.name=="moduleIcon" },screen)
        }
    }
    @Test fun splashPaintingKeepsVanillaLifecycleAndDoesNotReadReloadingFonts() {
        val mixin=node("com/endiq/client/mixin/ExampleClientMixin\$SplashMixin")
        val calls=mixin.methods.flatMap { it.instructions.toArray().toList() }.filterIsInstance<MethodInsnNode>()
        assertFalse(calls.any { it.owner=="org/spongepowered/asm/mixin/injection/callback/CallbackInfo" && it.name=="cancel" })
        assertTrue(mixin.fields.any { it.desc=="Lcom/endiq/client/gui/components/LoadingProgress;" })
        val render=node("com/endiq/client/compat/BrandingRenderer").methods.single { it.name=="renderSplash" }
        val paint=render.instructions.toArray().filterIsInstance<MethodInsnNode>()
        assertFalse(paint.any { it.name in setOf("drawText","drawTextWithShadow","getTextRenderer","getFont","label") })
    }
}
