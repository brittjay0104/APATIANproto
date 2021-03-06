package node_visitor;

import static code_parser.ModelRepository.CHECK_SEPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import code_parser.ModelSourceFile;
public class NoNullCheckVisitor extends ASTVisitor{
	
	public List<String> potentialNullVariables = new ArrayList<String>();
	public HashMap<String, ArrayList<String>> nullVariables = new HashMap<String, ArrayList<String>>();
	
	public List<String> potentialNullFields = new ArrayList<String>();
	
	private char[] source;
	
	public NoNullCheckVisitor(ModelSourceFile file) {
		this.source = file.getSource(); 
	}
	
	
	public HashMap<String, ArrayList<String>> getNullVariables(){
		return nullVariables;
	}
	
	public List<String> getNullFields(){
		return potentialNullFields;
	}
	
	private MethodDeclaration activeMethod;
	private String methodName;
	// Hash Map for MethodDeclaration (key) and list of invocations (value)
	public final HashMap<String, ArrayList<String>> invocationsForMethods = new HashMap<String, ArrayList<String>>();
	
	public boolean visit (MethodDeclaration node){
		
		activeMethod = node;
		methodName = activeMethod.getName().toString();
		
		return super.visit(node);
	}
	
	public boolean visit(MethodInvocation node){
				
		if (invocationsForMethods.get(methodName) == null){
			invocationsForMethods.put(methodName, new ArrayList<String>());
		}
		
		
		String methodInvoc = findSourceForNode(node);
		
		if (methodInvoc.contains(".")){
			invocationsForMethods.get(methodName).add(methodInvoc);
		}
		
		return super.visit(node);
		
	}
	
	public boolean visit(ReturnStatement node){
		
		
		return true;
		
	}

	public HashMap<String, ArrayList<String>> getInvocations(){
		return invocationsForMethods;
	}
	
	public List<String> assignments = new ArrayList<String>();
	public HashMap<String, ArrayList<String>> nullAssignments = new HashMap<String, ArrayList<String>>();
	
	public boolean visit (Assignment node){
		
		Expression eL = node.getLeftHandSide();
		Expression eR = node.getRightHandSide();
		
		// collect assignments to go through when checking for field that could be null
		String assignment = findSourceForNode(node);
		assignments.add(assignment);
		
		
		if (getMethodDeclaration(node) != null){
			String decMethod = findSourceForNode(getMethodDeclaration(node).getName());
			
			if (findSourceForNode(eR).contains("null")){			
				//System.out.println("Left side of assignment to null ==> " + findSourceForNode(eL));
				
				if (nullAssignments.get(decMethod) == null){
					nullAssignments.put(decMethod, new ArrayList<String>());
				}
				
				nullAssignments.get(decMethod).add(findSourceForNode(eL));
								
			}
			
			// is the left side null and then not checked (in that scope -- method)?
			
		} else if (getTypeDeclaration(node) != null){
			String typeDec = findSourceForNode(getTypeDeclaration(node).getName());
			
			if (findSourceForNode(eR).contains("null")){
				//System.out.println("Left side of assignment to null ==> " + findSourceForNode(eL));

				if (nullAssignments.get(typeDec) == null){
					nullAssignments.put(typeDec, new ArrayList<String>());
				}
				
				nullAssignments.get(typeDec).add(findSourceForNode(eL));
			}
		}
		
		return true;
		
	}
	
	public List<String> getAssignments(){
		return assignments;
	}
	
	public HashMap<String, ArrayList<String>> getNullAssignments(){
		return nullAssignments;
	}
	
