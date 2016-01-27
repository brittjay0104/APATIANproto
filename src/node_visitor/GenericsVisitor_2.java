package node_visitor;

import static code_parser.ModelRepository.CHECK_SEPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import code_parser.ModelSourceFile;

public class GenericsVisitor_2 extends ASTVisitor {

	// type declaration
	public ArrayList<String> typeDecs = new ArrayList<String>();
	
	//fields
	public ArrayList<String> genericFields = new ArrayList<String>();
	
	//variables
	public ArrayList<String> genericVariables = new ArrayList<String>();
	
	// parameterized types
	public ArrayList<String> simpleParamTypes = new ArrayList<String>();
	public ArrayList<String> advancedParamTypes = new ArrayList<String>();
	
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
	
	public boolean parentIsTypeDeclaration (ASTNode node){
		if (node.getParent() instanceof TypeDeclaration){
			return true;
		}
		
		return false;
	}
	
	
	// public class Box<T>
	public boolean visit (TypeDeclaration node){
		
//		node.getFields();
//		node.getMethods();
		
		StringBuffer sb = new StringBuffer();
		String fullyQualifiedName = node.getName().getFullyQualifiedName();
		node.modifiers();
		
		for (Object mod: node.modifiers()){
			sb.append(mod.toString());
		}
		
		sb.append(" class ");
		sb.append(fullyQualifiedName);
		
		if (node.typeParameters() != null){
			for (Object tp: node.typeParameters()){
				sb.append("<");
				sb.append(tp.toString());
				sb.append(">");
			}
		}

		System.out.println("Generic class --> " + sb.toString());
		
		return true;
	}
	
	// public T t;
	public boolean visit (FieldDeclaration node){
		
		String fieldDec = findSourceForNode(node).trim();
		//System.out.println("Field source: " + fieldDec);
		
		List<VariableDeclarationFragment> fields = node.fragments();
		
		if (!fields.isEmpty()){
			for (VariableDeclarationFragment field: fields){
				// field name
				//name = findSourceForNode(field).trim();
				Type t = node.getType();
				String type = t.toString().trim();
				
				for (String s: types){
					if (type.equals(s)){
						
						// add field to list if not already found
						//genField = type + CHECK_SEPERATOR + name;
						if (!genericFields.contains(fieldDec)){
							genericFields.add(fieldDec);
							System.out.println("Generic field --> " + fieldDec);
						}
					}			
				}
			}			
		}
		
		return true;
		
	}
	
	
	//Comparable<T>
	public boolean visit (ParameterizedType node){
		
		MethodDeclaration md = getMethodDeclaration(node);
		String methodDec = md.getName().getFullyQualifiedName();
				
		List typeArguments = node.typeArguments();
		if (typeArguments != null){
			for (Object ta: typeArguments){

				// advanced (nested generics)
				if (ta instanceof ParameterizedType){
					ParameterizedType t = (ParameterizedType) ta;
					
					if (t.typeArguments() != null){
						for (Object tta: t.typeArguments()){
							String paramType = methodDec + CHECK_SEPERATOR + ta.toString();
							
							if (!(advancedParamTypes.contains(paramType))){
								advancedParamTypes.add(paramType);
								System.out.println("Advanced Parameterized Type --> " + paramType);
							}
						}
					}
				}
				
				
				// store something different if is variable declaration rather than method
				if (node.getParent() instanceof VariableDeclarationStatement){
					
					ASTNode parent = (VariableDeclarationStatement) node.getParent();
					
					String varDec = findSourceForNode(parent);
					
					System.out.println("Variable Declaration Generics --> " + varDec);
					
					if (!(genericVariables.contains(varDec))){
						genericVariables.add(varDec);
					}
				}
				
				// only go here if not Variable Declaration
				if (stringContainsTypeParameter(ta.toString(), types)){
					String paramType = methodDec + CHECK_SEPERATOR + ta.toString();
					
					if (!(simpleParamTypes.contains(paramType)) & !(advancedParamTypes.contains(paramType))){
						
						simpleParamTypes.add(paramType);
						System.out.println("Simple Parameterized Type --> " + paramType);
					}	
				}
				
			}
		}
		
		return true;
	}
	
	//<T extends Comparable<T>> - need method attached; 
	public boolean visit (TypeParameter node){
		
		MethodDeclaration md = getMethodDeclaration(node);
		String methodDec = findSourceForNode(md);
		
		// ignore if immediate parent is TypeDeclaration
		if (!(node.getParent() instanceof TypeDeclaration)){
			
			
		}
		
		// TODO: if contains extends, advanced
		// TODO: can we see if after extends is Generics as well?? -- also advanced
		
		
		return true;
	}
	
	public boolean stringContainsTypeParameter(String input, List<String> types){
		
		for (String s: types){
			if (input.contains(s)){
				return true;
			}
		}
		
		return false;
		
	}
	
			
}
