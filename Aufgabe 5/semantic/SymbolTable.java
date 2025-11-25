package Aufgabe;

import java.util.*;
import ast.AST;

/**
 * SymbolTable mit Stack von Scopes. Jeder Scope ist Map<String,Symbol>.
 * Zusätzlich Map<ASTNode,Symbol> nodeToSymbol für Querverbindungen.
 */
public class SymbolTable {
    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();
    private final Map<AST.ASTNode, Symbol> nodeToSymbol = new IdentityHashMap<>();

    public SymbolTable() {
        enterScope(); // global scope
    }

    public void enterScope() { scopes.push(new HashMap<>()); }

    public void exitScope() {
        if (scopes.isEmpty()) throw new IllegalStateException("No scope to exit");
        scopes.pop();
    }

    /** define in current scope; returns false if name already present in current scope */
    public boolean defineInCurrentScope(Symbol sym) {
        Map<String, Symbol> cur = scopes.peek();
        if (cur.containsKey(sym.name)) return false;
        cur.put(sym.name, sym);
        return true;
    }

    /** resolve name searching from innermost to outermost scope */
    public Symbol resolve(String name) {
        for (Map<String, Symbol> s : scopes) {
            if (s.containsKey(name)) return s.get(name);
        }
        return null;
    }

    /** resolve only in current (innermost) scope */
    public Symbol resolveInCurrentScope(String name) {
        Map<String, Symbol> cur = scopes.peek();
        return cur.get(name);
    }

    /** attach symbol to AST node (Querverbindung) */
    public void link(AST.ASTNode node, Symbol sym) {
        nodeToSymbol.put(node, sym);
    }

    public Symbol getLinked(AST.ASTNode node) {
        return nodeToSymbol.get(node);
    }

    /** For debugging */
    public String dumpCurrentScope() {
        StringBuilder sb = new StringBuilder();
        Map<String, Symbol> cur = scopes.peek();
        for (Map.Entry<String, Symbol> e : cur.entrySet()) sb.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
        return sb.toString();
    }
}
