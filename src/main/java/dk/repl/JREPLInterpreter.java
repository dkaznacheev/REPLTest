package dk.repl;

import kotlin.script.experimental.api.ScriptCompilationConfiguration;
import kotlin.script.experimental.api.ScriptEvaluationConfiguration;
import kotlin.script.experimental.host.ScriptingHostConfiguration;
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator;
import kotlin.script.experimental.jvmhost.impl.JvmHostUtilKt;
import kotlin.script.experimental.jvmhost.repl.JvmReplCompiler;
import kotlin.script.experimental.jvmhost.repl.JvmReplEvaluator;
import org.jetbrains.kotlin.cli.common.repl.AggregatedReplStageState;
import org.jetbrains.kotlin.cli.common.repl.ReplCodeLine;
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult;
import org.jetbrains.kotlin.cli.common.repl.ReplCompileResult.CompiledClasses;
import org.jetbrains.kotlin.cli.common.repl.ReplEvalResult;
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmReplCompilerImpl;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static kotlin.script.experimental.jvm.JvmScriptingHostConfigurationKt.getDefaultJvmScriptingHostConfiguration;

public class JREPLInterpreter {

    private JvmReplCompiler compiler;
    private JvmReplEvaluator evaluator;

    private ReentrantReadWriteLock stateLock;
    private AggregatedReplStageState<?, ?> state;

    private AtomicInteger counter;

    @SuppressWarnings("unchecked")
    public JREPLInterpreter(ScriptCompilationConfiguration compilationConfiguration,
                            ScriptEvaluationConfiguration evaluationConfiguration) {
        ScriptingHostConfiguration hostConfiguration = getDefaultJvmScriptingHostConfiguration();
        KJvmReplCompilerImpl compilerImpl = new KJvmReplCompilerImpl(JvmHostUtilKt.withDefaults(hostConfiguration));
        compiler = new JvmReplCompiler(compilationConfiguration,
                hostConfiguration,
                compilerImpl);

        evaluator = new JvmReplEvaluator(evaluationConfiguration, new BasicJvmScriptEvaluator());

        stateLock = new ReentrantReadWriteLock();

        state = new AggregatedReplStageState(
                compiler.createState(stateLock),
                evaluator.createState(stateLock),
                stateLock);

        counter = new AtomicInteger(0);
    }

    public String eval(String code) {
        ReplCompileResult compileResult = compiler.compile(state,
                new ReplCodeLine(counter.getAndIncrement(), 0, code));
        if (compileResult instanceof CompiledClasses) {
            CompiledClasses classes = (CompiledClasses) compileResult;
            ReplEvalResult evalResult = evaluator.eval(state, classes, null, null);
            return evalResult.toString();
        }
        return null;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String output = eval(line);
            if (output != null) {
                System.out.println(output);
            }
            System.out.print("> ");
        }
    }

    public static void main(String[] args) {
        // how do I get createJvmCompilationConfigurationFromTemplate?
    }

}
