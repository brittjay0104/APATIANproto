package code_parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

import developer_creator.ModelDeveloper;

public class ModelRepository {

	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;
	private File repoPath;
	private Repository repository;
	private Git git;
	private ArrayList<ModelSourceFile> sourceFiles;
	private ArrayList<RevCommit> revisions;
	private ArrayList<ModelSourceFile> changedFiles;

	// for creating developers and attaching analysis results (only developer with results will show up here)
	private List<ModelDeveloper> developers;

	// for getting unique developers
	private List<String> devs;

	private List<String> allUsagePatterns;

	// for testing


	public ModelRepository() {

	}

	/**
	 *
	 * Creates a new ModelRepository for repository analysis.
	 *
	 * @param repoPath - String file path, which includes the .git file in the repository
	 * @throws RepositoryNotFoundException
	 */
	public ModelRepository(File repoPath) throws RepositoryNotFoundException {
		developers = new ArrayList<ModelDeveloper>();
		devs = new ArrayList<String>();
		allUsagePatterns = new ArrayList<String>();
		changedFiles = new ArrayList<ModelSourceFile>();

		try {
			this.repoPath = repoPath;
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			repository = builder.setGitDir(repoPath).readEnvironment().findGitDir().build();

			revisions = new ArrayList<RevCommit>();
			sourceFiles = new ArrayList<ModelSourceFile>();

			git = new Git(repository);

		} catch (IOException e) {
			throw new RepositoryNotFoundException(repoPath);
		}


	}

	/**
	 *
	 * Returns the Git object that represents the actual repository.
	 *
	 * @return - Git object representing the git repository (wraps Repository object)
	 */
	public Git getGitRepository() {
		return git;
	}

	public void setGitRepository(Git git){
		this.git = git;
	}

	/**
	 *
	 * Returns the Repository object that represents the actual repository.
	 *
	 * @return - Repository object that stores repository info (i.e. repository path)
	 */
	public Repository getRepo() {
		return repository;
	}

	public void setRepo(Repository repo){
		repository = repo;
	}

	/**
	 * @return - File object that stores the path to the repository (location of the .git file)
	 */
	public File getRepoPath() {
		return repoPath;
	}

	public void setRepoPath(){

	}

	/**
	 *
	 * Returns the MSF objects for a given ModelRepository instance.
	 *
	 * @return - List of MSF objects that represents the source files for a given
	 * ModelRepository instance.
	 */
	public ArrayList<ModelSourceFile> getSourceFiles() {
		return sourceFiles;
	}

	/**
	 *
	 * Returns all commits for a given repository.
	 *
	 * @return - List of RevCommit objects, overall representing all the revisions
	 * for a given repository.
	 */
	public ArrayList<RevCommit> getRevisions() {
		return revisions;
	}

	public List<ModelDeveloper> getDevelopers(){
		return developers;
	}

	public ModelDeveloper lookupDev(String name){
		for (ModelDeveloper dev: developers){
			if (dev.getDevName().equals(name)){
				return dev;
			}
		}

		return null;
	}

	/**
	 *
	 * Sets the source files for a given repository (used when reverting and analyzing as well)
	 *
	 * @param directory - path to the repository (without the .git file)
	 * @throws IOException
	 */
	public void setSourceFiles(String directory) throws IOException {

		File f = new File(directory);
		String [] extensions = new String[5];
		extensions[0] = "java";
		// clean out the sourceFiles array (in case some file are no longer in the new revision)
		if (!sourceFiles.isEmpty()){
			sourceFiles.clear();
		}

		// set source files for the repository at the current revision
		for (Iterator<File> files = FileUtils.iterateFiles(f, extensions, true); files.hasNext();){
			ModelSourceFile file = new ModelSourceFile(files.next());
			sourceFiles.add(file);
			System.out.println("Added " + file.source_file.getName() + " to " + directory + " repository source files.");
			//file.setRepository(repository);
		}

	}
	
	public void addChangedFile(ModelSourceFile file){
		changedFiles.add(file);		
		
	}
	
	public ArrayList<ModelSourceFile> getChangedFiles(){
		return changedFiles;
	}
	


	/**
	 *
	 * Stores all the revisions/commits for this repository (populates revisions list).
	 *
	 * @param git - Git object that represents the repository being analyzed.
	 *
	 */
	public Object setRepositoryRevisionHistory(Git git, ModelDeveloper developer) {

		Object o = "did it";
				
		try {
			LogCommand lc = git.log();
			Iterable<RevCommit> log;
			log = lc.call();

			for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext(); ) {
				RevCommit rev = iterator.next();

				
				revisions.add(rev);
//				System.out.println(rev.toString());
//				System.out.println(rev.getCommitterIdent().getName());

				if (rev.getCommitterIdent().getName().equals(developer.getDevName()) || rev.getCommitterIdent().getName().equals(developer.getUserName()) 
						|| developer.getDevName().contains(rev.getCommitterIdent().getName())){
					
					if (!(developer.getCommits().contains(ObjectId.toString(rev.getId())))){
						System.out.println(developer.getDevName() + " is responsible for commit " + ObjectId.toString(rev.getId()));
						developer.addCommit(ObjectId.toString(rev.getId()));						
					}
					
				}

			}
			

		} catch (NoHeadException e) {
			System.out.println("NoHeadException thrown!");
			o = null;
		} catch (GitAPIException e) {
			System.out.println("GitAPIException thrown!");
		}

			
		
		// TODO why is this here? incomplete?
		devs = new ArrayList<String>(new LinkedHashSet<String>(devs));

