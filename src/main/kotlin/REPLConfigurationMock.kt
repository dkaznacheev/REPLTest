import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.scripting.repl.ReplExceptionReporter
import org.jetbrains.kotlin.scripting.repl.ReplFromTerminal
import org.jetbrains.kotlin.scripting.repl.configuration.ReplConfiguration
import org.jetbrains.kotlin.scripting.repl.configuration.SnippetExecutionInterceptor
import org.jetbrains.kotlin.scripting.repl.messages.DiagnosticMessageHolder
import org.jetbrains.kotlin.scripting.repl.reader.ReplCommandReader
import org.jetbrains.kotlin.scripting.repl.writer.ReplWriter
import java.io.BufferedReader
import java.io.InputStreamReader


class MockHolder: DiagnosticMessageHolder {
    override fun renderMessage(): String = ""
    override fun report(diagnostic: Diagnostic, file: PsiFile, render: String) {}
}

class StdinREPLReader: ReplCommandReader {
    private val reader = BufferedReader(InputStreamReader(System.`in`))

    override fun flushHistory() {}

    override fun readLine(next: ReplFromTerminal.WhatNextAfterOneLine): String? {
        return reader.readLine()
    }
}

class StdoutREPLWriter: ReplWriter {
    override fun notifyCommandSuccess() {}

    override fun notifyIncomplete() {}

    override fun notifyReadLineEnd() {}

    override fun notifyReadLineStart() {}

    override fun outputCommandResult(x: String) {
        println("RESULT: $x")
    }

    override fun outputCompileError(x: String) {
        println("COMPILE ERROR: $x")
    }

    override fun outputRuntimeError(x: String) {
        println("RUNTIME ERROR: $x")
    }

    override fun printlnHelpMessage(x: String) {
        println("HELP MESSAGE: $x")
    }

    override fun printlnWelcomeMessage(x: String) {
        println("WELCOME MESSAGE: $x")
    }

    override fun sendInternalErrorReport(x: String) {
        println("INTERNAL ERROR REPORT: $x")
    }
}

class MockReplConfiguration: ReplConfiguration {
    override val allowIncompleteLines = true
    override val commandReader = StdinREPLReader()
    override val exceptionReporter = ReplExceptionReporter.DoNothing
    override val executionInterceptor = SnippetExecutionInterceptor.Plain
    override val writer = StdoutREPLWriter()
    override fun createDiagnosticHolder() = MockHolder()

}
