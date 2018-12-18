package cop5556fa18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPTypeChecker.SemanticException;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.Program;

public class PLPTypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		PLPScanner scanner = new PLPScanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new PLPParser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new PLPTypeChecker();
		ast.visit(v, null);
	}
	
	
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {print 1+2;}";
		typeCheck(input);
	}
	
	//Exception because + is not allowed with true
	@Test
	public void unaryTypeCheck1() throws Exception {
		thrown.expect(SemanticException.class);
		String input = "prog {boolean c = +true;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void declarationTypeCheck() throws Exception {
		String input = "prog {boolean d = 8 + 9;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	}
	

	@Test
	public void declarationTypeCheck2() throws Exception {
		String input = "prog {int e = 8 + 9;}";
		typeCheck(input);
	}
	
	@Test
	public void declarationTypeCheck3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {float f = 8 + 9;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	}
	
	@Test
	public void declarationTypeCheck4() throws Exception {
		
		String input = "prog {boolean g = true | false;}";
		typeCheck(input);
	}
	
	@Test
	public void declarationTypeCheck5() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {string s = 9 > 5;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	}
	
	@Test
	public void declarationTypeCheck6() throws Exception {
		
		String input = "prog {string s2 = \"cise\" + \"florida\";}";
		typeCheck(input);
	}
	
	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { print true+4; }"; //should throw an error due to incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void declarationTypeCheck7() throws Exception {
		
		String input = "prog {string s2 = \"cise\" + \"florida\";}";
		typeCheck(input);
	}
	
	@Test
	public void testVariableListDeclaration1() throws Exception {
		
		String input = "prog {int a,b,c;}";
		typeCheck(input);
	}
	
	@Test
	public void testVariableListDeclaration2() throws Exception {
		
		String input = "prog {int a,b,c; a = 9 ** 10; c = 5; string s1, s2;}";
		typeCheck(input);
	}
	
	@Test
	public void testVariableListDeclarationFail1() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; print d;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testVariableListDeclaration3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; if (true) { int a,b,c; float d = 5;};}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testVariableListDeclaration4() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; if (true) { int a,b,c; float d = 5; while(false) { int a=11; int c=33;};};}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	}
	
	@Test
	public void testVariableListDeclarationFail2() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; string d, e, c;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testIfStatement() throws Exception {
		
		String input = "prog {if (5 * 6 < 40) { print \"hello\"; };}";
		typeCheck(input);
	}
	
	@Test
	public void testIfStatementFail1() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {if (3 % 4) { print \"hello\"; };}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	@Test
	public void testWhileStatement() throws Exception {
		
		String input = "prog {while (5 * 6 < 40) { print \"hello\"; };}";
		typeCheck(input);
	}
	
	@Test
	public void testWhileStatementFail1() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {while (3 % 4) { print \"hello\"; };}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testExpressionBinary1() throws Exception {
		
		String input = "prog {boolean b = 3 == 4;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary2() throws Exception {
		
		String input = "prog {boolean b = 3 != 4;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary3() throws Exception {
		
		String input = "prog {boolean b = 3.9 <= 4.9 ;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary4() throws Exception {
		
		String input = "prog {boolean b = true != false ;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary6() throws Exception {
		
		String input = "prog {float res = 4.0 - 3;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary7() throws Exception {
		
		String input = "prog {float res = 3 ** 2.0;}";
		typeCheck(input);
	}
	
	
	@Test
	public void testExpressionBinary8() throws Exception {
		
		String input = "prog {int res = 3 % 1;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary9() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int res = 3 ** 1.0;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	
	}
	
	@Test
	public void testExpressionConditional() throws Exception {
		
		String input = "prog {int a = 9 < 10 ? 5 : 4;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionConditional2() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a = 9 < 10 ? 5 : 4.0;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	
	}
	
	@Test
	public void testExpressionConditional3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a = 9 < 10 ? true : false;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	
	}
	
	@Test
	public void testExpressionConditional4() throws Exception {
		
		String input = "prog {boolean a = 9 < 10 ? true : false;}";
		typeCheck(input);
	}
	
	@Test
	public void testUnaryExpression() throws Exception {
		
		String input = "prog {int a = +3; float b = -4.0; boolean t = !false;}";
		typeCheck(input);
	}
	
	@Test
	public void testSleepStatement1() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {sleep 8**4.0;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	
	}
	
	@Test
	public void testSleepStatement2() throws Exception {
		
		String input = "prog {sleep 8%4;}";
		typeCheck(input);
	}
	
	@Test
	public void testabsFunction() throws Exception {
		
		String input = "prog {print abs (8**4);}";
		typeCheck(input);
	}
	
	@Test
	public void testFunction2() throws Exception {
		
		String input = "prog {print abs (8**4.0); print cos(3.0); print log(5.0);}";
		typeCheck(input);
	}
	
	@Test
	public void testabsFunction3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {print log (4);}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	
	}
	
	@Test
	public void testFunction3() throws Exception {
		
		String input = "prog {print int (8**4.0); print float(3.0); print float(5); print int(3);}";
		typeCheck(input);
	}
	
	@Test
	public void testVariableListDeclarationFail3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; if (true) { int a,b,c; float d = 5;}; int c = 95;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testScope1() throws Exception {
		
		String input = "prog {int a = 5; if (true) {int b = 3;}; print a;}";
		typeCheck(input);
	}
	
	//tests the inner scope for identifier 'a'. 'a' should be visible in the if block
	@Test
	public void testScope2() throws Exception {
		
		String input = "prog {int a = 5; if (true) {int b = 3; print a;}; print a;}";
		typeCheck(input);
	}
	
	//should fail because b was not declared
	@Test
	public void testScopefail1() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a = 5; if (true) {int a = 3;}; print b;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testScope3() throws Exception {
		
		String input = "prog {int a = 5; if (true) {a = 3; print a;}; a = 3 == 3 ? 5 : 7;}";
		typeCheck(input);
	}
	
	@Test
	public void testScopefail2() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a = 5; if (true) {int a = 3; print a;}; a = 3 == 3 ? \"5\" : \"7\";}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	//here we declare 'a' in inner scope and try to use it outside
	@Test
	public void testScopefail3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {if (true) {int a = 3; print a;}; a = 9;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	//here we declare 'a' in outer scope and try to use it in the inner scope
	@Test
	public void testScopeInner() throws Exception {
		
		String input = "prog {int a = 20; if (true) {a = 3; print a;}; a = 9;}";
		typeCheck(input);
	}
	
	@Test
	public void testScopeUnitialized() throws Exception {
		
		String input = "prog {int a,b; a = b + 3;}";
		typeCheck(input);
	}
	
	@Test
	public void testScopefail4() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b; a = b + 3; if (a > b) {int c = 3; print b;}; b = c;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testDecFailExamTest() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog { if (true) { int z; z = x;}; }";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testScopefail4ExamTest() throws Exception {
		
		String input = "p{int var; if(true) {float var; var = 5.0;}; var = 5;}";
		typeCheck(input);
	}
	
	
	@Test
	public void testScopefail4ExamTest2() throws Exception {
		
		String input = "prog { int x; while(true) { int x; }; }";
		typeCheck(input);
	}
	
	@Test
	public void testScopefail4ExamTestVariation() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "p{int var; if(true) {float var; var = 5.0;}; var = 5.0;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}	
	}
	
	@Test
	public void testVariableListDeclarationFailInner2() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; string d, e; if(true) {float var; var = 5.0;}; var = 5;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testVariableListDeclarationFailInner3() throws Exception {
		
		thrown.expect(SemanticException.class);
		String input = "prog {int a,b,c; string d, e; if(true) {float var = 9;}; var = 5;}";
		try {
			typeCheck(input);
		}catch(SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testVariableListDeclarationPassInner() throws Exception {
		
		String input = "prog {int a,b,c; string d, e; if(true) {int var = 9;}; d = \"hi\"; c = 10;}";
		typeCheck(input);
	}
}
