package node_visitor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
	private List<String> unchecked = Arrays.asList("AnnotationTypeMismatchException", "ArithmeticException", "ArrayStoreException", "BufferOverflowException", "BufferUnderflowException", "CannotRedoException", "CannotUndoException", "ClassCastException", "CMMException"
			, "ConcurrentModificationException", "DataBindingException", "DOMException", "EmptyStackException", "EnumConstantNotPresentException", "EventException", "FileSystemAlreadyExistsException", "FileSystemNotFoundException", "IllegalArgumentException"
			, "IllegalMonitorStateException", "IllegalPathStateException", "IllegalStateException", "IllformedLocaleException", "ImagingOpException", "IncompleteAnnotationException", "IndexOutOfBoundsException", "JMRuntimeException", "LSException", "MalformedParameterizedTypeException"
			, "MirroredTypesException", "MissingResourceException", "NegativeArraySizeException", "NoSuchElementException", "NoSuchMechanismException", "NullPointerException", "ProfileDataException", "ProviderException", "ProviderNotFoundException", "RasterFormatException"
			, "RejectedExecutionException", "RuntimeException", "SecurityException", "SystemException", "TypeConstraintException", "TypeNotPresentException", "UndeclaredThrowableException", "UnknownEntityException", "UnmodifiableSetException", "UnsupportedOperationException"
			, "WebServiceException", "WrongMethodTypeException");
	
	public ExceptionsVisitor(ModelSourceFile file) {
		super(file);
	}
	
	/**
	 * When visiting a TryStatement, add the statement to our tryStatements
	 *   array.
	 */
	public boolean visit(TryStatement node) {
		
		String sig = signatureOfParent(node);		
		String src = findSourceForNode(node);
		
		List statements = node.getBody().statements();
		String tryLine = "";
		// has nested try
		for (Object statement : statements){
			if (statement != null){
				String stmt = statement.toString();
				tryLine = stmt.substring(0, stmt.indexOf("\n"));
				break;
			}
		}
		
		if (tryLine != "" && tryLine != "\n" && tryLine != "\t"){
			String tryBlock = sig + CHECK_SEPERATOR + tryLine;
			tryStatements.add(tryBlock);
			System.out.println("try statement found!");
		}
		
		
		// Get resources for try-with-resources statement
		if (node.resources() != null){
			for (Object resource : node.resources()){
				String source = src.substring(0, src.indexOf("{"));
				if (source.contains("\n")){
					source = source.substring(0, source.indexOf("\n"));
				}
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
		
		if (except.contains("|")){
			String[] exceptions = except.split(Pattern.quote("|"));
			
			for (String s: exceptions){
				for (String e : uncheckedExceptions){
					if (s.contains(e)){
						uncheckedExceptions.add("unchecked" + CHECK_SEPERATOR + catchSrc);
					}
				}
				
				for (String e : checkedExceptions){
					if (s.contains(e)){
						checkedExceptions.add("checked" + CHECK_SEPERATOR + catchSrc);
					}
				}
			}
		}
		
		//TODO why add "unchecked/checked" here and not later?
		if (unchecked.contains(exception)){
			uncheckedExceptions.add("unchecked" + CHECK_SEPERATOR + catchSrc);
		} else {
			checkedExceptions.add("checked" + CHECK_SEPERATOR + catchSrc);
		}
		
		if (node.getException().getType().isUnionType()){
			multiCatchBlocks.add(catchSrc);
		}
		
		return true;
	}
	
	public boolean visit(ThrowStatement node){
		MethodDeclaration md = getMethodDeclaration(node);
		
		if (md != null) {
			String src = findSourceForNode(node);
			String throwStatement = md.getName().toString() + CHECK_SEPERATOR + src;
			throwStatements.add(throwStatement);
			
			System.out.println("throw statement found!");
			
			for (String e : unchecked){
				if (src.contains(e)){
					uncheckedExceptions.add("unchecked" + CHECK_SEPERATOR + throwStatement);
				}
			}
			
			if (!unchecked.contains(throwStatement)){
				checkedExceptions.add("checked" + CHECK_SEPERATOR + throwStatement);
			}
			
		}
		return true;
	}
	
	
	public boolean visit(MethodDeclaration node) {
		//checking for thrown exceptions
		
		MethodDeclaration md = getMethodDeclaration(node);
		
		if(md != null){
			List thrownExceptions = node.thrownExceptions();
			if(!thrownExceptions.isEmpty()){
				
				for (Object except : thrownExceptions){
					String exception = except.toString();
					
					//Store more information here??
					String src = findSourceForNode(node);
					String source = src.substring(0, src.indexOf("{"));
					String throwsMethod = md.getName().toString() + CHECK_SEPERATOR + source.trim();
					throwsMethods.add(throwsMethod);
					
					System.out.println("thrown exception found!");	
					
					if (unchecked.contains(exception)){
						uncheckedExceptions.add("unchecked" + CHECK_SEPERATOR + throwsMethod);
					} else {
						checkedExceptions.add("checked" + CHECK_SEPERATOR + throwsMethod);
					}
				}
				
			}
			
		}
		return true;
	}
	
	
	public boolean visit(TypeDeclaration node) {
		//System.out.print(node.getName().toString());
		
		MethodDeclaration md = getMethodDeclaration(node);
		
		// check if superclass is an exception
		if (node.getSuperclassType() != null && node.getSuperclassType().toString().contains("Exception")) {
			String superClass = node.getSuperclassType().toString();
			String src = findSourceForNode(node);
			String source = src.substring(0, src.indexOf("{"));
			
			String exceptionClass = node.getName().toString() + CHECK_SEPERATOR + source.trim();
			exceptionClasses.add(exceptionClass);
			//System.out.println("\nexception subclass found!");
			
			System.out.println("exception subclass found!");
			
			// check if a checked or unchecked exception
			if (unchecked.contains(superClass)){
				uncheckedExceptions.add("unchecked" + CHECK_SEPERATOR + exceptionClass);
			} else {
				checkedExceptions.add("checked" + CHECK_SEPERATOR + exceptionClass);
			}
			
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
	
	private String signatureOfParent(ASTNode node) {
		Initializer init = getInitializer(node);
		MethodDeclaration md = getMethodDeclaration(node);
		
		String signature;
		if (md != null){
			signature = md.getName().toString();
		}else if (init != null){
			StringBuffer sb = new StringBuffer();
			for (Object i : init.modifiers()){
				sb.append(findSourceForNode((ASTNode)i));
			}
			signature = sb.toString();
		}else{
			throw new RuntimeException("Unknown container for catch clause!");
		}
		return signature;
	}
}
