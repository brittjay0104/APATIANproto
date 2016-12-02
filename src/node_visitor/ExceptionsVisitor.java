package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;

import code_parser.ModelSourceFile;

/**
 * This class collects Exception related information from a source file.
 * Specifically we collect following information :
 * <ul>
 * <li>Methods that <code>throw</code> exceptions </li>
 * <li>Information about <code>try</code> statements </li>
 * <li>Information about <code>try with resources</code> statements </li>
 * <li>Information about <code>catch</code> blocks </li>
 * <li>Information about multi-<code>catch</code> blocks </li>
 * <li>Information about <code>finally</code> blocks </li>
 * <li>Information about <code>throws</code> statements </li>
 * <li>Information about <code>Exception</code> classes </li>
 * <li>Information about checked <code>Exceptions</code> </li>
 * <li>Information about unchecked <code>Exceptions</code> </li>
 * <li>Information about caught <code>Exceptions</code> </li>s		
 * </ul>
 * @author ??
 *
 */
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
						checkedExceptions = new LinkedList<>(),
						catchExceptions = new LinkedList<>();
	
	public List<MethodDeclaration> allMethodDeclarations = new LinkedList<>();
	
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
	
//	public void preVisit (ASTNode node) {
//		if (node instanceof MethodDeclaration){
//			allMethodDeclarations.add((MethodDeclaration)node);
//		}
//	}
	
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
					//System.out.println("try statement found!");
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
				//System.out.println("try with resource found!");
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
				//System.out.println("finally block found!");					
			}
		}
		
		return true;
	}

	public boolean visit (CatchClause node){
		String signature = signatureOfParent(node);
		String catchBlock = node.toString();
		String catchLine = catchBlock.substring(0, catchBlock.indexOf("{"));
		String catchSrc = signature + CHECK_SEPERATOR + catchLine.trim();
		
		
		catchBlocks.add(catchSrc);
		
		IVariableBinding vb = node.getException().resolveBinding();
		String exception = vb.getType().getQualifiedName();
		if (!catchSrc.contains("|")){
			determineExceptionKind(exception, catchSrc);
			String e = exception.substring(exception.lastIndexOf("."), exception.length());
			if (e.equals("Exception")){
				catchExceptions.add(catchSrc);
			}
		}

		if (node.getException().getType().isUnionType()){
			vb = node.getException().resolveBinding();
			UnionType t = (UnionType) node.getException().getType();
			List<?> types = t.types();
			
			for (Object type: types){
				SimpleType e = (SimpleType)type;
				String except = e.resolveBinding().getQualifiedName();
				determineExceptionKind(except, catchSrc);
			}
			
			multiCatchBlocks.add(catchSrc);
		}
		
		return true;
	}

	private void determineExceptionKind(String exception, String source) {
		boolean found = false;
		if (importDeclarations != null){
			for (String i : importDeclarations){
				if (i.contains(exception)){
					addExceptionKind(exception, source);
					found = true;
				}
			}
		}
		//check for type declarations in this file
		if (exceptionClasses != null){
			for (String c : exceptionClasses){
				exception = exception.substring(exception.lastIndexOf("."), exception.length());
				if (c.contains(exception)){
					addExceptionKind(exception, source);
					found = true;
				}
			}
		}
		// the exception is defined elsewhere or is java.lang; assume unchecked
		if (!found){
			for (String uc : unchecked){
				if (uc.equals(exception)){
					uncheckedExceptions.add(source + CHECK_SEPERATOR + exception);
					found = true;
				}
			}
			if (!found){
				checkedExceptions.add(source + CHECK_SEPERATOR + exception);
			}
		}
		
	}
	
	public boolean visit(ThrowStatement node){
		
		String sig = signatureOfParent(node);

		String src = findSourceForNode(node);
		String throwStatement = sig + CHECK_SEPERATOR + src;
		throwStatements.add(throwStatement);
		
		//System.out.println("throw statement found!");
		
		Expression n = node.getExpression();

		ITypeBinding tb = n.resolveTypeBinding();
		if (tb.getQualifiedName().contains(".")){
			String name = tb.getQualifiedName();
			determineExceptionKind(name, throwStatement);
		} else {
			String name = tb.getSuperclass().getQualifiedName();
			determineExceptionKind(name, throwStatement);
		}
			
		return true;
	}
	
	
	public boolean visit(MethodDeclaration node) {
		String src = findSourceForNode(node);
		String source = "";
		if (src.contains("{")){
			source = src.substring(0, src.indexOf("{"));			
		} else {
			source = src;
		}
		String throwsMethod = node.getName().toString() + CHECK_SEPERATOR + source.trim();
		
		IMethodBinding mb = node.resolveBinding();
		
		if (mb != null){
			ITypeBinding[] types = mb.getExceptionTypes();
			if (types.length > 0){
				throwsMethods.add(throwsMethod);			
			}
			
			for (ITypeBinding t : types){
				String name = t.getSuperclass().getQualifiedName();
				determineExceptionKind(name, throwsMethod);
			}
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
			
			//System.out.println("exception subclass found!");
			
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
