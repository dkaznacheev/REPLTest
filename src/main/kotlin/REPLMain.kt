import org.jetbrains.kotlin.scripting.repl.ReplInterpreter
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
            println(interpreter.eval(it))
            print("> ")
        }
    }
}

fun main() {
    REPLMain().start()
}