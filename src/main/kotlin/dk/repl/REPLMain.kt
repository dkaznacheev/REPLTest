package dk.repl

import org.jetbrains.kotlin.cli.common.repl.ReplEvalResult
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.scripting.repl.ReplInterpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class REPLMain {
    private val disposable = Disposer.newDisposable()
    private val conf = buildConfiguration()
    private val interpreter = ReplInterpreter(disposable, conf, MockReplConfiguration())

    fun eval(code: String): ReplEvalResult = interpreter.eval(code)

    fun bind(className: String, alias: String) {
        eval("import $className as $alias")
    }

    fun start() {
        val reader = BufferedReader(InputStreamReader(System.`in`))

        // attempt to bind Holder
        bind(Holder::class.qualifiedName!!, "Holder")
        // attempt to access Holder
        println(eval("Holder.get()"))

        print("> ")
        val writer = PrintWriter(System.out)
        reader.forEachLine {
            interpreter.dumpClasses(writer)
            val result = eval(it)
            interpreter.dumpClasses(writer)
            writer.flush()
            if (result is ReplEvalResult.Incomplete) {
                print("... ")
            } else {
                if (result !is ReplEvalResult.UnitResult) {
                    println(result)
                }
                print("> ")
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args:Array<String>) {
            val repl = REPLMain()
            repl.start()
        }
    }
}
