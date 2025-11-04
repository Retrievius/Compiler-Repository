// Generated from C:/Uni Programme/Compilerbau Intellij/student-support-code-template-master/src/main/antlr/Aufgabe_3_1.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Aufgabe_3_1Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Aufgabe_3_1Visitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Aufgabe_3_1Parser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(Aufgabe_3_1Parser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link Aufgabe_3_1Parser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(Aufgabe_3_1Parser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link Aufgabe_3_1Parser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(Aufgabe_3_1Parser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link Aufgabe_3_1Parser#while}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile(Aufgabe_3_1Parser.WhileContext ctx);
	/**
	 * Visit a parse tree produced by {@link Aufgabe_3_1Parser#ifelse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfelse(Aufgabe_3_1Parser.IfelseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Aufgabe_3_1Parser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(Aufgabe_3_1Parser.ExprContext ctx);
}