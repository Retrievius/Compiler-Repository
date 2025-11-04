import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromFileName("src/main/resources/test.txt");
        Aufgabe_3_1Lexer lexer = new Aufgabe_3_1Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Aufgabe_3_1Parser parser = new Aufgabe_3_1Parser(tokens);

        ParseTree tree = parser.start();
        PrettyPrinter visitor = new PrettyPrinter();

        String result = visitor.visit(tree);
        System.out.println(result);
    }
}
