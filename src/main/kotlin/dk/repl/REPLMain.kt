package dk.repl

import org.jetbrains.kotlin.cli.common.repl.ReplEvalResult
import org.jetbrains.kotlin.scripting.repl.ReplInterpreter
import org.jetbrains.kotlin.utils.PathUtil
import java.io.BufferedReader
import java.io.InputStreamReader

class REPLMain {
    fun start() {
        val disposable = DisposableMock()
        val conf = prepareConfiguration()
        val interpreter = ReplInterpreter(disposable, conf, MockReplConfiguration())

        val reader = BufferedReader(InputStreamReader(System.`in`))

        print("> ")
        reader.forEachLine {
            val result = interpreter.eval(it)
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
            REPLMain().start()
        }
    }
}
