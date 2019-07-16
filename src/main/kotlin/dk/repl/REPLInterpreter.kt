package dk.repl

import org.jetbrains.kotlin.cli.common.repl.AggregatedReplStageState
import org.jetbrains.kotlin.cli.common.repl.ReplCodeLine
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.repl.JvmReplCompiler
import kotlin.script.experimental.jvmhost.repl.JvmReplEvaluator


class ImplicitReceiverConfiguration : ScriptCompilationConfiguration(
    {
        jvm { dependenciesFromCurrentContext(wholeClasspath = true) }
        implicitReceivers(ExecutionContext::class)
    }
)

@KotlinScript(fileExtension = "simplescript.kts", compilationConfiguration = ImplicitReceiverConfiguration::class)
abstract class SimpleScriptWithReceiver

@Suppress("unused")
class ExecutionContext(var ctx: String)

class REPLInterpreter(
    compilationConfiguration: ScriptCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration
) {
    private val compiler = JvmReplCompiler(compilationConfiguration)
    private val evaluator = JvmReplEvaluator(evaluationConfiguration)

    private val stateLock = ReentrantReadWriteLock()
    private val state = AggregatedReplStageState(compiler.createState(stateLock), evaluator.createState(stateLock), stateLock)
    private val counter = AtomicInteger(0)

    fun eval(code: String): String? {
        val compileResult = compiler.compile(state, ReplCodeLine(counter.getAndIncrement(), 0, code))
        return when (compileResult) {
            is ReplCompileResult.CompiledClasses -> {
                println(compileResult.mainClassName)
                println(compileResult.classes)
                evaluator.eval(state, compileResult).toString()
            }
            is ReplCompileResult.Incomplete -> {
                "error: incomplete"
            }
            is ReplCompileResult.Error -> {
                "${compileResult.message}\nlocation: ${compileResult.location}"
            }
        }
    }

    fun start() {
        val reader = BufferedReader(InputStreamReader(System.`in`))

        print("> ")
        reader.forEachLine {
            eval(it)?.let(::println)
            print("> ")
        }
    }

    companion object {
        @JvmStatic
        fun main(args:Array<String>) {

            val compilationConf = createJvmCompilationConfigurationFromTemplate<SimpleScriptWithReceiver>()
            val evaluationConf = createJvmEvaluationConfigurationFromTemplate<SimpleScriptWithReceiver> {
                implicitReceivers(ExecutionContext("CONTEXT"))
            }

            val repl = REPLInterpreter(compilationConf, evaluationConf)
            repl.start()
        }
    }
}