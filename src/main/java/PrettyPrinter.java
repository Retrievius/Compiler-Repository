import org.antlr.v4.runtime.tree.TerminalNode;

public class PrettyPrinter extends Aufgabe_3_1BaseVisitor<String> {

    private int indentLevel = 0;
    private final String INDENT = "    "; // 4 Leerzeichen pro Einrückung

    private String indent() {
        return INDENT.repeat(indentLevel);
    }

    // Hilfsfunktion für Operatoren mit korrektem Leerraum
    private String binOp(String left, String op, String right) {
        return left + " " + op + " " + right;
    }

    // --- Startregel ---
    @Override
    public String visitStart(Aufgabe_3_1Parser.StartContext ctx) {
        StringBuilder sb = new StringBuilder();
        for (var s : ctx.stmt()) {
            sb.append(visit(s));
        }
        return sb.toString();
    }

    // --- Statements ---
    @Override
    public String visitStmt(Aufgabe_3_1Parser.StmtContext ctx) {
        if (ctx.expr() != null) return visit(ctx.expr()) + "\n";
        if (ctx.var() != null) return visit(ctx.var()) + "\n";
        if (ctx.while_() != null) return visit(ctx.while_());
        if (ctx.ifelse() != null) return visit(ctx.ifelse());
        return "";
    }

    // --- Variable ---
    @Override
    public String visitVar(Aufgabe_3_1Parser.VarContext ctx) {
        return indent() + ctx.ID().getText();
    }

    // --- While-Schleife ---
    @Override
    public String visitWhile(Aufgabe_3_1Parser.WhileContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("while ").append(visit(ctx.expr())).append(" do\n");
        indentLevel++;
        for (var stmt : ctx.stmt()) {
            sb.append(visit(stmt));
        }
        indentLevel--;
        sb.append(indent()).append("end\n");
        return sb.toString();
    }

    // --- If-Else ---
    @Override
    public String visitIfelse(Aufgabe_3_1Parser.IfelseContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("if ").append(visit(ctx.expr())).append(" do\n");
        indentLevel++;
        sb.append(visit(ctx.stmt(0)));
        indentLevel--;
        sb.append(indent()).append("else do\n");
        indentLevel++;
        sb.append(visit(ctx.stmt(1)));
        indentLevel--;
        sb.append(indent()).append("end\n");
        return sb.toString();
    }

    // --- Ausdrücke ---
    @Override
    public String visitExpr(Aufgabe_3_1Parser.ExprContext ctx) {
        // Atomare Ausdrücke
        if (ctx.NUMBER() != null) return ctx.NUMBER().getText();
        if (ctx.STRING() != null) return ctx.STRING().getText();
        if (ctx.ID() != null) return ctx.ID().getText();

        // ( expr )
        if (ctx.expr().size() == 1 && ctx.getChildCount() == 3) {
            return "(" + visit(ctx.expr(0)) + ")";
        }

        // Zuweisung: var := expr
        if (ctx.var() != null && ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals(":=")) {
            return indent() + ctx.var().getText() + " := " + visit(ctx.expr(0));
        }

        // Binäre Operatoren (arithmetisch / Vergleich)
        if (ctx.expr().size() == 2) {
            String left = visit(ctx.expr(0));
            String right = visit(ctx.expr(1));
            String op = ctx.getChild(1).getText();
            return binOp(left, op, right);
        }

        return ""; // Fallback
    }
}
