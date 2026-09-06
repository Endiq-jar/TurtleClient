package com.endiq.client.accounts

import com.endiq.client.compat.GameSession
import com.endiq.client.compat.MinecraftClient
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import kotlin.test.*

/** Read bytecode, without initializing Minecraft, to verify type-based account discovery. */
class AccountApiCompatibilityTest {
    private fun node(name:String):ClassNode? = javaClass.classLoader.getResourceAsStream("$name.class")?.use { stream ->
        ClassNode().also { ClassReader(stream).accept(it,ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG) }
    }
    @Test fun accountFieldsAndRebuildFactoriesExistInEverySupportedTarget() {
        val minecraft=node(Type.getInternalName(MinecraftClient::class.java))!!
        val session=Type.getDescriptor(GameSession::class.java)
        val api="Lcom/mojang/authlib/minecraft/UserApiService;"
        val deps=setOf(session,api,"Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;","Lcom/mojang/authlib/yggdrasil/FriendsService;")
        assertEquals(1,minecraft.fields.count { it.desc==session })
        assertEquals(1,minecraft.fields.count { it.desc==api })
        val factories=minecraft.fields.filter { it.access and Opcodes.ACC_STATIC==0 && it.desc.startsWith("L") }.mapNotNull { field ->
            val name=Type.getType(field.desc).internalName
            val methods=node(name)?.methods.orEmpty().filter { m ->
                (m.name=="<init>" || (m.access and Opcodes.ACC_STATIC!=0 && Type.getReturnType(m.desc).descriptor==field.desc)) &&
                    Type.getArgumentTypes(m.desc).any { it.descriptor in deps }
            }
            if(methods.isEmpty())null else field.name to methods
        }
        // Social manager on every target; keys, reporting and telemetry on modern ones.
        assertTrue(factories.isNotEmpty(),"No account-scoped factories were discovered")
        if(minecraft.fields.any { it.name in setOf("profileKeys","profileKeyPairManager") })
            assertTrue(factories.size>=4,"Missing account services: ${factories.map { it.first }}")
        else assertTrue(factories.size>=2,"Missing legacy account services")
    }
}
