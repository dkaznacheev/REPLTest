package dk.repl

import javax.script.ScriptEngineManager
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory
import kotlin.script.experimental.jvmhost.jsr223.KotlinJsr223ScriptEngineImpl

@Suppress("unused")
class ExecutionContext(val ctx: String)

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
        println(eval("ctx"))
        println(eval("1 + 1")) // fails here
    }

    companion object {
        @JvmStatic
        fun main(args:Array<String>) {
            val engine = ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223ScriptEngineImpl

            val compilationConf = ScriptCompilationConfiguration(engine.compilationConfiguration){
                implicitReceivers(listOf(KotlinType(ExecutionContext::class)))
            }
            val evaluationConf = ScriptEvaluationConfiguration(engine.evaluationConfiguration) {
                implicitReceivers(ExecutionContext("CONTEXT"))
            }
            val repl = REPLInterpreter(compilationConf, evaluationConf)
            repl.start()
        }
    }
}
