package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.lang.model.type.UnionType;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;


import code_parser.ModelSourceFile;

public class ExceptionsVisitor extends AbstractVisitor {
	// Methods that declare "throws"
	public List<String> throwsMethods = new LinkedList<>(),
						tryStatements = new LinkedList<>(),
						tryWithResources = new LinkedList<>(),
						catchBlocks = new LinkedList<>(),
						multiCatchBlocks = new LinkedList<>(),
						finallyBlocks = new LinkedList<>(),
						throwStatements = new LinkedList<>(),
						exceptionClasses = new LinkedList<>(),
						uncheckedExceptions = new LinkedList<>(),
						checkedExceptions = new LinkedList<>();
	
	// List of all unchecked exceptions
	List<String> unchecked = Arrays.asList("java.lang.annotation.AnnotationTypeMismatchException", "java.lang.ArithmeticException", "java.lang.ArrayStoreException", "java.nio.BufferOverflowException", "java.nio.BufferUnderflowException", "javax.swing.undo.CannotRedoException", 
			"javax.swing.undo.CannotUndoException", "java.lang.ClassCastException", "java.awt.color.CMMException", "java.util.ConcurrentModificationException", "javax.xml.bind.DataBindingException", "org.w3c.dom.DOMException", "java.util.EmptyStackException", 
			"java.lang.EnumConstantNotPresentException", "org.w3c.dom.events.EventException", "java.nio.file.FileSystemAlreadyExistsException", "java.nio.file.FileSystemNotFoundException", "java.lang.IllegalArgumentException", "java.lang.IllegalMonitorStateException", 
			"java.awt.geom.IllegalPathStateException", "java.lang.IllegalStateException", "java.util.IllformedLocaleException", "java.awt.image.ImagingOpException", "java.lang.annotation.IncompleteAnnotationException", "java.lang.IndexOutOfBoundsException", 
			"javax.management.JMRuntimeException", "org.w3c.dom.ls.LSException", "java.lang.reflect.MalformedParameterizedTypeException", "javax.lang.model.type.MirroredTypesException", "java.util.MissingResourceException", "java.lang.NegativeArraySizeException", 
			"java.util.NoSuchElementException", "javax.xml.crypto.NoSuchMechanismException", "java.lang.NullPointerException", "java.awt.color.ProfileDataException", "java.security.ProviderException", "java.nio.file.ProviderNotFoundException", "java.awt.image.RasterFormatException", 
			"java.util.concurrent.RejectedExecutionException", "java.lang.RuntimeException", "java.lang.SecurityException", "org.omg.CORBA.SystemException", "javax.xml.bind.TypeConstraintException", "java.lang.TypeNotPresentException", "java.lang.reflect.UndeclaredThrowableException", 
			"javax.lang.model.UnknownEntityException", "javax.print.attribute.UnmodifiableSetException", "java.lang.UnsupportedOperationException", "javax.xml.ws.WebServiceException", "java.lang.invoke.WrongMethodTypeException");
	
	List<String> importDeclarations = new ArrayList<String>();
	
	
	public ExceptionsVisitor(ModelSourceFile file) {
		super(file);
	}
	
	/**
	 * When visiting a TryStatement, add the statement to our tryStatements
	 *   array.
	 */
	public boolean visit(TryStatement node) {
		
		String sig = signatureOfParent(node);		
		for (Object statement : node.getBody().statements()){
				String stmt = statement.toString();
				String tryLine = stmt.substring(0, stmt.indexOf("\n"));
				if (tryLine != "" && tryLine != "\n" && tryLine != "\t"){
					String tryBlock = sig + CHECK_SEPERATOR + tryLine;
					tryStatements.add(tryBlock);
					System.out.println("try statement found!");
				}
				break;
		}
		
		
		// Get resources for try-with-resources statement
		String src = findSourceForNode(node);
		String source = src.substring(0, src.indexOf("{"));
		if (source.contains("\n")){
			source = source.substring(0, source.indexOf("\n"));
		}
		if (node.resources() != null){
			for (Object resource : node.resources()){
				String tryResource = sig + CHECK_SEPERATOR + source.trim() + CHECK_SEPERATOR + resource.toString();
				tryWithResources.add(tryResource);
				System.out.println("try with resource found!");
			}
		}
		
		// Check for finally block
		if (node.getFinally() != null){
			String finallyBlock = node.getFinally().toString();
			String[] lines = finallyBlock.split("\n");
			String line = "";
			
			if (lines[0].trim().equals("{") && (!lines[1].trim().equals("\n") || !lines[1].trim().equals("{"))){
				line = lines[1].trim();					
			} else if (!lines[0].trim().equals("{")) {
				line = lines[0].trim();															
			} else {
				
			}
			if (!line.equals("")){
				finallyBlocks.add(sig + CHECK_SEPERATOR + line.trim());
				System.out.println("finally block found!");					
			}
		}
		
		return true;
	}