		return o;

	}

	/**
	 *
	 * Attaches commits/revision history to a specific file in the repository.
	 *
	 * @param git - Git object that represents the repository being analyzed
	 * @param file - MSF object that represents the file for which revision history is being set
	 */
	public void setFileRevisionHistory(Git git, ModelSourceFile file){

		for (RevCommit rev: this.getRevisions()) {
			List<PathChangeModel> files = JGitUtils.getFilesInCommit(git.getRepository(), rev);
			for (PathChangeModel f: files) {
				if (f.name.contains("/")){
					int start = f.name.lastIndexOf("/");
					String filename = f.name.substring(start + 1, f.name.length());
					if (file.getSourceFile().getName().equals(filename)){
						file.addCommit(rev);
					}
				} else {
					if (file.getSourceFile().getName().equals(f.name)){
						file.addCommit(rev);
					}
				}
			}
		}


	}

	
	public void analyzeForMethodInvocations(Git git, String hash, ModelDeveloper dev, String repoName) throws IOException {
		
		ModelParser parser = new ModelParser();
		//String devName = dev.getDevName();
		
		for (ModelSourceFile file: getSourceFiles()){
			
			parser.parseForMethodInvocations(file);
			System.out.println("Method Invocations in " + file.getName());
			Map<String, Integer> methodInvocs = file.getMethodInvocs();
			for (Map.Entry<String, Integer> entry: methodInvocs.entrySet()){
				String key = entry.getKey();
				Integer count = entry.getValue();
				
				System.out.println("	Method: " + key + "				" + count);
			}
		}
	}

	/**
	 *
	 * on the addition of usage patterns.
	 * 
	 * @param git - Git object that represents the repository to analyze.
	 * @param directory - String path to the directory of the repository in file system (without .git)
	 * @throws IOException
	 */
	public void revertAndAnalyzeForPatternAddition(Git git, String directory, ModelDeveloper dev, String repoName) throws IOException{

		ArrayList<String> commits = dev.getCommits();
		String devName = dev.getDevName();
		
		// iterate over repository revisions - parse at each revision 
		//     (can compare dev commits when determined with diff that pattern added/removed)
	
		for (int i=0; i<revisions.size(); i++){
			RevCommit current = revisions.get(i);
			String currentHash = ObjectId.toString(current);
			String previousHash = "";
			
			if (!(currentHash.equals(ObjectId.toString(revisions.get(revisions.size()-1))))){
				previousHash = ObjectId.toString(revisions.get(i+1));
				
				setAndParseSource(directory, previousHash, currentHash, dev);
				
				diffPrettyPrint(dev, currentHash, previousHash);
				
				try{
					
					git.revert().include(current).call();
					
					System.out.println("\n Reverted revision " + currentHash + " to " + previousHash + "\n");
					
				} catch (NoMessageException e) {
					System.out.println("NoMessageException thrown!");
				} catch (UnmergedPathsException e) {
					System.out.println("Paths did not successfully merge!");
				} catch (ConcurrentRefUpdateException e) {
					System.out.println("ConcurrentRefUpdateException thrown!");
				} catch (WrongRepositoryStateException e) {
					System.out.println("Wrong Repository State -- exception thrown!");
				} catch (GitAPIException e) {
					System.out.println("GitAPIException thrown!");
					System.out.println(e.getLocalizedMessage());
				}
				
			} else {
				outputPrettyPrint(dev, repoName, devName);
			}

		}
	}
	
	public void revertAndAnalyzeForPatternRemoval(Git git, String directory, ModelDeveloper dev, String repoName) throws IOException{
		
		String devName = dev.getDevName();
		
		for (String s: allUsagePatterns){
			System.out.println("Usage pattern --> " + s);
		}

		for (int i=0; i<revisions.size(); i++){
			RevCommit current = revisions.get(i);
			String currentHash = ObjectId.toString(current);
			String previousHash = "";
			
			if (!(currentHash.equals(ObjectId.toString(revisions.get(revisions.size()-1))))){
				previousHash = ObjectId.toString(revisions.get(i+1));
				
				// set and parse source (for removal)
				System.out.println("\n****Parsing for removal at revision " + currentHash + "****\n");
				
				setSourceFiles(directory);
				
				for (ModelSourceFile f : getSourceFiles()){
					removalDiff(directory, f, allUsagePatterns, previousHash, currentHash, dev);
				}
								
				diffPrettyPrint(dev, currentHash, previousHash);
				
				try{
					
					git.revert().include(current).call();
					
					System.out.println("\n Reverted revision " + currentHash + " to " + previousHash + "\n");
					
				} catch (NoMessageException e) {
					System.out.println("NoMessageException thrown!");
				} catch (UnmergedPathsException e) {
					System.out.println("Paths did not successfully merge!");
				} catch (ConcurrentRefUpdateException e) {
					System.out.println("ConcurrentRefUpdateException thrown!");
				} catch (WrongRepositoryStateException e) {
					System.out.println("Wrong Repository State -- exception thrown!");
				} catch (GitAPIException e) {
					System.out.println("GitAPIException thrown!");
					System.out.println(e.getLocalizedMessage());
				}
				
			} else {
				outputPrettyPrint(dev, repoName, devName);
			}

		}
	}

	private void outputPrettyPrint(ModelDeveloper dev, String repoName, String devName) {
		System.out.println("************Analysis complete************");
//		System.out.println(devName + " added null count = " + dev.getAddedNullCounts() + " in repository " + repoName);
//		System.out.println(devName + " removed null count = " + dev.getRemovedNullCounts() + " in repository " + repoName);
//		System.out.println(devName + " deref count = " + dev.getDerefCount() + " in repository " + repoName);
//		System.out.println(devName + " added catch blocks count = " + dev.getAddedCatchCounts());
//		System.out.println(devName + " removed catch blocks count = " + dev.getRemovedCatchCounts());
//		System.out.println(devName + " added collections count = " + dev.getAddedCollCounts());
//		System.out.println(devName + " removed collections count = " + dev.getRemovedCollCounts());
//		System.out.println(devName + " added optional count = " + dev.getAddedOptCounts());
//		System.out.println(devName + " removed optional count = " + dev.getRemovedOptCounts());
//		System.out.println(devName + "added Null Object Design Pattern count = " + dev.getAddedNODPCounts());
		
		
		// GENERICS
		System.out.println("SIMPLE GENERICS:");
		System.out.println(devName + " added " + dev.getAddedSimpleGenerics() + " instances of simple generics in repository " + repoName);
		System.out.println(devName + " added simple generic field count = " + dev.getAddedSimpleFields());
		System.out.println(devName + " added simple generic variables count =  " + dev.getAddedSimpleVariables());
		System.out.println(devName + " added simple generic methods count =  " + dev.getAddedSimpleMethods());
		System.out.println(devName + " added simple generic return count =  " + dev.getAddedSimpleReturn());
		System.out.println("\n");
		
		
		System.out.println("ADVANCED GENERICS:");
		System.out.println(devName + " added " + dev.getAddedAdvancedGenerics() + " instances of advanced generics in repository " + repoName);
		System.out.println(devName + " added advanced generic class count = " + dev.getAddedAdvancedClasses());
		System.out.println(devName + " added advanced generic field count = " + dev.getAddedAdvancedFields());
		System.out.println(devName + " added advanced generic method count = " + dev.getAddedAdvancedMethods());
		System.out.println(devName + " added advanced generic return count = " + dev.getAddedAdvancedReturn());
		System.out.println(devName + " added advanced generic nested count = " + dev.getAddedAdvancedNested());
		System.out.println(devName + " added advanced generic parameters count = " + dev.getAddedAdvancedParameters());
		System.out.println(devName + " added advanced generic bound count = " + dev.getAddedAdvancedBounds());
		System.out.println(devName + " added advanced generic wildcard count = " + dev.getAddedAdvancedWildcard());
		System.out.println(devName + " added advanced generic diamond count = " + dev.getAddedAdvancedDiamond());
		
	}

	private void diffPrettyPrint(ModelDeveloper dev, String currentHash, String previousHash) {
		System.out.println("\nDiff of " + currentHash + " and " + previousHash + ":");
//		System.out.println("	--> Added null checks = " + dev.getAddedNullCounts());
//		System.out.println("	--> Removed null checks = " + dev.getRemovedNullCounts());
//		System.out.println("	--> Null dereferences checked for null = " + dev.getDerefCount());
//		System.out.println("	--> Added Catch Blocks = " + dev.getAddedCatchCounts());
//		System.out.println(" 	--> Removed Catch Blocks = " + dev.getRemovedCatchCounts());
//		System.out.println("	--> Added Collections = " + dev.getAddedCollCounts());
//		System.out.println("	--> Removed Collections = " + dev.getRemovedCollCounts());
//		System.out.println("	--> Added Optional = " + dev.getAddedOptCounts());
//		System.out.println("	--> Removed Optional = " + dev.getRemovedOptCounts());
//		System.out.println("	--> Added Null Object Design Patterns = " + dev.getAddedNODPCounts());
		
		// GENERICS
		System.out.println("SIMPLE GENERICS = " + dev.getAddedSimpleGenerics());
		System.out.println("	--> Added simple generic fields = " + dev.getAddedSimpleFields());
		System.out.println("	--> Added simple generic variables  = " + dev.getAddedSimpleVariables());
		System.out.println("	--> Added simple generic methods = " + dev.getAddedSimpleMethods());
		System.out.println("	--> Added simple generic returns = " + dev.getAddedSimpleReturn());

		System.out.println("ADVANCED GENERICS:");
		System.out.println("	--> Added advanced generic classes = " + dev.getAddedAdvancedClasses());
		System.out.println("	--> Added advanced generic fields = " + dev.getAddedAdvancedFields());
		System.out.println("	--> Added advanced generic methods = " + dev.getAddedAdvancedMethods());
		System.out.println("	--> Added advanced generic returns = " + dev.getAddedAdvancedReturn());
		System.out.println("	--> Added advanced generic nested = " + dev.getAddedAdvancedNested());
		System.out.println("	--> Added advanced generic parameters = " + dev.getAddedAdvancedParameters());
		System.out.println("	--> Added advanced generic bounds = " + dev.getAddedAdvancedBounds());
		System.out.println("	--> Added advanced generic wildcards = " + dev.getAddedAdvancedWildcard());
		System.out.println("	--> Added advanced generic diamonds = " + dev.getAddedAdvancedDiamond());

	}
	
	public boolean hasGenerics(){
		
		// TODO: analysis for if repository or file has any generics at all
		
		return false;
	}

	/**
	 * @param directory
	 * @param i
	 * @param previousHash
	 * @throws IOException
	 */
	public void setAndParseSource(String directory, String previousHash, String currentHash, ModelDeveloper dev) throws IOException {
		
		System.out.println("\n****Parsing for addition at revision " + currentHash + "****\n");
		
		setSourceFiles(directory);
		
		for (ModelSourceFile f: getSourceFiles()) {
			System.out.println("\n File for diff --> " + f.getName() + "\n");
			
			//setFileRevisionHistory(git, f);

			ModelParser parser = new ModelParser();
 
			// parse the files AST for null checks, nodps, coll/opt usage, catches with NPE
			// diff the current and older revision
			List <String> checks = parser.parseForNull(f, previousHash);
			for (String check: checks){
				addUsagePattern(check);
			}
			// passing in ifAndNext
			additionDiff(directory, f, checks, previousHash, currentHash, dev);
			
		
			ArrayList<List<String>> npes = parser.parseForNPEAvoidance(f);
			
			List<String> catches = npes.get(2);
			for (String c: catches){
				addUsagePattern(c);
			}
			
			additionDiff(directory, f, catches, previousHash, currentHash, dev);

			
			// works the same as null checks (except only with statement
			List<String> colls = npes.get(0);
			for (String coll: colls){
				addUsagePattern(coll);
			}
			
			additionDiff(directory, f, colls, previousHash, currentHash, dev);
			
			List<String> opts = npes.get(1);
			for (String opt: opts){
				addUsagePattern(opt);
			}
			additionDiff(directory, f, opts, previousHash, currentHash, dev);
			
			
			List<String> nodps = parser.parseForNODP(f, currentHash);
			for (String nodp: nodps){
				addUsagePattern(nodp);
			}
			additionDiff(directory, f, nodps, previousHash, currentHash, dev);
			
			
			
			// GENERICS
			parser.parseForGenerics(f);
			
			// simple - fields
			List<String> sFields = f.getSimpleGenerics().get("fields");
			
			for (String s: sFields){
				addUsagePattern(s);
			}
			genericsAdditionDiff(directory, f, sFields, previousHash, currentHash, dev);
			
			// simple - variables
			List<String> sVariables = f.getSimpleGenerics().get("variables");
			
			for (String s: sVariables){
				addUsagePattern(s);
			}
			genericsAdditionDiff(directory, f, sVariables, previousHash, currentHash, dev);
			
			// simple - methods
			List<String> sMethods = f.getSimpleGenerics().get("methods");
			
			for (String s: sMethods){
				addUsagePattern(s);
			}
			genericsAdditionDiff(directory, f, sMethods, previousHash, currentHash, dev);
			
			// simple - return
			List<String> sReturn = f.getSimpleGenerics().get("return");
			
			for (String s: sReturn){
				addUsagePattern(s);
			}
			genericsAdditionDiff(directory, f, sReturn, previousHash, currentHash, dev);
			
			// advanced - classes
			List<String> aClass = f.getAdvancedGenerics().get("classes");
			
			for (String a: aClass){
				addUsagePattern(a);
			}
			
			genericsAdditionDiff(directory, f, aClass, previousHash, currentHash, dev);
			
			// advanced - fields
			List<String> aFields = f.getAdvancedGenerics().get("fields"); 
			
			for (String a: aFields){
				addUsagePattern(a);
			}			
			genericsAdditionDiff(directory, f, aFields, previousHash, currentHash, dev);
			
			// advanced - methods
			List<String> aMethods = f.getAdvancedGenerics().get("methods");
			
			for (String a: aMethods){
				addUsagePattern(a);
			}
			genericsAdditionDiff(directory, f, aMethods, previousHash, currentHash, dev);
			
			// advanced - return
			List<String> aReturn = f.getAdvancedGenerics().get("return");
			
			for (String a: aReturn){
				addUsagePattern(a);
			}
			genericsAdditionDiff(directory, f, aReturn, previousHash, currentHash, dev);
			
			// advanced - nested
			List<String> aNested = f.getAdvancedGenerics().get("nested");
			
			for (String a: aNested){
				addUsagePattern(a);
			}
			genericsAdditionDiff(directory, f, aNested, previousHash, currentHash, dev);
			
			// advanced - params
			List<String> aParams = f.getAdvancedGenerics().get("parameters");
			
			for (String a: aParams){
				addUsagePattern(a);
			}
			genericsAdditionDiff(directory, f, aParams, previousHash, currentHash, dev);
			
			// advanced - bounds
			List<String> aBounds = f.getAdvancedGenerics().get("bounds");
			
			for (String a: aBounds){
				addUsagePattern(a);
			}
			genericsAdditionDiff(directory, f, aBounds, previousHash, currentHash, dev);
			
			// advanced - wildcard
			List<String> aWildcard = f.getAdvancedGenerics().get("wildcard");
			
			for (String a: aWildcard){
				addUsagePattern(a);
			}
			genericsAdditionDiff(directory, f, aWildcard, previousHash, currentHash, dev);
			
			
		}
	}

	private void addUsagePattern(String c) {
		if (!(allUsagePatterns.contains(c))){
			allUsagePatterns.add(c);					
		}
	}

	private int calculateDerefValue(ModelSourceFile file, String check, ModelDeveloper dev, int deref) {		
				
		// **************** FIELDS (not initialized) ********************
		List<String> fields = file.getNullFields();
		
		// if field found in null check, remove from ongoing list and increment dev deref count
		for (String field: fields){
			if (check.contains(field)){
				deref +=1;
				//dev.incrementDerefCount();
				file.addRemovedField(field);
			}
		}
			
		removeFields(file);		

		
		
		// if var declared null found in null check (check method too), remove from ongoing list and increment dev deref count

		
		// ******************** VARIABLES (declared null; with method) ********************
		HashMap<String, ArrayList<String>> variables = file.getNullVars();
		Iterator<Entry<String, ArrayList<String>>> it2 = variables.entrySet().iterator();
		
		// Null Checks
		List<String> checks = file.getNullChecks();
		

		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it2.next();

			String methodDec = pairs.getKey();
			
			//System.out.println("\nNull variables: ");
			for (String var: pairs.getValue()){
				//System.out.println("		--> " + var + " found in " + methodDec);
				
				for (String nullCheck: checks){
					if (nullCheck.contains(check.substring(0, check.indexOf(CHECK_SEPERATOR)))){
						if (nullCheck.contains(methodDec)){
							//	System.out.println("Null check in " + methodDec);
							
							if (nullCheck.contains(var)){
								//	System.out.println("Null check for null var. Add to value!");
								deref +=1;
								//dev.incrementDerefCount();
								
								file.addRemovedVar(methodDec, var);							
							}
							
						}
						//System.out.println("Null check found in file: " + check);
					}	
				}							
			}
		}
		
		// iterate over hashmap and remove the necessary variables
		removeVariables(file);
		
		
		// if var assigned null found in null check (check method too), remove from ongoing list and increment dev deref count

		
		// ******************** VARIABLES (assigned null; with method) ********************
		
		HashMap<String, ArrayList<String>> assignedFile = file.getNullAssignments();
		Iterator<Entry<String, ArrayList<String>>> it3 = assignedFile.entrySet().iterator();
		
		
		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it3.next();
			
			String methodDec = pairs.getKey();
			
			//System.out.println("\nNull assignments: ");
			for (String assign: pairs.getValue()){
				
				for (String nullCheck: checks){
					if (nullCheck.contains(check.substring(0, check.indexOf(CHECK_SEPERATOR)))){
						if (nullCheck.contains(methodDec)){
							//	System.out.println("Null check in " + methodDec);
							
							if (nullCheck.contains(assign)){
								//	System.out.println("Null check for null var. Add to value!");
								deref +=1;
								// dev.incrementDerefCount();
								file.addRemovedAssign(methodDec, assign);							
							}
							
						}
						//System.out.println("Null check found in file: " + check);
					}
				}
				//System.out.println("		--> " + assign + " found in " + methodDec);
				
			}
			
		}

		
		// remove necessary fields
		removeAssigns(file);
		
		return deref;
		

	}


	private void removeFields(ModelSourceFile file) {
		
		ArrayList<String> fields = new ArrayList<String>();
		
		for (String field: file.getRemovedFields()){
			if (file.getRemovedFields().contains(field)){
				fields.add(field);
			}
		}
		
		
		for (String field: fields){
			file.removeNullField(field);
		}
		
	}

	public void removeVariables(ModelSourceFile file) {
		
		HashMap<String, ArrayList<String>> remove = new HashMap<String, ArrayList<String>>();
		
		Iterator<Entry<String, ArrayList<String>>> it = file.getRemovedVars().entrySet().iterator();
		
		while (it.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it.next();
			
			String methodDec = pairs.getKey();
			
			for (String var: file.getRemovedVars().get(methodDec)){
				if (remove.get(methodDec) == null){
					remove.put(methodDec, new ArrayList<String>());
				}
				
				remove.get(methodDec).add(var);
			}
		}
		
		// iterate through remove and call  file.removeNullVariable(methodDec, var);
		
		Iterator<Entry<String, ArrayList<String>>> it2 = remove.entrySet().iterator();
		
		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it2.next();
			
			String methodDec = pairs.getKey();
			
			for (String var: remove.get(methodDec)){
				file.removeNullVariable(methodDec, var);
			}	
		}

	}
	
	public void removeAssigns(ModelSourceFile file){
		
		HashMap<String, ArrayList<String>> remove = new HashMap<String, ArrayList<String>>();
		
		Iterator<Entry<String, ArrayList<String>>> it = file.getRemovedAssigns().entrySet().iterator();
		
		while (it.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it.next();
			
			String methodDec = pairs.getKey();
			
			for (String assign: file.getRemovedAssigns().get(methodDec)){
				if (remove.get(methodDec) == null){
					remove.put(methodDec, new ArrayList<String>());
				}
				
				remove.get(methodDec).add(assign); 
			}
		}
		
		// iterate through remove and call file.removeNullAssignment(methodDec, assign);
		
		Iterator<Entry<String, ArrayList<String>>> it2 = remove.entrySet().iterator();
		
		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it2.next();
			
			String methodDec = pairs.getKey();
			
			for (String assign: remove.get(methodDec)){
				file.removeNullAssignment(methodDec, assign);
			}	
		}
		
	}
	
	public void removalDiff (String directory, ModelSourceFile file, List<String> checks, String oldH, String newH, ModelDeveloper developer){
		File repoDir = new File(directory);
		
		int removedNullChecks = 0;
		int removedCollVar = 0;
		int removedOptVar = 0;
		int removedCatchBlock = 0;
		
		int removedGenericVarDeclarations = 0;
		int removedGenericFieldDeclarations = 0;
		
		Git git;
		//current revision
		String currentHash = newH;
				
		try {
			
			git = Git.open(repoDir);
			// next to current version in repository (older)
			String previousHash = getOldHash(newH);


			ObjectId headId = git.getRepository().resolve(currentHash + "^{tree}");
			ObjectId oldId = git.getRepository().resolve(previousHash + "^{tree}");

			ObjectReader reader = git.getRepository().newObjectReader();

			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldId);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, headId);

			List<DiffEntry> diffs;

			diffs = git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			df.setRepository(git.getRepository());
			
			System.out.println("********************REMOVAL DIFF*************************");
			
			for (DiffEntry diff : diffs){
				
				df.format(diff);
				diff.getOldId();
				String diffText = out.toString("UTF-8");
				
				//System.out.println("Diff Text --> " + diffText);
				
				String fileName = file.getName();
				//System.out.println("LOOK AT THIS --> " + fileName.substring(0, fileName.indexOf(".")));
				if (diffText.contains(fileName) || diffText.contains(fileName.substring(0, fileName.indexOf(".")))){
					
					//System.out.println(diffText);
					
					BufferedReader br = new BufferedReader(new StringReader(diffText));
					String line = null;	
					
					while ((line = br.readLine()) != null){
						line = line.trim();
						
						line = line.replaceAll("\t", " ");
						
						if (line.startsWith("-")){
							// TODO method for incrementing total LOC removed?
							for (String check: checks){
								if (check.contains("catch")){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										removedCatchBlock = checkNullRemoval(removedCatchBlock, currentHash, diffText, check);
									}
								} else if (check.contains("Collections")){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										removedCollVar = checkNullRemoval(removedCollVar, currentHash, diffText, check);
									}
								} else if (check.contains("Optional")){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										removedOptVar = checkNullRemoval(removedOptVar, currentHash, diffText, check);
									}
								} else if (StringUtils.countMatches(check, Character.toString(CHECK_SEPERATOR)) != 2 && check.contains("null")){
									String nullcheck = check.substring(0, check.indexOf(CHECK_SEPERATOR));
									String nc = nullcheck.trim();
									
									if (line.contains(nc)){
										removedNullChecks = checkNullRemoval(removedNullChecks, currentHash, diffText, check);									
									}
								} 
								
								// GENERIC Variable and Field Declarations
								else if (!(check.contains(Character.toString(CHECK_SEPERATOR)))) {
//									System.out.println("Generic removal check: " + check);
//									System.out.println("Generic removal line: " + line);
									
									if (line.contains(check)){
										// field
										if ((check.contains("public") || check.contains("private") || check.contains("protected"))){
											System.out.println("*************** Checking for removal of generics field " + check + " ***************");
											removedGenericFieldDeclarations = checkGenericsRemoval(removedGenericFieldDeclarations, currentHash, diffText, check);
										}
										
										// variable
										System.out.println("*************** Checking for removal of generics variable " + check + " ***************");
										removedGenericVarDeclarations = checkGenericsRemoval(removedGenericVarDeclarations, currentHash, diffText, check);
									
									}									
								}
							}
						}
					}
				}
				out.reset();
			}
		
			if (removedNullChecks > 0 && removedNullChecks <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setRemovedCounts(removedNullChecks);					
				}
			}
			if (removedCollVar > 0 && removedCollVar <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setRemovedCollCounts(removedCollVar);
				}
			} 
			
			if (removedOptVar > 0 && removedOptVar <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setRemovedOptCounts(removedOptVar);
				}
			}
			
			if (removedCatchBlock > 0 && removedCatchBlock <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setRemovedCatchCounts(removedCatchBlock);
				}
			}
			
