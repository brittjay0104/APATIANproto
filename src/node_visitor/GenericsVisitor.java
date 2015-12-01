package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

import code_parser.ModelSourceFile;

public class GenericsVisitor extends ASTVisitor {

	public ArrayList<String> genericFields = new ArrayList<>();
	public ArrayList<String> genericMethods = new ArrayList<>();
	public ArrayList<String> wildcardMethods = new ArrayList<>();
	public List<String> types = new ArrayList<String>();
	
	private char[] source;
	
	
	/**
	 * Constructor for visitor that analyzes for generics usage
	 */
	
	public GenericsVisitor (ModelSourceFile file){
		this.source = file.getSource();
		
		types.add("T");
		types.add("E");
		types.add("U");
		types.add("K");
		types.add("N");
		types.add("V");
		types.add("S");
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
	
	public List<String> getGenericFields(){
		return genericFields;
	}
	
	public List<String> getGenericMethods(){
		return genericMethods;
	}
	
	public List<String> getWildcardMethods(){
		return wildcardMethods;
	}
	
	/**
	 * VISITORS
	 */
	
	public boolean visit(FieldDeclaration node){
		String fieldDec = findSourceForNode(node);
		System.out.println("Field source: " + fieldDec);
		
		Type t = node.getType();
		String type = t.toString();
		
		System.out.println("Field type: " + type);

		
		for (String s: types){
			if (type.equals(s)){
				System.out.println("Generic field!");
			}			
		}
		
		return true;
	}
	
	
	public boolean visit(MethodDeclaration node) {
		
		//String methodDec = findSourceForNode(node);
	
		String method = findSourceForNode(node.getName());
		System.out.println("Method declaration: " + method);
		
//		Type retType = node.getReturnType2();
//		String type = retType.toString();
//		
//		System.out.println("Method dec return type: " + type);
		
		List<TypeParameter> decs = node.typeParameters();
		
		if (!decs.isEmpty()){
			System.out.println("Generic method!");
			
			// see if parameterized type
			for (TypeParameter dec: decs){
				List<TypeDeclaration> bounds = dec.typeBounds();
				for (TypeDeclaration bound: bounds){
					
				}
			}
		}
		// 
		
		
		return true;
	}
	
}
