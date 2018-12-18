package cop5556fa18;

import cop5556fa18.PLPAST.Declaration;

public class Attributes {
	
	private int scopeNumber;
	private Declaration declaration;
	
	public Attributes(int scopeNumber, Declaration declaration) {
		this.scopeNumber = scopeNumber;
		this.declaration = declaration;
	}
	
	//scopeNumber will be 0
	public Attributes (Declaration declaration) {
		this.declaration = declaration;
	}
	
	public int getScopeNumber() {
		return scopeNumber;
	}
	public void setScopeNumber(int scopeNumber) {
		this.scopeNumber = scopeNumber;
	}
	public Declaration getDeclaration() {
		return declaration;
	}
	public void setDeclaration(Declaration declaration) {
		this.declaration = declaration;
	}
	
}
