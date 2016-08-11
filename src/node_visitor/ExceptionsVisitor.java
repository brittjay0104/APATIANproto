package node_visitor;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;

import code_parser.ModelSourceFile;



public class ExceptionsVisitor extends ASTVisitor {
	// Methods that declare "throws"
	List<String> throwsMethods = new ArrayList<String>();
	
	// Try statements
	List<String> tryStatements = new ArrayList<String>();
	
	// Static try statements
	List<String> staticTryStatements = new ArrayList<String>();
	
	// Classes that extend an exception
	List<String> tryWithResources = new ArrayList<String>();
	
	// Static try with resources
	List<String> staticTryWithResources = new ArrayList<String>();
	
	// Catch blocks
	List<String> catchBlocks = new ArrayList<String>();
	
	// Static catch blocks
	List<String> staticCatchBlocks = new ArrayList<String>();
	
	// Multi-catch blocks
	List<String> multiCatchBlocks = new ArrayList<String>();
		
	// Static multi-catch blocks
	List<String> staticMultiCatchBlocks = new ArrayList<String>();
	
	// Finally blocks
	List<String> finallyBlocks = new ArrayList<String>();

	// Static finally blocks
	List<String> staticFinallyBlocks = new ArrayList<String>();
	
	// Throw statements (TBD)
	List<String> throwStatements = new ArrayList<String>();
	
	// Classes that extend an exception
	List<String> exceptionClasses = new ArrayList<String>();
	
	// Checked and Unchecked Exceptions
	List<String> uncheckedExceptions = new ArrayList<String>();
	List<String> checkedExceptions = new ArrayList<String>();
	
	//all exceptions patterns
	List<String> allExceptions = new ArrayList<String>();
	
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
	
	private Initializer getInitializer(ASTNode node) {
		if (node.getParent() != null){
			return node instanceof Initializer ? (Initializer)node : getInitializer(node.getParent());			
		}
		
		return null;
	}
	
	public boolean visit(ImportDeclaration node){
		
		String importStatement = node.getName().getFullyQualifiedName();
		importDeclarations.add(importStatement);
		
		return true;
	}
	
	
	// Get throwsMethods
	public List<String> getThrowsMethods() {
		return throwsMethods;
	}
	
	// Get tryStatements
	public List<String> getTryStatements() {
		return tryStatements;
	}
	
	// Get static tryStatements
	public List<String> getStaticTryStatements(){
		return staticTryStatements;
	}
	
	// Get try-with-resources statements
	public List<String> getTryWithResourceStatements() {
		return tryWithResources;
	}
	
	// Get static try-with-resources statements
	public List<String> getStaticTryWithResourceStatements() {
		return staticTryWithResources;
	}
	
	// Get catch blocks 
	public List<String> getCatchBlocks(){
		return catchBlocks;
	}
	
	// Get static catch blocks 
	public List<String> getStaticCatchBlocks(){
		return staticCatchBlocks;
	}

	// Get multi catch blocks 
	public List<String> getMultiCatchBlocks(){
		return multiCatchBlocks;
	}
	
	// Get static multi catch blocks 
	public List<String> getStaticMultiCatchBlocks(){
		return staticMultiCatchBlocks;
	}
	
	// Get try-with-resources statements
	public List<String> getFinallyBlock() {
		return finallyBlocks;
	}
	
	// Get static try-with-resources statements
	public List<String> getStaticFinallyBlock() {
		return staticFinallyBlocks;
	}
	
	// Get throwStatements (TBD)
	public List<String> getThrowStatements() {
		return throwStatements;
	}
	
	// Get exceptionClasses
	public List<String> getExceptionClasses() {
		return exceptionClasses;
	}
	
	public List<String> getUncheckedExceptions(){
		return uncheckedExceptions;
	}
	
	public List<String> getCheckedExceptions(){
		return checkedExceptions;
	}
	
