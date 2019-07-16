package dk.repl

import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.common.repl.AggregatedReplStageState
import org.jetbrains.kotlin.cli.common.repl.BasicReplStageHistory
import org.jetbrains.kotlin.cli.common.repl.ReplCodeLine
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.impl.KJvmReplCompilerImpl
import kotlin.script.experimental.jvmhost.repl.JvmReplCompiler
import kotlin.script.experimental.jvmhost.repl.JvmReplEvaluator

interface MyReceiverInterface {
    val ctx: String
}

open class MyReceiver(val ctxImpl: String = "SC"): MyReceiverInterface {
    override val ctx = ctxImpl
}

@KotlinScript(fileExtension = "simplescript.kts")
open class SimpleScript(sc: MyReceiver): MyReceiverInterface by sc

@Suppress("unused")
class ExecutionContext(val ctx: String)

fun evaluateInRepl(
    compilationConfiguration: ScriptCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration,
    snippets: Sequence<String>
): Sequence<ResultWithDiagnostics<EvaluationResult>> {
    val replCompilerProxy =
        KJvmReplCompilerImpl(defaultJvmScriptingHostConfiguration)
    val compilationState = replCompilerProxy.createReplCompilationState(compilationConfiguration)
    val compilationHistory = BasicReplStageHistory<ScriptDescriptor>()
    val replEvaluator = BasicJvmScriptEvaluator()
    var currentEvalConfig = evaluationConfiguration
    return snippets.mapIndexed { snippetNo, snippetText ->
        val snippetSource = snippetText.toScriptSource("Line_$snippetNo.simplescript.kts")
        val snippetId = ReplSnippetIdImpl(snippetNo, 0, snippetSource)
        replCompilerProxy.compileReplSnippet(compilationState, snippetSource, snippetId, compilationHistory)
            .onSuccess {
                runBlocking {
                    replEvaluator(it, currentEvalConfig)
                }
            }
            .onSuccess {
                val v: ResultValue = it.returnValue

                val scriptInstance = when(v) {
                    is ResultValue.Value -> v.scriptInstance
                    is ResultValue.UnitValue -> v.scriptInstance
                    else -> null
                }

                scriptInstance?.let { snippetInstance ->
                    currentEvalConfig = ScriptEvaluationConfiguration(currentEvalConfig) {
                        previousSnippets.append(snippetInstance)
                        jvm {
                            baseClassLoader(snippetInstance::class.java.classLoader)
                        }
                    }
                }
                it.asSuccess()
            }
    }
}

fun main() {
    val compilationConf = createJvmCompilationConfigurationFromTemplate<SimpleScript> {
        jvm { dependenciesFromCurrentContext(wholeClasspath = true) }
    }
    val evaluationConf = createJvmEvaluationConfigurationFromTemplate<SimpleScript> {
        constructorArgs(MyReceiver("SC!!"))
    }

    val commands = listOf("1 + 1", "ctx").asSequence()

    val resultWithDiagnostics = evaluateInRepl(compilationConf,
        evaluationConf,
        commands)
    resultWithDiagnostics.forEachIndexed { index, result ->
        println("$index: $result")
    }
}