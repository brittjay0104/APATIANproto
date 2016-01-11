package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import code_parser.ModelSourceFile;

public class NPE_Visitor extends ASTVisitor{

	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;
	
	// variables that use Collections
	List<String> collectionsVars = new ArrayList<String>();
	// variables that use Optional
	List<String> optionalVars = new ArrayList<String>();
	// methods with catch NPE
	List<String> catchMeths = new ArrayList<String>();
	
	private char[] source;
	
	public NPE_Visitor (ModelSourceFile file){
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
	
	// EMPTY INSTEAD OF NULL (COLLECTIONS and OPTIONAL<T>)
	
	public boolean visit(VariableDeclarationStatement node){
				
		String src = findSourceForNode(node);
		MethodDeclaration md = getMethodDeclaration(node);
		if(md != null){
			
			String method = md.getName().toString();			
		
		//System.out.println(method);
		//System.out.println(src);
			
			if (src.contains("Collections.emptyList()") || src.contains("Collections.emptyMap()")
					|| src.contains("Collections.emptySet()")){
				
				//System.out.println(src);
				
				List parts = node.fragments();
				
				for (int i=0; i < parts.size(); i++){
					String s = parts.get(i).toString();
					//String var = s.substring(0, s.indexOf("="));
					
					String collVar = method + CHECK_SEPERATOR + s.replace("=", " = ");
					//System.out.println(collVar);
					if (!collectionsVars.contains(collVar)){
						collectionsVars.add(collVar);	
					}
				
				}
				
			}
		
		
			if (src.contains("Optional.of")){
				//System.out.println(src);
				
				List parts = node.fragments();
				
				for (int i=0; i < parts.size(); i++){
					String s = parts.get(i).toString();
					//String var = s.substring(0, s.indexOf("="));
					
					String optVar = method + CHECK_SEPERATOR + s.replace("=", " = ");
					//System.out.println(optVar);
					if (!optionalVars.contains(optVar)){
						optionalVars.add(optVar);
					}
				}
			}
		}
		
		return true;
	}
	
	// CATCH BLOCKS WITH NPE
	
	public boolean visit (TryStatement node){
		
		List clauses = node.catchClauses();
		MethodDeclaration md = getMethodDeclaration(node);
		if (md != null){
			String method = md.getName().toString();
			
			for (int i=0; i < clauses.size(); i++){
				
				CatchClause clause = (CatchClause) clauses.get(i);
				String s = clause.toString();
				String c = s.substring(0, s.indexOf("{"));
				
				
				//System.out.println("Catch Clause ---> " + c);
				
				SingleVariableDeclaration exception = clause.getException();
				
				String ex = findSourceForNode(exception);
				
				if (ex.contains("NullPointerException")){
					// make sure unique -- for now, check method
					// TODO: improve this -- right now only allows one NPE per method
					String catchBlock = method + CHECK_SEPERATOR + c;
					if (!catchMeths.contains(catchBlock) && !catchBlock.contains("$missing$")){
						//System.out.println(catchBlock);	
						catchMeths.add(catchBlock);					
					}
				}
			}
		}
		
		return true;
	}
	
	public List<String> getCollVars(){
		return collectionsVars;
	}
	
	public List<String> getOptVars(){
		return optionalVars;
	}
	
	public List<String> getCatchBlocks(){
		return catchMeths;
	}
	
}
