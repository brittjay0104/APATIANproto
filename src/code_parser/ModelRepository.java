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
import java.util.regex.Pattern;

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
import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;

import developer_creator.ModelDeveloper;

public class ModelRepository {

	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;
	private File repoPath;
	private Repository repository;
	private Git git;
	private ArrayList<ModelSourceFile> sourceFiles;
	private ArrayList<RevCommit> revisions;

	// for creating developers and attaching analysis results (only developer with results will show up here)
	private List<ModelDeveloper> developers;

	// for getting unique developers
	private List<String> devs;

	private List<String> countedChecks;

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
		countedChecks = new ArrayList<String>();

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
		return countedChecks;
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
			for (int i = 0; i < sourceFiles.size(); i++){
				sourceFiles.remove(i);
			}
		}

		// set source files for the repository at the current revision
		for (Iterator<File> files = FileUtils.iterateFiles(f, extensions, true); files.hasNext();){
			ModelSourceFile file = new ModelSourceFile(files.next());
			sourceFiles.add(file);
			System.out.println("Added " + file.source_file.getName() + " to " + directory + " repository source files.");
			//file.setRepository(repository);
		}

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
				
				if (rev.getCommitterIdent().getName().equals(developer.getDevName()) || rev.getCommitterIdent().getName().equals(developer.getUserName())){
					developer.addCommit(ObjectId.toString(rev.getId()));
				}

				revisions.add(rev);
			}
			

		} catch (NoHeadException e) {
			System.out.println("NoHeadException thrown!");
			o = null;
		} catch (GitAPIException e) {
			System.out.println("GitAPIException thrown!");
		}

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

	/**
	 *
	 * Analyzes the current revision of the repository for Log4J API method usage.
	 *
	 * @param git - Git object that represents the repository to analyze.
	 * @throws IOException
	 */
	public void analyzeForLogging(Git git) throws IOException {
		ModelParser parser = new ModelParser();
		 for (ModelSourceFile file: getSourceFiles()){
			 //parse the files AST for null checks
			 String hash = ObjectId.toString(revisions.get(0).getId());
			 parser.parseForLogger(file, hash);
		 }

		 System.out.println("****Analysis complete for current revision****");
	}


	/**
	 *
	 * Analyzes the current revision of the repository for null checks.
	 *
	 * @param git - Git object that represents the repository to analyze.
	 * @throws IOException
	 */
	public void analyzeForNull(Git git, String newH, ModelDeveloper developer, String repoName) throws IOException {
		ModelParser parser = new ModelParser();

		String devName = developer.getDevName();

		if(developer.getCommits().contains(newH)){
			System.out.println("\n" + devName + " introduced null checks upon creation of the repository.");

			for (ModelSourceFile file: getSourceFiles()) {
				// parse the files AST for null checks
				List<String> checks = parser.parseForNull(file, newH);

				for (String check: checks){
					if (!(countedChecks.contains(check))){
						developer.incrementAddedNullCounts();
					}
				}

				//System.out.println("Number introduced in " + file.getName() +  " by " + devName + ": " + developer.getNullCount());

			}
		}

		System.out.println("****Analysis complete for first commit****");
		System.out.println(devName + " added null count = " + developer.getAddedNullCounts() + " in repository " + repoName);
		System.out.println(devName + " removed null count = " + developer.getRemovedNullCounts() + " in repository " + repoName);
		System.out.println(devName + " deref count = " + developer.getDerefCount() + " in repository " + repoName);


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

		for (int i = 0; i <= revisions.size()-1; i++) {

			//
			String newHash = ObjectId.toString(revisions.get(i).getId());
			String oldHash = "";
			if (!((revisions.get(i)) == revisions.get(revisions.size()-1))){
				oldHash = ObjectId.toString(revisions.get(i+1).getId());
			} else {
				analyzeForNull(git, newHash, dev, repoName);
			}

			try {

				// revert the repository
				git.revert().include(revisions.get(i)).call();
				System.out.println("\n" + "Reverted to commit " + newHash + "\n");

				setAndParseSource(directory, i, oldHash, newHash, dev);
				
				System.out.println("Added null checks = " + dev.getAddedNullCounts());
				System.out.println("Removed null checks = " + dev.getRemovedNullCounts());
				System.out.println("Null dereferences checked for null = " + dev.getDerefCount());
				
				// HERE! :) - need to remove other print statements and do some runs (archive in separate folder)

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

		setSourceFiles(directory);


		for (ModelSourceFile f: getSourceFiles()) {
			setFileRevisionHistory(git, f);

			ModelParser parser = new ModelParser();

			// parse the files AST for null checks
			List <String> checks = parser.parseForNull(f, oldHash);

			// diff the current and older revision
			diff(directory, f.getName(), checks, oldHash, newHash, dev);

			calculateNullValues(f, dev, newHash);
			
			



		}
	}

	private void calculateNullValues(ModelSourceFile file, ModelDeveloper dev, String newHash) {
		
		// **************** FIELDS (not initialized) ********************
		List<String> fields = file.getNullFields();
		// All assignments (for seeing if fields initialized) 
		List<String> assignments = file.getAssignments();
		
		List<String> removeFields = new ArrayList<String>();
		
		List<String> addAssignmentsForFile = new ArrayList<String>();
				
				
		System.out.println("\n\nNull Fields: ");
				
		for (String field: fields){
			System.out.println("		--> " + field);
					
			for (String assign: assignments){
				addAssignmentsForFile.add(assign);
				if (assign.contains(field)){
					System.out.println("Field assigned a value. Addition to knowledge value!");
					if (dev.getCommits().contains(newHash)){
						dev.incrementDerefCount();
						removeFields.add(field);						
					}
				}
			}
		}
			
		if (!(file.getNullFields().isEmpty())){
			// TODO deduct value?
			System.out.println("Nothing added to knowledge value.");
		}
		
		removeFields(fields, removeFields);
		addAssignments(addAssignmentsForFile, file);
		
		
			
		
		
		// ******************** VARIABLES (declared null; with method) ********************
		HashMap<String, ArrayList<String>> variables = file.getNullVars();
		Iterator<Entry<String, ArrayList<String>>> it2 = variables.entrySet().iterator();
		
		// Null Checks
		List<String> checks = file.getNullChecks();
		// HashMap for variables to remove after iterating over list
		HashMap<String, ArrayList<String>> removeVars = new HashMap<String, ArrayList<String>>();
		

		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it2.next();

			String methodDec = pairs.getKey();
			
			System.out.println("\nNull variables: ");
			for (String var: pairs.getValue()){
				System.out.println("		--> " + var + " found in " + methodDec);
				
				for (String check: checks){
					//System.out.println(check.substring(0, check.indexOf(CHECK_SEPERATOR)));
					
					if (check.contains(methodDec)){
						System.out.println("Null check in " + methodDec);
						
						if (check.contains(var)){
							System.out.println("Null check for null var. Add to value!");
							
							if (dev.getCommits().contains(newHash)){
								dev.incrementDerefCount();
							}
							
							if (removeVars.get(methodDec) == null){
								removeVars.put(methodDec, new ArrayList<String>());
							}
							
							// add to list of vars to be removed from file's list
							removeVars.get(methodDec).add(var);								
						}

					}
					//System.out.println("Null check found in file: " + check);
	
				}
								
			}
			if(!(file.getNullVars().isEmpty())){
				// TODO deduct?
				System.out.println("Nothing added to knowledge value.");				
			}
			

		}
		
		// iterate over hashmap and remove the necessary variables
		removeVariables(variables, removeVars);
		
		
		// ******************** VARIABLES (assigned null; with method) ********************
		
		HashMap<String, ArrayList<String>> assignedFile = file.getNullAssignments();
		Iterator<Entry<String, ArrayList<String>>> it3 = assignedFile.entrySet().iterator();
		
		HashMap<String, ArrayList<String>> removeAssigned = new HashMap<String, ArrayList<String>>();
		
		while (it2.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it3.next();
			
			String methodDec = pairs.getKey();
			
			System.out.println("\nNull assignments: ");
			for (String assign: pairs.getValue()){
				
				for (String check: checks){
					//System.out.println(check.substring(0, check.indexOf(CHECK_SEPERATOR)));
					
					if (check.contains(methodDec)){
						
						//System.out.println("Null check in " + methodDec);
						
						if (check.contains(assign)){
							System.out.println("Null check for null assignment. Add to value!");
							
							if (dev.getCommits().contains(newHash)){
								dev.incrementDerefCount();								
							}
							
							if (removeAssigned.get(methodDec) == null){
								removeAssigned.put(methodDec, new ArrayList<String>());
							}
							
							removeAssigned.get(methodDec).add(assign);
							
						}

					}
				}
				System.out.println("		--> " + assign + " found in " + methodDec);
				
			}
			
		}
		
		if (!(file.getNullAssignments().isEmpty())){
			// TODO deduct value?
			System.out.println("Nothing added to knowledge value.");
		}	
		
		// remove necessary fields
		removeVariables(assignedFile, removeAssigned);
		

	}

	private void addAssignments(List<String> assignments, ModelSourceFile file) {
		for (String assign: assignments){
			file.addAssignment(assign);
		}
	}

	private void removeFields(List<String> fields, List<String> removeFields) {
		
		for (String field: removeFields){
			fields.remove(field);
		}
		
	}

	public void removeVariables(HashMap<String, ArrayList<String>> variables, HashMap<String, ArrayList<String>> removeVars) {

		Iterator<Entry<String, ArrayList<String>>> it = removeVars.entrySet().iterator();
		
		while (it.hasNext()){
			Map.Entry<String, ArrayList<String>> pairs = (Map.Entry<String, ArrayList<String>>)it.next();
			
			String methodDec = pairs.getKey();
			
			for (String var: removeVars.get(methodDec)){
				variables.get(methodDec).remove(var);
			}
		}
	}

	private void diff(String directory, String filename, List<String> checks, String oldH, String newH, ModelDeveloper developer) {
		File repoDir = new File(directory);

		List<String> addedNullChecks = new ArrayList<String>();

		Git git;

		try {
			git = Git.open(repoDir);
			// next to current revision (older)
			String oldHash = oldH;
			// current revision
			String newHash = newH;

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

				if (diffText.contains(filename)){

					BufferedReader br = new BufferedReader(new StringReader(diffText));
					String line = null;

					while((line = br.readLine())!= null){
						line = line.trim();
						//System.out.println(line);
						for (String check: checks){
							if (line.startsWith("-") && line.contains(check.substring(0, check.indexOf(CHECK_SEPERATOR)))){
								developer.incrementLinesRemovedCount();
								// continue reading to find next line of null check make sure still there when line contains (-) and (+)
								if (isRemoval(diffText, check)){
									System.out.println("Null check was removed at revision " + newHash);

									if (developer.getCommits().contains(newHash)){
										developer.incrementRemovedNullCounts();
									}

									countedChecks.add(check);
								}
							} else if (line.startsWith("+") && line.contains(check.substring(0, check.indexOf(CHECK_SEPERATOR)))){
								developer.incrementLinesAddedCount();
								
								if (isAddition(diffText, check)){
									System.out.println("Null check was added at revision " + newHash);

									// list for added null checks added here (only add if not already there?)
									addedNullChecks.add(check);

									if (developer.getCommits().contains(newHash)){
										developer.incrementAddedNullCounts();
									}

									countedChecks.add(check);

								}
							}
						}

					}

				}

				out.reset();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
	}

	private boolean isAddition(String diff, String check) {
		int count = StringUtils.countMatches(diff, check.substring(0, check.indexOf(CHECK_SEPERATOR)));

		if (count == 1){
			return true;
		}

		return false;
	}

	private boolean isRemoval(String diff, String check) {
		String s2 = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
		int count = StringUtils.countMatches(diff, check.substring(0, check.indexOf(CHECK_SEPERATOR)));

		if (diff.contains(s2) && count == 1){
			return true;
		}

		return false;
	}




}
