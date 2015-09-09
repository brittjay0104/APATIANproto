package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import code_parser.ModelSourceFile;

public class StringMethodVisitor extends ASTVisitor {
	
	// each method invocation includes method its called in and 
	// actual invocation code (sb.append(...))
	ArrayList<String> methodInvocs = new ArrayList<String>();
	
	
	//map of methods used and how many times used
	Map<String, Integer> methods = new HashMap<String, Integer>();
	
	// TODO: determine if object passed in checked for null prior to 
	// -- if not, add point (if method already checks for null)
	// -- if so, add point if checking for null correctly 
	//    (if method returns special string for null)
	
	
	private char[] source;
	
	public StringMethodVisitor(ModelSourceFile file){
		this.source = file.getSource();
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
	
	public boolean visit (MethodInvocation node){
		
//		String invoc = findSourceForNode(node);
//		
		String methodName = node.getName().toString();
		
		Integer methodCount = methods.get(methodName);
		
		if (methodCount == null){
			// add to map and place 1 as count
			methods.put(methodName, 1);
		}
		
		// increment count by 1
		methods.put(methodName, methods.get(methodName) + 1);		 
		
		return true;
	}
	
	public Map<String, Integer> getMethods (){
		return methods;
	}

}
