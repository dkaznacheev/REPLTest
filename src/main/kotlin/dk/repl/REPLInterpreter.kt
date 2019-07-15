package dk.repl

import java.io.BufferedReader
import java.io.InputStreamReader
import javax.script.ScriptContext.GLOBAL_SCOPE
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings
import javax.script.SimpleScriptContext
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory
import kotlin.script.experimental.jvmhost.jsr223.KotlinJsr223ScriptEngineImpl


class ExecutionContext(val sc: String)

class REPLInterpreter(
    compilationConfiguration: ScriptCompilationConfiguration,
    evaluationConfiguration: ScriptEvaluationConfiguration
) {
    private val engine = KotlinJsr223ScriptEngineImpl(
        KotlinJsr223DefaultScriptEngineFactory(),
        compilationConfiguration,
        evaluationConfiguration
    )

    fun eval(code: String): String? {
        val res = engine.compile(code)
        return res.eval()?.toString()
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
            val engine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223ScriptEngineImpl

            val compilationConf = ScriptCompilationConfiguration(engine.compilationConfiguration){
                implicitReceivers(listOf(KotlinType(ExecutionContext::class)))
            }
            val evaluationConf = ScriptEvaluationConfiguration(engine.evaluationConfiguration) {
                implicitReceivers(ExecutionContext("SC!"))
            }
            val repl = REPLInterpreter(compilationConf, evaluationConf)
            repl.start()
        }
    }

}
