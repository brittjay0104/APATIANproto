package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import code_parser.ModelSourceFile;

public class GenericsVisitor_2 extends ASTVisitor {

	// method declaration related generics
	public ArrayList<String> genericMethodDec = new ArrayList<String>();
	public ArrayList<String> erasureMethodDec = new ArrayList<String>();
	
	
	public List<String> types = new ArrayList<String>();
	private char[] source;
	
	public GenericsVisitor_2 (ModelSourceFile file){
		this.source = file.getSource();
		
		types.add("T");
		types.add("E");
		types.add("U");
		types.add("K");
		types.add("N");
		types.add("V");
		types.add("S");
		types.add("?");
	}
	
	
	private String findSourceForNode(ASTNode node) {
		try {
			return new String(Arrays.copyOfRange(source, node.getStartPosition(), node.getStartPosition() + node.getLength()));
		}
		catch (Exception e) {
			System.err.println("OMG PROBLEM MAKING SOURCE FOR "+node);
			return "";
		}
	}
	
	public MethodDeclaration getMethodDeclaration(ASTNode node){		
		if (node.getParent() != null){
			return node instanceof MethodDeclaration ? (MethodDeclaration)node : getMethodDeclaration(node.getParent());			
		}
		
		return null;
	}
	
	// public class Box<T>
	public boolean visit (TypeDeclaration node){
		
		
		return false;
	}
	
	
	//Comparable<T> - need method attached\
	public boolean visit (ParameterizedType node){
		
		
		return false;
	}
	
	//<T extends Comparable<T>> - need method attached
	public boolean visit (TypeParameter node){
		
		
		return false;
	}
	
	// public T t;
	public boolean visit (FieldDeclaration node){
		
		
		return false;
	}
			
}
