/**
* 
* 
* Name     : Aswin Suresh Krishnan
* UFID     : 1890-1173
* Due Date : 19 September 2018
* 
* 
*/

package cop5556fa18;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PLPScanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}
	
	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL,
		STRING_LITERAL, CHAR_LITERAL,
		KW_print        /* print       */,
		KW_sleep        /* sleep       */,
		KW_int          /* int         */,
		KW_float        /* float       */,
		KW_boolean      /* boolean     */,
		KW_if           /* if          */,
		KW_while 		/* while 	   */,
		KW_char         /* char        */,
		KW_string       /* string      */,
		KW_abs			/* abs 		   */,
		KW_sin			/* sin 		   */,
		KW_cos			/* cos 		   */, 
		KW_atan			/* atan        */,
		KW_log			/* log 		   */,
		OP_ASSIGN       /* =           */, 
		OP_EXCLAMATION  /* !           */,
		OP_QUESTION		/* ? 		   */,
		OP_EQ           /* ==          */,
		OP_NEQ          /* !=          */, 
		OP_GE           /* >=          */,
		OP_LE           /* <=          */,
		OP_GT           /* >           */,
		OP_LT           /* <           */,
		OP_AND			/* & 		   */, 
		OP_OR			/* | 		   */,
		OP_PLUS         /* +           */,
		OP_MINUS        /* -           */,
		OP_TIMES        /* *           */,
		OP_DIV          /* /           */,
		OP_MOD          /* %           */,
		OP_POWER        /* **          */, 
		LPAREN          /* (           */,
		RPAREN          /* )           */,
		LBRACE          /* {           */, 
		RBRACE          /* }           */,
		LSQUARE			/* [           */, 
		RSQUARE			/* ]           */, 
		SEMI            /* ;           */,
		OP_COLON		/* : 		   */,
		COMMA           /* ,           */,
		DOT             /* .           */,
		EOF				/* end of file */,
	}
	
	/**
	 * Class to represent Tokens.
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token

		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}
		
		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return PLPScanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return PLPScanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return PLPScanner.this.posInLine(pos) + 1;
		}
		
		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}
		
		/**
		 * precondition:  This Token is an FLOAT_LITERAL
		 * 
		 * @returns the float value represented by the token
		 */
		public float floatVal() {
			assert kind == Kind.FLOAT_LITERAL;
			return Float.valueOf(String.copyValueOf(chars, pos, length));
		}
		
		/**
		 * precondition:  This Token is a CHAR_LITERAL
		 * 
		 * @returns the char value represented by the token
		 */
		public char charVal() {
			assert kind == Kind.CHAR_LITERAL;
			return String.copyValueOf(chars, pos, length).charAt(1);
		}
		
		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}
		
		public String toString() {
			int line = line();
			return "[" + kind + "," +
			       String.copyValueOf(chars, pos, length) + "," +
			       pos + "," +
			       length + "," +
			       line + "," +
			       posInLine(line) + "]";
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private PLPScanner getOuterType() {
			return PLPScanner.this;
		}
	}
	
	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}
	
	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
	
	/**
	 * A Map that stores the Kind of Enum that an identified String from chars[] corresponds to
	 */
	private Map<String, Kind> tokenMap = new HashMap<String, Kind>(){/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put("true", Kind.BOOLEAN_LITERAL);
		put("false", Kind.BOOLEAN_LITERAL);
		put("print", Kind.KW_print);
		put("int", Kind.KW_int);
		put("float", Kind.KW_float);
		put("double", Kind.KW_float);
		put("boolean", Kind.KW_boolean);
		put("char", Kind.KW_char);
		put("string", Kind.KW_string);
		put("sleep", Kind.KW_sleep);
		put("if", Kind.KW_if);
		put("while", Kind.KW_while);
		put("sin", Kind.KW_sin);
		put("cos", Kind.KW_cos);
		put("atan", Kind.KW_atan);
		put("abs", Kind.KW_abs);
		put("log", Kind.KW_log);
		put("int", Kind.KW_int);
		put("float", Kind.KW_float);
		put("=", Kind.OP_ASSIGN);
		put("!", Kind.OP_EXCLAMATION);
		put("?", Kind.OP_QUESTION);
		put("==", Kind.OP_EQ);
		put("!=", Kind.OP_NEQ);
		put(">=", Kind.OP_GE);
		put("<=", Kind.OP_LE);
		put(">", Kind.OP_GT);
		put("<", Kind.OP_LT);
		put("+", Kind.OP_PLUS);
		put("-", Kind.OP_MINUS);
		put("*", Kind.OP_TIMES);
		put("/", Kind.OP_DIV);
		put("|", Kind.OP_OR);
		put("&", Kind.OP_AND);
		put("%", Kind.OP_MOD);
		put(":", Kind.OP_COLON);		
		put("**", Kind.OP_POWER);
		put("(", Kind.LPAREN);
		put(")", Kind.RPAREN);
		put("{", Kind.LBRACE);
		put("}", Kind.RBRACE);
		put("[", Kind.LSQUARE);
		put("]", Kind.RSQUARE);		
		put(";", Kind.SEMI);
		put(",", Kind.COMMA);
		put(".", Kind.DOT);
		put(String.valueOf(EOFChar), Kind.EOF);
	}};
	
	/*
	 * A buffer to store the currently processing char from chars[]
	 */
	private StringBuffer buffer = new StringBuffer();
	
	
	PLPScanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		lineStarts = initLineStarts();
	}
	
	private enum State {START, MIDDLE_END, COMMENT_START, UNDEFINED};  //TODO:  this is incomplete
	
	public PLPScanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		int startPos = 0;
		
		//TODO:  this is incomplete

		while (pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
						case EOFChar: {
							tokens.add(new Token(Kind.EOF, startPos, 0));
							pos++; // next iteration will terminate loop
						}
						break;
						case '"':{
							do {
							   if ( (ch == '\\' && chars[pos+1] == EOFChar ) ||ch == EOFChar)
								   error(pos, line(pos), posInLine(pos), ExceptionConstants.STRING_NOT_CLOSED);
							   if (ch == '\\')
								   pos++;
							   ch = chars[++pos];
							}
							while (ch!='"');	
							tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '\'': {
							
							if (chars[pos+1] == '\'') 
								pos+=2;
							else if (chars[pos+2] == '\'')
								pos+=3;
							else
								error(pos, line(pos), posInLine(pos), ExceptionConstants.CHARACTER_NOT_CLOSED);
							tokens.add(new Token(Kind.CHAR_LITERAL, startPos, pos - startPos));
						}
						break;
						case '*':{
							char nextch = chars[pos+1]; 
							String chs = Character.toString(ch);
							if (nextch == '*') {
								chs = chs.concat("*");
								pos++;
							}
							tokens.add(new Token(tokenMap.get(chs), startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '?':
						case ':':
						case '|':
						case '&':
						case '/':
						case '+':
						case '-':
						case '%':{
							if (chars[pos+1] == '{') { // %{ indicates start of comment, so we go to another state 
								state = State.COMMENT_START;
								pos++;
								continue;
							}
							tokens.add(new Token(tokenMap.get(Character.toString(ch)), startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '(' : 
						case '{' : 
						case '[' :{
							tokens.add(new Token(tokenMap.get(Character.toString(ch)), startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ')' : case '}' : case ']' :{
							tokens.add(new Token(tokenMap.get(Character.toString(ch)), startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '.' :
						case ';': {
							tokens.add(new Token(tokenMap.get(String.valueOf(ch)), startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ',' :
							tokens.add(new Token(tokenMap.get(String.valueOf(ch)), startPos, pos - startPos + 1));
							pos++;
						break;
						case '\n' : case '\r' : case '\t' : case '\f' :
							pos++;
							break;
						//operands below
						case '=' : 
						case '!' :
						case '>' :
						case '<' :{
							char nextch = chars[pos+1]; 
							String chs = Character.toString(ch);
							if (nextch == '=') {
								chs = chs.concat("=");
								pos++;
							}
							tokens.add(new Token(tokenMap.get(chs), startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '0' : 
							if (chars[pos+1] != '.') {
								pos++;
								tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, 1));
								break;
							}
						case '1' : case '2' : case '3' : case '4' : case '5':
						case '6' : case '7' : case '8' : case '9' : {
							
							boolean hasDecimal = false;
							String value = "";
							do {
							   value += chars[pos];
							   ch = chars[++pos];
							   if (!hasDecimal && ch == '.') {
								   if (!Character.isDigit(chars[pos+1])) //This condition is for cases like 32..4 and 32.$
										error(pos+1, line(pos+1), posInLine(pos+1), ExceptionConstants.ILLEGAL_CHAR);
								   hasDecimal = true;
								   ch = chars[++pos];
							   }
							}
							while (Character.isDigit(ch));	
							
							if (value.length() > String.valueOf(Integer.MAX_VALUE).length())
								error(startPos, line(startPos), posInLine(startPos), ExceptionConstants.DOUBLE_OUT_OF_RANGE);
							if (value.length() == String.valueOf(Integer.MAX_VALUE).length() && (value.compareTo(String.valueOf(Integer.MIN_VALUE)) < 0 || value.compareTo(String.valueOf(Integer.MAX_VALUE)) > 0) )
								error(startPos, line(startPos), posInLine(startPos), ExceptionConstants.INTEGER_OUT_OF_RANGE);
							
							tokens.add(new Token(hasDecimal?Kind.FLOAT_LITERAL:Kind.INTEGER_LITERAL, startPos, pos - startPos));
						}
						break;
						case ' ' : pos++;
						break;
						default: {
							if (Character.isAlphabetic(ch)  || ch == '_') { //An identifier could start with alphabet or underscore
								buffer.append(String.valueOf(ch));
								state = State.MIDDLE_END;
								pos++;
								break;
							}
							else
								error(pos, line(pos), posInLine(pos), ExceptionConstants.ILLEGAL_CHAR + ch);
						}
					}//switch ch
				}
				break;
				case MIDDLE_END: {
					//during identifier construction
					if (Character.isAlphabetic(ch) || Character.isDigit(ch) || ch =='_') {
						buffer.append(ch);
						pos++;
					}
					else {
						switch (ch) {
							//if we suddenly encounter ';' or '=' or ' ' while processing a token
							//Here we do not increment 'pos, since we need to process the char at 'pos' in the START state
							default: {
								Kind foundKind = tokenMap.get(buffer.toString());
								if (foundKind == null) {
									if (!(buffer.toString().matches(".*[0-9].*") || buffer.toString().matches(".*[a-zA-Z].*")))
										error(pos, line(pos), posInLine(pos), ExceptionConstants.ILLEGAL_IDENTIFIER);
									foundKind = Kind.IDENTIFIER;
								}
								tokens.add(new Token(foundKind, startPos, pos - startPos));
								buffer.setLength(0); //clear the buffer
								state = State.START;
							}
						}
						
					}
				}
				break;
				case COMMENT_START :{
					if (ch == EOFChar)
						error(pos, line(pos), posInLine(pos), ExceptionConstants.COMMENT_NOT_CLOSED);
					if (ch == '%' && chars[pos+1] == '}') {
						state = State.START;
						pos++;
					}
					pos++;
				}
				break;
				default: {
					error(pos, 0, 0, ExceptionConstants.UNDEFINED_STATE);
				}
			}// switch state
		} // while
		
		return this;
	}
	
	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}
	
//	private boolean isKeyWord(Kind kind) {
//		
//		if (kind == Kind.KW_char || kind == Kind.KW_float ||
//				kind == Kind.KW_int  || kind == Kind.KW_string ||
//						kind == Kind.KW_float || kind == Kind.KW_boolean)
//			return true;
//		return false;
//	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}
	
	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}


}