	/**
	 * When visiting a TryStatement, add the statement to our tryStatements
	 *   array.
	 */
	public boolean visit(TryStatement node) {
		
		// check for nested try statements??
		if (node.getParent() instanceof TryStatement){
			
		}

		Initializer init = getInitializer(node);
		MethodDeclaration md = getMethodDeclaration(node);
		
		// check for try/catch in static block		
		if (init != null){
			List inits = init.modifiers();
			StringBuffer sb = new StringBuffer();
			for (Object i : inits){
				sb.append(i.toString());
			}
			
			String initializer = sb.toString();
			
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
				String tryBlock = initializer + CHECK_SEPERATOR + tryLine;
				staticTryStatements.add(tryBlock);				
			}
			
			// Get resources for try-with-resources statement
			if (node.resources() != null){
				for (Object resource : node.resources()){
					String source = src.substring(0, src.indexOf("{"));
					if (source.contains("\n")){
						source = source.substring(0, source.indexOf("\n"));
					}
					String tryResource = initializer + CHECK_SEPERATOR + source.trim() + CHECK_SEPERATOR + resource.toString();
					staticTryWithResources.add(tryResource);
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
					staticFinallyBlocks.add(initializer + CHECK_SEPERATOR + line.trim());
					System.out.println("finally block found!");					
				}
			}
			
			//System.out.println("try statement found!");
		}
		
		if (md != null) {
			
			String methodName = md.getName().toString();
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
				String tryBlock = methodName + CHECK_SEPERATOR + tryLine;
				tryStatements.add(tryBlock);					
			}
			
			
			// Get resources for try-with-resources statement
			if (node.resources() != null){
				for (Object resource : node.resources()){
					String source = src.substring(0, src.indexOf("{"));
					if (source.contains("\n")){
						source = source.substring(0, source.indexOf("\n"));
					}
					String tryResource = methodName + CHECK_SEPERATOR + source.trim() + CHECK_SEPERATOR + resource.toString();
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
					finallyBlocks.add(methodName + CHECK_SEPERATOR + line.trim());
					System.out.println("finally block found!");					
				}
			}
			
