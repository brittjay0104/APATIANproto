package node_visitor;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;

public class GenericsVisitor extends ASTVisitor {

	public ArrayList<String> genericMethods = new ArrayList<>();
	public ArrayList<String> wildcardMethods = new ArrayList<>();
	
	
}
