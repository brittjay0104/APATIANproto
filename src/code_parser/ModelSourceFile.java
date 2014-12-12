package code_parser;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	public ArrayList<String> nullChecks;
	public HashMap<String, ArrayList<String>> nullVars;
	public HashMap<String, ArrayList<String>> nullAssignments;
	public ArrayList<String> nullFields;
	public ArrayList<String> assignments;

	// ArrayList for keeping track of locations of null checks as they appear
	// and disappear from the
	// list of null checks returned from the visitor
	public ArrayList<String> methods;

	List<ModelDeveloper> devs; // List of developers that touched this file

	public ModelSourceFile() {
		this(null);
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

		commits = new ArrayList<ModelCommit>();
		methods = new ArrayList<String>();
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
	

	/**
	 * 
	 * Adds to the list of methods with null checks for this file.
	 * 
	 * @param method
	 *            - String identifier (method-startPosition) for a method
	 *            containing a null check
	 */
	public void addMethod(String method) {
		methods.add(method);
	}

	public void addNullCheck(String check) {
		nullChecks.add(check);
	}
	
	public void addNullField(String field) {
		nullFields.add(field);
	}
	
	public void removeNullField(String field){
		nullFields.remove(field);
	}
	
	public void addAssignment(String assign){
		assignments.add(assign);
	}
	
	public void addNullAssignment(String method, String assign){
		if (nullAssignments.get(method) == null){
			nullAssignments.put(method, new ArrayList<String>());
		}
		
		nullAssignments.get(method).add(assign);
		
	}
	
	public void removeNullAssignment(String method, String assign){
		if (nullAssignments.get(method) != null){
			nullAssignments.get(method).remove(assign);
		}
	}
	
	public void addNullVariable(String decMethod, String variable){
		if (nullVars.get(decMethod) == null){
			nullVars.put(decMethod, new ArrayList<String>());
		}
		
		nullVars.get(decMethod).add(variable);
	}
	
	public void removeNullVariable(String method, String assign){
		if (nullVars.get(method) != null){
			nullVars.get(method).remove(assign);
		}
	}
	
	public boolean hasThisCheck(String check){
		if (nullChecks.contains(check)){
			return true;
		}
		
		return false;
	}


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

}