			System.out.println("try statement found!");
		}
		return true;
	}



	public boolean visit (CatchClause node){
		Initializer init = getInitializer(node);
		MethodDeclaration md = getMethodDeclaration(node);
		
		if (init != null){
			List inits = init.modifiers();
			StringBuffer sb = new StringBuffer();
			for (Object i : inits){
				sb.append(findSourceForNode((ASTNode)i));
			}
			
			String initializer = sb.toString();
			String except = findSourceForNode(node.getException()).trim();
			String exception = except.substring(0, except.indexOf(" "));
			String catchBlock = node.toString();
			String catchLine = catchBlock.substring(0, catchBlock.indexOf("{"));
			
			String catchSrc = initializer + CHECK_SEPERATOR + catchLine.trim();
			
			staticCatchBlocks.add(catchSrc);
			
			// check import statements
			boolean found = false;
			// check import statements (java.lang or whatever if name matches)
			if (importDeclarations != null){
				for (String i : importDeclarations){
					// if the exception is found in one of the import statements, put in proper list based on that
					if (i.contains(exception)){
						if (unchecked.contains(i)){
							uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						} else {
							checkedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						}
					}
				}
			} 
			//check for type declarations in this file
			if (exceptionClasses != null){
				for (String c : exceptionClasses){
					// if the exception was declared in the file
					if(c.contains(exception)){
						if (unchecked.contains(exception)){
							uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						} else {
							checkedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						}
					}
				}
				
			}
			if (!found){
				uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
			}
			
			// get exceptions in multi-catch			
			if (node.getException().getType().isUnionType()){
				String e = node.getException().getType().toString();
				
				String[] exceptions = e.split(Pattern.quote("|"));
				
				for (String s: exceptions){
					found = false;
					// check import statements (java.lang or whatever if name matches)
					if (importDeclarations != null){
						for (String i : importDeclarations){
							// if the exception is found in one of the import statements, put in proper list based on that
							if (i.contains(s)){
								if (unchecked.contains(i)){
									uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								} else {
									checkedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								}
							}
						}
					} 
					//check for type declarations in this file
					if (exceptionClasses != null){
						for (String c : exceptionClasses){
							// if the exception was declared in the file
							if(c.contains(s)){
								if (unchecked.contains(s)){
									uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								} else {
									checkedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								}
							}
						}
						
					} 
					if (!found){
						// the exception is defined elsewhere or is java.lang; assume unchecked
						uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
					}
				}
				staticMultiCatchBlocks.add(catchSrc);
			}
		}

		if (md != null){
			String methodName = md.getName().toString();
			String except = node.getException().toString().trim();
			String exception = except.substring(0, except.indexOf(" "));
			String catchBlock = node.toString();
			String catchLine = catchBlock.substring(0, catchBlock.indexOf("{"));
			
			String catchSrc = methodName + CHECK_SEPERATOR + catchLine.trim();
			
			catchBlocks.add(catchSrc);
			
			boolean found = false;
			// check import statements (java.lang or whatever if name matches)
			if (importDeclarations != null){
				for (String i : importDeclarations){
					// if the exception is found in one of the import statements, put in proper list based on that
					if (i.contains(exception)){
						if (unchecked.contains(i)){
							uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						} else {
							checkedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						}
					}
				}
			} 
			//check for type declarations in this file
			if (exceptionClasses != null){
				for (String c : exceptionClasses){
					// if the exception was declared in the file
					if(c.contains(exception)){
						if (unchecked.contains(exception)){
							uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						} else {
							checkedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
							found = true;
						}
					}
				}
				
			} 
			if (!found){
				// the exception is defined elsewhere or is java.lang; assume unchecked
					uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + exception);
			}
				
			
			if (node.getException().getType().isUnionType()){
				String e = node.getException().getType().toString();
				
				String[] exceptions = e.split(Pattern.quote("|"));
				
				for (String s: exceptions){
					found = false;
					// check import statements (java.lang or whatever if name matches)
					if (importDeclarations != null){
						for (String i : importDeclarations){
							// if the exception is found in one of the import statements, put in proper list based on that
							if (i.contains(s)){
								if (unchecked.contains(i)){
									uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								} else {
									checkedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								}
							}
						}
					} 
					//check for type declarations in this file
					if (exceptionClasses != null){
						for (String c : exceptionClasses){
							// if the exception was declared in the file
							if(c.contains(s)){
								if (unchecked.contains(s)){
									uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								} else {
									checkedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
									found = true;
								}
							}
						}
						
					} 
					if (!found){
						// the exception is defined elsewhere or is java.lang; assume unchecked
						uncheckedExceptions.add(catchSrc + CHECK_SEPERATOR + s);
					}
				}
				multiCatchBlocks.add(catchSrc);
			} 
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



	/*
	 * How many interesting things did you find?
	 */
	public int findings() {
		return 	throwsMethods.size() + 
				tryStatements.size() + 
				tryWithResources.size() +
				staticTryStatements.size() + 
				staticTryWithResources.size() + 
				catchBlocks.size() + 
				staticCatchBlocks.size() + 
				multiCatchBlocks.size() + 
				staticMultiCatchBlocks.size() + 
				finallyBlocks.size() + 
				staticFinallyBlocks.size() + 
				throwStatements.size() + 
				exceptionClasses.size() + 
				uncheckedExceptions.size() + 
				checkedExceptions.size();
	}
	
	/*
	 * Did I find the right interesting things?
	 */
	public List<String> fullFindings(){
		List<String> findings = new ArrayList<String>();
		
		if (throwsMethods != null){
			findings.addAll(throwsMethods);
		} 
		if (tryStatements != null){
			findings.addAll(tryStatements);
		} 
		if (tryWithResources != null){
			findings.addAll(tryWithResources);
		}
		if (staticTryStatements != null){
			findings.addAll(staticTryStatements);
		} 
		if (staticTryWithResources != null){
			findings.addAll(staticTryWithResources);
		}
		if (catchBlocks != null){
			findings.addAll(catchBlocks);
		} 
		if (staticCatchBlocks != null){
			findings.addAll(staticCatchBlocks);
		} 
		if (multiCatchBlocks != null){
			findings.addAll(multiCatchBlocks);
		} 
		if (staticMultiCatchBlocks != null){
			findings.addAll(staticMultiCatchBlocks);
		} 
		if (finallyBlocks != null){
			findings.addAll(finallyBlocks);
		} 
		if (staticFinallyBlocks != null){
			findings.addAll(staticFinallyBlocks);
		}
		if (throwStatements != null){
			findings.addAll(throwStatements);
		} 
		if (exceptionClasses != null){
			findings.addAll(exceptionClasses);
		}
		if (uncheckedExceptions != null){
			findings.addAll(uncheckedExceptions);
		} 
		if (checkedExceptions != null){
			findings.addAll(checkedExceptions);
		}
		
		return findings;
	}
}
