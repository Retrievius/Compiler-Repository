package Aufgabe;

import java.nio.file.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*; // passe ggf. an

/**
 * Demo: Parsen einer Datei, AST bauen, AST ausgeben.
 * Usage: java ast.Main <input-file>
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java ast.Main <source.minic>");
            System.exit(1);
        }
        String src = Files.readString(Path.of(args[0]));

        CharStream cs = CharStreams.fromString(src);
        MiniCLexer lexer = new MiniCLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniCParser parser = new MiniCParser(tokens);

        // optional: Fehlerlistener anpassen
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener());
        parser.addErrorListener(ConsoleErrorListener.INSTANCE);

        MiniCParser.ProgramContext tree = parser.program();

        ASTBuilder builder = new ASTBuilder();
        List<AST.Stmt> program = new ArrayList<>();
        for (MiniCParser.StmtContext sctx : tree.stmt()) {
            AST.ASTNode node = builder.visit(sctx);
            if (node instanceof AST.Stmt) program.add((AST.Stmt) node);
            else throw new RuntimeException("Top-level produced non-stmt: " + node);
        }

        System.out.println("=== AST ===");
        AST.printProgram(program);
    }
}
