package Aufgabe;

import java.nio.file.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*;   // passe ggf. an dein ANTLR-Package an
import ast.AST;
import ast.AST.Stmt;
import ast.ASTBuilder;

public class SemanticMain {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java semantic.SemanticMain <source.minic>");
            System.exit(1);
        }
        String src = Files.readString(Path.of(args[0]));

        // ANTLR parsing
        CharStream cs = CharStreams.fromString(src);
        MiniCLexer lexer = new MiniCLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniCParser parser = new MiniCParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(ConsoleErrorListener.INSTANCE);

        MiniCParser.ProgramContext tree = parser.program();

        // Build AST
        ASTBuilder builder = new ASTBuilder();
        List<Stmt> program = new ArrayList<>();
        for (MiniCParser.StmtContext sctx : tree.stmt()) {
            AST.ASTNode node = builder.visit(sctx);
            if (node instanceof Stmt) program.add((Stmt) node);
            else throw new RuntimeException("Top-level produced non-stmt: " + node);
        }

        // Print AST (optional)
        System.out.println("=== AST ===");
        AST.printProgram(program);

        // Semantic analysis
        SemanticAnalyzer sa = new SemanticAnalyzer();
        sa.runPass1(program);
        sa.runPass2(program);

        System.out.println("\n=== Semantic Summary ===");
        sa.getErrorReporter().printSummary();
    }
}
