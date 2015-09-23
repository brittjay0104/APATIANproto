package code_parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import node_visitor.NODP;

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
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.apache.commons.lang.StringUtils;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;
import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;

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

	// TODO : do I need these??
	private List<String> countedUsagePatterns;

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
		countedUsagePatterns = new ArrayList<String>();
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

	public List<String> getCountedChecks(){
		return countedUsagePatterns;
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
			//System.out.println("Added " + file.source_file.getName() + " to " + directory + " repository source files.");
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
	
	public boolean revExists (RevCommit rev){
		// TODO: code that checks the existing list to see if a RevCommit with the same ID/hash exists in the list
		
		return true;
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
	 * Reverts and analyzes source files in the repository at each revision (currently
	 * parsing for null checks).
	 *
	 * @param git - Git object that represents the repository to analyze.
	 * @param directory - String path to the directory of the repository in file system (without .git)
	 * @throws IOException
	 */
	public void revertAndAnalyzeForNull(Git git, String directory, ModelDeveloper dev, String repoName) throws IOException{

		ArrayList<String> commits = dev.getCommits();
		String devName = dev.getDevName();
		
		for (int i = 0; i <= commits.size()-1; i++) {

			String newHash = commits.get(i);
			String oldHash = "";
			
			if (!((commits.get(i)).equals(commits.get(commits.size()-1)))){
				oldHash = commits.get(i+1);
			} else {
				//analyzeForNull(git, newHash, dev, repoName);
				System.out.println("****Analysis complete for first commit****");
				System.out.println(devName + " added null count = " + dev.getAddedNullCounts() + " in repository " + repoName);
				System.out.println(devName + " removed null count = " + dev.getRemovedNullCounts() + " in repository " + repoName);
				System.out.println(devName + " deref count = " + dev.getDerefCount() + " in repository " + repoName);
				// TODO add print statements for other patterns
			}

			try {

				// revert the repository
				for (RevCommit rev: revisions){
					
					//System.out.println(ObjectId.toString(rev.getId()));
					if (ObjectId.toString(rev.getId()).equals(commits.get(i))){
						RevCommit revert = rev;
						git.revert().include(revert).call();
					}
				}
				
				System.out.println("\n" + "Reverted to commit " + newHash + "\n");
				
				setAndParseSource(directory, i, oldHash, newHash, dev);
				
				System.out.println("\nDiff of " + oldHash + " and " + newHash + ":");
				System.out.println("	--> Added null checks = " + dev.getAddedNullCounts());
				System.out.println("	--> Removed null checks = " + dev.getRemovedNullCounts());
				System.out.println("	--> Null dereferences checked for null = " + dev.getDerefCount());
				System.out.println("	--> Added Null Object Design Patterns = " + dev.getAddedNODPCounts());
//						// TODO: add print statements for new patterns

//				for (String commit: commits){
//					//System.out.println(ObjectId.toString(rev.getId()));
//					if (commit.equals(newHash)){
////						
////						
//					}
//				}

				

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
		}

	}

	/**
	 * @param directory
	 * @param i
	 * @param oldHash
	 * @throws IOException
	 */
	private void setAndParseSource(String directory, int i, String oldHash, String newHash, ModelDeveloper dev) throws IOException {
		
		System.out.println("****Parsing at revision " + newHash + "****");

		setSourceFiles(directory);
		
		for (ModelSourceFile f: getSourceFiles()) {
			//System.out.println(f.getName());
			
			setFileRevisionHistory(git, f);

			ModelParser parser = new ModelParser();
 
			// parse the files AST for null checks, nodps, coll/opt usage, catches with NPE
			// diff the current and older revision
//			List <String> checks = parser.parseForNull(f, oldHash);
//			diff(directory, f, checks, oldHash, newHash, dev);
			
			List<String> nodps = parser.parseForNODP(f, newHash);
			diff(directory, f, nodps, oldHash, newHash, dev);
			
		
			ArrayList<List<String>> npes = parser.parseForNPEAvoidance(f);
			
			List<String> catches = npes.get(2);
			//diff(directory, f, catches, oldHash, newHash, dev);

			
			// TODO works the same as null checks -- implement addition/removal
//			List<String> colls = npes.get(0);
//			diff(directory, f, colls, oldHash, newHash, dev);
			
//			List<String> opts = npes.get(1);
//			diff(directory, f, opts, oldHash, newHash, dev);
			
			
			
			//System.out.println(f.getName());
			
//			ModelParser parser = new ModelParser();
//
//			// parse the files AST for null checks
////			List <String> checks = parser.parseForNull(f, oldHash);
			
//			System.out.println("FROM SOURCE FILES LIST:");
//			
//			System.out.println(f.getName());
//			
//			if (changedFiles.contains(f.getName())){
//				System.out.println("FROM CHANGES FILES LIST:");
//				System.out.println(f.getName());
//				

//				
//			}



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

	public void diff(String directory, ModelSourceFile file, List<String> checks, String oldH, String newH, ModelDeveloper developer) {
		File repoDir = new File(directory);
		
		int addedNullChecks = 0;
		int removedNullChecks = 0;
		int derefNullChecks = 0;
		
		int addedCollVar = 0;
		int removedCollVar = 0;
		
		int addedOptVar = 0;
		int removedOptVar = 0;
		
		int addedCatchBlock = 0;
		int removedCatchBlock = 0;
		
		int addedNODP = 0;
		
		Git git;
		//current revision
		String newHash = newH;

		try {
			git = Git.open(repoDir);
			// next to current revision (older)
			String oldHash = getOldHash(newH);


			ObjectId headId = git.getRepository().resolve(newHash + "^{tree}");
			ObjectId oldId = git.getRepository().resolve(oldHash + "^{tree}");

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

					BufferedReader br = new BufferedReader(new StringReader(diffText));
					String line = null;

					while((line = br.readLine())!= null){
						//line = line.trim().replace(" = ", "=");
						//System.out.println(line);
						 
						
						
						for (String check: checks){
							//System.out.println("Diff Line == " + line);
							//System.out.println("Check compare ==" + );
							String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());	
							
							System.out.println("Usage Pattern --> " + check);
							System.out.println("Code from Diff --> " + line);
							
							if (line.startsWith("-") && (line.contains(check.substring(0, check.indexOf(CHECK_SEPERATOR))) 
									|| line.contains(pattern))){
								developer.incrementLinesRemovedCount();
								// continue reading to find next line of null check make sure still there when line contains (-) and (+)
																
								if (file.getCatchBlocks().contains(check)){
									System.out.println("Removed Catch Block --> " + pattern);
									removedCatchBlock = checkRemoval(removedCatchBlock, newHash, diffText, pattern);
									
								}  else if (file.getCollVars().contains(check)){
									System.out.println("Removed Collections --> " + pattern);
									//removedCollVar = checkRemoval(removedCollVar, newHash, diffText, check);
									
								} else if (file.getOptVars().contains(check)) {
									System.out.println("Removed Optional --> " + pattern);
									//removedOptVar = checkRemoval(removedOptVar, newHash, diffText, check);
									
								} 
								
								removedNullChecks = checkRemoval(removedNullChecks, newHash, diffText, check);
								
							} else if (line.startsWith("+") && (line.contains(check.substring(0, check.indexOf(CHECK_SEPERATOR))) 
									|| line.contains(pattern))){
								developer.incrementLinesAddedCount();
								
								if (file.getCatchBlocks().contains(check)){
									System.out.println("Added Catch Blocks --> " + pattern);
									addedCatchBlock = checkAdded(addedCatchBlock, newHash, diffText, pattern);
								}  else if (file.getCollVars().contains(check)){
									System.out.println("Added Collections --> " + pattern);
									//addedCollVar = checkAdded(addedCollVar, newHash, diffText, check);
									
								} else if (file.getOptVars().contains(check)) {
									System.out.println("Added Optional --> " + pattern);
									//addedOptVar = checkAdded(addedOptVar, newHash, diffText, check);
									
								} else if (file.getNODPs().contains(check)){
									System.out.println("Added NODP --> " + pattern);
									addedNODP = checkAdded(addedNODP, newHash, diffText, pattern);
								}
								
								addedNullChecks = checkAdded(addedNullChecks, newHash, diffText, pattern);
								
								if (developer.getCommits().contains(newHash)){
									// now see if this null check is related to any null fields, variables, or assignments
									derefNullChecks = calculateDerefValue(file, check, developer, derefNullChecks);
								}
							}
						}
						

					}

				}

				out.reset();
			}
			
			if (removedNullChecks <= 10 || removedCollVar <= 10 || removedOptVar <= 10 || removedCatchBlock <= 10){
				if (developer.getCommits().contains(newHash)){
					developer.setRemovedCounts(removedNullChecks);
					developer.setRemovedCollCounts(removedCollVar);
					developer.setRemovedOptCounts(removedOptVar);
					developer.setRemovedCatchCounts(removedCatchBlock);
					
				}
			}
			
			if (addedNullChecks <= 10 || addedCollVar <= 10 || addedOptVar <= 10 || addedCatchBlock <= 10 || addedNODP <= 10){
				if (developer.getCommits().contains(newHash)){
					developer.setAddedNullCounts(addedNullChecks);
					developer.setAddedCollCounts(addedCollVar);
					developer.setAddedOptCounts(addedOptVar);
					developer.setAddedCatchCounts(addedCatchBlock);
					developer.setAddedNODPCounts(addedNODP);
				}
			}
			
			if (derefNullChecks <= 10){
				if (developer.getCommits().contains(newHash)){
					developer.setDerefCount(derefNullChecks);
				}
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
	}

	private int checkAdded(int added, String newHash, String diffText, String pattern) {
		if (isAddition(diffText, pattern)){
			System.out.println("Null usage pattern was added at revision " + newHash);
			
			added +=1;			
			
			countedUsagePatterns.add(pattern);

		}		
		
		return added;
	}

	private int checkRemoval(int removed, String newHash, String diffText, String check) {
		if (isRemoval(diffText, check)){
			System.out.println("Null usage pattern was removed at revision " + newHash);
			
			removed +=1;

			countedUsagePatterns.add(check);
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

	private boolean isAddition(String diff, String check) {
		int count = 0;
		
		// for patterns with 3 parts, i.e. NODP
		if (StringUtils.countMatches(check, Character.toString(CHECK_SEPERATOR)) == 2){
			// TODO break string down into 3 parts to check for
			
			String type = check.substring(0, check.indexOf(CHECK_SEPERATOR));
			System.out.println("Type for NODP addition check --> " + type);
			String field = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.lastIndexOf(CHECK_SEPERATOR));
			System.out.println("Field for NODP addition check --> " + field);
			String ret = check.substring(check.lastIndexOf(CHECK_SEPERATOR), check.length()); 
			System.out.println("Return for NODP addition check --> " + ret);
			
		}
		
		count = StringUtils.countMatches(diff, check.substring(0, check.indexOf(CHECK_SEPERATOR)));						

		if (count == 1){
			return true;
		}

		return false;
	}

	private boolean isRemoval(String diff, String check) {
		int count = 0;
		String s2 = "";
		
		if (check.indexOf(CHECK_SEPERATOR) >=0){
			s2 = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
			count = StringUtils.countMatches(diff, check.substring(0, check.indexOf(CHECK_SEPERATOR)));			
		}
		
		
		

		if (diff.contains(s2) && count == 1){
			return true;
		}

		return false;
	}




}
