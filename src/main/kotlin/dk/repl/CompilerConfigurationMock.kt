package dk.repl

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.scripting.compiler.plugin.ScriptingCompilerConfigurationComponentRegistrar
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

fun buildConfiguration(): CompilerConfiguration {
    val configuration = CompilerConfiguration()
    configuration.put(CommonConfigurationKeys.MODULE_NAME, "KotlinREPL")
    configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                      PrintingMessageCollector(System.out, MessageRenderer.WITHOUT_PATHS, false))

    val stdlibPath = PathUtil.getResourcePathForClass(Pair::class.java).canonicalPath

    configuration.add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(File(stdlibPath)))
    configuration.add(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS, ScriptingCompilerConfigurationComponentRegistrar())
    return configuration
}
