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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import sun.reflect.generics.tree.TypeArgument;

import code_parser.ModelSourceFile;

public class GenericsVisitor extends ASTVisitor {

	public ArrayList<String> genericFields = new ArrayList<>();
	public ArrayList<String> genericMethods = new ArrayList<>();
	public ArrayList<String> genericInvocations = new ArrayList<>();
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
	
	/**
	 * VISITORS
	 */
	
	// DONE!
	public boolean visit(FieldDeclaration node){
		String fieldDec = findSourceForNode(node);
		//System.out.println("Field source: " + fieldDec);
		
		List<VariableDeclarationFragment> fields = node.fragments();
		String name = "";
		String genField = "";
		
		if (!fields.isEmpty()){
			for (VariableDeclarationFragment field: fields){
				// field name
				name = findSourceForNode(field);
			}			
			Type t = node.getType();
			String type = t.toString();
			
			for (String s: types){
				if (type.equals(s)){
					
					// add field to list if not already found
					genField = type + CHECK_SEPERATOR + name;
					if (!genericFields.contains(genField)){
						genericFields.add(genField);
						System.out.println("Generic field: " + genField);
					}
				}			
			}
		}
		
		return true;
	}
	
	
	public boolean visit(MethodDeclaration node) {
		
		//String methodDec = findSourceForNode(node);
	
		String method = findSourceForNode(node.getName());
		//System.out.println("Method declaration: " + method);
		
		List<TypeParameter> decs = node.typeParameters();
		
		if (!decs.isEmpty()){
			//System.out.println("Generic method!");
			
			// see if parameterized type
			for (TypeParameter dec: decs){
				List<ParameterizedType> bounds = dec.typeBounds();
				
				// is this worth more? add another point if know how to do this?
				if (!bounds.isEmpty()){
					//System.out.println("Parameterized Type Bound!");
					for (ParameterizedType t: bounds){
						//System.out.println("Type Bound: " + t.toString()); 
						String type = t.toString();
						
						String genMethDec =  type + CHECK_SEPERATOR + method;
						System.out.println("Generic method declaration: " + genMethDec);
						if (!genericMethods.contains(genMethDec)){
							genericMethods.add(genMethDec);
						}
					}
				}
			}
		}
		
		
		// TODO: find example to see if this works -- otherwise can probably remove
		IMethodBinding mb = node.resolveBinding();
		
		if (mb != null){
			ITypeBinding[] args = mb.getTypeArguments();
			
			// checking for parameters that are generic or wildcard
			if (args != null){
				for (ITypeBinding arg : args){
					System.out.println("Type argument binding: " + arg.toString());
				}			
			}			
		}
		
		// 
		
		
		return true;
	}
	
	public boolean visit(MethodInvocation node){
		
		String methodInvoc = findSourceForNode(node.getName());
		System.out.println("Method Invocation: " + methodInvoc);
		
		IMethodBinding mb = node.resolveMethodBinding();
		
		if (mb != null){
			if (mb.isParameterizedMethod()){
				System.out.println("Parameterized method invocation!");
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
	
	public boolean visit(VariableDeclarationStatement node){
		
		//  TODO test with collections example; looking for same thing as expression statement
		
		return true;
	}
	
	// TODO: finish this one! Examples broke, but getting fixed
	public boolean visit(ExpressionStatement node){
		
		Expression e = node.getExpression();
		
		String statement = findSourceForNode(e);
		
		if (e instanceof MethodInvocation){
			System.out.println("expression statement invoc: " + statement);	
			
			// TODO: which one will tell me the invoc has explicit type versus implicit type?
			
			if (((MethodInvocation) e).isResolvedTypeInferredFromExpectedType()){
				System.out.println("Type Inferred From Expected Type!");
			}
			
			e.resolveTypeBinding();
			((MethodInvocation) e).resolveMethodBinding();
			((MethodInvocation) e).typeArguments();
		}
		
		return true;
	}
	
}
