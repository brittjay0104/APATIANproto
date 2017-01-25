package code_parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import developer_creator.RunAnalysis;
import node_visitor.ExceptionsVisitor;
import node_visitor.GenericsVisitor_2;
import node_visitor.Log4JVisitor;
import node_visitor.NODP;
import node_visitor.NODP_Visitor;
import node_visitor.NPE_Visitor;
import node_visitor.NoNullCheckVisitor;
import node_visitor.NullCheckVisitor;
import node_visitor.StringMethodVisitor;
import node_visitor.VariableData;
import node_visitor.VariableData.VariableType;
import node_visitor.VariablesVisitor;

public class ModelParser {

	List<String> expressions;
	List<String> ifAndNext;
	List<String> nullVariables;
	List<String> nullFields;
	List<String> directories = new ArrayList<String>();

	/**
	 * Creates new ModelParser (and list of methods/startPositions for comparison after each
	 * analysis)
	 */
	public ModelParser() {
		expressions = new ArrayList<String>();
		ifAndNext = new ArrayList<String>();
		nullVariables = new ArrayList<String>();
		nullFields = new ArrayList<String>();

	}
	
	public void printSomething(){
		System.out.println("I'm happy :)");
	}


	/**
	 *
	 * Parses a source file for null checks and stores each unique null check found
	 * (with method it was found in and the start position) into an ongoing list, saved
	 *  with the file that was passed in, as well as in the list for null checks found in this
	 *  current revision.
	 *
	 * @param file - ModelSourceFile that is being parsed for null checks
	 * @param commit - String of the revision hash for the current revision of the file
	 * being parsed
	 *
	 * @return List of null check objects created during parsing
	 * @throws IOException
	 */
	public List<String> parseForNull(ModelSourceFile file, String commit) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());

		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(src.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);


		NullCheckVisitor visitor = new NullCheckVisitor(file);
		cu.accept(visitor);

		// add to file's list of null checks (if not already there)
		//ArrayList<String> fileNullChecks = file.getNullChecks();

		//System.out.println("********Null checks found in " + file.getName() + "********");
		
		for (String check: visitor.getNullChecks()){
			//System.out.println(check);
			file.addNullCheck(check);
//			if (file.hasThisCheck(check) == false){
//			}
		}

		ArrayList<String> methods = file.getNullMethods();
		setIfAndNext(visitor.getIfAndNext());
		
		parseForNoNullCheck(file);

		return ifAndNext;

	}
	
	
	public Map<String,Integer> parseForMethodInvocations(ModelSourceFile file) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());

		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(src.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		StringMethodVisitor visitor = new StringMethodVisitor(file);
		cu.accept(visitor);
		
		Map<String, Integer> methods = visitor.getMethods();

		Map<String, Integer> fileMethods = file.getMethodInvocs();
		
		for (Map.Entry<String, Integer> entry: methods.entrySet()){
			String key = entry.getKey();
			Integer count = entry.getValue();
			
			file.addMethodInvoc(key, count);
		}
		
		
		return methods;		
		
	}
	

	public void parseForNoNullCheck(ModelSourceFile file) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());
		
		 Map options = JavaCore.getOptions();
		 JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		 parser.setCompilerOptions(options);

		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(src.toCharArray());
		parser.setUnitName(file.getName());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		NoNullCheckVisitor visitor = new NoNullCheckVisitor(file);
		cu.accept(visitor);

		// store variables, fields, assignments with file
		
		transferNullFields(file, visitor);

		transferNullVariables(file, visitor);		
		
		transferNullAssignments(file, visitor);


	}

	public List<String> parseForNODP(ModelSourceFile file, String commit) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());
		
		 Map options = JavaCore.getOptions();
		 JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		 parser.setCompilerOptions(options);

		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(src.toCharArray());
		parser.setUnitName(file.getName());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		NODP_Visitor visitor = new NODP_Visitor(file, commit);
		cu.accept(visitor);
		// NODP only 
						
		// built up from file to preserve counts
		List<String> nodp = visitor.getNODPs();
		
		
		for (String dp: nodp){
			
			//System.out.println("NODP --> " + dp);
			
			file.addNODP(dp);
		}
		
		
		return nodp;
	}
	
	public void parseForGenerics(ModelSourceFile file) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());
		
		 Map options = JavaCore.getOptions();
		 JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		 parser.setCompilerOptions(options);

		 
		 parser.setResolveBindings(true);
		 parser.setStatementsRecovery(true);
		 parser.setSource(src.toCharArray());
		 parser.setUnitName(file.getName());
		 parser.setKind(ASTParser.K_COMPILATION_UNIT);

		 CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		 GenericsVisitor_2 visitor = new GenericsVisitor_2(file);
		 cu.accept(visitor);
		
		 List<String> allGenerics = visitor.getAllGenerics();
		 HashMap<String, List<String>> generics = visitor.getGenerics();
		 
		 for (String s: allGenerics){
		 	//System.out.println("Generics usage --> " + s);
		 	file.addGeneric(s);
		 }
	
		 // assign hashmaps to file 
		
		 file.setGenerics(generics);
		 

