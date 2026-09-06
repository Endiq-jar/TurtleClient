package com.endiq.client.gui

import com.endiq.client.modules.Module
import kotlin.test.*

class ModuleKeyBindingTest {
    @Test fun typingDoesNotToggleModulesOrQueueAHeldKeyForAfterTheMenuCloses() {
        val module = object : Module("Test", "", Module.Category.UTILITY) {}
        module.updateKeyState(true, allowToggle = false) // Typing in a screen.
        assertFalse(module.enabled)
        module.updateKeyState(true, allowToggle = true) // Still held after closing it.
        assertFalse(module.enabled)
        module.updateKeyState(false, allowToggle = true)
        module.updateKeyState(true, allowToggle = true)
        assertTrue(module.enabled)
        module.updateKeyState(true, allowToggle = true) // No repeated toggles.
        assertTrue(module.enabled)
        module.updateKeyState(false, allowToggle = false) // Release in a screen.
        module.updateKeyState(true, allowToggle = true)
        assertFalse(module.enabled)
    }
}
