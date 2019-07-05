package dk.repl

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.plugins.PluginCliParser
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

const val JVM_RT_PATH = "/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/"
const val LIB_PATH = "/Users/dmitry.kaznacheev/zeppelin/dist/kotlinc/lib/"
fun prepareConfiguration(): CompilerConfiguration {
    val configuration = newConfiguration()
    loadScriptingPlugin(configuration)
    return configuration
}

fun pathTo(jar: String): String {
    return "$LIB_PATH$jar"
}

fun newConfiguration(): CompilerConfiguration {
    val configuration = CompilerConfiguration()
    configuration.put(CommonConfigurationKeys.MODULE_NAME, "test")

    configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, object : MessageCollector {
        override fun clear() {}

        override fun report(
            severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?
        ) {
            if (severity == CompilerMessageSeverity.ERROR) {
                val prefix = if (location == null)
                    ""
                else
                    "(" + location.path + ":" + location.line + ":" + location.column + ") "
                throw AssertionError(prefix + message)
            }
        }

        override fun hasErrors(): Boolean {
            return false
        }
    })

    configuration.add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(File(JVM_RT_PATH)))
    configuration.add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(File(
        pathTo("kotlin-stdlib.jar"))))

    return configuration
}

fun loadScriptingPlugin(configuration: CompilerConfiguration) {
    val pluginList = listOf(
        PathUtil.KOTLIN_SCRIPTING_COMPILER_PLUGIN_JAR,
        PathUtil.KOTLIN_SCRIPTING_COMPILER_IMPL_JAR,
        PathUtil.KOTLIN_SCRIPTING_COMMON_JAR,
        PathUtil.KOTLIN_SCRIPTING_JVM_JAR
    )
    val pluginClasspath = pluginList.map(::pathTo)

    println(pluginClasspath)
    PluginCliParser.loadPluginsSafe(pluginClasspath, null, configuration)
}