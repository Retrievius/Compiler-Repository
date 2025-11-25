package Aufgabe;

import java.util.*;
import ast.AST;

/** Symbole: Basisklasse + VarSymbol + FnSymbol */
public abstract class Symbol {
    public final String name;
    public Symbol(String name) { this.name = name; }
}

public class VarSymbol extends Symbol {
    public final AST.PrimType type;
    public VarSymbol(String name, AST.PrimType type) { super(name); this.type = type; }
    public String toString() { return "VarSymbol("+name+":"+type+")"; }
}

public class FnSymbol extends Symbol {
    public final AST.PrimType returnType;
    public final List<AST.Param> params;
    public FnSymbol(String name, AST.PrimType returnType, List<AST.Param> params) {
        super(name); this.returnType = returnType; this.params = List.copyOf(params);
    }
    public String toString() { return "FnSymbol("+name+"->"+returnType+"/"+params.size()+" params)"; }
}
