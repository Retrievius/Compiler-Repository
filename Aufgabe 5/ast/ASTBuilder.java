package Aufgabe;

import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*; // Annahme: Dein ANTLR-Package heißt 'parser' (MiniCLexer/Parser erzeugt), passe an

/**
 * ASTBuilder: Ein Visitor, der den ANTLR-ParseTree in unsere AST-Klassen umwandelt.
 * Erweitert die automatisch erzeugte MiniCBaseVisitor<ASTNode>.
 */
public class ASTBuilder extends MiniCBaseVisitor<AST.ASTNode> {

    /* Helper: convert type token text -> PrimType */
    private AST.PrimType toPrimType(ParseTree typeCtx) {
        String t = typeCtx.getText();
        return switch (t) {
            case "int" -> AST.PrimType.INT;
            case "string" -> AST.PrimType.STRING;
            case "bool" -> AST.PrimType.BOOL;
            default -> throw new RuntimeException("Unknown type: " + t);
        };
    }

    /* program : stmt+ EOF ; -> we will call visit on each stmt from Main */
    // stmt rules:
    @Override
    public AST.ASTNode visitVardecl(MiniCParser.VardeclContext ctx) {
        AST.PrimType t = toPrimType(ctx.type());
        String id = ctx.ID().getText();
        AST.Expr init = null;
        if (ctx.expr() != null) {
            init = (AST.Expr) visit(ctx.expr());
        }
        return new AST.VarDecl(t, id, init);
    }

    @Override
    public AST.ASTNode visitAssign(MiniCParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        AST.Expr value = (AST.Expr) visit(ctx.expr());
        return new AST.Assign(id, value);
    }

    @Override
    public AST.ASTNode visitFndecl(MiniCParser.FndeclContext ctx) {
        AST.PrimType rt = toPrimType(ctx.type());
        String name = ctx.ID().getText();
        List<AST.Param> params = new ArrayList<>();
        if (ctx.params() != null) {
            for (int i = 0; i < ctx.params().type().size(); i++) {
                AST.PrimType pt = toPrimType(ctx.params().type(i));
                String pname = ctx.params().ID(i).getText();
                params.add(new AST.Param(pt, pname));
            }
        }
        AST.Block body = (AST.Block) visit(ctx.block());
        return new AST.FnDecl(rt, name, params, body);
    }

    @Override
    public AST.ASTNode visitReturn(MiniCParser.ReturnContext ctx) {
        AST.Expr e = (AST.Expr) visit(ctx.expr());
        return new AST.ReturnStmt(e);
    }

    @Override
    public AST.ASTNode visitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        AST.Expr e = (AST.Expr) visit(ctx.expr());
        return new AST.ExprStmt(e);
    }

    @Override
    public AST.ASTNode visitBlock(MiniCParser.BlockContext ctx) {
        List<AST.Stmt> stmts = new ArrayList<>();
        for (MiniCParser.StmtContext sctx : ctx.stmt()) {
            AST.ASTNode node = visit(sctx);
            if (node instanceof AST.Stmt) stmts.add((AST.Stmt) node);
            else throw new RuntimeException("Block contains non-stmt: " + node);
        }
        return new AST.Block(stmts);
    }

    @Override
    public AST.ASTNode visitWhile(MiniCParser.WhileContext ctx) {
        AST.Expr cond = (AST.Expr) visit(ctx.expr());
        AST.Block body = (AST.Block) visit(ctx.block());
        return new AST.WhileStmt(cond, body);
    }

    @Override
    public AST.ASTNode visitCond(MiniCParser.CondContext ctx) {
        AST.Expr cond = (AST.Expr) visit(ctx.expr());
        AST.Block thenB = (AST.Block) visit(ctx.block(0));
        AST.Block elseB = (ctx.block().size() > 1) ? (AST.Block) visit(ctx.block(1)) : new AST.Block(Collections.emptyList());
        return new AST.IfStmt(cond, thenB, elseB);
    }

    /* fncall & args */
    @Override
    public AST.ASTNode visitFncall(MiniCParser.FncallContext ctx) {
        String name = ctx.ID().getText();
        List<AST.Expr> args = new ArrayList<>();
        if (ctx.args() != null) {
            for (MiniCParser.ExprContext ectx : ctx.args().expr()) {
                args.add((AST.Expr) visit(ectx));
            }
        }
        return new AST.Call(name, args);
    }

    /* expr:
       - fncall
       - expr ('*'|'/') expr
       - expr ('+'|'-') expr
       - expr ('>'|'<') expr
       - expr ('=='|'!=') expr
       - ID | NUMBER | STRING | 'T' | 'F' | '(' expr ')'
     */
    @Override
    public AST.ASTNode visitExpr(MiniCParser.ExprContext ctx) {
        // if it's a function call alternative, ANTLR nests appropriately; test presence:
        if (ctx.fncall() != null) {
            return visit(ctx.fncall());
        }
        // literal booleans 'T' / 'F' are terminals
        if (ctx.getText().equals("T")) return new AST.BoolLiteral(true);
        if (ctx.getText().equals("F")) return new AST.BoolLiteral(false);

        // NUMBER
        if (ctx.NUMBER() != null) {
            int v = Integer.parseInt(ctx.NUMBER().getText());
            return new AST.IntLiteral(v);
        }

        // STRING
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText();
            // remove surrounding quotes (simple)
            String unquoted = raw.substring(1, raw.length()-1);
            return new AST.StringLiteral(unquoted);
        }

        // ID (variable)
        if (ctx.ID() != null && ctx.getChildCount() == 1) {
            return new AST.Variable(ctx.ID().getText());
        }

        // parentheses: '(' expr ')'
        if (ctx.getChildCount() == 3 && "(".equals(ctx.getChild(0).getText()) && ctx.expr().size() == 1) {
            return visit(ctx.expr(0));
        }

        // binary operators: there will be two expr children for binary alternatives
        if (ctx.expr().size() == 2) {
            AST.Expr left = (AST.Expr) visit(ctx.expr(0));
            AST.Expr right = (AST.Expr) visit(ctx.expr(1));
            String op = ctx.getChild(1).getText();
            AST.Operator operator = switch (op) {
                case "*" -> AST.Operator.MUL;
                case "/" -> AST.Operator.DIV;
                case "+" -> AST.Operator.PLUS;
                case "-" -> AST.Operator.MINUS;
                case ">" -> AST.Operator.GT;
                case "<" -> AST.Operator.LT;
                case "==" -> AST.Operator.EQ;
                case "!=" -> AST.Operator.NEQ;
                default -> throw new RuntimeException("Unknown operator: " + op);
            };
            return new AST.Binary(left, operator, right);
        }

        throw new RuntimeException("Unhandled expr form: " + ctx.getText());
    }

    /* Default: forward to children */
    @Override
    public AST.ASTNode visitChildren(RuleNode node) {
        return super.visitChildren(node);
    }
}
