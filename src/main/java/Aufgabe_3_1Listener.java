// Generated from C:/Uni Programme/Compilerbau Intellij/student-support-code-template-master/src/main/antlr/Aufgabe_3_1.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Aufgabe_3_1Parser}.
 */
public interface Aufgabe_3_1Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Aufgabe_3_1Parser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(Aufgabe_3_1Parser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link Aufgabe_3_1Parser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(Aufgabe_3_1Parser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link Aufgabe_3_1Parser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(Aufgabe_3_1Parser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Aufgabe_3_1Parser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(Aufgabe_3_1Parser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Aufgabe_3_1Parser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(Aufgabe_3_1Parser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link Aufgabe_3_1Parser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(Aufgabe_3_1Parser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link Aufgabe_3_1Parser#while}.
	 * @param ctx the parse tree
	 */
	void enterWhile(Aufgabe_3_1Parser.WhileContext ctx);
	/**
	 * Exit a parse tree produced by {@link Aufgabe_3_1Parser#while}.
	 * @param ctx the parse tree
	 */
	void exitWhile(Aufgabe_3_1Parser.WhileContext ctx);
	/**
	 * Enter a parse tree produced by {@link Aufgabe_3_1Parser#ifelse}.
	 * @param ctx the parse tree
	 */
	void enterIfelse(Aufgabe_3_1Parser.IfelseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Aufgabe_3_1Parser#ifelse}.
	 * @param ctx the parse tree
	 */
	void exitIfelse(Aufgabe_3_1Parser.IfelseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Aufgabe_3_1Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(Aufgabe_3_1Parser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Aufgabe_3_1Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(Aufgabe_3_1Parser.ExprContext ctx);
}