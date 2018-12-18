/**
 * JUunit tests for the PLPScanner
 */

package cop5556fa18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner.Kind;;

public class PLPScannerTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}

		/**
		 *Retrieves the next token and checks that it is an EOF token. 
		 *Also checks that this was the last token.
		 *
		 * @param scanner
		 * @return the Token that was retrieved
		 */
		
		Token checkNextIsEOF(PLPScanner scanner) {
			PLPScanner.Token token = scanner.nextToken();
			assertEquals(PLPScanner.Kind.EOF, token.kind);
			assertFalse(scanner.hasTokens());
			return token;
		}


		/**
		 * Retrieves the next token and checks that its kind, position, length, line, and position in line
		 * match the given parameters.
		 * 
		 * @param scanner
		 * @param kind
		 * @param pos
		 * @param length
		 * @param line
		 * @param pos_in_line
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		 	Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(pos, t.pos);
			assertEquals(length, t.length);
			assertEquals(line, t.line());
			assertEquals(pos_in_line, t.posInLine());
			return t;
		}

		/**
		 * Retrieves the next token and checks that its kind and length match the given
		 * parameters.  The position, line, and position in line are ignored.
		 * 
		 * @param scanner
		 * @param kind
		 * @param length
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int length) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(length, t.length);
			return t;
		}
		

		/**
		 * Retrieves the next token and checks that its kind and length match the given
		 * parameters.  The position, line, and position in line are ignored.
		 * 
		 * @param scanner
		 * @param kind
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			return t;
		}
		
		

		/**
		 * Simple test case with an empty program.  The only Token will be the EOF Token.
		 *   
		 * @throws LexicalException
		 */
		@Test
		public void testEmpty() throws LexicalException {
			String input = "";  //The input is the empty string.  This is legal
			show(input);        //Display the input 
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a PLPScanner and initialize it
			show(scanner);   //Display the PLPScanner
			checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
		}

		
		/**
		 * This example shows how to test that your scanner is behaving when the
		 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
		 * 
		 * The example shows catching the exception that is thrown by the scanner,
		 * looking at it, and checking its contents before rethrowing it.  If caught
		 * but not rethrown, then JUnit won't get the exception and the test will fail.  
		 * 
		 * The test will work without putting the try-catch block around 
		 * new PLPScanner(input).scan(); but then you won't be able to check 
		 * or display the thrown exception.
		 * 
		 * @throws LexicalException
		 */
		@Test
		public void failIllegalChar() throws LexicalException {
			String input = ";;~";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
		/**
		 * Using the two previous functions as a template.  You can implement other JUnit test cases.
		 */
		
		@Test
		public void testBooleanVariableDeclaration() throws LexicalException{
			
			String input = "boolean a;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  //Create a PLPScanner and initialize it
			show(scanner);   //Display the PLPScanner
			
		}
		
		//WILL PASS - IT WILL IDENTIFY boolefan as an IDENTIFIER
		@Test
		public void testNegativeBooleanVariableDeclaration() throws LexicalException{
			
			String input = "boolefan a;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			checkNext(scanner, Kind.IDENTIFIER, 0, 8, 1, 1);
			checkNext(scanner, Kind.IDENTIFIER, 9, 1, 1, 10);
			checkNext(scanner, Kind.SEMI, 10, 1, 1, 11);
		}
		
		@Test
		public void testMultipleIntVariableDeclaration() throws LexicalException{
			
			String input = "int b, x, y;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);   
			
		}
		
		@Test
		public void testMultipleIntVariableDeclaration2() throws LexicalException{
			
			String input = "int b, x, y, ;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			checkNext(scanner, Kind.KW_int, 0, 3, 1, 1);
			checkNext(scanner, Kind.IDENTIFIER, 4, 1, 1, 5);
			checkNext(scanner, Kind.COMMA, 5, 1, 1, 6);
			checkNext(scanner, Kind.IDENTIFIER, 7, 1, 1, 8);
			checkNext(scanner, Kind.COMMA, 8, 1, 1, 9);
			checkNext(scanner, Kind.IDENTIFIER, 10, 1, 1, 11);
			checkNext(scanner, Kind.COMMA, 11, 1, 1, 12);
			checkNext(scanner, Kind.SEMI, 13, 1, 1, 14);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testCharVariableDeclaration() throws LexicalException{
			
			String input = "char c;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testDoubleVariableDeclaration() throws LexicalException{
			
			String input = "double d, t;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testStringVariableDeclaration() throws LexicalException{
			
			String input = "string e;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testBooleanVariableInitialization() throws LexicalException{
			
			String input = "boolean a = true;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testIntVariableInitialization() throws LexicalException{
			
			String input = "int B = 10;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testCharVariableInitialization() throws LexicalException{
			
			String input = "char c = 'a';";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testDoubleVariableInitialization() throws LexicalException{
			
			String input = "double d = 23.2;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testStringVariableInitialization() throws LexicalException{
			
			String input = "double e = \"Hello, World\"";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testBinaryOperands() throws LexicalException{
			
			String input = "int a = 1+2;";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
			
			input = "double d = 2.12-1;";
			show(input);
			scanner = new PLPScanner(input).scan();  
			show(scanner);
			
			input = "double a  = 1 + 2 *4.5;";
			show(input);
			scanner = new PLPScanner(input).scan();  
			show(scanner);
			
			input = "double t = (1+2) * 4.5;";
			show(input);
			scanner = new PLPScanner(input).scan();  
			show(scanner);
			
			input = "double t = (((4-2)*5.6)/3)+2;";
			show(input);
			scanner = new PLPScanner(input).scan();  
			show(scanner);
			
			input = "double t = 4 - 2 * 5.6 / 3;";
			show(input);
			scanner = new PLPScanner(input).scan();  
			show(scanner);
			
		}
		
		@Test
		public void testIfStatement() throws LexicalException{
			
			String input = "if ( true ){  print (\"Value of a is 100\")  }";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
		}
		
		@Test
		public void testIfStatement2() throws LexicalException{
			
			String input = "if ( true) {" + 
					" print( \"hi\" );" + 
					" print(\"what\");" + 
					" print(82);" + 
					"}";
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();  
			show(scanner);
		}
		
		@Test
		public void testSemi() throws LexicalException {
			String input = ";;\r\n;;";	//\r\n are counted as 2
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.SEMI, 0, 1, 1, 1);
			checkNext(scanner, Kind.SEMI, 1, 1, 1, 2);
			checkNext(scanner, Kind.SEMI, 4, 1, 2, 1);
			checkNext(scanner, Kind.SEMI, 5, 1, 2, 2);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void whiteSpaces() throws LexicalException {
			String input = "; ;\t;\r;\n;\r\n;\f;";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.SEMI, 0, 1, 1, 1);
			checkNext(scanner, Kind.SEMI, 2, 1, 1, 3);
			checkNext(scanner, Kind.SEMI, 4, 1, 1, 5);
			checkNext(scanner, Kind.SEMI, 6, 1, 2, 1);
			checkNext(scanner, Kind.SEMI, 8, 1, 3, 1);
			checkNext(scanner, Kind.SEMI, 11, 1, 4, 1);
			checkNext(scanner, Kind.SEMI, 13, 1, 4, 3);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void whiteSpaces1() throws LexicalException {
			String input = "12345\n654321 int";
			PLPScanner scanner = new PLPScanner(input).scan() ;
			show(input);
			show(scanner);
			checkNext(scanner, Kind.INTEGER_LITERAL, 0, 5, 1, 1);
			checkNext(scanner, Kind.INTEGER_LITERAL, 6, 6, 2, 1);
			checkNext(scanner, Kind.KW_int, 13, 3, 2, 8);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testParan() throws LexicalException {
			String input = "()\n[]";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.LPAREN, 0, 1, 1, 1);
			checkNext(scanner, Kind.RPAREN, 1, 1, 1, 2);
			checkNext(scanner, Kind.LSQUARE, 3, 1, 2, 1);
			checkNext(scanner, Kind.RSQUARE, 4, 1, 2, 2);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testOper() throws LexicalException {
			String input = "!===\n-";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.OP_NEQ, 0, 2, 1, 1);
			checkNext(scanner, Kind.OP_EQ, 2, 2, 1, 3);
			checkNext(scanner, Kind.OP_MINUS, 5, 1, 2, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testZero() throws LexicalException {
			String input = "0\n0";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.INTEGER_LITERAL, 0, 1, 1, 1);
			checkNext(scanner, Kind.INTEGER_LITERAL, 2, 1, 2, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testnewline() throws LexicalException {
			String input = ";\r\n,";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.SEMI, 0, 1, 1, 1);
			checkNext(scanner, Kind.COMMA, 3, 1, 2, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void integerLiteral() throws LexicalException {
			String input = ",123456\n05460;";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.COMMA, 0, 1, 1, 1);
			checkNext(scanner, Kind.INTEGER_LITERAL, 1, 6, 1, 2);
			checkNext(scanner, Kind.INTEGER_LITERAL, 8, 1, 2, 1);
			checkNext(scanner, Kind.INTEGER_LITERAL, 9, 4, 2, 2);
			checkNext(scanner, Kind.SEMI, 13, 1, 2, 6);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void integerdecimalLiteral() throws LexicalException {
			String input = "00.45";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.INTEGER_LITERAL, 0, 1, 1, 1);
			checkNext(scanner, Kind.FLOAT_LITERAL, 1, 4, 1, 2);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void integerdecimalLiteral2() throws LexicalException {
			String input = "10.45";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.FLOAT_LITERAL, 0, 5, 1, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testPower() throws LexicalException {
			String input = "**1";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.OP_POWER, 0, 2, 1, 1);
			checkNext(scanner, Kind.INTEGER_LITERAL, 2, 1, 1, 3);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void testPower2() throws LexicalException {
			String input = "***";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.OP_POWER, 0, 2, 1, 1);
			checkNext(scanner, Kind.OP_TIMES, 2, 1, 1, 3);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void integerOverflow() throws LexicalException {
			String input = "12345678901234567890;";
			thrown.expect(LexicalException.class);
			try {
				PLPScanner scanner = new PLPScanner(input).scan();
				show(input);
				show(scanner);
			} catch (LexicalException e) {  
				show(e);
				assertEquals(0,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void integerOverflow2() throws LexicalException {
			String input = "2147483649";
			thrown.expect(LexicalException.class);
			try {
				PLPScanner scanner = new PLPScanner(input).scan();
				show(input);
				show(scanner);
			} catch (LexicalException e) {  //
				show(e);
				assertEquals(0,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void integerOverflow3() throws LexicalException {
			String input = "12343454636745543441344353456766745456756879800453.123";
			thrown.expect(LexicalException.class);
			try {
				PLPScanner scanner = new PLPScanner(input).scan();
				show(input);
				show(scanner);
			} catch (LexicalException e) {  //
				show(e);
				assertEquals(0,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void decimalIllegalChar() throws LexicalException {
			String input = "32..45";
			thrown.expect(LexicalException.class);
			try {
				PLPScanner scanner = new PLPScanner(input).scan();
				show(input);
				show(scanner);
			} catch (LexicalException e) { 
				show(e);
				assertEquals(3,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void integerInRange() throws LexicalException {
			String input = "2147483647";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.INTEGER_LITERAL, 0, 10, 1, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void identifier() throws LexicalException {
			String input = "abcd;_12,5";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.IDENTIFIER, 0, 4, 1, 1);
			checkNext(scanner, Kind.SEMI, 4, 1, 1, 5);
			checkNext(scanner, Kind.IDENTIFIER, 5, 3, 1, 6);
			checkNext(scanner, Kind.COMMA, 8, 1, 1, 9);
			checkNext(scanner, Kind.INTEGER_LITERAL, 9, 1, 1, 10);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void illegalIdentifier1() throws LexicalException {
			String input = "___";
			thrown.expect(LexicalException.class); 
			try {
				PLPScanner scanner = new PLPScanner(input).scan();
				show(input);
				show(scanner);
			} catch (LexicalException e) {
				show(e);
				throw e;
			}
		}
		
		@Test
		public void booleanLiteral() throws LexicalException {
			String input = "true;false";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.BOOLEAN_LITERAL, 0, 4, 1, 1);
			checkNext(scanner, Kind.SEMI, 4, 1, 1, 5);
			checkNext(scanner, Kind.BOOLEAN_LITERAL, 5, 5, 1, 6);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void keywords() throws LexicalException {
			String input = "int boolean";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.KW_int, 0, 3, 1, 1);
			checkNext(scanner, Kind.KW_boolean, 4, 7, 1, 5);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void statement() throws LexicalException {
			String input = "if(5.2<=7){\n%{ %%xyz5% %}\nfloat xyz= 9;\n}";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.KW_if, 0, 2, 1, 1);
			checkNext(scanner, Kind.LPAREN, 2, 1, 1, 3);
			checkNext(scanner, Kind.FLOAT_LITERAL, 3, 3, 1, 4);
			checkNext(scanner, Kind.OP_LE, 6, 2, 1, 7);
			checkNext(scanner, Kind.INTEGER_LITERAL, 8, 1, 1, 9);
			checkNext(scanner, Kind.RPAREN, 9, 1, 1, 10);
			checkNext(scanner, Kind.LBRACE, 10, 1, 1, 11);
			checkNext(scanner, Kind.KW_float, 26, 5, 3, 1);
			checkNext(scanner, Kind.IDENTIFIER, 32, 3, 3, 7);
			checkNext(scanner, Kind.OP_ASSIGN, 35, 1, 3, 10);
			checkNext(scanner, Kind.INTEGER_LITERAL, 37, 1, 3, 12);
			checkNext(scanner, Kind.SEMI, 38, 1, 3, 13);
			checkNext(scanner, Kind.RBRACE, 40, 1, 4, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void stringLiterals() throws LexicalException {
			String input = "\"F\'o'\tur\"";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.STRING_LITERAL, 0, 9, 1, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void stringLiterals1() throws LexicalException {
			String input = "\"abc\b\"";
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.STRING_LITERAL, 0, 6, 1, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void escapeSeq() throws LexicalException {
			String input = "\" \\tabc \\n\""; 
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.STRING_LITERAL, 0, 11, 1, 1);
			checkNextIsEOF(scanner);
		}
		
//		@Test
//		public void escapeSeqExcep() throws LexicalException {
//			String input = "\"abc\\cn  ";
//			show(input);
//			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
//			try {
//				new PLPScanner(input).scan();
//			} catch (LexicalException e) {  //
//				show(e);
//				assertEquals(5,e.getPos());
//				throw e;
//			}
//		}
		
		@Test
		public void illegalCharacters() throws LexicalException {
			String input = "ab#cd";
			show(input);
			thrown.expect(LexicalException.class);  
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //
				show(e);
				assertEquals(2,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void failUnclosedStringLiteral() throws LexicalException {
			String input = "\" greetings  ";
			show(input);
			thrown.expect(LexicalException.class);  
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  
				show(e);
				assertEquals(13,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void failUnclosedStringLiteral2() throws LexicalException {
			String input = "\" greetings \\\"  ";
			show(input);
			thrown.expect(LexicalException.class);  
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  
				show(e);
				assertEquals(16,e.getPos());
				throw e;
			}
		}
		
		@Test
		public void comment() throws LexicalException {
			String input = ";%{ abcd int \n;}%}"; 
			PLPScanner scanner = new PLPScanner(input).scan();
			show(input);
			show(scanner);
			checkNext(scanner, Kind.SEMI, 0, 1, 1, 1);
			checkNextIsEOF(scanner);
		}
		
		@Test
		public void failComment() throws LexicalException {
			String input = ";%{ abcd %%%int \n;}%"; 
			show(input);
			thrown.expect(LexicalException.class);  
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  
				show(e);
				assertEquals(20,e.getPos());
				throw e;
			}
		}
		
}
