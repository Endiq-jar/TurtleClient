package com.endiq.client.compat

import com.google.gson.JsonParser
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** Inspect bytecode without bootstrapping Minecraft or requiring a graphics device. */
class MixinCompatibilityTest {
    private val loader = javaClass.classLoader
    private val classes = mutableMapOf<String, ClassNode>()

    private fun readClass(name: String): ClassNode = classes.getOrPut(name) {
        val stream = loader.getResourceAsStream("$name.class") ?: fail("Missing class on test classpath: $name")
        stream.use { ClassNode().also { node -> ClassReader(it).accept(node, ClassReader.SKIP_CODE) } }
    }

    private fun AnnotationNode.value(key: String): Any? {
        val pairs = values ?: return null
        for (index in pairs.indices step 2) if (pairs[index] == key) return pairs[index + 1]
        return null
    }

    private fun MethodNode.annotations() = (visibleAnnotations ?: emptyList()) + (invisibleAnnotations ?: emptyList())

    @Test
    fun configuredInjectionTargetsAndCallbackSignaturesExist() {
        var checked = 0
        for (config in listOf("turtle-client.mixins.json", "turtle-client.client.mixins.json")) {
            val stream = loader.getResourceAsStream(config) ?: fail("Missing processed mixin configuration: $config")
            val json = stream.bufferedReader().use { JsonParser.parseReader(it).asJsonObject }
            val pkg = json["package"].asString.replace('.', '/')
            for (section in listOf("mixins", "client", "server")) {
                for (name in json.getAsJsonArray(section) ?: continue) {
                    val mixin = readClass("$pkg/${name.asString}")
                    val annotation = ((mixin.visibleAnnotations ?: emptyList()) + (mixin.invisibleAnnotations ?: emptyList()))
                        .single { it.desc == "Lorg/spongepowered/asm/mixin/Mixin;" }
                    val targets = (annotation.value("value") as? List<*>)?.map { (it as Type).internalName }
                        ?: fail("No class targets declared by ${mixin.name}")
                    for (targetName in targets) {
                        val target = readClass(targetName)
                        for (handler in mixin.methods) {
                            for (inject in handler.annotations().filter { it.desc == "Lorg/spongepowered/asm/mixin/injection/Inject;" }) {
                                val selectors = inject.value("method") as List<*>
                                for (selector in selectors.map { it as String }) {
                                    val methodName = selector.substringBefore('(')
                                    val descriptor = selector.substringAfter(methodName)
                                    val methods = target.methods.filter { it.name == methodName && (descriptor.isEmpty() || it.desc == descriptor) }
                                    assertTrue(methods.isNotEmpty(), "${mixin.name}.${handler.name}: no target $targetName.$selector")
                                    for (method in methods) {
                                        validateCallback(targetName, method, handler)
                                        checked++
                                    }
                                }
                            }
                            for (accessor in handler.annotations().filter { it.desc == "Lorg/spongepowered/asm/mixin/gen/Accessor;" }) {
                                val field = accessor.value("value") as String
                                assertTrue(target.fields.any { it.name == field }, "${mixin.name}: no accessor target $targetName.$field")
                                checked++
                            }
                        }
                    }
                }
            }
        }
        assertTrue(checked > 0, "No mixin hooks were checked")
    }

    private fun validateCallback(owner: String, target: MethodNode, handler: MethodNode) {
        val description = "$owner.${target.name}${target.desc} <- ${handler.name}${handler.desc}"
        val arguments = Type.getArgumentTypes(handler.desc).toList()
        val callback = arguments.indexOfFirst {
            it.className == "org.spongepowered.asm.mixin.injection.callback.CallbackInfo" ||
                it.className == "org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable"
        }
        assertTrue(callback >= 0, "Missing callback info: $description")
        assertEquals(arguments.lastIndex, callback, "Unexpected local capture in $description")
        assertEquals(Type.VOID_TYPE, Type.getReturnType(handler.desc), "Injector must return void: $description")
        val callbackType = if (Type.getReturnType(target.desc) == Type.VOID_TYPE) "CallbackInfo" else "CallbackInfoReturnable"
        assertEquals("org.spongepowered.asm.mixin.injection.callback.$callbackType", arguments[callback].className, description)
        if (callback > 0) assertEquals(Type.getArgumentTypes(target.desc).toList(), arguments.take(callback), description)
        assertEquals(target.access and Opcodes.ACC_STATIC, handler.access and Opcodes.ACC_STATIC, "Static mismatch: $description")
    }
}
