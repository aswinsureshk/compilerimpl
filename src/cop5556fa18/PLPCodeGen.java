package cop5556fa18;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa18.PLPTypes.Type;
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
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slot_number;
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	

	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		for (PLPASTNode node : block.declarationsAndStatements) 
			node.visit(this, null);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		
		declaration.setCurrent_slot(++slot_number);
		Type type = PLPTypes.getType(declaration.type);
		if ((type == Type.INTEGER) || (type == Type.FLOAT) ||
			    (type == Type.BOOLEAN) || (type == Type.CHAR) ||
			    (type == Type.STRING)){
				
	        	mv.visitInsn(ACONST_NULL);
	        	mv.visitVarInsn(ASTORE, declaration.getCurrent_slot());
				
		}
        Expression exp = declaration.expression;
		if (exp != null){       
			LHS lhs_temp = new LHS(declaration.firstToken, declaration.name);
	        lhs_temp.setType(declaration.getType());
	        lhs_temp.setDeclaration(declaration);
			AssignmentStatement assign_temp = new AssignmentStatement(declaration.firstToken, lhs_temp, declaration.expression);         
	        assign_temp.visit(this,arg);
		}
        return null;	
    }

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		
		for (String name : declaration.names) {
			VariableDeclaration vd = new VariableDeclaration(declaration.firstToken, declaration.type, name, null);
			vd.setType(declaration.getType());
			declaration.declarations.add(vd);
			vd.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
        expressionBinary.rightExpression.visit(this, arg);
        PLPScanner.Kind op = expressionBinary.op;
        Expression leftExp = expressionBinary.leftExpression;
        Type leftExpType = leftExp.getType();
        Expression rightExp = expressionBinary.rightExpression;
        Type rightExpType = rightExp.getType();
        
        Label startLabel = new Label();
		Label endLabel = new Label();
        switch (op) {
            case OP_PLUS:
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER)
                    {
                            mv.visitInsn(IADD);    
                    } 
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.FLOAT) 
                    {
                            mv.visitInsn(FADD);
                    } 
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.INTEGER)
                    {
                            mv.visitInsn(I2F);
                            mv.visitInsn(FADD);
                    } 
                    else if(leftExpType == Type.INTEGER && rightExpType == Type.FLOAT)
                    {
                            mv.visitInsn(SWAP);
                            mv.visitInsn(I2F);
                            mv.visitInsn(SWAP);
                            mv.visitInsn(FADD);
                    } 
                    else if(leftExpType == Type.STRING && rightExpType == Type.STRING) 
                    {
                    	mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"addstr", MathRunTime.addstrSignature,false);                
                    }
                    else 
                    {
                        throw new UnsupportedOperationException("ADD operation is only supported for INTEGER and FLOAT operands.");
                    }
                break;
            case OP_MINUS: 
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER)
                    {
                            mv.visitInsn(ISUB);    
                    } 
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.FLOAT) 
                    {
                            mv.visitInsn(FSUB);
                    }
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.INTEGER)
                    {
                            mv.visitInsn(I2F);
                            mv.visitInsn(FSUB);
                    } 
                    else if(leftExpType == Type.INTEGER && rightExpType == Type.FLOAT) 
                    {
                            mv.visitInsn(SWAP);
                            mv.visitInsn(I2F);
                            mv.visitInsn(SWAP);
                            mv.visitInsn(FSUB);
                    }
                    else
                    {
                            throw new UnsupportedOperationException("SUB operation is only supported for INTEGER and FLOAT operands.");
                    }
                break;
            case OP_TIMES: 
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER)
                    {
                            mv.visitInsn(IMUL);    
                    }
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.FLOAT) 
                    {
                            mv.visitInsn(FMUL);
                    }
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.INTEGER) 
                    {
                            mv.visitInsn(I2F);
                            mv.visitInsn(FMUL);
                    }
                    else if(leftExpType == Type.INTEGER && rightExpType == Type.FLOAT) 
                    {
                            mv.visitInsn(SWAP);
                            mv.visitInsn(I2F);
                            mv.visitInsn(SWAP);
                            mv.visitInsn(FMUL);
                    }
                    else 
                    {
                        throw new UnsupportedOperationException("MUL operation is only supported for INTEGER and FLOAT operands.");
                    }
                break;
            case OP_DIV:
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER) 
                    {
                            mv.visitInsn(IDIV);    
                    }
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.FLOAT) 
                    {
                            mv.visitInsn(FDIV);
                    } 
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.INTEGER) 
                    {
                            mv.visitInsn(I2F);
                            mv.visitInsn(FDIV);
                    }
                    else if(leftExpType == Type.INTEGER && rightExpType == Type.FLOAT)
                    {
                            mv.visitInsn(SWAP);
                            mv.visitInsn(I2F);
                            mv.visitInsn(SWAP);
                            mv.visitInsn(FDIV);
                    } else
                    {
                            throw new UnsupportedOperationException("DIV operation is only supported for INTEGER and FLOAT operands.");
                    }
                break;
            case OP_POWER:
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER)
                    {
                    		mv.visitInsn(I2F);
                    		mv.visitInsn(SWAP);
                    		mv.visitInsn(I2F);
                    		mv.visitInsn(SWAP);
                    		mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className, "pow", MathRunTime.powSignature, false);    
                    		mv.visitInsn(F2I);
                    }
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.FLOAT) 
                    {
                    		mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className, "pow", "(FF)F", false);
                    } 
                    else if(leftExpType == Type.FLOAT && rightExpType == Type.INTEGER)
                    {
                    	mv.visitInsn(I2F);
                        mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className, "pow", MathRunTime.powSignature, false);
                    } 
                    else if(leftExpType == Type.INTEGER && rightExpType == Type.FLOAT)
                    {
                        mv.visitInsn(SWAP);
                        mv.visitInsn(I2F);
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className, "pow", MathRunTime.powSignature, false);
                    }
                    else
                    {
                        throw new UnsupportedOperationException("EXPONENT operation is only supported for INTEGER and FLOAT operands.");
                    }
                break;
            case OP_MOD:
                if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER) 
                        mv.visitInsn(IREM);    
                else 
                    throw new UnsupportedOperationException("MOD operation is only supported for INTEGER operands.");
                break;
            case OP_AND:
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER) 
                        mv.visitInsn(IAND);
                    else if(leftExpType == Type.BOOLEAN && rightExpType == Type.BOOLEAN) 
                    	mv.visitInsn(IAND);
                    else 
                        throw new UnsupportedOperationException("AND operation is only supported for INTEGER operands.");
                break;
            case OP_OR:
                    if(leftExpType == Type.INTEGER && rightExpType == Type.INTEGER) 
                        mv.visitInsn(IOR);
                    else if(leftExpType == Type.BOOLEAN && rightExpType == Type.BOOLEAN) 
                    	mv.visitInsn(IOR);
                    else
                        throw new UnsupportedOperationException("OR operation is only supported for INTEGER operands.");
                break;
                
            case OP_EQ:
            	if (leftExpType == Type.FLOAT ) {
            		mv.visitInsn(FCMPG);
            		mv.visitJumpInsn(IFEQ, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	else { //INTEGER or BOOLEAN
            		mv.visitJumpInsn(IF_ICMPEQ, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	break;
            	
            case OP_NEQ:
            	if (leftExpType == Type.FLOAT ) {
            		mv.visitInsn(FCMPG);
            		mv.visitJumpInsn(IFNE, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	else { //INTEGER or BOOLEAN
            		mv.visitJumpInsn(IF_ICMPNE, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
				break;
            	           
            case OP_GE:
            	if (leftExpType == Type.FLOAT ) {
            		mv.visitInsn(FCMPG); 
            		mv.visitJumpInsn(IFGE, startLabel); 
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	else { //INTEGER or BOOLEAN
            		mv.visitJumpInsn(IF_ICMPGE, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            break;
            	
            case OP_LE:
            	if (leftExpType == Type.FLOAT ) {
            		mv.visitInsn(FCMPG); 
            		mv.visitJumpInsn(IFLE, startLabel); 
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	else { //INTEGER or BOOLEAN
            		mv.visitJumpInsn(IF_ICMPLE, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
			break;
				
            case OP_GT:
            	if (leftExpType == Type.FLOAT ) {
            		mv.visitInsn(FCMPG); 
            		mv.visitJumpInsn(IFGT, startLabel); 
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	else { //INTEGER or BOOLEAN
            		mv.visitJumpInsn(IF_ICMPGT, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
			break;
            	
            case OP_LT:
            	if (leftExpType == Type.FLOAT ) {
            		mv.visitInsn(FCMPG); 
            		mv.visitJumpInsn(IFLT, startLabel); 
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            	else { //INTEGER or BOOLEAN
            		mv.visitJumpInsn(IF_ICMPLT, startLabel);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, endLabel);
					mv.visitLabel(startLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(endLabel); 
            	}
            break;
            	
            default:
                    throw new UnsupportedOperationException("Operation not supported.");
        }  
        return expressionBinary;
	}	
	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		
		Expression e0=expressionConditional.condition;
		Expression e1=expressionConditional.trueExpression;
		Expression e2=expressionConditional.falseExpression;
		
		if(e0!=null)  { e0.visit(this, arg);	}
		if(e1!=null)  { e1.visit(this, arg);	}
		if(e2!=null)  { e2.visit(this, arg);	}
		
		Label startLabel= new Label();
		Label endLabel = new Label();
		e0.visit(this, arg);
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, startLabel);
		e2.visit(this, arg);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(startLabel);
		e1.visit(this, arg);
		mv.visitLabel(endLabel);
		
		return expressionConditional;

	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		Expression e=FunctionWithArg.expression;
		PLPScanner.Kind function =FunctionWithArg.functionName;
		e.visit(this, arg);
		System.out.println("func args " + function);
		switch (function)
		{
			case KW_sin:
				System.out.println("arg sin visited");
				if(e.getType() == Type.FLOAT)
					mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"sin", MathRunTime.sinSignature,false);
				break;
			case KW_cos:
				if(e.getType() == Type.FLOAT)
				mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"cos", MathRunTime.cosSignature,false);
			break;
			case KW_atan:
				if(e.getType() == Type.FLOAT)
				mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"atan", MathRunTime.atanSignature,false);
				break;
			case KW_log:
				if(e.getType() == Type.FLOAT)
				mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"log", MathRunTime.logSignature,false);
				break;
			case KW_abs:
				if(e.getType() == Type.FLOAT)
				mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"absF", MathRunTime.absFSignature,false);
				else
				if(e.getType() == Type.INTEGER)
				mv.visitMethodInsn(INVOKESTATIC, MathRunTime.className,"absI", MathRunTime.absISignature,false);
				break;
			case KW_int:
				if(e.getType() == Type.FLOAT) mv.visitInsn(F2I);
				break;
			case KW_float:
				if(e.getType() == Type.INTEGER) mv.visitInsn(I2F);
				break;
			default:
				break;
		}
		return FunctionWithArg;

	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {

		System.out.println("expression ident ");
//		mv.visitFieldInsn(GETSTATIC, className, expressionIdent.name, PLPCodeGenUtils.getJVMType(expressionIdent.getType()));
		
		Declaration declaration = expressionIdent.getDeclaration();
		
		if (declaration instanceof VariableListDeclaration) {
			
			VariableListDeclaration listdec = (VariableListDeclaration) declaration;
			for (VariableDeclaration d : listdec.declarations) {
				
				if (d.name.equals(expressionIdent.name)) {
					if(d.getType() == Type.INTEGER || d.getType() == Type.BOOLEAN
							|| d.getType() == Type.CHAR) {
						mv.visitVarInsn(ILOAD, d.getCurrent_slot());
					}
					else if(declaration.getType() == Type.FLOAT) {
						mv.visitVarInsn(FLOAD, d.getCurrent_slot());
					}
					else if(declaration.getType() == Type.STRING) {
						mv.visitVarInsn(ALOAD, d.getCurrent_slot());
					}
				}
			}
		}
		else {
			if(declaration.getType() == Type.INTEGER || declaration.getType() == Type.BOOLEAN
						|| declaration.getType() == Type.CHAR) {
				mv.visitVarInsn(ILOAD, declaration.getCurrent_slot());
			}
			else if(declaration.getType() == Type.FLOAT) {
				mv.visitVarInsn(FLOAD, declaration.getCurrent_slot());
			}
			else if(declaration.getType() == Type.STRING) {
				mv.visitVarInsn(ALOAD, declaration.getCurrent_slot());
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {

		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {

		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {

		System.out.println("assign visited");
		statementAssign.expression.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return statementAssign;

	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		
		System.out.println("lhs ident visited "+lhs.getType());
		Type decType = lhs.getDeclaration().getType();
		
		Declaration declaration = lhs.getDeclaration();
		
		if (declaration instanceof VariableListDeclaration) {
			
			VariableListDeclaration listdec = (VariableListDeclaration) declaration;
			for (VariableDeclaration d : listdec.declarations) {
				
				if (d.name.equals(lhs.identifier)) {
					 switch(decType) {
				        
					        case INTEGER:
					        	mv.visitVarInsn(ISTORE, d.getCurrent_slot());
					            break;
					        
					        case BOOLEAN:
					        	mv.visitVarInsn(ISTORE, d.getCurrent_slot());
					            break;
					        
					        case FLOAT:
					        	mv.visitVarInsn(FSTORE, d.getCurrent_slot());
					            break;
					            
					        case CHAR:
					        	mv.visitVarInsn(ISTORE, d.getCurrent_slot());
					            break;
					        
					        case STRING:
					        	mv.visitVarInsn(ASTORE, d.getCurrent_slot());
					        	break;
					        
					        default:
					            break;
					        }
				}
			}
		}
		else {
	        switch(decType) {
	        
	        case INTEGER:
	        	mv.visitVarInsn(ISTORE, lhs.getDeclaration().getCurrent_slot());
	            break;
	        
	        case BOOLEAN:
	        	mv.visitVarInsn(ISTORE, lhs.getDeclaration().getCurrent_slot());
	            break;
	        
	        case FLOAT:
	        	mv.visitVarInsn(FSTORE, lhs.getDeclaration().getCurrent_slot());
	            break;
	            
	        case CHAR:
	        	mv.visitVarInsn(ISTORE, lhs.getDeclaration().getCurrent_slot());
	            break;
	       
	        case STRING:
	        	mv.visitVarInsn(ASTORE, lhs.getDeclaration().getCurrent_slot());
	        	break;
	        
	        default:
	            break;
	        }
		}
        return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		ifStatement.condition.visit(this, arg);
		Label AFTER = new Label();
		mv.visitJumpInsn(IFEQ, AFTER);
		ifStatement.block.visit(this, arg);
		mv.visitLabel(AFTER);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {

		Label CONDITION = new Label();
		Label AFTER = new Label();
		mv.visitLabel(CONDITION);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(IFEQ, AFTER);
		whileStatement.b.visit(this, arg);
		mv.visitJumpInsn(GOTO, CONDITION);
		mv.visitLabel(AFTER);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
			case INTEGER : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
			break;
			case BOOLEAN : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
			}
			break;
			case FLOAT : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
			break;
			case CHAR : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(C)V", false);
			}
			break;
			case STRING : {
				PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Ljava/lang/String;)V", false);
			}
			break;
			default : throw new UnsupportedOperationException();
		}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression exp = sleepStatement.time;
		exp.visit(this,arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {

		expressionUnary.expression.visit(this, arg);
        Type expUnaryType = expressionUnary.getType();
        PLPScanner.Kind op = expressionUnary.op;
        switch(expUnaryType) {
        case INTEGER:
            switch(op) {
            case OP_PLUS:
                break;
            case OP_MINUS:
                mv.visitInsn(INEG);
                break;
            case OP_EXCLAMATION:
                mv.visitInsn(ICONST_M1);
                mv.visitInsn(IXOR);
                break;
            default:
                break;
            }
            break;
        case FLOAT:
            switch(op) {
            case OP_PLUS:
                break;
            case OP_MINUS:
                mv.visitInsn(FNEG);
                break;
            default:
                break;
            }
            break;
        case BOOLEAN:
            switch(op) {
            case OP_EXCLAMATION:
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        return null;
	}

}