//		Iterator  sfit = file.getSimpleGenerics().entrySet().iterator();
//		
//		while (sfit.hasNext()){
//			Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>)sfit.next();
//			
//			System.out.println("***********Simple Generics under " + pair.getKey() + " category" + file.getName() + "***********");
//			
//			for (String s: pair.getValue()){
//				System.out.println(s);
//			}
//			
//		}
//				
//		Iterator afit = file.getAdvancedGenerics().entrySet().iterator();
//		
//		while (afit.hasNext()){
//			Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>)afit.next();
//			
//			System.out.println("***********Advanced Generics under " + pair.getKey() + " category" + file.getName() + "***********");
//			
//			for (String s: pair.getValue()){
//				System.out.println(s);
//			}			
//		}

	}
	
	//parseForExceptions
	public void parseForExceptions(ModelSourceFile file, String directory, String jarFile) throws IOException{
		
		//System.out.println("IN parseForExceptions");
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		parser.setBindingsRecovery(true);

		String unitName = file.getName();
		parser.setUnitName(unitName);
		
		// can I do file.getSourceFile().getPath()?
		String [] sources = {directory};
		
		String[] classpath = {jarFile};
		
		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		file.setSource(src.toCharArray());
		
		Map options = JavaCore.getOptions();
		//JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		parser.setCompilerOptions(options);

		parser.setSource(src.toCharArray());


		CompilationUnit cu = (CompilationUnit) parser.createAST(null);			
		ExceptionsVisitor visitor = new ExceptionsVisitor(file);
		cu.accept(visitor);
	
//		if (cu.getAST().hasBindingsRecovery()){
//			System.out.println("it worked!");
//		}
		
		
		//System.out.println("reaching changed part");
		
		for (String throwsMethod : visitor.throwsMethods){
			//System.out.println("Method throws --> " + throwsMethod);
			file.addThrowsMethod(throwsMethod);
		}
		
		for (String tryStatement : visitor.tryStatements){
			//System.out.println("Try statement --> " + tryStatement);
			file.addTryStatement(tryStatement);
		}
		
		for (String catchBlock : visitor.catchBlocks){
			//System.out.println("Catch block --> " + catchBlock);
			file.addCatchBlock(catchBlock);
		}
		
		for (String catchBlock : visitor.multiCatchBlocks){
			//System.out.println("Catch block --> " + catchBlock);
			file.addMultiCatchBlock(catchBlock);
		}
		
		for (String tryWithResource : visitor.tryWithResources){
			//System.out.println("Try with resource --> " + tryWithResource);
			file.addTryWithResource(tryWithResource);
		}
		
		for (String finallyBlock : visitor.finallyBlocks){
			//System.out.println("Finally block --> " + finallyBlock);
			file.addFinallyBlock(finallyBlock);
		}
		
		for (String throwStatement : visitor.throwStatements){
			//System.out.println("Throw statement --> " + throwStatement);
			file.addThrowStatement(throwStatement);
		}
		
		for (String exceptionClass : visitor.exceptionClasses){
			//System.out.println("Exception class --> " + exceptionClass);
			file.addExceptionClass(exceptionClass);
		}
		
		
		for (String checkedException : visitor.checkedExceptions){
			//System.out.println("Checked exception --> " + checkedException);
			file.addCheckedException(checkedException);
		}
		
		for (String uncheckedException : visitor.uncheckedExceptions){
			//System.out.println("Unchecked exception --> " + uncheckedException);
			file.addUncheckedException(uncheckedException);
		}
		
		for (String catchException : visitor.catchExceptions){
			file.addCatchException(catchException);
		}
//

	}
	
	public void parseForVariables(ModelSourceFile file, String directory, String jarFile) throws IOException{
		//System.out.println("IN parseForExceptions");
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		parser.setBindingsRecovery(true);

		String unitName = file.getName();
		parser.setUnitName(unitName);
		
		// can I do file.getSourceFile().getPath()?
		String [] sources = {directory};
		
		String[] classpath = {jarFile};
		
		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		file.setSource(src.toCharArray());
		
		Map options = JavaCore.getOptions();
		//JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		parser.setCompilerOptions(options);

		parser.setSource(src.toCharArray());


		CompilationUnit cu = (CompilationUnit) parser.createAST(null);			
		VariablesVisitor visitor = new VariablesVisitor(file);
		cu.accept(visitor);
		
		for (VariableData data: visitor.getAllVariableData()){
			// separate out the data being collected (see list)
			
			//primitive data types
			if (data.getIsPrimitive() == true){
				file.addPrimitive(data);
			}
			
			// non-primitive data types
			if (data.getIsPrimitive() == false){
				file.addNonPrimitives(data);
			}
			
			// fields
			if (data.getVariableType() == VariableType.FIELD){
				file.addField(data);
			}
			
			// local variables
			if (data.getVariableType() == VariableType.LOCAL){
				file.addLocalVariable(data);
			}
			
			// parameters
			if (data.getVariableType() == VariableType.PARAMETER){
				file.addParameter(data);
			}
			
			// public
			if (data.getModifiers() == Modifier.PUBLIC){
				file.addPublicVariable(data);
			}
			
			// private
			if (data.getModifiers() == Modifier.PRIVATE){
				file.addPrivateVariable(data);
			}			
			// protected
			if (data.getModifiers() == Modifier.PROTECTED){
				file.addProtectedVariable(data);
			}	
			
			// static
			if (data.getModifiers() == Modifier.STATIC){
				file.addStaticVariable(data);
			}
			
			// final
			if (data.getModifiers() == Modifier.FINAL){
				file.addFinalVariable(data);
			}
			
			// transient
			if (data.getModifiers() == Modifier.TRANSIENT){
				file.addTransientVariable(data);
			}
			
			// volatile
			if (data.getModifiers() == Modifier.VOLATILE){
				file.addVolatileVariable(data);
			}
			
		}
		
	}

	public void findSubDir(String dir){
		File file = new File(dir);
		String[] directories = file.list();
		
		//System.out.println(new File(dir + directories[0]).isDirectory());
		
		
		for(String name : directories){
			//System.out.println(dir + name);
			//System.out.println(new File(dir + name).isDirectory());
			if(new File(dir + name).isDirectory()){
				//System.out.println(dir + name);
				//System.out.println("calling findSubDir on " + dir + name + "\\");
				findSubDir(dir + name + "\\");
				this.directories.add(dir + name);
				
			}
		}
		
		return;
	}
	
	public boolean containsField(List<NODP> currNodp, String field, String type){
		
		for (NODP n: currNodp){
			if (n.getField().equals(field) && n.getType().equals(type)){
				return true;
			}
		}
		
		return false;
	}
	
	public ArrayList<List<String>> parseForNPEAvoidance(ModelSourceFile file) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());
		
		 Map options = JavaCore.getOptions();
		 JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		 parser.setCompilerOptions(options);

		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(src.toCharArray());
		parser.setUnitName(file.getName());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		NPE_Visitor visitor = new NPE_Visitor(file);
		cu.accept(visitor);
		// includes Collections, Optional, and Catch Blocks 
		
		List<String> colls = visitor.getCollVars();
		List<String> opts = visitor.getOptVars();
		List<String> catches = visitor.getCatchBlocks();
		
		ArrayList<List<String>> allVars = new ArrayList<>();
		allVars.add(colls);
		allVars.add(opts);
		allVars.add(catches);
		
		//System.out.println("********Collections usage found in " + file.getName() + "********");
		for (String coll:colls){
			//System.out.println(coll);
			file.addCollVar(coll);
		}
		
		//System.out.println("********Optional Usage found in " + file.getName() + "********");
		for (String opt:opts){
			//System.out.println("Optional usage -- " + opt);
			file.addOptVar(opt);
		}
			
		//System.out.println("********Catch Blocks found in " + file.getName() + "********");
		for (String c:catches){
			//System.out.println("Catch block -- " + c);
			file.addCatchBlock(c);
		}
		
		return allVars;
	}

	private void transferNullAssignments(ModelSourceFile file,
			NoNullCheckVisitor visitor) {
		HashMap<String, ArrayList<String>> assigned = visitor.getNullAssignments();
		Iterator<Entry<String, ArrayList<String>>> it3 = assigned.entrySet().iterator();
		
		while (it3.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it3.next();
			
			String methodDec = pairs.getKey();

			for (String var: pairs.getValue()){
				file.addNullAssignment(methodDec, var);
			}
		}
		
	}


	private void transferNullFields(ModelSourceFile file, NoNullCheckVisitor visitor) {
		HashMap<String, ArrayList<String>> invocs =  visitor.getInvocations();
		Iterator<Entry<String, ArrayList<String>>> it2 = invocs.entrySet().iterator();

		// check all invocations and use to find possible null pointer exceptions
		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it2.next();
			//System.out.println("\n\n\nMethod invocations in " + pairs.getKey() + " ==> ");
			
			String method = pairs.getKey();

			for (String invocation: pairs.getValue()){
				
				file.addInvocation(method, invocation);
				
				// for each invocation, see if it contains the field of interest (need to iterate through them)
				for (String field: visitor.getNullFields()){
					if (!(file.getRemovedFields().contains(field))){
						if (invocation.contains(field)){
							file.addNullField(field);
						}						
					}
				}
				
//				ArrayList<String> nullFields = findNullFields(visitor, invocation);
//
//				for (String field: nullFields){
//					file.addNullField(field);
//					//System.out.println("POTENTIALLY NULL FIELD: " + field);
//				}
			}
		}
		
		
		List<String> assignments = visitor.getAssignments();
		
		for (String assign: assignments){
			file.addAssignment(assign);
		}	
		
		
	}


	private void transferNullVariables(ModelSourceFile file,
			NoNullCheckVisitor visitor) {
		// getting map of potentially null variables and methods they are in
		HashMap<String, ArrayList<String>> vars = visitor.getNullVariables();
		Iterator<Entry<String, ArrayList<String>>> it1 = vars.entrySet().iterator();

		while (it1.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it1.next();

			String methodDec = pairs.getKey();

			for (String var: pairs.getValue()){
				file.addNullVariable(methodDec, var);			}

		}
	}

	// TODO finish testing this LATER (after migrate to Maven and convert into plugin)
	public void foo(ModelSourceFile file) throws Exception {
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("sampleProject");
		project.create(null);
		project.open(null);
		
		//set the Java nature
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		
		//create the project
		project.setDescription(description, null);
		IJavaProject javaProject = JavaCore.create(project);
		
		//set the build path
		IClasspathEntry[] buildPath = {
				JavaCore.newSourceEntry(project.getFullPath().append("src")) };
		
		javaProject.setRawClasspath(buildPath, project.getFullPath().append(
				"bin"), null);
		
		//create folder by using resources package
		IFolder folder = project.getFolder("src");
		folder.create(true, true, null);
		
		//Add folder to Java element
		IPackageFragmentRoot srcFolder = javaProject
				.getPackageFragmentRoot(folder);
		
		//create package fragment
		IPackageFragment fragment = srcFolder.createPackageFragment(
				"com.programcreek", true, null);
		
		//init code string and create compilation unit
		String str = "package com.programcreek;" + "\n"
				+ "public class Test  {" + "\n" + " private boolean foo() {return Boolean.FALSE;}"
				+ "\n" + "}";
		
		ICompilationUnit icu = fragment.createCompilationUnit("Test.java", str, false, null);
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setUnitName("Test.java");
		parser.setProject(javaProject);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		//System.out.println(cu.getJavaElement());
		
		
		NoNullCheckVisitor visitor = new NoNullCheckVisitor(file);
		cu.accept(visitor);
	}

	// TODO maybe don't need this method (seeing if initialized but really should just see if checked for null before use)
	private ArrayList<String> findNullFields(NoNullCheckVisitor visitor, String invocation) {
		ArrayList<String> nullFields = new ArrayList<String>();

		if (invocation != null){
			String field = isPotentiallyNullField(invocation, visitor.getNullFields());
			if ( field != null){
				List<String> assignments = visitor.getAssignments();

				// determines if field was initialized; if not, possible NPE (value deduction)
				if (isInitialized(field, assignments) == false){

					//System.out.println("DEDUCT FROM KNOWLEDGE VALUE --> POSSIBLE NULL POINTER EXCEPTION --> " + field);

					this.nullFields.add(field);
					nullFields.add(field);


				}
			}


			//System.out.println("			" + invocation);
		}
		return nullFields;

	}

	public List<String> getNullFields(){
		return nullFields;
	}

	public List<String> getNullVariables(){
		return nullVariables;
	}

	private boolean isInitialized(String field, List<String> assignments) {
		for (String assignment: assignments){
			if (assignment.contains(field)){
				return true;
			}
		}

		return false;
	}


	private String isPotentiallyNullField(String invocation, List<String> fields) {
		for (String field: fields){
			if (invocation.contains(field)){
				return field;
			}
		}

		return null;
	}


	public List<String> parseForLogger(ModelSourceFile file, String commit) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		String src = readFiletoString(file.getSourceFile().getCanonicalPath());

		file.setSource(src.toCharArray());

		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(src.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);


		Log4JVisitor visitor = new Log4JVisitor();
		cu.accept(visitor);

		return visitor.getMethodCalls();
	}

	public void setIfAndNext(List<String> ifAndNext){
		this.ifAndNext = ifAndNext;
	}

	public void addIfAndNextStatement(String s){
		ifAndNext.add(s);
	}

	public List<String> getIfAndNext(){
		return ifAndNext;
	}

	/**
	 *
	 * Returns a string that stores the contents of the file passed in.
	 *
	 * @param filename
	 * @return
	 */
	public static String readFiletoString(String filename) {
		StringBuffer sb = new StringBuffer();
		for(String s: readFile(filename))
		{
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();

	}

	/**
	 *
	 * Helper method for readFileToString (reads file to List of Strings)
	 *
	 * @param file
	 * @return
	 */
	
	public static List<String> readFile(String file) {
		List<String> retList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				retList.add(line);
			}
		} catch (Exception e) {
			retList = new ArrayList<String>();
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					retList = new ArrayList<String>();
					e.printStackTrace();
				}
			}
		}
		return retList;
	}

}
