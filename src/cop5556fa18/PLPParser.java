/**
* 
* 
* Name     : Aswin Suresh Krishnan
* UFID     : 1890-1173
* Due Date : 5 October 2018
* 
* 
*/

package cop5556fa18;

import java.util.ArrayList;
import java.util.List;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	private void read() {
		
		t = scanner.nextToken();
	}
	
	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}
	
	/**
	 * Program -> Identifier Block
	 */
	public Program program() throws SyntaxException {
		
		Token firstToken = t;
		String name = t.getText();
		match(Kind.IDENTIFIER);
		Block block = block();
		return new Program(firstToken, name, block);
	}
	
	/**
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string};
	Kind[] firstStatement = {Kind.KW_if,Kind.KW_while, Kind.KW_sleep, Kind.KW_print, Kind.IDENTIFIER};

	public Block block() throws SyntaxException {
		
		Token firstToken = t;
		List<PLPASTNode> declarationsAndStatements = new ArrayList<PLPASTNode>();
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
		    if (checkKind(firstDec)) {
				declarationsAndStatements.add(declaration());
			} 
		    else if (checkKind(firstStatement)) {
				declarationsAndStatements.add(statement());
			}
			match(Kind.SEMI); 
		}
		match(Kind.RBRACE);
		return new Block(firstToken, declarationsAndStatements);
	}
	
	/**
	 * Declaration  Type Identifier ( = Expression |  ) | Type IDENTIFIERLIST
	 */
	public Declaration declaration() throws SyntaxException {
		Declaration declaration = null;
		Expression expression = null;
		Token firstToken = t;
		Kind type = t.kind;
		
		read();
		String name = t.getText();
		match(Kind.IDENTIFIER);
		if(checkKind(Kind.OP_ASSIGN)) {
			
			read();
			expression = expression();
			declaration = new VariableDeclaration(firstToken, type, name, expression);
		}
		else {
			
			List<String> names = new ArrayList<String>();
			names.add(name);
			// If it is not 'int a = 5;' pattern it should be 'int a,b,c';			
			while(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				names.add(t.getText());
				match(Kind.IDENTIFIER);
			}
			if (names.size() == 1)
				declaration = new VariableDeclaration(firstToken, type, name, expression);
			else
				declaration = new VariableListDeclaration(firstToken, type, names);
		}
		return declaration;
	}
	
	
	/**
	 * Expression  OrExpression ? Expression : Expression | OrExpression
	 */
	public Expression expression() throws SyntaxException {
		
		Token firstToken = t;
		Expression condition = orExpression();
		if(checkKind(Kind.OP_QUESTION)) {
			read();
			Expression trueExpression = expression();
			match(Kind.OP_COLON);
			Expression falseExpression = expression();
			
			return new ExpressionConditional(firstToken, condition, trueExpression, falseExpression);
		}
		return condition;
	}
	
	/**
	 * OrExpression  AndExpression ( | AndExpression )*
	 */
	public Expression orExpression () throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = andExpression();
		while (checkKind(Kind.OP_OR)){
			Kind op = t.kind;
			read();
			Expression rightExpression = andExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/**
	 * OrExpression  AndExpression ( | AndExpression )*
	 */
	public Expression andExpression () throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = eqExpression();
		while (checkKind(Kind.OP_AND)){
			Kind op = t.kind;
			read();
			Expression rightExpression = eqExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/**
	 * AndExpression  EqExpression ( & EqExpression )*
	 */
	public Expression eqExpression () throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = relExpression();
		while (checkKind(Kind.OP_EQ) || checkKind(Kind.OP_NEQ)){
			Kind op = t.kind;
			read();
			Expression rightExpression = relExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/**
	 * EqExpression  RelExpression ( ( == | != ) RelExpression )*
	 */
	public Expression relExpression () throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = addExpression();
		while (checkKind(Kind.OP_LT) || checkKind(Kind.OP_GT) || checkKind(Kind.OP_LE) || checkKind(Kind.OP_GE)){
			Kind op = t.kind;
			read();
		    Expression rightExpression = addExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}	
		return leftExpression;
	}
	/**
	 * RelExpression  AddExpression ( ( < | > | <= | >= ) AddExpression )*
	 */
	public Expression addExpression () throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = multExpression();
		while (checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS)){
			Kind op = t.kind;
			read();
			Expression rightExpression = multExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/**
	 * AddExpression  MultExpression ( ( + | - ) MultExpression )
	 */
	public Expression multExpression() throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = powerExpression();
		while (checkKind(Kind.OP_TIMES) || checkKind(Kind.OP_DIV) || checkKind(Kind.OP_MOD)){
			Kind op = t.kind;
			read();
			Expression rightExpression = powerExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/**
	 * MultExpression  PowerExpression ( ( * | / | % ) PowerExpression )*
	 */
	public Expression powerExpression() throws SyntaxException {
		
		Token firstToken = t;
		Expression leftExpression = unaryExpression();
		while (checkKind(Kind.OP_POWER)){
			Kind op = t.kind;
			read();
			Expression rightExpression = powerExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/**
	 * 
	 * UnaryExpression  + UnaryExpression | - UnaryExpression | ! UnaryExpression | Primary
	 */
	public Expression unaryExpression() throws SyntaxException {
		
		Token firstToken = t;
		Kind op = t.kind;
		if (checkKind(Kind.OP_PLUS)){
			read();
			Expression expression = unaryExpression();
			return new ExpressionUnary(firstToken, op, expression);
		}
		else if (checkKind(Kind.OP_MINUS)){
			read();
			Expression expression = unaryExpression();
			return new ExpressionUnary(firstToken, op, expression);
		}
		else if (checkKind(Kind.OP_EXCLAMATION)){
			read();
			Expression expression = unaryExpression();
			return new ExpressionUnary(firstToken, op, expression);
		}
		else 
			return primary();	
	}
	
	/**
	 * Primary  INTEGER_LITERAL | BOOLEAN_LITERAL ​| ​FLOAT_LITERAL | CHAR_LITERAL
 	 * ​| ​STRING_LITERAL | ( Expression ) | IDENTIFIER | Function
	 */
	public Expression primary() throws SyntaxException{
		
		Token firstToken = t;
		if (checkKind(Kind.INTEGER_LITERAL)){
		     int value = t.intVal();
	         read();
	    	 return new ExpressionIntegerLiteral(firstToken, value);

		}
		else if (checkKind(Kind.BOOLEAN_LITERAL)){
			boolean bool_lit;
			if (t.length == 4) {
				bool_lit = true;
			}
			else {
				bool_lit = false;
			}
			read();
        	return new ExpressionBooleanLiteral(firstToken, bool_lit);
		}
		else if (checkKind(Kind.FLOAT_LITERAL)){
			
			float value = t.floatVal();
			read();
			return new ExpressionFloatLiteral(firstToken, value);
		}
		else if (checkKind(Kind.CHAR_LITERAL)) {
			
			char text = t.charVal();
			read();
			return new ExpressionCharLiteral(firstToken, text);
		}
		else if (checkKind(Kind.STRING_LITERAL)) {
			
			String text = t.getText();
			read();
			return new ExpressionStringLiteral(firstToken, text);
		}
		else if (checkKind(Kind.IDENTIFIER)) {
			
			String name = t.getText();
			read();
			return new ExpressionIdentifier(firstToken, name);
		}
		else if (checkKind(Kind.LPAREN)) {
			
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			return e;
		}		
		else 
			return function();
	}
	
	/**
	 * Function  FunctionName ( Expression )
	 */
	public Expression function() throws SyntaxException{
		
		Token firstToken = t;
		Kind function = functionName();
		match(Kind.LPAREN);
		Expression e = expression();
		match(Kind.RPAREN);
		return new FunctionWithArg(firstToken, function, e);
	}
	
	/**
	 * FunctionName  sin | cos | atan | abs | log | int | float
	 */
	public Kind functionName() throws SyntaxException{
		
		if (checkKind(Kind.KW_sin) || checkKind(Kind.KW_cos) || checkKind(Kind.KW_atan) 
				|| checkKind(Kind.KW_abs) || checkKind(Kind.KW_log) || checkKind(Kind.KW_int) || checkKind(Kind.KW_float)) {
			
			Kind function = t.kind;
			read();	
			return function;
		}else {
			// Nothing matched
			throw new SyntaxException(t, "Invalid token: " + t.kind + ", pos: " + t.pos);
		}
	}
	
	/**
	 * Statement  IfStatement | AssignmentStatement | SleepStatement 
	 *	| PrintStatement | WhileStatment
	 */
	public Statement statement() throws SyntaxException, UnsupportedOperationException {
		
		Statement s = null;
		switch (t.kind) {
			case KW_if : s = ifstatement(); break;
			case KW_while : s = whilestatement(); break;
			case IDENTIFIER : s = assignstatement(); break;
			case KW_sleep : s = sleepstatement(); break;
			case KW_print : s = printstatement(); break;
			default : throw new UnsupportedOperationException();
		}
		return s;
	}
	
	/**
	 * IfStatement  if ( Expression ) Block
	 */
	public Statement ifstatement() throws SyntaxException {
		
		Token firstToken = t;
		match(Kind.KW_if);
		match(Kind.LPAREN);
		Expression condition = expression();
		match(Kind.RPAREN);
		Block b = block();
		return new IfStatement(firstToken, condition, b);
	}
	
	/**
	 * WhileStatement  while ( Expression ) Block
	 */
	public Statement whilestatement() throws SyntaxException {
		
		Token firstToken = t;
		match(Kind.KW_while);
		match(Kind.LPAREN);
		Expression condition = expression();
		match(Kind.RPAREN);
		Block b = block();
		return new WhileStatement(firstToken, condition, b);
	}
	
	/**
	 * AssignmentStatement  Identifier = Expression
	 */
	public Statement assignstatement() throws SyntaxException {
		
		Token firstToken = t;
		String name = t.getText();
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		LHS lhs = new LHS(firstToken, name);
		Expression expression = expression();
		return new AssignmentStatement(firstToken, lhs, expression);
	}
	
	/**
	 * SleepStatement  sleep Expression
	 */
	public Statement sleepstatement() throws SyntaxException {
		
		Token firstToken = t;
		match(Kind.KW_sleep);
		Expression time = expression();
		return new SleepStatement(firstToken, time);
	}
	
	/**
	 * PrintStatement  print Expression
	 */
	public Statement printstatement() throws SyntaxException {
		
		Token firstToken = t;
		match(Kind.KW_print);
		Expression expression = expression();
		return new PrintStatement(firstToken, expression);
	}
	
	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (checkKind(kind)) {
			t = scanner.nextToken();
			return;
		}
		//TODO  give a better error message!
		throw new SyntaxException(t,"Syntax Error");
	}
	
}	