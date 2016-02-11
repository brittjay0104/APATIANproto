package node_visitor;

import static code_parser.ModelRepository.CHECK_SEPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
	
	//list of all (unique) code with generics
	public List<String> allGenerics = new ArrayList<String>();
	
	public HashMap<String, List<String>> generics = new HashMap<String, List<String>>();
	
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
		
		generics.put("type argument methods", new ArrayList<String>());
		generics.put("wildcard", new ArrayList<String>());
		generics.put("type declarations", new ArrayList<String>());
		generics.put("type parameter methods", new ArrayList<String>());
		generics.put("type parameter fields", new ArrayList<String>());
		generics.put("diamond", new ArrayList<String>());
		generics.put("method invocations", new ArrayList<String>());
		generics.put("class instantiation", new ArrayList<String>());
		generics.put("nested", new ArrayList<String>());
		
		
		simpleGenerics.put("fields", new ArrayList<String>());
		simpleGenerics.put("class instances", new ArrayList<String>());
		simpleGenerics.put("method invocations", new ArrayList<String>());
		//simpleGenerics.put("variables", new ArrayList<String>());
		simpleGenerics.put("methods", new ArrayList<String>());
		simpleGenerics.put("return", new ArrayList<String>());
		
		advancedGenerics.put("classes", new ArrayList<String>());
		advancedGenerics.put("fields", new ArrayList<String>());
		advancedGenerics.put("methods", new ArrayList<String>());
		advancedGenerics.put("return", new ArrayList<String>());
		advancedGenerics.put("nested", new ArrayList<String>());
		advancedGenerics.put("parameters", new ArrayList<String>());
		advancedGenerics.put("bounds", new ArrayList<String>());
		advancedGenerics.put("wildcard", new ArrayList<String>());
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
	
	public List<String> getAllGenerics(){
		return allGenerics;
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
			//System.out.println("Wildcard Type --> " + parent);	
			String method = findSourceForNode(md.getName());
			
			String pattern = method + CHECK_SEPERATOR + parent;
			
			if (!(allGenerics.contains(parent))){
				allGenerics.add(parent);
			}
			
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
			sb.append(" ");
		}
		
		sb.append("class ");
		sb.append(fullyQualifiedName);
		
		if (node.typeParameters() != null){
			for (Object tp: node.typeParameters()){
				sb.append("<");
				sb.append(tp.toString());
				sb.append(">");
			}
		}

		//System.out.println("Generic class --> " + sb.toString());
		String gClass = sb.toString();
		if (!(allGenerics.contains(gClass))){
			allGenerics.add(gClass);
		}

		advancedGenerics.get("classes").add(gClass);
		
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
					String meth = "";
					
					if (method.contains("{")){
						meth = method.substring(0, method.indexOf("{")).trim();						
					}
					
					if (!(method.contains("{"))){
						meth = method.substring(0, method.indexOf(";")).trim();
					}
					
					//System.out.println("Advanced generic type in method --> " + meth);
					
					if (!(allGenerics.contains(meth))){
						allGenerics.add(meth);
					}
					
					advancedGenerics.get("parameters").add(meth);
					
				}
				
				// fields
				if (node.getParent() instanceof FieldDeclaration){
					FieldDeclaration fd = (FieldDeclaration) node.getParent();
					
					if (fd != null){
						String field = findSourceForNode(fd);
						
						//System.out.println("Advanced generic field --> " + field);
						if (!(allGenerics.contains(field))){
							allGenerics.add(field);
						}
						
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
			String methDec = "";
			
			if (method.contains("{")){
				methDec = method.substring(0, method.indexOf("{")).trim();	
			}
			
			if (!(method.contains("{"))){
				methDec = method.substring(0, method.indexOf(";")).trim();				
			}
			
			List typeArguments = node.typeArguments();
			
			if (typeArguments.size() == 0){
				// diamond notation
				ASTNode thisParent = node.getParent();
				String source = findSourceForNode(thisParent);
				
				String pattern = methodDec + CHECK_SEPERATOR + source;
				
				if (!(allGenerics.contains(source))){
					allGenerics.add(source);
				}
				
				advancedGenerics.get("diamond").add(pattern);
				
			}
			
			if (typeArguments != null){
				for (Object ta: typeArguments){
					
					// class instance creation
					ASTNode thisParent = node.getParent();
					
					if (thisParent instanceof ClassInstanceCreation){
						
						ASTNode parent = (ClassInstanceCreation) thisParent;
						
						String classInstance = findSourceForNode(parent);
						
						//System.out.println("Class Instance Creation Generics --> " + classInstance);
						
						if (classInstance.contains("{")){
							classInstance = classInstance.substring(0, classInstance.indexOf("{")).trim();
						}
						
						String pt = methodDec + CHECK_SEPERATOR + classInstance;
						
						if (!(allGenerics.contains(classInstance))){
							allGenerics.add(classInstance);
						}
						
						simpleGenerics.get("class instances").add(pt);
						
					}
					
					if (thisParent instanceof MethodInvocation){
						ASTNode parent = (MethodInvocation) thisParent;
						String methodInvocation = findSourceForNode(parent);
						
						String pt = methodDec + CHECK_SEPERATOR + methodInvocation;
						
						if (!(allGenerics.contains(methodInvocation))){
							allGenerics.add(methodInvocation);
						}
						
						simpleGenerics.get("method invocations").add(pt);
					}
					
					
					if (thisParent instanceof MethodDeclaration){
						//System.out.println("Simple generics in method declaration! --> " + ta.toString());
						
						String generics = ta.toString() + CHECK_SEPERATOR + methDec;
						
						// advanced (written)
						if (stringContainsTypeParameter(ta.toString(), types)){	
							if (!(allGenerics.contains(methDec))){
								allGenerics.add(methDec);
							}
							advancedGenerics.get("methods").add(generics);
						}				
						
					}					
					
					
					// nested generics 
					if (ta instanceof ParameterizedType){
						ParameterizedType t = (ParameterizedType) ta;
						
						if (t.typeArguments() != null){
							for (Object tta: t.typeArguments()){
								
								//System.out.println("Advanced Parameterized Type --> " + node.getParent().toString());
								
								// other than method
								if (! (thisParent instanceof MethodDeclaration)){
									String parent = thisParent.toString();
									String paramType = methodDec + CHECK_SEPERATOR + parent;
									
									if (!(allGenerics.contains(parent))){
										allGenerics.add(parent);
									}
									
									advancedGenerics.get("nested").add(paramType);
								}
								
								// method
								if (thisParent instanceof MethodDeclaration){
									String meth = ta.toString() + CHECK_SEPERATOR + methDec;
									
									if (!(allGenerics.contains(methDec))){
										allGenerics.add(methDec);
									}
									
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
			String meth = "";
			
			if (s.contains("{")){
				meth = s.substring(0, s.indexOf("{")).trim();				
			}
			if (!(s.contains("{"))){
				meth = s.substring(0, s.indexOf(";")).trim();
			}
			
			List typeBounds = node.typeBounds();
			
			// type bounds
			for (Object tb: typeBounds){
				//System.out.println("Type bound --> " + tb.toString());
				String methodTypeBound = tb.toString() + CHECK_SEPERATOR + meth ;
				
				if (!(allGenerics.contains(meth))){
					allGenerics.add(meth);
				}
				
				advancedGenerics.get("bounds").add(methodTypeBound);
			}
			
			// type parameters
			String typeParam = node.toString();
			String methodParam = typeParam + CHECK_SEPERATOR + meth.trim();
			
			if (!(allGenerics.contains(meth.trim()))){
				allGenerics.add(meth.trim());
			}
			
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
