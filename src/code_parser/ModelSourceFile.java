package code_parser;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import node_visitor.NODP;

import org.eclipse.jdt.core.dom.ASTNode;

import developer_creator.ModelDeveloper;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class ModelSourceFile {

	public File source_file;
	String filename;
	Repository repo;
	public char[] source;
	private ArrayList<ModelCommit> commits;
	
	//exceptions
	public List<String> throwsMethods;
	public List<String> tryStatements;
	public List<String> staticTryStatements;
	public List<String> catchBlocks;
	public List<String> staticCatchBlocks;
	public List<String> multiCatchBlocks;
	public List<String> staticMultiCatchBlocks;
	public List<String> tryWithResources;
	public List<String> staticTryWithResources;
	public List<String> finallyBlocks;
	public List<String> staticFinallyBlocks;
	public List<String> throwStatements;
	public List<String> exceptionClasses;
	public List<String> uncheckedExceptions;
	public List<String> checkedExceptions;
	public List<String> catchExceptions;
	
	// null
	public HashMap<String, ArrayList<String>> invocs;
	public HashMap<String, ArrayList<String>> nullVars;
	public HashMap<String, ArrayList<String>> nullAssignments;
	public ArrayList<String> nullFields;
	public ArrayList<String> nullChecks;
	public ArrayList<String> assignments;
	public ArrayList<String> removedFields;
	public HashMap<String, ArrayList<String>> removedVars;
	public HashMap<String, ArrayList<String>> removedAssigns;
	
	public List<String> NODPs;
	public List<String> collVars;
	public List<String> optVars;
	
//	public List<String> genericFields;
//	public List<String> genericMethods;
//	public List<String> genericInvocations;
//	public HashMap<String, List<String>> genericParameters;
//	//public List<String> genericParameters;
//	public List<String> genericVarDeclarations;
	
	//generics
	public List<String> allGenerics;
	public HashMap<String, List<String>> generics;
//	public HashMap<String, List<String>> simpleGenerics;
//	public HashMap<String, List<String>> advancedGenerics;

	// ArrayList for keeping track of locations of null checks as they appear
	// and disappear from the
	// list of null checks returned from the visitor
	public ArrayList<String> methods;
	
	public Map<String, Integer> methodInvocs;

	List<ModelDeveloper> devs; // List of developers that touched this file

	public ModelSourceFile() {
		
	}

	/**
	 * 
	 * Creates new ModelSourceFile for inclusion in the ModelRepository for
	 * analysis.
	 * 
	 * @param file
	 *            - File object
	 * @param ast
	 *            - ASTNode object
	 */
	public ModelSourceFile(File file) {
		source_file = file;
		filename = file.getName();
		nullChecks = new ArrayList<String>();
		nullVars = new HashMap<String, ArrayList<String>>();
		nullFields = new ArrayList<String>();
		assignments = new ArrayList<String>();
		nullAssignments = new HashMap<String, ArrayList<String>>();
		invocs = new HashMap<String, ArrayList<String>>();
		removedFields = new ArrayList<String>();
		removedVars = new HashMap<String, ArrayList<String>>();
		removedAssigns = new HashMap<String, ArrayList<String>>();
		NODPs = new ArrayList<String>();
		collVars = new ArrayList<String>();
		optVars = new ArrayList<String>();
		catchBlocks = new ArrayList<String>();
		
		//exceptions
		throwsMethods = new ArrayList<String>();
		tryStatements = new ArrayList<String>();
		staticTryStatements = new ArrayList<String>();
		catchBlocks = new ArrayList<String>();
		staticCatchBlocks = new ArrayList<String>();
		multiCatchBlocks = new ArrayList<String>();
		staticMultiCatchBlocks = new ArrayList<String>();
		tryWithResources = new ArrayList<String>();
		staticTryWithResources = new ArrayList<String>();
		finallyBlocks = new ArrayList<String>();
		staticFinallyBlocks = new ArrayList<String>();
		throwStatements = new ArrayList<String>();
		exceptionClasses = new ArrayList<String>();
		checkedExceptions = new ArrayList<String>();
		uncheckedExceptions = new ArrayList<String>();
		catchExceptions = new ArrayList<String>();

		allGenerics = new ArrayList<String>();
		generics = new HashMap<String, List<String>>();
		
		generics.put("type argument methods", new ArrayList<String>());
		generics.put("wildcard", new ArrayList<String>());
		generics.put("type declarations", new ArrayList<String>());
		generics.put("type parameter methods", new ArrayList<String>());
		generics.put("type parameter fields", new ArrayList<String>());
		generics.put("diamond", new ArrayList<String>());
		generics.put("method invocations", new ArrayList<String>());
		generics.put("class instantiation", new ArrayList<String>());
		generics.put("nested", new ArrayList<String>());
		
		commits = new ArrayList<ModelCommit>();
		methods = new ArrayList<String>();
		methodInvocs = new HashMap<String, Integer>();
	}

	/**
	 * 
	 * Sets the filename for this instance of MSF
	 * 
	 * @param filename
	 *            - String name for file (with extension)
	 */
	public void setName(String filename) {
		this.filename = filename;
	}

	/**
	 * 
	 * Returns this instance's file name
	 * 
	 * @return file name in form of String
	 */
	public String getName() {
		return filename;
	}

	/**
	 * 
	 * Sets the source to be parsed.
	 * 
	 * @param source
	 *            - character array that contains the source for the MSF being
	 *            created
	 */
	public void setSource(char[] source) {
		this.source = source;
	}

	/**
	 * 
	 * Returns the source code, in the form of an array, for parsing.
	 * 
	 * @return - character array holding the source for the file
	 */
	public char[] getSource() {
		return source;
	}

	/**
	 * @param file
	 *            - File object that the MSF represents
	 */
	public void setSourceFile(File file) {
		source_file = file;
	}

	/**
	 * @return - File object that represents this instance of MSF.
	 */
	public File getSourceFile() {
		return source_file;
	}

	public String srcToString(char[] src) {
		String source = "";

		for (Character c : src) {
			source += c.toString();
		}

		return source;
	}

	/**
	 * 
	 * Sets the repository for this MSF.
	 * 
	 * @param repo
	 *            - Repository object
	 */
	public void setRepository(Repository repo) {
		this.repo = repo;
	}

	public Repository getRepository() {
		return repo;
	}

	/**
	 * 
	 * Returns commits for this source file and attached them to the MSF.
	 * 
	 * @return - List of ModelCommit objects
	 */
	public ArrayList<ModelCommit> getCommits() {
		return commits;
	}

	/**
	 * 
	 * Adds a ModelCommit to this file's list of commits.
	 * 
	 * @param commit
	 *            - RevCommit object
	 */
	public void addCommit(RevCommit commit) {
		ObjectId id = commit.getId();
		String dev = commit.getCommitterIdent().getName();
		commits.add(new ModelCommit(dev, ObjectId.toString(id)));
	}

	public ModelCommit getCommit(String hash) {
		for (ModelCommit commit : commits) {
			if (commit.getCommitHash().equals(hash)) {
				return commit;
			}
		}

		return null;
	}
	
	//exceptions
	public List<String> getThrowsMethods(){
		return throwsMethods;
	}
	
	public List<String> getTryStatements(){
		return tryStatements;
	}
	
	public List<String> getStaticTryStatements(){
		return staticTryStatements;
	}
	
	public List<String> getCatchBlocks(){
		return catchBlocks;
	}
	
	public List<String> getStaticCatchBlocks(){
		return staticCatchBlocks;
	}
	
	public List<String> getMultiCatchBlocks(){
		return multiCatchBlocks;
	}
	
	public List<String> getStaticMultiCatchBlocks(){
		return staticMultiCatchBlocks;
	}
	
	public List<String> getTryWithResources(){
		return tryWithResources;
	}
	
	public List<String> getStaticTryWithResources(){
		return staticTryWithResources;
	}
	
	public List<String> getFinallyBlocks(){
		return finallyBlocks;
	}
	
	public List<String> getStaticFinallyBlocks(){
		return staticFinallyBlocks;
	}
	
	public List<String> getThrowStatements(){
		return throwStatements;
	}
	
	public List<String> getExceptionClasses(){
		return exceptionClasses;
	}
	
	
	public List<String> getCheckedExceptions(){
		return checkedExceptions;
	}
	
	public List<String> getUncheckedExceptions(){
		return uncheckedExceptions;
	}
	
	public List<String> getCatchExceptions(){
		return catchExceptions;
	}
	
	//adders
	
	public void addThrowsMethod(String throwsMethod){
		throwsMethods.add(throwsMethod);
		return;
	}
	
	public void addTryStatement(String tryStatement){
		tryStatements.add(tryStatement);
		return;
	}
	
	public void addStaticTryStatement(String tryStatement){
		staticTryStatements.add(tryStatement);
		return;
	}
	
	public void addCatchBlock(String catchBlock){
		catchBlocks.add(catchBlock);
		return;
	}
	
	public void addStaticCatchBlock(String catchBlock){
		staticCatchBlocks.add(catchBlock);
		return;
	}
	
	public void addMultiCatchBlock(String catchBlock){
		multiCatchBlocks.add(catchBlock);
		return;
	}
	
	public void addStaticMultiCatchBlock(String catchBlock){
		staticMultiCatchBlocks.add(catchBlock);
		return;
	}
	
	public void addTryWithResource(String tryWithResource){
		tryWithResources.add(tryWithResource);
		return;
	}
	
	public void addStaticTryWithResource(String tryWithResource){
		staticTryWithResources.add(tryWithResource);
		return;
	}
	
	public void addFinallyBlock(String finallyBlock){
		finallyBlocks.add(finallyBlock);
		return;
	}
	
	public void addStaticFinallyBlock(String finallyBlock){
		staticFinallyBlocks.add(finallyBlock);
		return;
	}
	
	public void addThrowStatement(String throwStatement){
		throwStatements.add(throwStatement);
		return;
	}
	
	public void addExceptionClass(String exceptionClass){
		exceptionClasses.add(exceptionClass);
		return;
	}
	
	
	public void addCheckedException(String checkedException){
		checkedExceptions.add(checkedException);
		return;
	}
	
	public void addUncheckedException(String uncheckedException){
		uncheckedExceptions.add(uncheckedException);
		return;
	}
	
	public void addCatchException(String catchException){
		catchExceptions.add(catchException);
		return;
	}
	
	//end exceptions
	
	public HashMap<String, ArrayList<String>> getNullVars(){
		return nullVars;
	}
	
	public ArrayList<String> getNullFields() {
		return nullFields;
	}

	/**
	 * 
	 * returns the list of NullCheck objects for this file.
	 * 
	 * @return - List of NullCheck objects
	 */
	public ArrayList<String> getNullChecks() {
		return nullChecks;
	}
	
	public ArrayList<String> getAssignments(){
		return assignments;
	}
	
	public HashMap<String, ArrayList<String>> getNullAssignments(){
		return nullAssignments;
	}
	
	public HashMap<String, ArrayList<String>> getInvocations(){
		return invocs;
	}
	
	public void setNullChecks(ArrayList<String> checks){
		nullChecks = checks;
	}

	@Override
	public String toString() {
		return "ModelSourceFile [source_file=" + source_file + ", filename="
				+ filename + ", repo=" + repo + "]";
	}

	/**
	 * 
	 * Returns list of unique null checks in this file at any given revision.
	 * 
	 * @return - List of method signatures (and start positions) for null checks
	 *         found in this file.
	 */
	public ArrayList<String> getNullMethods() {
		return methods;
	}
	
	
	public Map<String, Integer> getMethodInvocs(){
		return methodInvocs;
	}
	
	public void addMethodInvoc(String method, Integer count){
		methodInvocs.put(method, count);
	}
	
	public List<String> getAllGenerics(){
		return allGenerics;
	}
	
	public HashMap<String, List<String>> getGenerics(){
		return generics;
	} 
	

	/**
	 * 
	 * Adds to the list of methods with null checks for this file.
	 * 
	 * @param method
	 *            - String identifier (method-startPosition) for a method
	 *            containing a null check
	 */
	public void addMethod(String method) {
		if (!(methods.contains(method))){
			methods.add(method);			
		}
	}

	public void addNullCheck(String check) {
		nullChecks.add(check);
	}
	
	public void addNullField(String field) {
		if (!(nullFields.contains(field))){
			nullFields.add(field);			
		}
	}
	
	public void addNODP(String nodp){
		
		if (!NODPs.contains(nodp)){
			NODPs.add(nodp);
		}
	}
	
	public void addCollVar(String collVar){
		if (!(collVars.contains(collVar))){
			collVars.add(collVar);
		}
	}
	
	public void addOptVar(String optVar){
		if (!(optVars.contains(optVar))){
			optVars.add(optVar);
		}
	}

	
	public void removeNODP(String nodp){
		if (NODPs.contains(nodp)){
			NODPs.remove(nodp);
		}
	}
	
	public void removeCollVar(String collVar){
		if (collVars.contains(collVar)){
			collVars.remove(collVar);
		}
	}
	
	public void removeOptVar(String optVar){
		if (optVars.contains(optVar)){
			optVars.remove(optVar);
		}
	}
	
	public void removeCatchBlock(String cblock){
		if (catchBlocks.contains(cblock)){
			catchBlocks.remove(cblock);
		}
	}
	
	public void removeNullField(String field){
		if (nullFields.contains(field)){
			nullFields.remove(field);
		}
	}
	
	public void addAssignment(String assign){
		if (!(assignments.contains(assign))){
			assignments.add(assign);			
		}
	}
	
	public void addNullAssignment(String method, String assign){
		if (nullAssignments.get(method) == null){
			nullAssignments.put(method, new ArrayList<String>());
		}
		
		if (!(nullAssignments.get(method).contains(assign))){
			nullAssignments.get(method).add(assign);			
		}
		
	}
	
	public void removeNullAssignment(String method, String assign){
		if (nullAssignments.get(method) != null){
			nullAssignments.get(method).remove(assign);
		}
	}
	
	public void addInvocation(String method, String invoc){
		if (invocs.get(method) == null){
			invocs.put(method, new ArrayList<String>());
		}
		
		if (!(invocs.get(method).contains(invoc))){
			invocs.get(method).add(invoc);			
		}
	} 
	
	public void addNullVariable(String decMethod, String variable){
		if (nullVars.get(decMethod) == null){
			nullVars.put(decMethod, new ArrayList<String>());
		}
		
		if (!(nullVars.get(decMethod).contains(variable))){
			nullVars.get(decMethod).add(variable);			
		}
	}
	
	public void removeNullVariable(String method, String assign){
		if (nullVars.get(method) != null){
			nullVars.get(method).remove(assign);
		}
	}
	
	public void addGeneric (String generic){
		allGenerics.add(generic);
	}
	
	public void setAllGenerics(List<String> generics){
		for (String s: generics){
			allGenerics.add(s);
		}
	}
	
	public void setGenerics(HashMap<String, List<String>> map){
		generics.putAll(map);
	}
	
//	public void removeGenericField(String field){
//		if (genericFields.contains(field)){
//			genericFields.remove(field);
//		}
//	}
//	
//	public void removeGenericInvoc(String invoc){
//		if (genericInvocations.contains(invoc)){
//			genericInvocations.remove(invoc);
//		}
//	}
	
	public boolean hasThisCheck(String check){
		if (nullChecks.contains(check)){
			return true;
		}
		
		return false;
	}
	
	public ArrayList<String> getRemovedFields(){
		return removedFields;
	}
	
	
	public void addRemovedField(String field){
		if (!(removedFields.contains(field))){
			removedFields.add(field);			
		}
	}
	
	public HashMap<String, ArrayList<String>> getRemovedVars(){
		return removedVars;
	}
	
	public void addRemovedVar(String method, String var){
		if (removedVars.get(method) == null){
			removedVars.put(method, new ArrayList<String>());
		}
		
		removedVars.get(method).add(var);
	}
	
	public HashMap<String, ArrayList<String>> getRemovedAssigns(){
		return removedAssigns;
	}
	
	public void addRemovedAssign(String method, String assign){
		if (removedAssigns.get(method) == null){
			removedAssigns.put(method, new ArrayList<String>());
		}
		
		removedAssigns.get(method).add(assign);
	}
//	
//	public boolean isRemovedField(String field){
//		if (removedFields.contains(field)){
//			return true;
//		}
//		
//		return false;
//	}	


	/**
	 * 
	 * Removes null method at the index provided.
	 * 
	 * @param index
	 *            - index of the method to remove from the ongoing list of null
	 *            check in this file.
	 */
	public void removeMethod(int index) {
		methods.remove(index);
	}

	/**
	 * 
	 * Removes null methods using the name (String) provided.
	 * 
	 * @param method
	 *            - name of the method to remove from the list on ongoing
	 *            methods with null checks
	 */
	public void removeMethod(String method) {
		methods.remove(method);
	}
	
	public List<String> getNODPs (){
		return NODPs;
	}
	
	public List<String> getCollVars() {
		return collVars;
	}
	
	public List<String> getOptVars(){
		return optVars;
	}

}