	public boolean visit(VariableDeclarationStatement node){
		
		
		if (node != null){ 
						
			if (getMethodDeclaration(node) != null){
				MethodDeclaration dec = getMethodDeclaration(node);
				String decMethod = findSourceForNode(dec.getName());
				
				String statement = findSourceForNode(node);
				if (statement.contains("=")){
					int index = statement.indexOf("=");
					
					String RHS = statement.substring(index+1, statement.length());
					String LHS = statement.substring(0, index);
					
					
					// know these are set to null  
					if (RHS.contains("null")){
//						System.out.println("Left hand side of variable declared null ==> " + LHS.trim());

						String variable = "";
						String s = LHS.trim();
						int startIndex = s.indexOf(" ");
						
						while (startIndex != -1){
							variable = s.substring(startIndex, s.length());
							s = variable.trim();
							startIndex = s.indexOf(" "); 
						} 
												
						
						if (nullVariables.get(decMethod) == null){
							nullVariables.put(decMethod, new ArrayList<String>());
						}
						
						nullVariables.get(decMethod).add(s.trim());
						
					}				
				}	
				
			} else if (getTypeDeclaration(node) != null){
				TypeDeclaration type = getTypeDeclaration(node);
				String typeDec = findSourceForNode(type);
				
				String statement = findSourceForNode(node);
				if (statement.contains("=")){
					int index = statement.indexOf("=");
					
					String RHS = statement.substring(index+1, statement.length());
					String LHS = statement.substring(0, index);
					
					// know these are set to null  
					if (RHS.contains("null")){
						//System.out.println("Left hand side of variable declared null ==> " + LHS.trim());

						String variable = "";
						String s = LHS.trim();
						int startIndex = s.indexOf(" ");
						
						while (startIndex != -1){
							variable = s.substring(startIndex, s.length());
							s = variable.trim();
							startIndex = s.indexOf(" "); 
						} 
						//System.out.println("	--> " + variable);
						
						
						if (nullVariables.get(typeDec) == null){
							nullVariables.put(typeDec, new ArrayList<String>());
						}
						
						nullVariables.get(typeDec).add(s.trim());
						
					}	
				} else {
					int index = statement.indexOf(";");
					String LHS = statement.substring(0, index);
					
					//System.out.println("Left hand side of variable declared ==> " + LHS.trim());					
					
					String variable = "";
					String s = LHS.trim();
					int startIndex = s.indexOf(" ");
					
					while (startIndex != -1){
						variable = s.substring(startIndex, s.length());
						s = variable.trim();
						startIndex = s.indexOf(" "); 
					} 
										
					if (nullVariables.get(typeDec) == null){
						nullVariables.put(typeDec, new ArrayList<String>());
					}
										
					nullVariables.get(typeDec).add(s.trim());
					
				}
			}
		}

		
		return true;
	}
	
	public boolean visit (FieldDeclaration node){
		
		if (node != null){ 
			String statement = findSourceForNode(node);
				
			if (statement.contains("=")){
				int index = statement.indexOf("=");
				
				String RHS = statement.substring(index+1, statement.length());
				String LHS = statement.substring(0, index-1);
								
				// know these are set to null  				
				if (RHS.contains("null")){
					//System.out.println("Field declared null ==> " + statement);
					//int j = 1;
					String s = LHS.trim();
					
										
					String variable = "";
					int startIndex = s.indexOf(" ");
					
					while (startIndex != -1){
						variable = s.substring(startIndex, s.length());
						s = variable.trim();
						startIndex = s.indexOf(" "); 
					} 
					
					//System.out.println(variable.trim());
					potentialNullFields.add(s.trim());						
					
										
				}
				
			} 
			// know these are set to null;
			else if (statement.contains(";") && !(statement.contains("="))){
				int index = statement.indexOf(";");
				String LHS = statement.substring(0, index);
				
				if (LHS.indexOf(" ") != -1){
					int i2 = LHS.lastIndexOf(" ");
					String variable = LHS.substring(i2+1, LHS.length());
					potentialNullFields.add(variable);					
				}
				
			}

		}
		
		return true;
	}
	
	public MethodDeclaration getMethodDeclaration(ASTNode node){		
		if (node.getParent() != null){
			return node instanceof MethodDeclaration ? (MethodDeclaration)node : getMethodDeclaration(node.getParent());			
		}
		
		return null;
	}
	
	public TypeDeclaration getTypeDeclaration(ASTNode node){
		if (node.getParent() != null){
			return node instanceof TypeDeclaration ? (TypeDeclaration) node : getTypeDeclaration(node.getParent());
		}
		
		return null;
	}
	
	private String findSourceForNode(ASTNode node) {
		try {
			return new String(Arrays.copyOfRange(source, node.getStartPosition(), node.getStartPosition() + node.getLength()));
		}
		catch (Exception e) {
			System.err.println("OMG PROBLEM MAKING SOURCE FOR "+ node);
			return "";
		}
	}
	

}
