package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes;

public abstract class Declaration extends PLPASTNode {
	
	PLPTypes.Type type;
	Declaration declaration;
	int current_slot;

	public Declaration(Token firstToken) {
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

	public int getCurrent_slot() {
		return current_slot;
	}

	public void setCurrent_slot(int current_slot) {
		this.current_slot = current_slot;
	}

}
