package com.endiq.client.gui

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import kotlin.test.*

class GuiInputWiringTest {
    @Test fun everyScrollableScreenActuallyHandlesWheelInput() {
        for (name in listOf("ClickGui", "settings/ModSettingsGui")) {
            val resource = "com/endiq/client/gui/$name.class"
            val node = javaClass.classLoader.getResourceAsStream(resource)?.use {
                ClassNode().also { node -> ClassReader(it).accept(node, 0) }
            } ?: fail("Missing screen $name")
            val handler = node.methods.singleOrNull { it.name == "onMouseScrolled" && it.desc == "(DDDD)Z" }
                ?: fail("$name does not override the common wheel handler")
            assertTrue(handler.instructions.any { it is MethodInsnNode && it.owner == "com/endiq/client/gui/components/ScrollState" && it.name == "wheel" },
                "$name does not forward wheel input to the scroll model")
        }
    }
}