//			if (removedGenericFieldDeclarations > 0 && removedGenericFieldDeclarations <= 10){
//				if (developer.getCommits().contains(currentHash)){
//					developer.setRemovedGenericFields(removedGenericFieldDeclarations);
//				}
//			}
//			
//			if (removedGenericVarDeclarations > 0 && removedGenericVarDeclarations <= 10){
//				if (developer.getCommits().contains(currentHash)){
//					developer.setRemovedGenericVarDecs(removedGenericVarDeclarations);
//				}
//			}
		
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
	}
	
	public void genericsAdditionDiff(String directory, ModelSourceFile file, List<String> patterns, String prevH, String currH, ModelDeveloper developer) {
		
		File repoDir = new File(directory);
		
		int addedSimpleGenerics = 0;
		int addedSimpleFields = 0;
		int addedSimpleVariables = 0;
		int addedSimpleMethods = 0;
		int addedSimpleReturn = 0;
		
		int addedAdvancedGenerics = 0;
		int addedAdvancedClasses = 0;
		int addedAdvancedFields = 0;
		int addedAdvancedMethods = 0;
		int addedAdvancedReturn = 0;
		int addedAdvancedNested = 0;
		int addedAdvancedParams = 0;
		int addedAdvancedBounds = 0;
		int addedAdvancedWildCards = 0;
		int addedAdvancedDiamonds = 0;
		
		Git git;
		//current revision
		String currentHash = currH;
		
		try {
			
			git = Git.open(repoDir);
			// next to current version in repository (older)
			String previousHash = getOldHash(currH);


			Repository repo = git.getRepository();
			
			ObjectId headId = repo.resolve(currentHash + "^{tree}");
			ObjectId oldId = repo.resolve(previousHash + "^{tree}");

			ObjectReader reader = repo.newObjectReader();
			

			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldId);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, headId);
			
			List<DiffEntry> diffs;

			diffs = git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			df.setRepository(repo);
			df.setDetectRenames(true);
			df.scan(oldId, headId);
			
			RenameDetector rd = df.getRenameDetector();
			
			// RENAME DETECTION
			List<DiffEntry> compute = rd.compute();
			
			if (!(compute.isEmpty())){
				System.out.println("Rename happened!");
			}
			
