package node_visitor;

import static code_parser.ModelRepository.CHECK_SEPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import code_parser.ModelSourceFile;

public class GenericsVisitor_2 extends ASTVisitor {
	
	// simple generics: using generics in fields, methods, and variable declarations
	public HashMap<String, List<String>> simpleGenerics = new HashMap<String, List<String>>();
	
	//advanced generics: writing generics and using nested generics
	public HashMap<String, List<String>> advancedGenerics = new HashMap<String, List<String>>();
	
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
		
		simpleGenerics.put("fields", new ArrayList<String>());
		simpleGenerics.put("variables", new ArrayList<String>());
		simpleGenerics.put("methods", new ArrayList<String>());
		simpleGenerics.put("return", new ArrayList<String>());
		
		advancedGenerics.put("classes", new ArrayList<String>());
		advancedGenerics.put("fields", new ArrayList<String>());
		advancedGenerics.put("methods", new ArrayList<String>());
		advancedGenerics.put("return", new ArrayList<String>());
		advancedGenerics.put("nested", new ArrayList<String>());
		advancedGenerics.put("parameters", new ArrayList<String>());
		advancedGenerics.put("bounds", new ArrayList<String>());
		advancedGenerics.put("wildcard", new ArrayList<>());
		advancedGenerics.put("diamond", new ArrayList<String>());
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
	
	public ReturnStatement getReturnStatement (ASTNode node) {
		if (node.getParent() != null){
			return node instanceof ReturnStatement ? (ReturnStatement)node : getReturnStatement(node.getParent());			
		}
		
		return null;
	}
	
	public boolean parentIsTypeDeclaration (ASTNode node){
		if (node.getParent() instanceof TypeDeclaration){
			return true;
		}
		
		return false;
	}
	
	public HashMap<String, List<String>> getSimpleGenerics (){
		return simpleGenerics;
	}
	
	public HashMap<String, List<String>> getAdvancedGenerics(){
		return advancedGenerics;
	}
	
	// ?
	public boolean visit (WildcardType node){
		
		// advanced
		MethodDeclaration md = getMethodDeclaration(node);
		
		if (md != null){
			String parent = node.getParent().toString();
			System.out.println("Wildcard Type --> " + parent);	
			String method = findSourceForNode(md.getName());
			
			String pattern = method + CHECK_SEPERATOR + parent;
			
			advancedGenerics.get("wildcard").add(pattern);
		}
		
		return true;
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

		//System.out.println("Generic class --> " + sb.toString());
		advancedGenerics.get("classes").add(sb.toString());
		
		return true;
	}
	
	// public T t; 
	// simple generic type (T, ETC) fields, return types AND parameters
	public boolean visit (SimpleType node){
		
		for (String t: types){
			if (node.getName().getFullyQualifiedName().equals(t)){
				
				// parameters
				if (node.getParent() instanceof MethodDeclaration){
					MethodDeclaration m = (MethodDeclaration) node.getParent();
					String method = findSourceForNode(m);
										
					String meth = method.substring(0, method.indexOf("{")).trim();
					
					//System.out.println("Advanced generic type in method --> " + meth);
					
					advancedGenerics.get("parameters").add(meth);
				}
				
				// fields
				if (node.getParent() instanceof FieldDeclaration){
					FieldDeclaration fd = (FieldDeclaration) node.getParent();
					
					if (fd != null){
						String field = findSourceForNode(fd);
						
						//System.out.println("Advanced generic field --> " + field);
						advancedGenerics.get("fields").add(field);
					}
				}
			}
		}
		
				
		return true;
	}
	
	
	//Comparable<T>; Box<String> ... 
	public boolean visit (ParameterizedType node){
				
		MethodDeclaration md = getMethodDeclaration(node);
		if (md != null){
			String methodDec = findSourceForNode(md.getName());
			String method = findSourceForNode(md);
			String methDec = method.substring(0, method.indexOf("{"));	
			
			List typeArguments = node.typeArguments();
			
			if (typeArguments.size() == 0){
				// diamond notation
				ASTNode thisParent = node.getParent();
				String source = findSourceForNode(thisParent);
				
				String pattern = methodDec + CHECK_SEPERATOR + source;
				advancedGenerics.get("diamond").add(pattern);
				
			}
			
			if (typeArguments != null){
				for (Object ta: typeArguments){
					
					// variable declaration
					ASTNode thisParent = node.getParent();
					if (thisParent instanceof VariableDeclarationStatement){
						
						ASTNode parent = (VariableDeclarationStatement) thisParent;
						
						String varDec = findSourceForNode(parent);
						
						//System.out.println("Variable Declaration Generics --> " + varDec);
						
						String pt = methodDec + CHECK_SEPERATOR + varDec;
						
						simpleGenerics.get("variables").add(pt);
						
					}
					
					
					if (thisParent instanceof MethodDeclaration){
						//System.out.println("Simple generics in method declaration! --> " + ta.toString());
						
						String generics = ta.toString() + CHECK_SEPERATOR + methDec;
						
						// advanced (written)
						if (stringContainsTypeParameter(ta.toString(), types)){						
							advancedGenerics.get("methods").add(generics);
						}
						
						// simple (used)
						simpleGenerics.get("methods").add(generics);
						
						
					}
					
					ReturnStatement rt = getReturnStatement(node);
					
					if (rt != null && rt instanceof ReturnStatement){
						
						String ret = findSourceForNode(rt);
						String generics = methodDec + CHECK_SEPERATOR + ta.toString() + CHECK_SEPERATOR + ret;
						
						// advanced
						if (stringContainsTypeParameter(ta.toString(), types)){
							advancedGenerics.get("return").add(generics);
						}
						
						// simple
						simpleGenerics.get("return").add(generics);
					}						
					
					
					// nested generics 
					if (ta instanceof ParameterizedType){
						ParameterizedType t = (ParameterizedType) ta;
						
						if (t.typeArguments() != null){
							for (Object tta: t.typeArguments()){
								
								//System.out.println("Advanced Parameterized Type --> " + node.getParent().toString());
								
								// other than method
								if (! (thisParent instanceof MethodDeclaration)){
									String paramType = methodDec + CHECK_SEPERATOR + thisParent.toString();
									advancedGenerics.get("nested").add(paramType);
								}
								
								// method
								if (thisParent instanceof MethodDeclaration){
									String meth = ta.toString() + CHECK_SEPERATOR + methDec;
									advancedGenerics.get("nested").add(meth);								
								}
								
							}
						}
					}
					
				}
			}
		}
		
		
		return true;
	}
	
	//<T extends Comparable<T>> - need method attached; 
	public boolean visit (TypeParameter node){
				
		// ignore if immediate parent is TypeDeclaration
		if (!(node.getParent() instanceof TypeDeclaration)){
			MethodDeclaration md = getMethodDeclaration(node);
			String methodDec = findSourceForNode(md.getName());
			String s = findSourceForNode(md);
			String meth = s.substring(0, s.indexOf("{"));
			
			List typeBounds = node.typeBounds();
			
			// type bounds
			for (Object tb: typeBounds){
				//System.out.println("Type bound --> " + tb.toString());
				String methodTypeBound = tb.toString() + CHECK_SEPERATOR + meth ;
				
				advancedGenerics.get("bounds").add(methodTypeBound);
			}
			
			// type parameters
			String typeParam = node.toString();
			String methodParam = typeParam + CHECK_SEPERATOR + meth.trim();
			
			advancedGenerics.get("parameters").add(methodParam);
			//System.out.println("Method Type Parameter --> " + typeParam);
		}
				
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