	public boolean visit (CatchClause node){
		String signature = signatureOfParent(node);
		String except = findSourceForNode(node.getException()).trim();
		String exception = except.substring(0, except.indexOf(" "));
		String catchBlock = node.toString();
		String catchLine = catchBlock.substring(0, catchBlock.indexOf("{"));
		String catchSrc = signature + CHECK_SEPERATOR + catchLine.trim();
		
		catchBlocks.add(catchSrc);
		
		boolean found = false;
		// check import statements
		determineExceptionKind(exception, catchSrc, found);
		
		
		if (node.getException().getType().isUnionType()){
			String e = node.getException().getType().toString();
			String[] exceptions = e.split(Pattern.quote("|"));
			
			for (String s : exceptions){
				found = false;
				determineExceptionKind(s, catchSrc, found);
			}
			
			multiCatchBlocks.add(catchSrc);
		}
		
		return true;
	}

	private void determineExceptionKind(String exception, String catchSrc, boolean found) {
		if (importDeclarations != null){
			for (String i : importDeclarations){
				if (i.contains(exception)){
					addExceptionKind(exception, catchSrc);
					found = true;
				}
			}
		}
		//check for type declarations in this file
		if (exceptionClasses != null){
			for (String c : exceptionClasses){
				if (c.contains(exception)){
					addExceptionKind(exception, catchSrc);
					found = true;
				}
			}
		}
		// the exception is defined elsewhere or is java.lang; assume unchecked
		if (!found){
			uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
		}
		
	}
	
	public boolean visit(ThrowStatement node){
		
		String sig = signatureOfParent(node);

		String src = findSourceForNode(node);
		String throwStatement = sig + CHECK_SEPERATOR + src;
		throwStatements.add(throwStatement);
		
		System.out.println("throw statement found!");
		
		addExceptionKind(((ClassInstanceCreation)node.getExpression()).getType().toString(),throwStatement);
			
		return true;
	}
	
	
	public boolean visit(MethodDeclaration node) {
		
		for (Object except : node.thrownExceptions()){
		
			String exception = except.toString();
			
			//Store more information here??
			String src = findSourceForNode(node);
			String source = src.substring(0, src.indexOf("{"));
			String throwsMethod = node.getName().toString() + CHECK_SEPERATOR + source.trim();
			throwsMethods.add(throwsMethod);
			
			System.out.println("thrown exception found!");	
			
			addExceptionKind(exception, throwsMethod);
		}
		return true;
	}
	
	
	public boolean visit(TypeDeclaration node) {
		
		// check if superclass is an exception
		if (node.getSuperclassType() != null && node.getSuperclassType().toString().contains("Exception")) {
			String superClass = node.getSuperclassType().toString();
			String src = findSourceForNode(node);
			String source = src.substring(0, src.indexOf("{"));
			
			String exceptionClass = node.getName().toString() + CHECK_SEPERATOR + source.trim();
			exceptionClasses.add(exceptionClass);
			//System.out.println("\nexception subclass found!");
			
			System.out.println("exception subclass found!");
			
			addExceptionKind(superClass, exceptionClass);			
		}
		else {
			//System.out.println("");
		}
		
		
		return true;
	}
	
	private void addExceptionKind(String exception, String context) {
		if (unchecked.contains(exception)){
			uncheckedExceptions.add(context + CHECK_SEPERATOR + exception);
		} else {
			checkedExceptions.add(context + CHECK_SEPERATOR + exception);
		}
	}
}
