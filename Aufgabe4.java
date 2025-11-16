import java.util.*;

// =========================================
// Hauptklasse – führt alles aus
// =========================================
public class Aufgabe4 {
    public static void main(String[] args) {
        String code = "(print (+ 1 2))";  // Beispielcode
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        System.out.println("== Tokens ==");
        for (Token t : tokens) {
            System.out.println(t);
        }

        System.out.println("\n== Parse Tree ==");
        Parser parser = new Parser(tokens);
        Node program = parser.parseProgram();
        program.prettyPrint(0);
    }
}

// =========================================
// Token-Typen
// =========================================
enum TokenType {
    LPAREN, RPAREN,
    PLUS, MINUS, MUL, DIV,
    EQ, LT, GT,
    PRINT, STR, IF, DO, DEF, DEFN, LET, HEAD, TAIL, LIST, NTH,
    INT, STRING, BOOLEAN,
    IDENT,
    EOF
}

// =========================================
// Token-Datenstruktur
// =========================================
class Token {
    public final TokenType type;
    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "('" + value + "')";
    }
}

// =========================================
// Lexer – wandelt Code in Tokens um
// =========================================
class Lexer {
    private final String input;
    private int pos = 0;
    private final int length;

    public Lexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }

    private char next() {
        return pos < length ? input.charAt(pos++) : '\0';
    }

    private boolean isAtEnd() {
        return pos >= length;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            char c = peek();

            if (Character.isWhitespace(c)) {
                next();
                continue;
            }

            if (c == ';' && pos + 1 < length && input.charAt(pos + 1) == ';') {
                while (!isAtEnd() && peek() != '\n') next();
                continue;
            }

            if (c == '(') { tokens.add(new Token(TokenType.LPAREN, "(")); next(); continue; }
            if (c == ')') { tokens.add(new Token(TokenType.RPAREN, ")")); next(); continue; }

            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            if (c == '"') {
                tokens.add(readString());
                continue;
            }

            if ("+-*/=<>\0".indexOf(c) >= 0) {
                tokens.add(readOperator());
                continue;
            }

            if (Character.isLetter(c)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            throw new RuntimeException("Unexpected character: " + c);
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token readNumber() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(peek())) sb.append(next());
        return new Token(TokenType.INT, sb.toString());
    }

    private Token readString() {
        StringBuilder sb = new StringBuilder();
        next(); // "
        while (peek() != '"' && !isAtEnd()) sb.append(next());
        if (isAtEnd()) throw new RuntimeException("Unterminated string");
        next(); // "
        return new Token(TokenType.STRING, sb.toString());
    }

    private Token readOperator() {
        char c = next();
        switch (c) {
            case '+': return new Token(TokenType.PLUS, "+");
            case '-': return new Token(TokenType.MINUS, "-");
            case '*': return new Token(TokenType.MUL, "*");
            case '/': return new Token(TokenType.DIV, "/");
            case '=': return new Token(TokenType.EQ, "=");
            case '<': return new Token(TokenType.LT, "<");
            case '>': return new Token(TokenType.GT, ">");
            default: throw new RuntimeException("Unknown operator: " + c);
        }
    }

    private Token readIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(peek())) sb.append(next());
        String word = sb.toString();

        switch (word) {
            case "print": return new Token(TokenType.PRINT, word);
            case "str":   return new Token(TokenType.STR, word);
            case "if":    return new Token(TokenType.IF, word);
            case "do":    return new Token(TokenType.DO, word);
            case "def":   return new Token(TokenType.DEF, word);
            case "defn":  return new Token(TokenType.DEFN, word);
            case "let":   return new Token(TokenType.LET, word);
            case "list":  return new Token(TokenType.LIST, word);
            case "head":  return new Token(TokenType.HEAD, word);
            case "tail":  return new Token(TokenType.TAIL, word);
            case "nth":   return new Token(TokenType.NTH, word);
            case "true":
            case "false": return new Token(TokenType.BOOLEAN, word);
            default:      return new Token(TokenType.IDENT, word);
        }
    }
}

// =========================================
// AST-Knoten
// =========================================
class Node {
    String value;
    List<Node> children = new ArrayList<>();

    Node(String value) {
        this.value = value;
    }

    void addChild(Node n) {
        children.add(n);
    }

    void prettyPrint(int indent) {
        System.out.println("  ".repeat(indent) + value);
        for (Node c : children) {
            c.prettyPrint(indent + 1);
        }
    }
}

// =========================================
// Parser – recursive descent
// =========================================
class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : new Token(TokenType.EOF, "");
    }

    private Token next() {
        return pos < tokens.size() ? tokens.get(pos++) : new Token(TokenType.EOF, "");
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private void expect(TokenType type) {
        if (peek().type != type) {
            throw new RuntimeException("Parser error: expected " + type + " but found " + peek().type);
        }
        next();
    }

    public Node parseProgram() {
        Node program = new Node("Program");
        while (!check(TokenType.EOF)) {
            program.addChild(parseExpression());
        }
        return program;
    }

    private Node parseExpression() {
        if (check(TokenType.LPAREN)) {
            return parseList();
        }
        return parseAtom();
    }

    private Node parseList() {
        expect(TokenType.LPAREN);
        Node list = new Node("List");

        while (!check(TokenType.RPAREN) && !check(TokenType.EOF)) {
            list.addChild(parseExpression());
        }

        expect(TokenType.RPAREN);
        return list;
    }

    private Node parseAtom() {
        Token t = next();
        switch (t.type) {
            case INT:
            case STRING:
            case BOOLEAN:
            case IDENT:
            case PLUS:
            case MINUS:
            case MUL:
            case DIV:
            case EQ:
            case LT:
            case GT:
            case PRINT:
            case STR:
            case IF:
            case DO:
            case DEF:
            case DEFN:
            case LET:
            case LIST:
            case HEAD:
            case TAIL:
            case NTH:
                return new Node(t.value);
            default:
                throw new RuntimeException("Parser error: unexpected token " + t.type);
        }
    }
}
