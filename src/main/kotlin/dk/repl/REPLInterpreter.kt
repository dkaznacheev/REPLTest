package dk.repl

import java.io.BufferedReader
import java.io.InputStreamReader
import javax.script.ScriptContext.GLOBAL_SCOPE
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings
import javax.script.SimpleScriptContext
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory
import kotlin.script.experimental.jvmhost.jsr223.KotlinJsr223ScriptEngineImpl

@ExperimentalStdlibApi
class REPLInterpreter(
    val compilationConfiguration: ScriptCompilationConfiguration,
    val evaluationConfiguration: ScriptEvaluationConfiguration
) {
    private val engine = KotlinJsr223ScriptEngineImpl(
        KotlinJsr223DefaultScriptEngineFactory(),
        compilationConfiguration,
        evaluationConfiguration
    )

    val ctx = SimpleScriptContext()
    val bindings = SimpleBindings()

    fun eval(code: String): String? {
        val res = engine.compile(code, ctx)
        return res.eval(bindings)?.toString()
    }

    fun start() {
        bindings.put("a", "b")
        ctx.setBindings(bindings, GLOBAL_SCOPE)

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
            }
            val evaluationConf = engine.evaluationConfiguration
            val repl = REPLInterpreter(compilationConf, evaluationConf)
            repl.start()
        }
    }

}
