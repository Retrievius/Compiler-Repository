package Aufgabe;

import java.util.*;
import ast.AST;
import ast.AST.*;
import static ast.AST.PrimType;
import static ast.AST.Operator;

/**
 * SemanticAnalyzer führt beide Pässe aus:
 *  - pass1: Scopes/Definitionen (Variablen, Funktionen), Prüfung: mehrfache Definition im Scope,
 *           Variablenverwendung muss vorher deklariert sein, Variablen dürfen nicht als Funktionen benutzt werden.
 *  - pass2: Prüfung von Funktionsaufrufen: Existenz/Sichtbarkeit und Argumentanzahl
 *
 * Hinweise:
 *  - Funktionen dürfen vor Aufruf deklariert werden (also call-before-def ist erlaubt). Deshalb
 *    überprüfen wir in Pass1 nicht das Vorhandensein einer Funktion; stattdessen sammeln wir Calls
 *    und prüfen sie in Pass2 gegen die komplette Symboltabelle.
 */
public class SemanticAnalyzer {

    private final SymbolTable symbols = new SymbolTable();
    private final ErrorReporter errors = new ErrorReporter();

    // Gesammelte Funktionsaufrufe zum Prüfen in Pass2
    private final List<Call> callSites = new ArrayList<>();

    public ErrorReporter getErrorReporter() { return errors; }
    public SymbolTable getSymbolTable() { return symbols; }

    /* ------------------ PASS 1 ------------------ */
    public void runPass1(List<Stmt> program) {
        // global scope already angelegt im SymbolTable-Konstruktor
        for (Stmt s : program) {
            visitStmtPass1(s);
        }
    }

    private void visitStmtPass1(Stmt s) {
        if (s instanceof VarDecl vd) {
            // variable must not be already defined in current scope
            if (!symbols.defineInCurrentScope(new VarSymbol(vd.name, vd.type))) {
                errors.error("Duplicate variable declaration in same scope: " + vd.name);
            } else {
                // link VarDecl AST node -> VarSymbol (Querverbindung)
                Symbol sym = symbols.resolveInCurrentScope(vd.name);
                symbols.link(vd, sym);
            }
            // initializer: evaluate expressions now (variable must be visible if used)
            if (vd.initializer != null) visitExprPass1(vd.initializer);
        }
        else if (s instanceof Assign asg) {
            // variable must be defined (visible) at assignment time
            Symbol sym = symbols.resolve(asg.name);
            if (sym == null) {
                errors.error("Assignment to undefined variable: " + asg.name);
            } else if (sym instanceof FnSymbol) {
                errors.error("Assignment target is a function name (not a variable): " + asg.name);
            }
            visitExprPass1(asg.value);
        }
        else if (s instanceof FnDecl fd) {
            // functions cannot be multiply defined in same scope
            if (!symbols.defineInCurrentScope(new FnSymbol(fd.name, fd.returnType, fd.params))) {
                errors.error("Duplicate function declaration in same scope: " + fd.name);
            } else {
                Symbol sym = symbols.resolveInCurrentScope(fd.name);
                symbols.link(fd, sym);
            }
            // Enter function scope, add parameters as variable symbols (in order)
            symbols.enterScope();
            Set<String> paramNames = new HashSet<>();
            for (Param p : fd.params) {
                if (paramNames.contains(p.name)) {
                    errors.error("Duplicate parameter name in function " + fd.name + ": " + p.name);
                } else {
                    // parameters can't collide inside the function's parameter list
                    paramNames.add(p.name);
                    if (!symbols.defineInCurrentScope(new VarSymbol(p.name, p.type))) {
                        errors.error("Parameter name shadows another symbol already in function scope: " + p.name);
                    } else {
                        // link parameter declaration AST node is not a Stmt, so we don't have a direct node to attach.
                        // If desired, one could create Param AST nodes as separate AST nodes for linking.
                    }
                }
            }
            // traverse body statements (variables must be defined before use here)
            visitBlockPass1(fd.body);
            symbols.exitScope();
        }
        else if (s instanceof ReturnStmt rs) {
            visitExprPass1(rs.value);
        }
        else if (s instanceof ExprStmt es) {
            visitExprPass1(es.expr);
        }
        else if (s instanceof Block b) {
            symbols.enterScope();
            visitBlockPass1(b);
            symbols.exitScope();
        }
        else if (s instanceof WhileStmt w) {
            visitExprPass1(w.condition);
            symbols.enterScope();
            visitBlockPass1(w.body);
            symbols.exitScope();
        }
        else if (s instanceof IfStmt iff) {
            visitExprPass1(iff.condition);
            symbols.enterScope();
            visitBlockPass1(iff.thenBranch);
            symbols.exitScope();
            symbols.enterScope();
            visitBlockPass1(iff.elseBranch);
            symbols.exitScope();
        }
        else {
            throw new RuntimeException("Unknown Stmt in pass1: " + s);
        }
    }

    private void visitBlockPass1(Block b) {
        for (Stmt s : b.statements) visitStmtPass1(s);
    }

    private void visitExprPass1(Expr e) {
        if (e instanceof IntLiteral || e instanceof StringLiteral || e instanceof BoolLiteral) {
            // nothing to do
        }
        else if (e instanceof Variable v) {
            Symbol sym = symbols.resolve(v.name);
            if (sym == null) {
                errors.error("Use of undefined variable: " + v.name);
            } else if (sym instanceof FnSymbol) {
                // variable cannot be used as variable if it's a function
                errors.error("Function name used where variable expected: " + v.name);
            }
            // else OK
        }
        else if (e instanceof Binary b) {
            visitExprPass1(b.left);
            visitExprPass1(b.right);
            // type checks will come later (not required in A5.2/A5.3) — could be added here or a later pass
        }
        else if (e instanceof Call c) {
            // If the name resolves to a variable -> error (variable used as function).
            Symbol sym = symbols.resolve(c.name);
            if (sym instanceof VarSymbol) {
                errors.error("Attempt to call a variable as function: " + c.name);
            }
            // we do NOT error if function is not yet defined (calls before defs permitted).
            // Instead: collect call site to check in pass2.
            callSites.add(c);
            // visit arguments
            for (Expr arg : c.args) visitExprPass1(arg);
        }
        else {
            throw new RuntimeException("Unknown Expr in pass1: " + e);
        }
    }

    /* ------------------ PASS 2 ------------------ */
    public void runPass2(List<Stmt> program) {
        // For pass2 we use the symbol table as built in pass1 (it contains all definitions found).
        // Check each collected call site for existence, kind (must be FnSymbol), and param count.
        for (Call call : callSites) {
            Symbol sym = symbols.resolve(call.name);
            if (sym == null) {
                errors.error("Call to undefined function: " + call.name);
                continue;
            }
            if (!(sym instanceof FnSymbol fn)) {
                errors.error("Call target is not a function: " + call.name);
                continue;
            }
            // Num args match?
            if (fn.params.size() != call.args.size()) {
                errors.error(String.format("Argument count mismatch in call to %s: expected %d, got %d",
                        call.name, fn.params.size(), call.args.size()));
            }
            // Optional: check arg types match param types — task A5.3 did not strictly demand type-checking of args,
            // but you can add it here by inferring expression types (would need a type inference/evaluation pass).
        }
    }
}
