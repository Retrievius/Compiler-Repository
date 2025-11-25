package Aufgabe;

import java.util.*;

/**
 * AST Klassen: Stmt, Expr, Hilfs-Typen.
 * Einfach gehalten: nur Felder + Konstruktoren + toString / prettyPrint.
 */

public class AST {

    /* === Primitive Types & Operatoren === */
    public enum PrimType { INT, STRING, BOOL }
    public enum Operator { EQ, NEQ, PLUS, MINUS, MUL, DIV, LT, GT }

    /* === Basis-Klassen === */
    public static abstract class ASTNode {}

    // Statements
    public static abstract class Stmt extends ASTNode {}

    // Expressions
    public static abstract class Expr extends ASTNode {}

    /* === Param === */
    public static class Param {
        public final PrimType type;
        public final String name;
        public Param(PrimType t, String n) { this.type = t; this.name = n; }
        public String toString() { return type + " " + name; }
    }

    /* === Statements impl === */
    public static class VarDecl extends Stmt {
        public final PrimType type;
        public final String name;
        public final Expr initializer; // may be null
        public VarDecl(PrimType t, String n, Expr init) { type=t; name=n; initializer=init; }
    }

    public static class Assign extends Stmt {
        public final String name;
        public final Expr value;
        public Assign(String n, Expr v) { name=n; value=v; }
    }

    public static class FnDecl extends Stmt {
        public final PrimType returnType;
        public final String name;
        public final List<Param> params;
        public final Block body;
        public FnDecl(PrimType rt, String n, List<Param> p, Block b) { returnType=rt; name=n; params=p; body=b; }
    }

    public static class ReturnStmt extends Stmt {
        public final Expr value;
        public ReturnStmt(Expr v) { value=v; }
    }

    public static class ExprStmt extends Stmt {
        public final Expr expr;
        public ExprStmt(Expr e) { expr=e; }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> stmts) { statements = stmts; }
    }

    public static class WhileStmt extends Stmt {
        public final Expr condition;
        public final Block body;
        public WhileStmt(Expr cond, Block b) { condition=cond; body=b; }
    }

    public static class IfStmt extends Stmt {
        public final Expr condition;
        public final Block thenBranch;
        public final Block elseBranch; // never null (can be empty block)
        public IfStmt(Expr cond, Block thenB, Block elseB) { condition=cond; thenBranch=thenB; elseBranch=elseB; }
    }

    /* === Expressions impl === */
    public static class IntLiteral extends Expr {
        public final int value;
        public IntLiteral(int v) { value=v; }
    }

    public static class StringLiteral extends Expr {
        public final String value;
        public StringLiteral(String v) { value = v; }
    }

    public static class BoolLiteral extends Expr {
        public final boolean value;
        public BoolLiteral(boolean v) { value=v; }
    }

    public static class Variable extends Expr {
        public final String name;
        public Variable(String n) { name=n; }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Operator op;
        public final Expr right;
        public Binary(Expr l, Operator o, Expr r) { left=l; op=o; right=r; }
    }

    public static class Call extends Expr {
        public final String name;
        public final List<Expr> args;
        public Call(String n, List<Expr> a) { name=n; args=a; }
    }

    /* === Hilfs-Funktionen: Pretty Print === */
    public static void printProgram(List<Stmt> program) {
        for (Stmt s : program) {
            prettyPrint(s, 0);
        }
    }

    private static void prettyPrint(ASTNode node, int indent) {
        String pad = "  ".repeat(indent);
        if (node instanceof VarDecl) {
            VarDecl v = (VarDecl) node;
            System.out.print(pad + "VarDecl " + v.type + " " + v.name);
            if (v.initializer != null) {
                System.out.println(" =");
                prettyPrint(v.initializer, indent+1);
            } else System.out.println();
        } else if (node instanceof Assign) {
            Assign a = (Assign) node;
            System.out.println(pad + "Assign " + a.name + " =");
            prettyPrint(a.value, indent+1);
        } else if (node instanceof FnDecl) {
            FnDecl f = (FnDecl) node;
            System.out.println(pad + "FnDecl " + f.returnType + " " + f.name + "(" + String.join(", ",
                    f.params.stream().map(Object::toString).toArray(String[]::new)) + ")");
            prettyPrint(f.body, indent+1);
        } else if (node instanceof ReturnStmt) {
            ReturnStmt r = (ReturnStmt) node;
            System.out.println(pad + "Return");
            prettyPrint(r.value, indent+1);
        } else if (node instanceof ExprStmt) {
            ExprStmt es = (ExprStmt) node;
            System.out.println(pad + "ExprStmt");
            prettyPrint(es.expr, indent+1);
        } else if (node instanceof Block) {
            Block b = (Block) node;
            System.out.println(pad + "Block {");
            for (Stmt s : b.statements) prettyPrint(s, indent+1);
            System.out.println(pad + "}");
        } else if (node instanceof WhileStmt) {
            WhileStmt w = (WhileStmt) node;
            System.out.println(pad + "While");
            prettyPrint(w.condition, indent+1);
            prettyPrint(w.body, indent+1);
        } else if (node instanceof IfStmt) {
            IfStmt i = (IfStmt) node;
            System.out.println(pad + "If");
            prettyPrint(i.condition, indent+1);
            System.out.println(pad + "Then:");
            prettyPrint(i.thenBranch, indent+1);
            System.out.println(pad + "Else:");
            prettyPrint(i.elseBranch, indent+1);
        } else if (node instanceof IntLiteral) {
            System.out.println(pad + "IntLiteral " + ((IntLiteral) node).value);
        } else if (node instanceof StringLiteral) {
            System.out.println(pad + "StringLiteral \"" + ((StringLiteral) node).value + "\"");
        } else if (node instanceof BoolLiteral) {
            System.out.println(pad + "BoolLiteral " + ((BoolLiteral) node).value);
        } else if (node instanceof Variable) {
            System.out.println(pad + "Variable " + ((Variable) node).name);
        } else if (node instanceof Binary) {
            Binary b = (Binary) node;
            System.out.println(pad + "Binary " + b.op);
            prettyPrint(b.left, indent+1);
            prettyPrint(b.right, indent+1);
        } else if (node instanceof Call) {
            Call c = (Call) node;
            System.out.println(pad + "Call " + c.name + "(");
            for (Expr e : c.args) prettyPrint(e, indent+1);
            System.out.println(pad + ")");
        } else {
            System.out.println(pad + "Unknown node: " + node);
        }
    }
}