//			for (DiffEntry diff: compute){
//				System.out.println("MIGHT BE FILE NAME FOR COMPARISON:");
//				System.out.println(diff.getNewPath());
//				System.out.println(diff.getOldPath());
//			}
//			
			//System.out.println("******************** GENERICS ADDITION DIFF *************************");
			
			for (DiffEntry diff: diffs){							
				
				System.out.println("MIGHT BE FILE NAME FOR RENAME:");
				System.out.println(diff.getNewPath());
				System.out.println(diff.getOldPath());
				
				df.format(diff);
				diff.getOldId();
				String diffText = out.toString("UTF-8");
				
				if (diffText.contains(file.getName())){
					
					BufferedReader br = new BufferedReader(new StringReader(diffText));
					String line = null;
					
					while ((line = br.readLine()) != null){
						
						line = line.trim();
						
						line = line.replaceAll("\t", " ");
						
						// iterate over parser patterns for addition
						if (line.startsWith("+")){
							
							for (String pattern: patterns){
								// SIMPLE
								if (file.getSimpleGenerics().get("fields").contains(pattern)){
									//System.out.println(pattern);
									
									addedSimpleFields = checkAddedGenerics(addedSimpleFields, currH, diffText, line, pattern);
								} 
								else if (file.getSimpleGenerics().get("variables").contains(pattern)){
									//System.out.println(pattern);
									
									addedSimpleVariables = checkAddedGenerics(addedSimpleVariables, currH, diffText, line, pattern);
								}
								else if (file.getSimpleGenerics().get("methods").contains(pattern)){
									//System.out.println(pattern);
									
									addedSimpleMethods = checkAddedGenerics(addedSimpleMethods, currH, diffText, line, pattern);
								}
								else if (file.getSimpleGenerics().get("return").contains(pattern)){
									//System.out.println(pattern);
									
									addedSimpleReturn = checkAddedGenerics(addedSimpleReturn, currH, diffText, line, pattern);
								}
								//ADVANCED
								else if (file.getAdvancedGenerics().get("classes").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedClasses = checkAddedGenerics(addedAdvancedClasses, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("fields").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedFields = checkAddedGenerics(addedAdvancedFields, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("methods").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedMethods = checkAddedGenerics(addedAdvancedMethods, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("return").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedReturn = checkAddedGenerics(addedAdvancedReturn, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("nested").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedNested = checkAddedGenerics(addedAdvancedNested, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("parameters").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedParams = checkAddedGenerics(addedAdvancedParams, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("bounds").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedBounds = checkAddedGenerics(addedAdvancedBounds, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("wildcard").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedWildCards = checkAddedGenerics(addedAdvancedWildCards, currH, diffText, line, pattern);
								}
								else if (file.getAdvancedGenerics().get("diamond").contains(pattern)){
//									System.out.println(pattern);
									
									addedAdvancedDiamonds = checkAddedGenerics(addedAdvancedDiamonds, currH, diffText, line, pattern);
								}
							}
							
						}
						
					}
					
				}
				
			out.reset();
				
		}
			
			// TODO might need 10 or less filter back! Run without and see what we get.
			if (developer.getCommits().contains(currentHash)){
				// SIMPLE
				developer.setAddedSimpleFields(addedSimpleFields);
				developer.setAddedSimpleMethods(addedSimpleMethods);
				developer.setAddedSimpleVariables(addedSimpleVariables);
				developer.setAddedSimpleReturn(addedSimpleReturn);
				
				//ADVANCED
				developer.setAddedAdvancedClasses(addedAdvancedClasses);
				developer.setAddedAdvancedFields(addedAdvancedFields);
				developer.setAddedAdvancedMethods(addedAdvancedMethods);
				developer.setAddedAdvancedNested(addedAdvancedNested);
				developer.setAddedAdvancedParameters(addedAdvancedParams);
				developer.setAddedAdvancedReturn(addedAdvancedReturn);
				developer.setAddedAdvancedBounds(addedAdvancedBounds);
				developer.setAddedAdvancedWildcard(addedAdvancedWildCards);
				developer.setAddedAdvancedDiamond(addedAdvancedDiamonds);
				
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
		
		
	}

	public void additionDiff(String directory, ModelSourceFile file, List<String> checks, String oldH, String newH, ModelDeveloper developer) {
		
		File repoDir = new File(directory);
		
		int addedNullChecks = 0;
		int derefNullChecks = 0;
		
		int addedCollVar = 0;
		int addedOptVar = 0;		
		int addedCatchBlock = 0;
		int addedNODP = 0;
		
		
		Git git;
		//current revision
		String currentHash = newH;

		try {
		
			git = Git.open(repoDir);
			// next to current version in repository (older)
			String previousHash = getOldHash(newH);


			Repository repo = git.getRepository();
			
			ObjectId headId = repo.resolve(currentHash + "^{tree}");
			ObjectId oldId = repo.resolve(previousHash + "^{tree}");

			ObjectReader reader = repo.newObjectReader();
			

			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldId);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, headId);
			
			List<DiffEntry> diffs;

			diffs = git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			df.setRepository(repo);
			df.setDetectRenames(true);
	
			
			//System.out.println("******************** ADDITION DIFF *************************");
			
			
			for(DiffEntry diff : diffs)
			{
				
				RenameDetector rd = df.getRenameDetector();
				
				List<DiffEntry> compute = rd.compute();
				
				if (!(compute.isEmpty())){
					System.out.println("This diff is a file rename!");
				}

				df.format(diff);
				diff.getOldId();
				String diffText = out.toString("UTF-8");
				
				if (diffText.contains(file.getName())){
//					if (currentHash.equals("6a9bda47c7c18265dcc682be5513a82db528cdfd") & file.getName().equals("NullObjectPattern_test.java")){
//						System.out.println(diffText);
//					}	
					
					BufferedReader br = new BufferedReader(new StringReader(diffText));
					String line = null;
					
					//System.out.println(diffText);
					
					while((line = br.readLine())!= null){ 
						line = line.trim();
						
						line = line.replaceAll("\t", " ");
						
						// iterate over parser checks for addition
						if (line.startsWith("+")){
							// TODO method for incrementing total LOC added?
							for (String check: checks){
								
//								System.out.println("Diff line --> " + line);
//								System.out.println("Usage pattern --> " + check);
								
								if (file.getCatchBlocks().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										addedCatchBlock = checkAddedNull(addedCatchBlock, currentHash, diffText, check);
									}
									
								} else if (file.getCollVars().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									String p = pattern.trim();
									
									if (line.contains(p)){
										addedCollVar = checkAddedNull(addedCollVar, currentHash, diffText, check);
									}
									
								} else if (file.getOptVars().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									String p = pattern.trim();
									
									if (line.contains(p)){
										addedOptVar = checkAddedNull(addedOptVar, currentHash, diffText, check);
									}
								} else if (file.getNODPs().contains(check)){
									// check for return statement addition (presumably last piece)
									String pattern = check.substring(check.lastIndexOf(CHECK_SEPERATOR)+1, check.length());
									String p = pattern.trim();
									
									if (line.contains(p)){
										addedNODP = checkAddedNull(addedNODP, currentHash, diffText, check);
									} 
									
								}
								
							}
						}						
					}
				}
				
				out.reset();
			}
						
			// add counts from analysis to developer counts (if within threshold)
			if (addedNullChecks > 0 && addedNullChecks <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedNullCounts(addedNullChecks);
				}
			}
			
			if (addedCollVar > 0 && addedCollVar <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedCollCounts(addedCollVar);
				}
			}
			
			if (addedOptVar > 0 && addedOptVar <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedOptCounts(addedOptVar);
				}
				
			}
			
			if (addedCatchBlock > 0 && addedCatchBlock <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedCatchCounts(addedCatchBlock);
				}
			}
		
			if (addedNODP > 0 && addedNODP <= 10) {
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedNODPCounts(addedNODP);
				}
			}
			
			if (derefNullChecks > 0 && derefNullChecks <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setDerefCount(derefNullChecks);
				}
			}			
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
	}
	
	private int checkAddedNull(int added, String newHash, String diffText, String check){
		if (isAddition(diffText, check)){
			System.out.println("Null usage pattern was added at revision " + newHash);
			
			added +=1;			
			

		} else if (isCheckAddition(diffText, check)){
			System.out.println("Null check pattern was added at revision " + newHash);
			
			added +=1;
		}
		
		return added;
	}
	
	private int checkAddedGenerics(int added, String newHash, String diffText, String line, String check){		
		// get part after last checksep; that's what we search diff for
		
		int count = StringUtils.countMatches(check, Character.toString(CHECK_SEPERATOR));
		String code = "";
		
		if (count == 1){
			code = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
		}
		
		if (count == 2){
			code = check.substring(check.lastIndexOf(CHECK_SEPERATOR)+1, check.length());			
		}
		
		if (isGenericsAddition(diffText, line, code)){
			System.out.println("\n Generics pattern " + code + " was added at revision " + newHash + "\n");
			
//			System.out.println("******************** Diff Text ********************");
//			System.out.println(diffText);
			
			added +=1;
		}
		
		return added;
	}
	
	private boolean isGenericsAddition(String diff, String line, String check){
		
		int count = 0;		
		
		if (line.contains(check)){
			count = StringUtils.countMatches(diff, check);
			
			if (count == 1){
				return true;
			}			
		}
		
		return false;
	}
	
	private int checkGenericsRemoval(int removed, String newHash, String diffText, String check){
		if (check.contains("public") || check.contains("private") || check.contains("protected")){
			if (isGenericsFieldRemoval(diffText, check)){
				System.out.println("**** Generics usage pattern " + check + " was removed at revision " + newHash + " ****");
				
				System.out.println("******************** Diff Text ********************");
				System.out.println(diffText);
				
				removed +=1;
			}
		}
		
		if (check.contains(">")){
			if (isGenericsVariableRemoval(diffText, check)){
				System.out.println("**** Generics usage pattern " + check + " was removed at revision " + newHash + " ****");
				
				System.out.println("******************** Diff Text ********************");
				System.out.println(diffText);
				
				removed +=1;
			} 			
		}
		
		return removed;
	}

	private boolean isGenericsFieldRemoval(String diffText, String check) {
		
		String field = check.substring(check.lastIndexOf(" "), check.indexOf(";"));
		
		int count1 = StringUtils.countMatches(diffText, check);
		
		int count2 = StringUtils.countMatches(diffText, field);
		
		if (count1 == 1 && count2 >= 2){
			return true;
		}
		
		return false;
	}

	private boolean isGenericsVariableRemoval(String diffText, String check) {

		//System.out.println(check);
		

		String variable = check.substring(check.indexOf(">")+1, check.indexOf("=")).trim();
		String type = check.substring(0, check.indexOf(" "));
		//System.out.println("Checking for removal of " + variable + "of type " + type);
		
		int count1 = StringUtils.countMatches(diffText, check);
		int count2 = StringUtils.countMatches(diffText, variable);
		int count3 = StringUtils.countMatches(diffText, type);
		
		if (count1 == 1 && count2 >= 2 && count3 >= 4){
			return true;
		}

		return false;
	}

	private int checkNullRemoval(int removed, String newHash, String diffText, String check) {
		if (isRemoval(diffText, check)){
			//System.out.println("Null usage pattern was removed at revision " + newHash);
			
			removed +=1;

		} else if (isCheckRemoval(diffText, check)){
			//System.out.println("Null usage pattern was removed at revision " + newHash);
			
			removed +=1;

		}
		return removed;
	}

	private String getOldHash(String newH) {
		String oldHash = "";
		
		for (int i=0; i < revisions.size()-1; i++){
			RevCommit rev = revisions.get(i);
			String hash = ObjectId.toString(rev.getId());
			
			if (hash.equals(newH)){
				oldHash = ObjectId.toString(revisions.get(i+1));
			}
			
		}
		return oldHash;
	}
	
	private boolean isGenericParameterAddition(String diffText, String check) {
		String segments[] = check.split(Character.toString(CHECK_SEPERATOR));
		String method = segments[0];
		
		StringBuilder sb = new StringBuilder(method);
		sb.append("(");
		
		for (int i=1; i < segments.length; i++){
			sb.append(segments[i]);
			
			if (i < segments.length-1){
				sb.append(",");
			}
		}
		sb.append(")");
		
		String pattern = sb.toString().trim();
		
		int count = StringUtils.countMatches(diffText, pattern);
		
		if (count == 1){
			return true;
		}
		
		return false;
	}

	private boolean isAddition(String diff, String check) {
		int count = 0;
		
		// for patterns with 3 parts, i.e. NODP
		if (StringUtils.countMatches(check, Character.toString(CHECK_SEPERATOR)) == 2){
			// break string down into 3 parts to check for presence of pattern
			
			String type = check.substring(0, check.indexOf(CHECK_SEPERATOR));
			type = type.trim();
			//System.out.println("Type for NODP addition check --> " + type);
			String ret = check.substring(check.lastIndexOf(CHECK_SEPERATOR), check.length()); 
			ret = ret.trim();
			//System.out.println("Return for NODP addition check --> " + ret);
			
			if (diff.contains(type) && diff.contains(ret)){
				count = StringUtils.countMatches(diff, type);
				int count2 = StringUtils.countMatches(diff, ret);
				
				if (count == 5 && count2 == 1)
					return true;
			}
			
		} else {
			// code (relevant pattern)
			String pattern = check.substring(check.indexOf(CHECK_SEPERATOR) +1, check.length());
			pattern = pattern.trim();
			
			count = StringUtils.countMatches(diff, pattern);
			
			if (count == 1)
				return true;
		}
	
		return false;
	}
	
	private boolean isCheckAddition (String diff, String ifAndNext){
	
		// null check
		String ifStatement = ifAndNext.substring(0, ifAndNext.indexOf(CHECK_SEPERATOR));
				
		int count = StringUtils.countMatches(diff, ifStatement);
		
		if (count ==1 ){
			return true;
		}
		
		return false;
		
		
	}

	private boolean isRemoval(String diff, String check) {
		// NODP pattern removal not included -- TODO: analyze for removal of usage

		//String method = check.substring(0, check.indexOf(CHECK_SEPERATOR));
		String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
		
		int count = StringUtils.countMatches(diff, pattern);				
		
		if (count == 1){
			return true;
		}

		return false;
	}
	
	private boolean isCheckRemoval (String diff, String ifAndNext){
		int count = 0;
		
		// null check
		String ifStatement = ifAndNext.substring(0, ifAndNext.indexOf(CHECK_SEPERATOR));
		// statement following if
		String nextStatement = ifAndNext.substring(ifAndNext.indexOf(CHECK_SEPERATOR) +1, ifAndNext.length());
		
		count = StringUtils.countMatches(diff, ifStatement);
		
		if (diff.contains(nextStatement) && count == 1){
			return true;
		}
		
		return false;
	}




}