package cop5556fa18;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
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
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPTypeChecker implements PLPASTVisitor {
	
	PLPTypeChecker() {
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		symbolTable.clear();
		return null;
	}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		symbolTable.enterScope();
		try {
			
			for (PLPASTNode node : block.declarationsAndStatements)
				node.visit(this, arg);
		}catch(Exception rethrowable) {
			throw rethrowable;
		}
		//leaveScope inspite of exceptions in the block
		finally {
			symbolTable.leaveScope();
		}
		
		return block;
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
	
		if (!symbolTable.containsKeyInCurrentScope(declaration.name)) {
			declaration.setType(PLPTypes.getType(declaration.type));
			if (declaration.expression != null) {
				declaration.expression.visit(this, arg);
				if (declaration.expression.getType() != declaration.getType()) {
					throw new SemanticException(declaration.firstToken, "Type mismatch : " + declaration.expression.getType() + " cannot be converted to " + declaration.getType());
				}
			}
			symbolTable.put(declaration.name, declaration);
		}
		else
			throw new SemanticException(declaration.firstToken, "Identifier " + declaration.name + " is already declared");
		
		return declaration;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
	
		for (String name : declaration.names) {
			if (!symbolTable.containsKey(name)) {
				declaration.setType(PLPTypes.getType(declaration.type));
				symbolTable.put(name, declaration);
			}
			else
				throw new SemanticException(declaration.firstToken, "Identifier " + name + " is already declared");
		}
		return declaration;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		
		expressionBooleanLiteral.setType(PLPTypes.Type.BOOLEAN);
		return expressionBooleanLiteral;	
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
	
		if (expressionBinary.leftExpression != null && expressionBinary.rightExpression != null) {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			
			PLPTypes.Type leftExpressionType = expressionBinary.leftExpression.getType();
			PLPTypes.Type rightExpressionType = expressionBinary.rightExpression.getType();
			
			//calculating inferredType
			if (leftExpressionType == rightExpressionType || 
				(leftExpressionType == PLPTypes.Type.INTEGER && rightExpressionType == PLPTypes.Type.FLOAT) ||
				(leftExpressionType == PLPTypes.Type.FLOAT   && rightExpressionType == PLPTypes.Type.INTEGER)) {
				
				/**
				 * Expression0.type		Expression1.type	Operator				inferred type for ExpressionBinary.type
				 * integer					integer			==, !=, >,>=, <, <=				boolean
				 * float					float			==, !=, >,>=, <, <=				boolean
				 * boolean					boolean			==, !=, >,>=, <, <=				boolean
				 */
				if( (expressionBinary.op == Kind.OP_EQ || expressionBinary.op == Kind.OP_NEQ ||
					expressionBinary.op == Kind.OP_GT || expressionBinary.op == Kind.OP_GE || 
					expressionBinary.op == Kind.OP_LT || expressionBinary.op == Kind.OP_LE) 
						&&
					leftExpressionType == rightExpressionType
						&&
					(leftExpressionType == PLPTypes.Type.INTEGER || leftExpressionType == PLPTypes.Type.FLOAT || 
					leftExpressionType == PLPTypes.Type.BOOLEAN) ){
					
					expressionBinary.setType(PLPTypes.Type.BOOLEAN);
				}
				/**
				 * Expression0.type	Expression1.type	Operator	inferred type for ExpressionBinary.type
				 * boolean			boolean					&, |			boolean
				 * integer			integer					&, | 			integer
				 */
				else if ((expressionBinary.op == Kind.OP_AND || expressionBinary.op == Kind.OP_OR) 
						 &&
					leftExpressionType == rightExpressionType
						 && 
						(leftExpressionType == PLPTypes.Type.INTEGER || leftExpressionType == PLPTypes.Type.BOOLEAN)){
					expressionBinary.setType(leftExpressionType);
				}
				/**
				 * Except for Operator = %
				 * Expression0.type		Expression1.type	Operator				inferred type for ExpressionBinary.type
				 *   integer				integer			+,-,*,/,%,**, &, |				integer
				 *	 float					float			+,-,*,/,**						float
				 *	 float					integer			+,-,*,/,**						float
				 *	 integer				float		    +,-,*,/,**						float
				 * 
				 * 
				 */
				else if ((expressionBinary.op == Kind.OP_PLUS || expressionBinary.op == Kind.OP_MINUS || 
						  expressionBinary.op == Kind.OP_TIMES || expressionBinary.op == Kind.OP_DIV  || 
						  expressionBinary.op == Kind.OP_POWER)
						  && 
						  (leftExpressionType == PLPTypes.Type.INTEGER || leftExpressionType == PLPTypes.Type.FLOAT)){
					
					if (leftExpressionType == PLPTypes.Type.FLOAT || rightExpressionType == PLPTypes.Type.FLOAT)
						expressionBinary.setType(PLPTypes.Type.FLOAT);
					else
						expressionBinary.setType(PLPTypes.Type.INTEGER);
				}
				/**
				 * Expression0.type		Expression1.type	Operator				inferred type for ExpressionBinary.type
				 * integer					integer				%							integer
				 */
				else if (expressionBinary.op == Kind.OP_MOD 
						 &&
						 (leftExpressionType == PLPTypes.Type.INTEGER || leftExpressionType == PLPTypes.Type.INTEGER))
					expressionBinary.setType(PLPTypes.Type.INTEGER);
				/**
				 * Expression0.type		Expression1.type	Operator				inferred type for ExpressionBinary.type
				 * string					string				+							string
				 */
				else if (expressionBinary.op == Kind.OP_PLUS && leftExpressionType == PLPTypes.Type.STRING)
						expressionBinary.setType(PLPTypes.Type.STRING);
				
				else {
					throw new SemanticException(expressionBinary.firstToken, "Semantic Exception found! Unable to determine type.");
				}
			}
			else
				throw new SemanticException(expressionBinary.firstToken, "Type mismatch : " + rightExpressionType + " cannot be converted to " + leftExpressionType);
		}
		else
			throw new SemanticException(expressionBinary.firstToken, "Type mismatch :  null field");
		
		return expressionBinary;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
	
		if (expressionConditional.condition != null && expressionConditional.trueExpression != null && expressionConditional.falseExpression != null) {
			
			expressionConditional.condition.visit(this, arg);
			expressionConditional.trueExpression.visit(this, arg);
			expressionConditional.falseExpression.visit(this, arg);
			
			if (expressionConditional.condition.getType() == PLPTypes.Type.BOOLEAN 
					&&
				expressionConditional.trueExpression.getType() == expressionConditional.falseExpression.getType()) 
				expressionConditional.setType(expressionConditional.trueExpression.getType());
			else 
				throw new SemanticException(expressionConditional.firstToken, expressionConditional.condition.getType() != PLPTypes.Type.BOOLEAN ? "Type mismatch : " + expressionConditional.condition.getType() + " cannot be converted to BOOLEAN" : expressionConditional.trueExpression.getType() + " expression is not equal to " + expressionConditional.falseExpression.getType());
		}
		else 
			throw new SemanticException(expressionConditional.firstToken, "Type mismatch : null field");
		
		return expressionConditional;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.setType(PLPTypes.Type.FLOAT);
		return expressionFloatLiteral;
	}

	
	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		
		
		if (FunctionWithArg.expression != null) {
		
				FunctionWithArg.expression.visit(this, arg);
				PLPTypes.Type expressionType = FunctionWithArg.expression.getType();
				
				
				/**
				 * Expression.type		Function		inferred type for FunctionWithArg
				 *	  integer				abs						integer
				 */
				if (expressionType == PLPTypes.Type.INTEGER && FunctionWithArg.functionName == Kind.KW_abs)
					FunctionWithArg.setType(PLPTypes.Type.INTEGER);
				
				/**
				 * Expression.type				Function					inferred type for FunctionWithArg
				 *	float				abs, sin, cos, atan, log						float
				 */
				else if (expressionType == PLPTypes.Type.FLOAT && 
							(FunctionWithArg.functionName == Kind.KW_abs || 
								FunctionWithArg.functionName == Kind.KW_sin ||
									FunctionWithArg.functionName == Kind.KW_cos ||
										FunctionWithArg.functionName == Kind.KW_atan ||
											FunctionWithArg.functionName == Kind.KW_log))
					FunctionWithArg.setType(PLPTypes.Type.FLOAT);
				
				/**
				 * Expression.type			Function		inferred type for FunctionWithArg
				 *		int					float						float
				 *		float				float						float
				 *		float				int							int
				 *		int 				int 						int
				 */
				else if ((expressionType == PLPTypes.Type.INTEGER ||  expressionType == PLPTypes.Type.FLOAT)
							&& 
							(FunctionWithArg.functionName == Kind.KW_int || FunctionWithArg.functionName == Kind.KW_float))
					FunctionWithArg.setType(PLPTypes.getType(FunctionWithArg.functionName)); 
				
				else
					throw new SemanticException(FunctionWithArg.firstToken, "Type mismatch : " + expressionType + " is illegal for function argument");
		}
		else
			throw new SemanticException(FunctionWithArg.firstToken, "Type mismatch : null field");

		return FunctionWithArg;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {

		Declaration declaration = symbolTable.lookup(expressionIdent.name);
		
		if (declaration != null) {
			expressionIdent.setType(declaration.getType());
			expressionIdent.setDeclaration(declaration);
		}
		else
			throw new SemanticException(expressionIdent.firstToken, "Unknown Identifier : " + expressionIdent.name + ". Identifier was not previously declared");
		
		return expressionIdent;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.setType(PLPTypes.Type.INTEGER);
		return expressionIntegerLiteral;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		expressionStringLiteral.setType(PLPTypes.Type.STRING);
		return expressionStringLiteral;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		expressionCharLiteral.setType(PLPTypes.Type.CHAR);
		return expressionCharLiteral;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		if (statementAssign.lhs != null && statementAssign.expression != null) {
			statementAssign.lhs.visit(this, arg);
			statementAssign.expression.visit(this, arg);
			
			if (statementAssign.lhs.getType() != statementAssign.expression.getType())
				throw new SemanticException(statementAssign.firstToken, "Type mismatch : " + statementAssign.expression.getType() + " cannot be converted to " + statementAssign.lhs.getType());
		}
		else
			throw new SemanticException(statementAssign.firstToken, "Type mismatch : null field");
		
		return statementAssign;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		
		if (ifStatement.condition != null) {
			ifStatement.condition.visit(this, arg);
			if (ifStatement.condition.getType() != PLPTypes.Type.BOOLEAN)
				throw new SemanticException(ifStatement.firstToken, "Type mismatch : " + ifStatement.condition.getType() + " cannot be converted to BOOLEAN");
			ifStatement.block.visit(this, arg);
		}
		else
			throw new SemanticException(ifStatement.firstToken, "Type mismatch : null field");
		
		return ifStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		if (whileStatement.condition != null) {
			whileStatement.condition.visit(this, arg);
			if (whileStatement.condition.getType() != PLPTypes.Type.BOOLEAN)
				throw new SemanticException(whileStatement.firstToken, "Type mismatch : " + whileStatement.condition.getType() + " cannot be converted to BOOLEAN");
			whileStatement.b.visit(this, arg);
		}
		else
			throw new SemanticException(whileStatement.firstToken, "Type mismatch : null fields");

		return whileStatement;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {

		if (printStatement.expression != null) {
				printStatement.expression.visit(this, arg);
				if (printStatement.expression.getType() != PLPTypes.Type.INTEGER && 
						printStatement.expression.getType() != PLPTypes.Type.BOOLEAN && 
						printStatement.expression.getType() != PLPTypes.Type.FLOAT && 
						printStatement.expression.getType() != PLPTypes.Type.CHAR && 
						printStatement.expression.getType() != PLPTypes.Type.STRING)
							throw new SemanticException(printStatement.firstToken, "Type mismatch : print expression type cannot be determined");
		}
		else
			throw new SemanticException(printStatement.firstToken, "Type mismatch");

		return printStatement;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {

		if (sleepStatement.time != null) {
			sleepStatement.time.visit(this, arg);
			if (sleepStatement.time.getType() != PLPTypes.Type.INTEGER)
				throw new SemanticException(sleepStatement.firstToken, "Type mismatch : " + sleepStatement.time.getType() + " cannot be converted to " + PLPTypes.Type.INTEGER);
		}
		else
			throw new SemanticException(sleepStatement.firstToken, "Type mismatch : " + sleepStatement.time.getType() + " cannot be converted to int");

		return sleepStatement;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		
		if (expressionUnary.expression != null) {
			expressionUnary.expression.visit(this, arg);
			if ((expressionUnary.op == Kind.OP_EXCLAMATION && (expressionUnary.expression.getType() == PLPTypes.Type.BOOLEAN || expressionUnary.expression.getType() == PLPTypes.Type.INTEGER)))
				expressionUnary.setType(expressionUnary.expression.getType());
			else if ((expressionUnary.op == Kind.OP_PLUS || expressionUnary.op == Kind.OP_MINUS) && (expressionUnary.expression.getType() == PLPTypes.Type.INTEGER || expressionUnary.expression.getType() == PLPTypes.Type.FLOAT))
					expressionUnary.setType(expressionUnary.expression.getType());
			else
				throw new SemanticException(expressionUnary.firstToken, "Type mismatch : " + expressionUnary.expression.getType() + " is not suitable with the operator : " + expressionUnary.op);
		}
		else
			throw new SemanticException(expressionUnary.firstToken, "Type mismatch : null fields");

		return expressionUnary;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		
		if (lhs.identifier != null) {
			//This lookup is in all the scopes
			Declaration declaration = symbolTable.lookup(lhs.identifier);
			if (declaration != null) {
				lhs.setType(declaration.getType());
				lhs.setDeclaration(declaration);
			}
			else
				throw new SemanticException(lhs.firstToken, "Unknown Identifier : " + lhs.identifier + ". Identifier was not previously declared");
		}
		else
			throw new SemanticException(lhs.firstToken, "Type mismatch : null fields");

		return lhs;
	}

}
