package cop5556fa18;

import static cop5556fa18.PLPScanner.Kind.OP_PLUS;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPParserTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static final boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}


		//creates and returns a parser for the given input.
		private PLPParser makeParser(String input) throws LexicalException {
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
			show(scanner);   //Display the Scanner
			PLPParser parser = new PLPParser(scanner);
			return parser;
		}
		
		/**
		 * Test case with an empty program.  This throws an exception 
		 * because it lacks an identifier and a block
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  //The input is the empty string.  
			thrown.expect(SyntaxException.class);
			PLPParser parser = makeParser(input);
			@SuppressWarnings("unused")
			Program p = parser.parse();
		}
		
		/**
		 * Smallest legal program.
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testSmallest() throws LexicalException, SyntaxException {
			String input = "b{}";  
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			assertEquals("b", p.name);
			assertEquals(0, p.block.declarationsAndStatements.size());
		}	
		
		
		/**
		 * Utility method to check if an element of a block at an index is a declaration with a given type and name.
		 * 
		 * @param block
		 * @param index
		 * @param type
		 * @param name
		 * @return
		 */
		Declaration checkDec(Block block, int index, Kind type, String name) {
			PLPASTNode node = block.declarationsAndStatements(index);
			assertEquals(VariableDeclaration.class, node.getClass());
			VariableDeclaration dec = (VariableDeclaration) node;
			assertEquals(type, dec.type);
			assertEquals(name, dec.name);
			return dec;
		}	
		
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int i; char c;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "i");
			checkDec(p.block, 1, Kind.KW_char, "c");
		}
		
		/** 
		 * Test a specific grammar element by calling a corresponding parser method rather than parse.
		 * This requires that the methods are visible (not private). 
		 * 
		 * @throws LexicalException
		 * @throws SyntaxException
		 */
		
		@Test
		public void testExpression() throws LexicalException, SyntaxException {
			String input = "x + 2";
			PLPParser parser = makeParser(input);
			Expression e = parser.expression();  //call expression here instead of parse
			show(e);	
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("x", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(2, right.value);
			assertEquals(OP_PLUS, b.op);
		}
		
		@Test	
		public void testifcondition() throws LexicalException, SyntaxException {
			String input = "if(true) { print \"hi\"; };";
			PLPParser parser = makeParser(input);
			Statement s = parser.ifstatement();
			show(s);	
			assertEquals(IfStatement.class, s.getClass());
			IfStatement i = (IfStatement)s;
			assertEquals(ExpressionBooleanLiteral.class,i.condition.getClass());
			ExpressionBooleanLiteral be =(ExpressionBooleanLiteral)i.condition;
			assertEquals(true, be.value);
			Block b = i.block;
			assertEquals(1, b.declarationsAndStatements.size());
			assertEquals(PrintStatement.class, b.declarationsAndStatements.get(0).getClass());
			PrintStatement ps = (PrintStatement) b.declarationsAndStatements.get(0);
			Expression e = ps.expression;
			assertEquals(ExpressionStringLiteral.class, e.getClass());
			ExpressionStringLiteral esl = (ExpressionStringLiteral)e;
			assertEquals("hi", esl.text);
		}
		
		@Test	
		public void testifconditionstatement() throws LexicalException, SyntaxException {
			String input = "if(true) { int a = 5; print \"hi\"; };";
			PLPParser parser = makeParser(input);
			Statement s = parser.ifstatement();
			show(s);	
			assertEquals(IfStatement.class, s.getClass());
			IfStatement i = (IfStatement)s;
			assertEquals(ExpressionBooleanLiteral.class,i.condition.getClass());
			ExpressionBooleanLiteral be =(ExpressionBooleanLiteral)i.condition;
			assertEquals(true, be.value);
			Block b = i.block;
			assertEquals(2, b.declarationsAndStatements.size());
			checkDec(b, 0, Kind.KW_int, "a");
			assertEquals(PrintStatement.class, b.declarationsAndStatements.get(1).getClass());
			PrintStatement ps = (PrintStatement) b.declarationsAndStatements.get(1);
			Expression e = ps.expression;
			assertEquals(ExpressionStringLiteral.class, e.getClass());
			ExpressionStringLiteral esl = (ExpressionStringLiteral)e;
			assertEquals("hi", esl.text);		
		}
		
		@Test	
		public void testifconditionstatementFalse() throws LexicalException, SyntaxException {
			String input = "a{if(true) { int a = 5; print \"hi\"; ;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test	
		public void testwhileconditionstatement() throws LexicalException, SyntaxException {
			String input = "while(name != \"Aswin\") { int a = 5; print a ** 5; };";
			PLPParser parser = makeParser(input);
			Statement s = parser.whilestatement();
			show(s);	
			assertEquals(WhileStatement.class, s.getClass());
			WhileStatement i = (WhileStatement)s;
			assertEquals(ExpressionBinary.class,i.condition.getClass());
			ExpressionBinary b = (ExpressionBinary)i.condition;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("name", left.name);
			assertEquals(ExpressionStringLiteral.class, b.rightExpression.getClass());
			ExpressionStringLiteral right = (ExpressionStringLiteral)b.rightExpression;
			assertEquals("Aswin", right.text);
			assertEquals(Kind.OP_NEQ, b.op);
			
			Block block = i.b;
			assertEquals(2, block.declarationsAndStatements.size());
			checkDec(block, 0, Kind.KW_int, "a");
			assertEquals(PrintStatement.class, block.declarationsAndStatements.get(1).getClass());
			PrintStatement ps = (PrintStatement) block.declarationsAndStatements.get(1);
			Expression e = ps.expression;
			assertEquals(ExpressionBinary.class, e.getClass());
			b = (ExpressionBinary)e;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("a", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral rightE = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(5, rightE.value);
			assertEquals(Kind.OP_POWER, b.op);
		}
		
		
		@Test	
		public void teststatementparse1() throws LexicalException, SyntaxException {
			String input = "b{int a=5; float k = a+10; int b = 6;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "a");
			checkDec(p.block, 1, Kind.KW_float, "k");		
			checkDec(p.block, 2, Kind.KW_int, "b");	
		}
		
		@Test	
		public void teststatementparse1false() throws LexicalException, SyntaxException {
			String input = "b{int a=5; float k = a+10 int b =6;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test	
		public void teststatementparse1false2() throws LexicalException, SyntaxException {
			String input = "b{int a=5; a = a+10; int b =6;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "a");
			checkDec(p.block, 2, Kind.KW_int, "b");	
			assertEquals(AssignmentStatement.class,p.block.declarationsAndStatements.get(1).getClass());
			AssignmentStatement as = (AssignmentStatement)p.block.declarationsAndStatements.get(1);
			assertEquals("a", as.lhs.identifier);
			Expression e = as.expression;
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("a", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(10, right.value);
			assertEquals(OP_PLUS, b.op);
		}
		
		@Test	
		public void teststatementparse2() throws LexicalException, SyntaxException {
			String input = "b{int a=5; if (a == 5) { while (false) { print \"hi\" ; } ;};}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			checkDec(p.block, 0, Kind.KW_int, "a");
			
			assertEquals(p.block.declarationsAndStatements.size(), 2);
			Statement s = (Statement) p.block.declarationsAndStatements.get(1);
			
			assertEquals(IfStatement.class, s.getClass());
			IfStatement i = (IfStatement) s;
			ExpressionBinary b = (ExpressionBinary)i.condition;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("a", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(5, right.value);
			assertEquals(Kind.OP_EQ, b.op);
			
			assertEquals(i.block.declarationsAndStatements.size(), 1);
			s = (Statement) i.block.declarationsAndStatements.get(0);
			
			assertEquals(WhileStatement.class, s.getClass());
			WhileStatement w = (WhileStatement)s;
			assertEquals(ExpressionBooleanLiteral.class,w.condition.getClass());
			ExpressionBooleanLiteral be =(ExpressionBooleanLiteral)w.condition;
			assertEquals(false, be.value);
			Block block = w.b;
			
			assertEquals(1, block.declarationsAndStatements.size());
			assertEquals(PrintStatement.class, block.declarationsAndStatements.get(0).getClass());
			PrintStatement ps = (PrintStatement) block.declarationsAndStatements.get(0);
			Expression e = ps.expression;
			assertEquals(ExpressionStringLiteral.class, e.getClass());
			ExpressionStringLiteral esl = (ExpressionStringLiteral)e;
			assertEquals("hi", esl.text);
		}
		
		
		@Test	
		public void teststatementparse2fail() throws LexicalException, SyntaxException {
			String input = "b{int a=5; if (a == 5) { while (false) { print \"hi\" ; }; };}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		
		@Test
		public void testBooleanVariableDeclaration() throws LexicalException, SyntaxException{
			
			String input = "b{boolean a;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();		
			checkDec(p.block, 0, Kind.KW_boolean, "a");
		}
		
		@Test
		public void testAssignStatement() throws LexicalException, SyntaxException{
			
			String input = "b{a=5;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();		
			assertEquals(1, p.block.declarationsAndStatements.size());
			AssignmentStatement as = (AssignmentStatement)p.block.declarationsAndStatements.get(0);
			assertEquals("a", as.lhs.identifier);
			Expression e = as.expression;
			assertEquals(ExpressionIntegerLiteral.class, e.getClass());
			ExpressionIntegerLiteral ei = (ExpressionIntegerLiteral)e;
			assertEquals(5, ei.value);

		}
		
		@Test
		public void testSleepStatement() throws LexicalException, SyntaxException{
			
			String input = "b{sleep atan(9**8) + 3;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();		
			assertEquals(1, p.block.declarationsAndStatements.size());
			SleepStatement ss = (SleepStatement)p.block.declarationsAndStatements.get(0);
			assertEquals(ExpressionBinary.class, ss.time.getClass());
			
			ExpressionBinary eb = (ExpressionBinary) ss.time;
			assertEquals(FunctionWithArg.class, eb.leftExpression.getClass());
			FunctionWithArg fa = (FunctionWithArg)eb.leftExpression;
			assertEquals(Kind.KW_atan, fa.functionName);
			ExpressionBinary b = (ExpressionBinary)fa.expression;
			assertEquals(ExpressionIntegerLiteral.class, b.leftExpression.getClass());
			ExpressionIntegerLiteral left = (ExpressionIntegerLiteral)b.leftExpression;			
			assertEquals(9, left.value);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(8, right.value);
			assertEquals(Kind.OP_POWER, b.op);
			
			assertEquals(ExpressionIntegerLiteral.class, eb.rightExpression.getClass());
			ExpressionIntegerLiteral eb_right = (ExpressionIntegerLiteral)eb.rightExpression;
			assertEquals(3, eb_right.value);
			assertEquals(Kind.OP_PLUS, eb.op);
			
			
		}
		
		
		@Test
		public void testdeclaration() throws LexicalException, SyntaxException{
			
			String input = "dec{char c = 't';}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();		
			assertEquals(1, p.block.declarationsAndStatements.size());
			PLPASTNode node = p.block.declarationsAndStatements(0);
			assertEquals(VariableDeclaration.class, node.getClass());
			VariableDeclaration dec = (VariableDeclaration) node;
			assertEquals(Kind.KW_char, dec.type);
			assertEquals("c", dec.name);
			assertEquals(ExpressionCharLiteral.class, dec.expression.getClass());
			ExpressionCharLiteral e = (ExpressionCharLiteral) dec.expression;
			assertEquals('t', e.text);
		}
		
		@Test
		public void testUnaryBooleanLiteral() throws LexicalException, SyntaxException{
			
			String input = "!true";
			PLPParser parser = makeParser(input);
			Expression e = parser.expression();		
			assertEquals(ExpressionUnary.class, e.getClass());
			ExpressionUnary eu = (ExpressionUnary) e;
			assertEquals(Kind.OP_EXCLAMATION, eu.op);
			assertEquals(ExpressionBooleanLiteral.class, eu.expression.getClass());
			ExpressionBooleanLiteral ebl = (ExpressionBooleanLiteral) eu.expression;
			assertEquals(true, ebl.value);
		}
		
		@Test
		public void testPrintStatement() throws LexicalException, SyntaxException{
			
			String input = "b{print atan(9**8) + 3 / 6;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testMixStatement() throws LexicalException, SyntaxException{
			
			String input = "b{print atan(9**8) + 3 / 6; sleep atan(9**8) + 3 / 6; a = 7 % 2; int k = 4;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testMixStatement2() throws LexicalException, SyntaxException{
			
			String input = "b{print atan(9**8) + 3 / 6; while (true) { string name = \"Aswin\";};}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testMixStatement2False() throws LexicalException, SyntaxException{
			
			String input = "b{print atan(9**8) + 3 / 6; while ( < 5) { string name = \"Aswin\";};}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();			
		}
		
		@Test
		public void testNegativeBooleanVariableDeclaration() throws LexicalException, SyntaxException{
			
			String input = "b{boolefan a;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test
		public void testMultipleIntVariableDeclaration() throws LexicalException, SyntaxException{
			
			String input = "b{int b, x, y;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testMultipleIntVariableDeclaration2() throws LexicalException, SyntaxException{
			
			String input = "b{int b, x, y, ;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();	
		}
		
		@Test
		public void testCharVariableDeclaration() throws LexicalException, SyntaxException{
			
			String input = "b{char c;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testDoubleVariableDeclaration() throws LexicalException, SyntaxException{
			
			String input = "b{double d, t;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testStringVariableDeclaration() throws LexicalException, SyntaxException{
			
			String input = "b{string e;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testBooleanVariableInitialization() throws LexicalException, SyntaxException{
			
			String input = "b{boolean a = true;}";
			PLPParser parser = makeParser(input);
			parser.parse();		
		}
		
		@Test
		public void testIntVariableInitialization() throws LexicalException, SyntaxException{
			
			String input = "b{int B = 10;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testCharVariableInitialization() throws LexicalException, SyntaxException{
			
			String input = "b{char c = 'a';}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testDoubleVariableInitialization() throws LexicalException, SyntaxException{
			
			String input = "b{double d = 23.2;}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testStringVariableInitialization() throws LexicalException, SyntaxException{
			
			String input = "b{double e = \"Hello, World\";}";
			PLPParser parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testBinaryOperands() throws LexicalException, SyntaxException{
			
			String input = "b{int a = 1+2;}";
			PLPParser parser = makeParser(input);
			parser.parse();
			
			input = "b{double d = 2.12-1;}";
			parser = makeParser(input);
			parser.parse();	
			
			input = "b{double a  = 1 + 2 *4.5;}";
			parser = makeParser(input);
			parser.parse();
			
			input = "b{double t = (1+2) * 4.5;}";
			parser = makeParser(input);
			parser.parse();		
			
			input = "b{double t = (((4-2)*5.6)/3)+2;}";
			parser = makeParser(input);
			parser.parse();
			
			input = "b{double t = 4 - 2 * 5.6 / 3;}";
			parser = makeParser(input);
			parser.parse();			
		}
		
		@Test
		public void testIfStatement() throws LexicalException, SyntaxException{
			
			String input = "b{if ( true ){  print (\"Value of a is 100\");  };}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testIfStatement2() throws LexicalException, SyntaxException{
			
			String input = "b{if ( true) {" + 
					" print( \"hi\" );" + 
					" print(\"what\");" + 
					" print(82);" + 
					"};}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testOrExpression() throws LexicalException, SyntaxException {
			String input = "b{int b = true | false;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testOrExpression2() throws LexicalException, SyntaxException {
			String input = "b{int b = true | false | 9**2 | 6/5;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testAndExpression() throws LexicalException, SyntaxException {
			String input = "b{int b = true & false;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testAndExpressionFalse() throws LexicalException, SyntaxException {
			String input = "b{int b = true & false a = 5;}";
			thrown.expect(SyntaxException.class);
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testEqExpression() throws LexicalException, SyntaxException {
			String input = "b{int a_ = 9 == 5 != 10 == false;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testEqExpressionFalse() throws LexicalException, SyntaxException {
			String input = "b{int _ = 9 == 5 != 10 == false;}";
			thrown.expect(LexicalException.class);
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testRelExpression() throws LexicalException, SyntaxException {
			String input = "b{int a = 9 < 5 <= 10 >= 3;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		

		@Test
		public void testRelExpressionFalse() throws LexicalException, SyntaxException {
			String input = "b{int a = 9 <  <= 10 >= 3;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test
		public void testAddExpression() throws LexicalException, SyntaxException {
			String input = "b{int a = 9 + 3 - 2;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testMultExpression() throws LexicalException, SyntaxException {
			String input = "b{int a = 9 * 3 / 2 % 10;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testPowerExpression() throws LexicalException, SyntaxException {
			String input = "b{int b = 5**1;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testPowerExpressionfalse() throws LexicalException, SyntaxException {
			String input = "b{int b = 5**;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test
		public void testUnaryExpression() throws LexicalException, SyntaxException {
			String input = "b{a = + - + - ! ! + 6;}"; 
			PLPParser parser = makeParser(input); 
			parser.parse();
		}
		
		@Test
		public void testUnaryExpressionFalse() throws LexicalException, SyntaxException {
			String input = "b{a = + - + - ! ! + ;}"; 
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test
		public void testFunction() throws LexicalException, SyntaxException {
			String input = "b{a = sin(3) + cos(5) + atan(2) - abs(1) * log(3) - int(6) * float(9);}"; 
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testqnexpression() throws LexicalException, SyntaxException {
			String input = "b{int b = 9 == 5 ? true : b == true ? 8 : 9**4;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}

		@Test
		public void testqnexpressionFalse() throws LexicalException, SyntaxException {
			String input = "b{int b = 9 == 5 ? true : b ==  ? ;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test
		public void testqnexpressionFalse2() throws LexicalException, SyntaxException {
			String input = "b{int b = 9 == 5 ? true : b ==  ? 8 : ;}";
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
		}
		
		@Test
		public void testMixExpression() throws LexicalException, SyntaxException {
			String input = "b{int b = 5**(2+(9-3*7)/4%2);}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void testMixExpression2() throws LexicalException, SyntaxException {
			String input = "b{a = sin(3) + 9 - 6 * atan(8**7);}"; 
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void statement() throws LexicalException, SyntaxException {
			String input = "b{if(5.2<=7){\n\n%{ %%xyz5% %}\nfloat xyz= 9;};}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		
		@Test
		public void comment() throws LexicalException, SyntaxException {
			String input = "b{%{ int a=3;}%}}"; 
			PLPParser parser = makeParser(input);
			parser.parse();
		}

}
