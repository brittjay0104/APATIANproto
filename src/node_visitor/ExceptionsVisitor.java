package node_visitor;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import code_parser.ModelSourceFile;



public class ExceptionsVisitor extends ASTVisitor {
	// Methods that declare "throws"
	List<String> throwsMethods = new ArrayList<String>();
	
	// Try statements
	List<String> tryStatements = new ArrayList<String>();
	
	// Throw statements (TBD)
	List<String> throwStatements = new ArrayList<String>();
	
	// Classes that extend an exception
	List<String> exceptionClasses = new ArrayList<String>();
	
	// The source file
	// Note: This seems to be present in all "Visitor" classes in this package,
	//   so it probably should be pulled into an abstract parent class
	// TODO: Pull into abstract parent class
	private char[] source;
	
	// Some separator character for printing
	// Note: This seems to be present in many "Visitor" classes in this package,
	//   so it probably should be pulled into an abstract parent class
	// TODO: Pull into abstract parent class
	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;
	
	
	
	/**
	 * Constructor
	 * @param file
	 */
	public ExceptionsVisitor(ModelSourceFile file) {
		this.source = file.getSource();
	}
	
	
	
	// Note: This seems to be present in all "Visitor" classes in this package,
	//   so it probably should be pulled into an abstract parent class
	// TODO: Pull into abstract parent  class
	private String findSourceForNode(ASTNode node) {
		try {
			return new String(Arrays.copyOfRange(source, node.getStartPosition(), node.getStartPosition() + node.getLength()));
		}
		catch (Exception e) {
			System.err.println("OMG PROBLEM MAKING SOURCE FOR "+node);
			return "";
		}
	}
	
	
	
	// Note: This seems to be present in all "Visitor" classes in this package,
	//   so it probably should be pulled into an abstract parent class
	// TODO: Pull into abstract parent class
	public MethodDeclaration getMethodDeclaration(ASTNode node){
		if (node.getParent() != null){
			return node instanceof MethodDeclaration ? (MethodDeclaration)node : getMethodDeclaration(node.getParent());			
		}
		
		return null;
	}
	
	
	
	// Get throwsMethods
	public List<String> getThrowsMethods() {
		return throwsMethods;
	}
	
	// Get tryStatements
	public List<String> getTryStatements() {
		return tryStatements;
	}
	
	// Get throwStatements (TBD)
	public List<String> getThrowStatements() {
		return throwStatements;
	}
	
	// Get exceptionClasses
	public List<String> getExceptionClasses() {
		return exceptionClasses;
	}
	
	
	/**
	 * When visiting a TryStatement, add the statement to our tryStatements
	 *   array.
	 */
	public boolean visit(TryStatement node) {
		
		MethodDeclaration md = getMethodDeclaration(node);
		if (md != null) {
			
			tryStatements.add(md.getName().toString() + CHECK_SEPERATOR + findSourceForNode(node));
			
			System.out.println("try statment found!");
		}
		return true;
	}
	
	
	public boolean visit(ThrowStatement node){
		MethodDeclaration md = getMethodDeclaration(node);
		if (md != null) {
			throwStatements.add(md.getName().toString() + CHECK_SEPERATOR + findSourceForNode(node));
			
			System.out.println("throw statment found!");
		}
		return true;
	}
	
	
	public boolean visit(MethodDeclaration node) {
		//checking for thrown exceptions
		
		MethodDeclaration md = getMethodDeclaration(node);
		
		if(md != null){
			List thrownExceptions = node.thrownExceptions();
			if(!thrownExceptions.isEmpty()){
				
				//Store more information here??
				throwsMethods.add(md.getName().toString() + CHECK_SEPERATOR + findSourceForNode(node));

				System.out.println("thrown exception found!");
			}
			
		}
		return true;
	}
	
	
	public boolean visit(TypeDeclaration node) {
		//System.out.print(node.getName().toString());
		
		//MethodDeclaration md = getMethodDeclaration(node);
		
		// check if superclass is an exception
		
		if (node.getSuperclassType() != null && descendsFromException(node.getSuperclassType().resolveBinding())) {
			exceptionClasses.add(node.getName().toString() + CHECK_SEPERATOR + findSourceForNode(node));
			//System.out.println("\nexception subclass found!");
			
			System.out.println("exception subclass found!");
		}
		else {
			//System.out.println("");
		}
		
		
		return true;
	}
	
	
	private boolean descendsFromException(ITypeBinding node){
		if (node == null) {
			return false;
		}
		else {
			//System.out.print(" < " + node.getName());
		}
		
		if (node.getPackage().getName().equals("java.lang") && node.getName().equals("Exception")) {
			return true;
		}
		else {
			return descendsFromException(node.getSuperclass());
		}
	}
}
