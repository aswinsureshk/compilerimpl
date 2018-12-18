package cop5556fa18;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cop5556fa18.PLPAST.Declaration;

/**
 * This class represents the SymbolTable that stores the identifier and its scope
 * Implemented as per LeBlanc and Cook - Chained symbolTable 
 * @author asureshk
 */
public class SymbolTable {

	private final Map<String, List<Attributes>> symbolTable = new HashMap<>();
	private final Stack<Integer> scopeStack = new Stack<>();
	private int nextScope;
	private int currentScope;
    
    /**
     * Adds an entry to the symbolTable with the currentScope
     * @param identifier
     * @param declaration
     */
    public void put(String identifier, Declaration declaration){
    	
    	List<Attributes> lAttributes = symbolTable.getOrDefault(identifier, new ArrayList<Attributes>());
    	Attributes attributes = new Attributes(currentScope, declaration);
    	lAttributes.add(attributes);
    	symbolTable.put(identifier, lAttributes);
    }
    
    /**
     * Does a lookup to check if the provided identifier 
     * is present in the SymbolTable
     * Starts from last entry in the chain to see if this is the latest result
     * @param identifier
     * @return Declaration if identifier is present, else null
     */
    public Declaration lookup(String identifier) {
    	
    	if (symbolTable.containsKey(identifier)) {
    		
    		List<Attributes> lAttributes = symbolTable.get(identifier);
    		int index = lAttributes.size()-1;
    		while (index >= 0) {
    			Attributes attributes = lAttributes.get(index);
    			if (scopeStack.contains(attributes.getScopeNumber()))
    				return attributes.getDeclaration();
    			index--;
    		}
    	}
    	return null;
    }
    
    
    /**
     * Does a lookup to check if the provided identifier 
     * is present in the SymbolTable in the current scope
     * Starts from last entry in the chain to see if this is the latest result
     * @param identifier
     * @return Declaration if identifier is present, else null
     */
    public Declaration lookupInCurrentScope(String identifier) {
    	
    	if (symbolTable.containsKey(identifier)) {
    		
    		List<Attributes> lAttributes = symbolTable.get(identifier);
    		int index = lAttributes.size()-1;
    		while (index >= 0) {
    			Attributes attributes = lAttributes.get(index);
    			if (scopeStack.peek().equals(attributes.getScopeNumber()))
    				return attributes.getDeclaration();
    			index--;
    		}
    	}
    	return null;
    }
    
    /**
     * Checks to see if the identifier is present in the symbolTable
     * @param identifier
     * @return true if identifier is present
     */
    public boolean containsKey(String identifier) {
    	
    	Object lookup_result = lookup(identifier);
    	return lookup_result != null;
    }
    
    /**
     * Checks to see if the identifier is present in the symbolTable in the currentScope
     * @param identifier
     * @return true if identifier is present
     */
    public boolean containsKeyInCurrentScope(String identifier) {
    	
    	Object lookup_result = lookupInCurrentScope(identifier);
    	return lookup_result != null;
    }
    
    /**
     * Called when a block is entered
     */
    public void enterScope() {
    	
    	currentScope = nextScope++;
    	scopeStack.push(currentScope);
    }
    
    /**
     * Called when leaving a block
     */
    public void leaveScope() {
    	
    	currentScope = scopeStack.pop();
    }
    
    /**
     * Called to clear symbolTable when program exits
     */
    public void clear() {
    	
    	symbolTable.clear();
    	scopeStack.clear();
    	nextScope = 0;
    	currentScope = 0;
    }
}
