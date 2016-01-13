package node_visitor;

import static code_parser.ModelRepository.CHECK_SEPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

import sun.reflect.generics.tree.TypeArgument;

import code_parser.ModelSourceFile;

public class GenericsVisitor extends ASTVisitor {

	public ArrayList<String> genericFields = new ArrayList<String>();
	public ArrayList<String> genericMethods = new ArrayList<String>();
	public ArrayList<String> genericInvocations = new ArrayList<String>();
	public ArrayList<String> genericVarDecs = new ArrayList<String>();
	public ArrayList<String> genericParameters = new ArrayList<String>();
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
	
	public List<String> getGenericFields(){
		return genericFields;
	}
	
	public List<String> getGenericMethods(){
		return genericMethods;
	}
	
	public List<String> getGenericInvocations(){
		return genericInvocations;
	}
	
	public List<String> getGenericVariableDecs(){
		return genericVarDecs;
	}
	
	public List<String> getGenericParameters(){
		return genericParameters;
	}
	
	/**
	 * VISITORS
	 */
	
	// DONE!
	public boolean visit(FieldDeclaration node){
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
							System.out.println("Generic field: " + fieldDec);
						}
					}			
				}
			}			
		}
		
		return true;
	}
	
	// methods with type bounds
	public boolean visit(MethodDeclaration node) {
		
		//String methodDec = findSourceForNode(node);
	
		String method = findSourceForNode(node.getName()).trim();
		//System.out.println("Method declaration: " + method);
		
		List<TypeParameter> decs = node.typeParameters();
		
		if (!decs.isEmpty()){
			//System.out.println("Generic method!");
			
			// see if parameterized type
			for (TypeParameter dec: decs){
				
				if (!(dec.typeBounds().isEmpty())){
					for (int i = 0; i < dec.typeBounds().size(); i++ ){
						//System.out.println("Generic Type Bound!");
						String type = dec.typeBounds().get(i).toString().trim();
						//System.out.println("Type Bound: " + type); 

						
						String genMethDec =  type + CHECK_SEPERATOR + method;
						System.out.println("Generic method declaration: " + genMethDec);
						if (!genericMethods.contains(genMethDec)){
							genericMethods.add(genMethDec);
						}
					}
				}				
			}
		}
		
		
		/*// TODO: find example to see if this works -- otherwise can probably remove
		IMethodBinding mb = node.resolveBinding();
		
		if (mb != null){
			ITypeBinding[] args = mb.getTypeArguments();
			
			// checking for parameters that are generic or wildcard
			if (args != null){
				for (ITypeBinding arg : args){
					System.out.println("Type argument binding: " + arg.toString());
				}			
			}			
		}*/
		
		// 
		
		
		return true;
	}
	
	public boolean visit(MethodInvocation node){
		
		String methodInvoc = findSourceForNode(node.getName());
		//System.out.println("Method Invocation: " + methodInvoc);
		
		IMethodBinding mb = node.resolveMethodBinding();
		
		if (mb != null){
			if (mb.isGenericMethod()){
				System.out.println("Generic method invocation!");
			}
			if (mb.isParameterizedMethod()){
				System.out.println("Parameterized method invocation!");
			}
			if (mb.isRawMethod()){
				System.out.println("Raw generic method invocation!");
			}
			
			//get type arguments to see if invocation has explicit type
			ITypeBinding[] args = mb.getTypeArguments();
			
			if (args != null){
				for (ITypeBinding arg: args){
					System.out.println("Invocation type binding: " + arg.toString());
				}
			}
		}
		
		return true;
	}
	

	// methods and constructors with generic parameters
	public boolean visit(SingleVariableDeclaration node){
		
		String dec = findSourceForNode(node);
					
		String type = node.getType().toString().trim();
		
		MethodDeclaration md = getMethodDeclaration(node);
		String method = md.getName().toString().trim();
		
		//System.out.println("Declaring method --> " + method);
		
		for (String t: types){
			if (type.equals(t)){
				String variable = dec.substring(dec.indexOf(" "), dec.length()).trim();
				
				String genParam = type + CHECK_SEPERATOR + variable + CHECK_SEPERATOR + method;
				if (!(genericParameters.contains(genParam))){
					genericParameters.add(genParam);
				}
			}
		}
		
		return true;
	}
	
	// instantiation of generic type
	public boolean visit(VariableDeclarationStatement node){
		
		String dec = findSourceForNode(node);
		
		String type = node.getType().toString().trim();
		
		for (String t: types){
			if (type.equals(t)){
				MethodDeclaration md = getMethodDeclaration(node);
				String method = md.getName().toString().trim();
				
				String variable = dec.substring(dec.indexOf(" "), dec.indexOf("=")).trim();
				
				//System.out.println("Variable --> " + variable.trim());
				
				String genVarDec = type + CHECK_SEPERATOR + variable + CHECK_SEPERATOR + method; 
				
				if (!(genericVarDecs.contains(genVarDec))){
					genericVarDecs.add(genVarDec);
				}
			}
		}
		
		if (type.contains("<") && type.contains(">")){
			MethodDeclaration md = getMethodDeclaration(node);
			String method = md.getName().toString().trim();
			
			String variable = dec.substring(dec.indexOf(" "), dec.indexOf("=")).trim();
			
			String genVarDec = type + CHECK_SEPERATOR + variable + CHECK_SEPERATOR + method; 
			
			if (!(genericVarDecs.contains(genVarDec))){
				genericVarDecs.add(genVarDec);
			} 
		}
		
		return true;
	}
	
	// TODO: finish this one! Examples broke, but getting fixed
	public boolean visit(ExpressionStatement node){
		
		Expression e = node.getExpression();
		
		String statement = findSourceForNode(e).trim();
		
		if (e instanceof MethodInvocation){
			//System.out.println("expression statement invoc: " + statement);	
			
//			MethodInvocation meth = (MethodInvocation)e;
//			String methodInvoc = findSourceForNode(meth.getName());
//			System.out.println("Method Invocation: " + methodInvoc);
//			
//			IMethodBinding mb = meth.resolveMethodBinding();
							
			// YES! -type argument for generic method invoc
			for (Object t: ((MethodInvocation) e).typeArguments()){
				System.out.println("Type argument: " + t.toString());
				
				String genInvoc = t.toString().trim() + CHECK_SEPERATOR + statement;
				
				if (!(genericInvocations.contains(genInvoc))){
					genericInvocations.add(genInvoc);
				}
			}
			
			if (((MethodInvocation) e).isResolvedTypeInferredFromExpectedType()){
				System.out.println("Type Inferred From Expected Type!");
			}
			//e.resolveTypeBinding();
		}
		
		return true;
	}
	
}
