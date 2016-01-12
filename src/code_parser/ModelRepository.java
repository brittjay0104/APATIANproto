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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.apache.commons.lang.StringUtils;

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
	 * Reverts and analyzes source files in the repository at each revision for usage patterns, focusing
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
		System.out.println(devName + " added generic field count = " + dev.getAddedGenericsFieldsCount() + " in repository " + repoName);
		System.out.println(devName + " added generic method count = " + dev.getAddedGenericsMethodsCount() + " in repository " + repoName);
		System.out.println(devName + " added generic invocations count = " + dev.getAddedGenericsInvocsCount() + " in repository " + repoName);
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
		System.out.println("	--> Added generic fields = " + dev.getAddedGenericsFieldsCount());
		System.out.println("	--> Added generic methods = " + dev.getAddedGenericsMethodsCount());
		System.out.println(" 	--> Added generic invocations = " + dev.getAddedGenericsInvocsCount());
	}
	
	public boolean hasGenerics(){
		
		// TODO: analysis for if repository has any generics at all
		
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
			HashMap<String, List<String>> map = parser.parseForGenerics(f);
			List<String> fields = map.get("fields");
			List<String> methods = map.get("methods");
			List<String> invocs = map.get("invocations");
			List<String> params = map.get("parameters");
			List<String> varDecs = map.get("varDecs");
			
			
			for (String field: fields){
				addUsagePattern(field);
			}
			additionDiff(directory, f, fields, previousHash, currentHash, dev);
			
			for (String method: methods){
				addUsagePattern(method);
			}
			additionDiff(directory, f, methods, previousHash, currentHash, dev);
			
			for (String invoc: invocs){
				addUsagePattern(invoc);
			}
			additionDiff(directory, f, invocs, previousHash, currentHash, dev);
			
			for (String param: params){
				addUsagePattern(param);
			}
			additionDiff(directory, f, params, previousHash, currentHash, dev);
			
			for (String varDec: varDecs){
				addUsagePattern(varDec);
			}
			additionDiff(directory, f, varDecs, previousHash, currentHash, dev);
			
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
			
			for (DiffEntry diff : diffs){
				
				df.format(diff);
				diff.getOldId();
				String diffText = out.toString("UTF-8");
				
				if (diffText.contains(file.getName())){
					
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
										removedCatchBlock = checkRemoval(removedCatchBlock, currentHash, diffText, check);
									}
								} else if (check.contains("Collections")){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										removedCollVar = checkRemoval(removedCollVar, currentHash, diffText, check);
									}
								} else if (check.contains("Optional")){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										removedOptVar = checkRemoval(removedOptVar, currentHash, diffText, check);
									}
								} else if (StringUtils.countMatches(check, Character.toString(CHECK_SEPERATOR)) != 2){
									String nullcheck = check.substring(0, check.indexOf(CHECK_SEPERATOR));
									String nc = nullcheck.trim();
									
									if (line.contains(nc)){
										removedNullChecks = checkRemoval(removedNullChecks, currentHash, diffText, check);									
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
		
		int addedGenericFields = 0;
		int addedGenericMethods = 0;
		int addedGenericInvocs = 0;
		int addedGenericParams = 0;
		int addedGenericVarDecs = 0;
		
		
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
			

			for(DiffEntry diff : diffs)
			{
				
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
								// GENERICS HERE!
								else if (file.getGenericFields().contains(check)){
									String pattern1 = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									String p1 = pattern1.trim();
									
									String pattern2 = check.substring(0, check.indexOf(CHECK_SEPERATOR));
									String p2 = pattern2.trim();
									
									if (line.contains(p1) && line.contains(p2)){
										addedGenericFields = checkAddedGenerics(addedGenericFields, currentHash, diffText, check);
									}
								}
								else if (file.getGenericMethods().contains(check)){
									String pattern1 = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									String p1 = pattern1.trim();
									
									String pattern2 = check.substring(0, check.indexOf(CHECK_SEPERATOR));
									String p2 = pattern2.trim();
									
									if (line.contains(p1) && line.contains(p2)){
										addedGenericMethods = checkAddedGenerics(addedGenericMethods, currentHash, diffText, check);
									}
								}
								else if (file.getGenericInvocations().contains(check)){
									String pattern1 = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									String p1 = pattern1.trim();
									
									String pattern2 = check.substring(0, check.indexOf(CHECK_SEPERATOR));
									String p2 = pattern2.trim();
									
									if (line.contains(p1) && line.contains(p2)){
										addedGenericInvocs = checkAddedGenerics(addedGenericInvocs, currentHash, diffText, check);
									}
								}
								else if (file.getGenericParameters().contains(check)){
									String type = check.substring(0, check.indexOf(CHECK_SEPERATOR)).trim();
									String variable = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.lastIndexOf(CHECK_SEPERATOR)).trim();
									String method = check.substring(check.lastIndexOf(CHECK_SEPERATOR)+1, check.length()).trim();
									
									if (line.contains(type) && line.contains(variable) && line.contains(method)){
										addedGenericParams = checkAddedGenerics(addedGenericParams, currentHash, diffText, check);
									}
								}
								
								else if (file.getGenericVarDeclarations().contains(check)){
									String type = check.substring(0, check.indexOf(CHECK_SEPERATOR)).trim();
									String variable = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.lastIndexOf(CHECK_SEPERATOR)).trim();
									String method = check.substring(check.lastIndexOf(CHECK_SEPERATOR)+1, check.length()).trim();
									
									if (line.contains(type) && line.contains(variable) && line.contains(method)){
										addedGenericVarDecs = checkAddedGenerics(addedGenericVarDecs, currentHash, diffText, check);
									}
								}
								
								else {
									String nullcheck = check.substring(0, check.indexOf(CHECK_SEPERATOR));
									String nc = nullcheck.trim();
									
									if (line.contains(nc)){
										addedNullChecks = checkAddedNull(addedNullChecks, currentHash, diffText, check);
										derefNullChecks = calculateDerefValue(file, check, developer, derefNullChecks);
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
			
			if (addedGenericFields > 0 && addedGenericFields <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedGenericFields(addedGenericFields);
				}
			}
			
			if (addedGenericMethods >0 && addedGenericMethods <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedGenericMethods(addedGenericMethods);
				}
			}
			
			if (addedGenericInvocs > 0 && addedGenericInvocs <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedGenericInvocs(addedGenericInvocs);
				}
			}
			
			if (addedGenericParams > 0 && addedGenericParams <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedGenericParams(addedGenericParams);
				}
			}
			
			if (addedGenericVarDecs > 0 && addedGenericVarDecs <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedGenericParams(addedGenericVarDecs);
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
	
	private int checkAddedGenerics(int added, String newHash, String diffText, String check){
		if (isGenericsAddition(diffText, check)){
			System.out.println("Generics pattern was added at revision " + newHash);
			
			added +=1;
		}
		
		return added;
	}
	
	private int checkRemoval(int removed, String newHash, String diffText, String check) {
		if (isRemoval(diffText, check)){
			System.out.println("Null usage pattern was removed at revision " + newHash);
			
			removed +=1;

		} else if (isCheckRemoval(diffText, check)){
			System.out.println("Null usage pattern was removed at revision " + newHash);
			
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
	
	private boolean isGenericsAddition(String diff, String check){
		
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		
		// parameters and variable dec -- 3 parts
		if (StringUtils.countMatches(check, Character.toString(CHECK_SEPERATOR)) == 2){
			String type = check.substring(0, check.indexOf(CHECK_SEPERATOR)).trim();
			String variable = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.lastIndexOf(CHECK_SEPERATOR)).trim();
			String method = check.substring(check.lastIndexOf(CHECK_SEPERATOR)+1, check.length()).trim();
			
			count1 = StringUtils.countMatches(diff, type);
			count2 = StringUtils.countMatches(diff, variable);
			count3 = StringUtils.countMatches(diff, method);
			
			if (count1 == 1 && count2 == 1 && count3 == 1){
				return true;
			}
						
		}
		
		// need both parts of check (front and back) to be added
		String pattern1 = check.substring(check.indexOf(CHECK_SEPERATOR) +1, check.length()).trim();
		String pattern2 = check.substring(0, check.indexOf(CHECK_SEPERATOR)).trim();
		
		count1 = StringUtils.countMatches(diff, pattern1);
		count2 = StringUtils.countMatches(diff, pattern2);
		
		if (count1 == 1 && count2 == 1)
			return true;
		
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