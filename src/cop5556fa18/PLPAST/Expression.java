package cop5556fa18.PLPAST;

import cop5556fa18.PLPTypes;
import cop5556fa18.PLPScanner.Token;

public abstract class Expression extends PLPASTNode {
	
	PLPTypes.Type type;
	Declaration declaration;

	public Expression(Token firstToken) {
		super(firstToken);
	}	
	
	public PLPTypes.Type getType() {
		return type;
	}

	public void setType(PLPTypes.Type type) {
		this.type = type;
	}

	public Declaration getDeclaration() {
		return declaration;
	}

	public void setDeclaration(Declaration declaration) {
		this.declaration = declaration;
	}
	
	
}
