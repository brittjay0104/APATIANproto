package code_parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

import developer_creator.ModelDeveloper;
import node_visitor.VariableData;

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

	// for recency
	HashMap <String, String> recency = new HashMap<String, String>();
	//generics
	private int classInstanceDC = 1;
	private int expMethInvocDC = 1;
	private int impMethInvocDC = 1;
	private int typeDecDC = 1;
	private int typeArgMethDC = 1;
	private int wildcardDC = 1;
	private int nestedDC = 1;
	private int typeParamMethDC = 1;
	private int typeParamFieldDC = 1;
	private int diamondDC = 1;
	private int boundsDC = 1;
	
	//exceptions
	private int throwsMethodDC = 1;
	private int tryStatementDC = 1;
	private int catchBlockDC = 1;
	private int multiCatchBlockDC = 1;
	private int tryWithResourceDC = 1;
	private int finallyBlockDC = 1;
	private int throwStatementDC = 1;
	private int exceptionClassDC = 1;
	private int checkedExceptionDC = 1;
	private int uncheckedExceptionDC = 1;
	private boolean isChecked = false;
	private boolean isUnchecked = false;
	
	// variables
	private int primitiveDC = 1;
	private int nonPrimitiveDC = 1;
	private int fieldDC = 1;
	private int localVariableDC = 1;
	private int parameterDC = 1;
	private int publicVariableDC = 1;
	private int privateVariableDC = 1;
	private int protectedVariableDC = 1;
	private int staticVariableDC = 1;
	private int finalVariableDC = 1;
	private int transientVariableDC = 1;
	private int volatileVariableDC = 1;	
	
	// general objects 
	String jarFile;
	String sourceDir;


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
	
	public void setJarFile (String file){
		jarFile = file;
	}
	
	public void setSourceDirectory (String dir){
		sourceDir = dir;
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
	
	/**
	 * Create jar for project source code.
	 * @param directory
	 * @param jarFile
	 * @throws IOException 
	 */
	public void createJarFile(String directory, String jarFile) throws IOException {
//		Manifest manifest = new Manifest();
//		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile));
		add(new File(directory), target);
		target.close();
	}
	
	private void add(File source, JarOutputStream target) throws IOException {
		BufferedInputStream in = null;
		  try
		  {
		    if (source.isDirectory())
		    {
		      String name = source.getPath().replace("\\", "/");
		      if (!name.isEmpty())
		      {
		        if (!name.endsWith("/"))
		          name += "/";
		        JarEntry entry = new JarEntry(name);
		        entry.setTime(source.lastModified());
		        target.putNextEntry(entry);
		        target.closeEntry();
		      }
		      for (File nestedFile: source.listFiles())
		        add(nestedFile, target);
		      return;
		    }

		    JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
		    entry.setTime(source.lastModified());
		    target.putNextEntry(entry);
		    in = new BufferedInputStream(new FileInputStream(source));

		    byte[] buffer = new byte[1024];
		    while (true)
		    {
		      int count = in.read(buffer);
		      if (count == -1)
		        break;
		      target.write(buffer, 0, count);
		    }
		    target.closeEntry();
		  }
		  finally
		  {
		    if (in != null)
		      in.close();
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
	public void revertAndAnalyzeForPatternAddition(Git git, String directory, ModelDeveloper dev, String repoName, String jarFile) throws IOException{

		ArrayList<String> commits = dev.getCommits();
		String devName = dev.getDevName();
		
		// iterate over repository revisions - parse at each revision 
		//     (can compare dev commits when determined with diff that pattern added/removed)
	
		for (int i=0; i<revisions.size(); i++){
			RevCommit current = revisions.get(i);
			String currentHash = ObjectId.toString(current);
			String previousHash = "";
			
			// only analyze if developer made contribution to that commit
			if (!(currentHash.equals(ObjectId.toString(revisions.get(revisions.size()-1)))) && commits.contains(currentHash)){
				previousHash = ObjectId.toString(revisions.get(i+1));
				
 				
 				//System.out.println(difference);
 				
				//pass in how long ago commit made?
				setAndParseSource(directory, previousHash, currentHash, dev, current);
					
				diffPrettyPrint(dev, currentHash, previousHash);
				
				try{
					
					git.revert().include(current).call();
					
					System.out.println("\n Reverted revision " + currentHash + " to " + previousHash + "\n");
					
				} catch (NoMessageException e) {
					System.out.println("NoMessageException thrown!");
				} catch (UnmergedPathsException e) {
					System.out.println("Paths did not successfully merge!");
					continue;
				} catch (JGitInternalException e) {
					System.out.println("Something's not right...");
					continue;
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
//		System.out.println("Added generics to repository " + repoName + " = " + dev.getAllAddedGenerics());
//		System.out.println("");
//		System.out.println(devName + " added type argument method count = " + dev.getAddedTypeArgumentMethods());
//		System.out.println("	--> recency = " + recency.get("type argument methods"));
//		System.out.println(devName + " added wildcard count = " + dev.getAddedWildcard());
//		System.out.println("	--> recency = " + recency.get("wildcards"));
//		System.out.println(devName + " added type declaration count = " + dev.getAddedTypeDecs());
//		System.out.println("	--> recency = " + recency.get("type declarations"));
//		System.out.println(devName + " added type parameter method count = " + dev.getAddedTypeParamMethods());
//		System.out.println("	--> recency = " + recency.get("type parameter methods"));
//		System.out.println(devName + " added type parameter field count = " + dev.getAddedTypeParamFields());
//		System.out.println("	--> recency = " + recency.get("type parameter fields"));
//		System.out.println(devName + " added diamond count = " + dev.getAddedDiamond());
//		System.out.println("	--> recency = " + recency.get("diamonds"));
//		System.out.println(devName + " added method invocation count = " + dev.getAddedMethodInvocs());
//		System.out.println("	--> recency = " + recency.get("method invocations"));
//		System.out.println(devName + " added implicit method invocation count = " + dev.getAddedImpMethInvocs());
//		System.out.println("	--> recency = " + recency.get("implicit method invocations"));
//		System.out.println(devName + " added class instantiation count = " + dev.getAddedClassInstances());
//		System.out.println("	--> recency = " + recency.get("class instantiations"));
//		System.out.println(devName + " added nested count = " + dev.getAddedNested());
//		System.out.println("	--> recency = " + recency.get("nested"));
//		System.out.println(devName + " added bounds count = " + dev.getAddedBounds());
//		System.out.println("	--> recency = " + recency.get("bounds"));
		
		// EXCEPTIONS
//		System.out.println(devName + " added throws method count = " + dev.getAddedThrowsMethods());
//		System.out.println("	--> recency = " + recency.get("throws methods"));
//		System.out.println(devName + " added try statement count = " + dev.getAddedTryStatements());
//		System.out.println("	--> recency = " + recency.get("try statements"));
//		System.out.println(devName + " added catch block count = " + dev.getAddedCatchBlocks());
//		System.out.println("	--> recency = " + recency.get("catch blocks"));
//		System.out.println(devName + " added multi-catch block count = " + dev.getAddedMultiCatchBlocks());
//		System.out.println("	--> recency = " + recency.get("multi-catch blocks"));
//		System.out.println(devName + " added try with resources count = " + dev.getAddedTryWithResources());
//		System.out.println("	--> recency = " + recency.get("try with resources"));
//		System.out.println(devName + " added finally block count = " + dev.getAddedFinallyBlocks()); 
//		System.out.println("	--> recency = " + recency.get("finally blocks"));
//		System.out.println(devName + " added throw statement count = " + dev.getAddedThrowStatements());
//		System.out.println("	--> recency = " + recency.get("throw statements"));
//		System.out.println(devName + " added exception class count = " + dev.getAddedExceptionClasses());
//		System.out.println("	--> recency = " + recency.get("exception classes"));
//		System.out.println(devName + " added checked exception count = " + dev.getAddedCheckedExceptions());
//		System.out.println("	--> recency = " + recency.get("checked exceptions"));
//		System.out.println(devName + " added unchecked exception count = " + dev.getAddedUncheckedExceptions());
//		System.out.println("	--> recency = " + recency.get("unchecked exceptions"));
//		System.out.println(devName + " added catch exception count = " + dev.getAddedCatchExceptions());
		
		// VARIABLES
		System.out.println(devName + " added primitive variable count = " + dev.getAddedPrimitives());
		System.out.println("	--> recency = " + recency.get("primitive variables"));
		
		System.out.println(devName + " added non-primitive variable count = " + dev.getAddedNonPrimitives());
		System.out.println("	--> recency = " + recency.get("non primitive variables"));
		
		System.out.println(devName + " added field count = " + dev.getAddedFields());
		System.out.println("	--> recency = " + recency.get("fields"));
		
		System.out.println(devName + " added local variable count = " + dev.getAddedLocalVariables());
		System.out.println("	--> recency = " + recency.get("local variables"));
		
		System.out.println(devName + " added parameter count = " + dev.getAddedParameters());
		System.out.println("	--> recency = " + recency.get("parameters"));
		
		System.out.println(devName + " added public variable count = " + dev.getAddedPublicVariables()); 
		System.out.println("	--> recency = " + recency.get("public variables"));
		
		System.out.println(devName + " added private variable count = " + dev.getAddedPrivateVariables());
		System.out.println("	--> recency = " + recency.get("private variables"));
		
		// HERE!!
		System.out.println(devName + " added exception class count = " + dev.getAddedExceptionClasses());
		System.out.println("	--> recency = " + recency.get("exception classes"));
		
		System.out.println(devName + " added checked exception count = " + dev.getAddedCheckedExceptions());
		System.out.println("	--> recency = " + recency.get("checked exceptions"));
		
		System.out.println(devName + " added unchecked exception count = " + dev.getAddedUncheckedExceptions());
		System.out.println("	--> recency = " + recency.get("unchecked exceptions"));
		
		System.out.println(devName + " added catch exception count = " + dev.getAddedCatchExceptions());
		
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
//		System.out.println("	--> Added type argument methods = " + dev.getAddedTypeArgumentMethods());
//		System.out.println("	--> recency = " + recency.get("type argument methods"));
//		System.out.println("	--> Added wildcards = " + dev.getAddedWildcard());
//		System.out.println("	--> recency = " + recency.get("wildcards"));
//		System.out.println("	--> Added type declarations  = " + dev.getAddedTypeDecs());
//		System.out.println("	--> recency = " + recency.get("type declarations"));
//		System.out.println("	--> Added type parameter methods = " + dev.getAddedTypeParamMethods());
//		System.out.println("	--> recency = " + recency.get("type parameter methods"));
//		System.out.println("	--> Added type parameter fields = " + dev.getAddedTypeParamFields());
//		System.out.println("	--> recency = " + recency.get("type parameter fields"));
//		System.out.println("	--> Added diamonds = " + dev.getAddedDiamond());
//		System.out.println("	--> recency = " + recency.get("diamonds"));
//		System.out.println("	--> Added explicit method invocations = " + dev.getAddedMethodInvocs());
//		System.out.println("	--> recency = " + recency.get("method invocations"));
//		System.out.println("	--> Added implicit method invocations = " + dev.getAddedImpMethInvocs());
//		System.out.println("	--> recency = " + recency.get("implicit method invocations"));
//		System.out.println("	--> Added class instantiations = " + dev.getAddedClassInstances());
//		System.out.println("	--> recency = " + recency.get("class instantiations"));
//		System.out.println("	--> Added nested = " + dev.getAddedNested());
//		System.out.println("	--> recency = " + recency.get("nesteds"));
//		System.out.println("	--> Added bounds = " + dev.getAddedBounds());
//		System.out.println("	--> recency = " + recency.get("bounds"));
		
		// EXCEPTIONS
//		System.out.println("	--> Added throws methods = " + dev.getAddedThrowsMethods());
//		System.out.println("	--> recency = " + recency.get("throws methods"));
//		System.out.println("	--> Added try statements = " + dev.getAddedTryStatements());
//		System.out.println("	--> recency = " + recency.get("try statements"));
//		System.out.println("	--> Added catch blocks = " + dev.getAddedCatchBlocks());
//		System.out.println("	--> recency = " + recency.get("catch blocks"));
//		System.out.println("	--> Added multi-catch blocks = " + dev.getAddedMultiCatchBlocks());
//		System.out.println("	--> recency = " + recency.get("multi-catch blocks"));
//		System.out.println("	--> Added try with resources = " + dev.getAddedTryWithResources());
//		System.out.println("	--> recency = " + recency.get("try with resources"));
//		System.out.println("	--> Added finally blocks = " + dev.getAddedFinallyBlocks()); 
//		System.out.println("	--> recency = " + recency.get("finally blocks"));
//		System.out.println("	--> Added throw statement = " + dev.getAddedThrowStatements());
//		System.out.println("	--> recency = " + recency.get("throw statements"));
//		System.out.println("	--> Added exception classes = " + dev.getAddedExceptionClasses());
//		System.out.println("	--> recency = " + recency.get("exception classes"));
//		System.out.println("	--> Added checked exceptions = " + dev.getAddedCheckedExceptions());
//		System.out.println("	--> recency = " + recency.get("checked exceptions"));
//		System.out.println("	--> Added unchecked exceptions = " + dev.getAddedUncheckedExceptions());
//		System.out.println("	--> recency = " + recency.get("unchecked exceptions"));
//		
//		System.out.println("	--> Added catch exceptions = " + dev.getAddedCatchExceptions());
		
		// VARIABLES
		System.out.println("	--> Added primitive types = " + dev.getAddedPrimitives());
		System.out.println("	--> recency = " + recency.get("primitive variables"));
		
		System.out.println("	--> Added non-primitive types = " + dev.getAddedNonPrimitives());
		System.out.println("	--> recency = " + recency.get("non primitive variables"));
		
		System.out.println("	--> Added fields = " + dev.getAddedFields());
		System.out.println("	--> recency = " + recency.get("fields"));
		
		System.out.println("	--> Added local variables = " + dev.getAddedLocalVariables());
		System.out.println("	--> recency = " + recency.get("local variables"));
		
		System.out.println("	--> Added parameters = " + dev.getAddedParameters());
		System.out.println("	--> recency = " + recency.get("parameters"));
		
		System.out.println("	--> Added public variables = " + dev.getAddedPublicVariables()); 
		System.out.println("	--> recency = " + recency.get("public variables"));
		
		System.out.println("	--> Added private variables = " + dev.getAddedPrivateVariables());
		System.out.println("	--> recency = " + recency.get("private variables"));
		
		System.out.println("	--> Added protected variables = " + dev.getAddedProtectedVariables());
		System.out.println("	--> recency = " + recency.get("protected variables"));
		
		System.out.println("	--> Added static variables = " + dev.getAddedStaticVariables());
		System.out.println("	--> recency = " + recency.get("static variables"));
		
		System.out.println("	--> Added final variables = " + dev.getAddedFinalVariables());
		System.out.println("	--> recency = " + recency.get("final variables"));
		
		System.out.println("	--> Added transient variables = " + dev.getAddedTransientVariables());
		System.out.println("	--> recency = " + recency.get("transient variables"));
		
		System.out.println("	--> Added volatile variables = " + dev.getAddedVolatileVariables());
		System.out.println("	--> recency = " + recency.get("volatile variables"));

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
	public void setAndParseSource(String directory, String previousHash, String currentHash, ModelDeveloper dev, RevCommit currentCommit) throws IOException {
		
		System.out.println("\n****Parsing for addition at revision " + currentHash + "****\n");
		
		setSourceFiles(directory);
		
		for (ModelSourceFile f: getSourceFiles()) {
			//System.out.println("\n File for diff --> " + f.getName() + "\n");
			
			//setFileRevisionHistory(git, f);

			ModelParser parser = new ModelParser();
 
			// parse the files AST for null checks, nodps, coll/opt usage, catches with NPE
			// diff the current and older revision
			//List <String> checks = parser.parseForNull(f, previousHash);
//			for (String check: checks){
//				addUsagePattern(check);
//			}
//			// passing in ifAndNext
//			additionDiff(directory, f, checks, previousHash, currentHash, dev);
			
			//COMMENTED OUT FOR TESTING
			
			
		
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
			
			List<String> generics = f.getAllGenerics();
			
			for (String s: generics){
				addUsagePattern(s);
			}
			genericsAdditionDiff(directory, f, generics, previousHash, currentHash, dev, currentCommit);
			
			
			
			//END COMMENT
			
			//EXCEPTIONS
			
			//System.out.println("About to run parseForExceptions in ModelRepository");
			parser.parseForExceptions(f, directory, jarFile);
			
			List<String> throwsMethods = f.getThrowsMethods();
			for (String throwMethod: throwsMethods){
				addUsagePattern(throwMethod);
			}
			
			exceptionsAdditionDiff(directory, f, throwsMethods, previousHash, currentHash, dev, currentCommit);
			
			List<String> tryStatements = f.getTryStatements();
			for (String tryStatement: tryStatements){
				addUsagePattern(tryStatement);
			}
			
			exceptionsAdditionDiff(directory, f, tryStatements, previousHash, currentHash, dev, currentCommit);
			
			List<String> catchBlocks = f.getCatchBlocks();
			for (String catchBlock: catchBlocks){
				addUsagePattern(catchBlock);
			}
			
			exceptionsAdditionDiff(directory, f, catchBlocks, previousHash, currentHash, dev, currentCommit);
			
			List<String> multiCatchBlocks = f.getMultiCatchBlocks();
			for (String catchBlock: multiCatchBlocks){
				addUsagePattern(catchBlock);
			}
			
			exceptionsAdditionDiff(directory, f, multiCatchBlocks, previousHash, currentHash, dev, currentCommit);
						
			List<String> tryWithResources = f.getTryWithResources();
			for (String tryWithResource: tryWithResources){
				addUsagePattern(tryWithResource);
			}
			
			exceptionsAdditionDiff(directory, f, tryWithResources, previousHash, currentHash, dev, currentCommit);
			
			List<String> finallyBlocks = f.getFinallyBlocks();
			for (String finallyBlock: finallyBlocks){
				addUsagePattern(finallyBlock);
			}
			
			exceptionsAdditionDiff(directory, f, finallyBlocks, previousHash, currentHash, dev, currentCommit);	
			
			List<String> throwStatements = f.getThrowStatements();
			for (String throwStatement: throwStatements){
				addUsagePattern(throwStatement);
			}
			
			exceptionsAdditionDiff(directory, f, throwStatements, previousHash, currentHash, dev, currentCommit);
			
			List<String> exceptionClasses = f.getExceptionClasses();
			for (String exceptionClass: exceptionClasses){
				addUsagePattern(exceptionClass);
			}
			
			exceptionsAdditionDiff(directory, f, exceptionClasses, previousHash, currentHash, dev, currentCommit);
			
//			List<String> checkedExceptions = f.getCheckedExceptions();
//			for (String checked: checkedExceptions){
//				addUsagePattern(checked);
//			}
//			
//			exceptionsAdditionDiff(directory, f, checkedExceptions, previousHash, currentHash, dev, currentCommit);
			
//			List<String> uncheckedExceptions = f.getUncheckedExceptions();
//			for (String unchecked: uncheckedExceptions){
//				addUsagePattern(unchecked);
//			}
			
//			exceptionsAdditionDiff(directory, f, uncheckedExceptions, previousHash, currentHash, dev, currentCommit);
			
			
			//VARIABLES
			parser.parseForVariables(f, directory, jarFile);
			
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
	
	public void genericsAdditionDiff(String directory, ModelSourceFile file, List<String> patterns, String prevH, String currH, ModelDeveloper developer, RevCommit timeDiff) {
		
		File repoDir = new File(directory);
		
		int addedGenerics = 0;
		
		int addedTypeArgumentMethods = 0;
		int addedWildCards = 0;
		int addedTypeDeclarations = 0;
		int addedTypeParameterMethods = 0;
		int addedTypeParameterFields = 0;
		int addedDiamonds = 0;
		int addedMethodInvocations = 0;
		int addedImpMethInvocations = 0;
		int addedClassInstatiations = 0;
		int addedNested = 0;
		int addedBounds = 0;
		
		
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
			
			for (DiffEntry diff: compute){
				//System.out.println("MIGHT BE FILE NAME FOR COMPARISON:");
				df.format(diff);
				String diffText = out.toString("UTF-8");
				
				// checking if rename (paths won't equal if rename)
				if (diff.getNewPath().equals(diff.getOldPath())){
					
					if (diffText.contains(file.getName())){
						
						BufferedReader br = new BufferedReader(new StringReader(diffText));
						String line = null;
						
						while ((line = br.readLine()) != null){
							
							line = line.trim();
							
							line = line.replaceAll("\t", " ");
							
							// iterate over parser patterns for addition
							if (line.startsWith("+")){
								
								for (String pattern: patterns){		
									
									if (line.contains(pattern)){																				
										
										addedGenerics = checkAddedGenerics(addedGenerics, currentHash, diffText, pattern);
										
										// generics iteration
										Iterator it = file.getGenerics().entrySet().iterator();
										while(it.hasNext()){
											Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
												
											// extract to its own method (call in each if statement below to put in recency
											
											if (genericsClassified("type argument methods", pattern, pair)){
												addedTypeArgumentMethods = classifyAddedGenerics("type argument methods", addedTypeArgumentMethods, pattern, pair);
											
												if (typeArgMethDC == 1){
													String difference = checkDifference(timeDiff);
													typeArgMethDC +=1;													
													recency.put("type argument methods", difference);
												}
											}
											
											if (genericsClassified("wildcards", pattern, pair)){
												addedWildCards = classifyAddedGenerics("wildcards", addedWildCards, pattern, pair);
												
												if (wildcardDC == 1){
													String difference = checkDifference(timeDiff);
													wildcardDC +=1;													
													recency.put("wildcards", difference);
												}												
											}
											
											if (genericsClassified("type declarations", pattern, pair)){
												addedTypeDeclarations = classifyAddedGenerics("type declarations", addedTypeDeclarations, pattern, pair);
												
												if (typeDecDC == 1){
													String difference = checkDifference(timeDiff);
													typeDecDC +=1;													
													recency.put("type declarations", difference);
												}												
											}
											
											if (genericsClassified("type parameter methods", pattern, pair)){
												addedTypeParameterMethods = classifyAddedGenerics("type parameter methods", addedTypeParameterMethods, pattern, pair);
												
												if (typeParamMethDC == 1){
													String difference = checkDifference(timeDiff);
													typeParamMethDC +=1;													
													recency.put("type parameter methods", difference);
												}
											}
											
											if (genericsClassified("type parameter fields", pattern, pair)){
												addedTypeParameterFields = classifyAddedGenerics("type parameter fields", addedTypeParameterFields, pattern, pair);
												
												if (typeParamFieldDC == 1){
													String difference = checkDifference(timeDiff);
													typeParamFieldDC +=1;													
													recency.put("type parameter fields", difference);
												}
											}
											
											if (genericsClassified("diamonds", pattern, pair)){
												addedDiamonds = classifyAddedGenerics("diamonds", addedDiamonds, pattern, pair);
												
												if (diamondDC == 1){
													String difference = checkDifference(timeDiff);
													diamondDC +=1;													
													recency.put("diamonds", difference);
												}
											}
											
											if (genericsClassified("method invocations", pattern, pair)){
												addedMethodInvocations = classifyAddedGenerics("method invocations", addedMethodInvocations, pattern, pair);
												
												if (expMethInvocDC == 1){
													String difference = checkDifference(timeDiff);
													expMethInvocDC +=1;													
													recency.put("method invocations", difference);
												}
											}
											
											if (genericsClassified("implicit method invocations", pattern, pair)){
												addedImpMethInvocations = classifyAddedGenerics("implicit method invocations", addedImpMethInvocations, pattern, pair);
												
												if (impMethInvocDC == 1){
													String difference = checkDifference(timeDiff);
													impMethInvocDC +=1;													
													recency.put("implicit method invocations", difference);
												}
											}
											
											if (genericsClassified("class instantiations", pattern, pair)){
												addedClassInstatiations = classifyAddedGenerics("class instantiations", addedClassInstatiations, pattern, pair);
												
												if (classInstanceDC == 1){
													String difference = checkDifference(timeDiff);
													classInstanceDC +=1;													
													recency.put("class instantiations", difference);
												}
											}
											
											if (genericsClassified("nesteds", pattern, pair)){
												addedNested = classifyAddedGenerics("nesteds", addedNested, pattern, pair);
												
												if (nestedDC == 1){
													String difference = checkDifference(timeDiff);
													nestedDC +=1;													
													recency.put("nesteds", difference);
												}
											}
											
											if (genericsClassified("bounds", pattern, pair)){
												addedBounds = classifyAddedGenerics("bounds", addedBounds, pattern, pair);
												
												if (boundsDC == 1){
													String difference = checkDifference(timeDiff);
													boundsDC +=1;													
													recency.put("bounds", difference);
												}
											}
										}									
										
									}
									
								}
								
							}
							
						}
						
					}
					
				out.reset();
				}
			}
			
			
			developer.setAddedGenerics(addedGenerics);
			
			
			developer.setAddedTypeArgumentMethods(addedTypeArgumentMethods);
			developer.setAddedWildcard(addedWildCards);
			developer.setAddedTypeDeclarations(addedTypeDeclarations);
			developer.setAddedTypeParameterMethods(addedTypeParameterMethods);
			developer.setAddedTypeParameterFields(addedTypeParameterFields);
			developer.setAddedDiamond(addedDiamonds);
			developer.setAddedMethodInvocation(addedMethodInvocations);
			developer.setAddedImpMethInvocations(addedImpMethInvocations);
			developer.setAddedClassInstantiations(addedClassInstatiations);
			developer.setAddedNested(addedNested);
			developer.setAddedBounds(addedBounds);
				
			
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
		
		
	}
	
	private String checkDifference(RevCommit timeDiff) {
		PersonIdent author = timeDiff.getAuthorIdent();
		java.util.Date authorDate = author.getWhen();
						
		java.util.Date currentDate = new java.util.Date();
		
		long duration = currentDate.getTime() - authorDate.getTime();
		long diffInHoursNow =TimeUnit.MILLISECONDS.toHours(duration);

		//System.out.println(diffInHours);
		String difference = "";
		 	
		// last week
		if (diffInHoursNow > 0 && diffInHoursNow < 169){
			difference = "week";
		}
		// last month
		else if (diffInHoursNow >= 169 && diffInHoursNow <= 730){
			difference = "month";
		}
		// more than month, less than 6 months
		else if (diffInHoursNow >= 731 && diffInHoursNow <= 4380){
			difference = "months";
		}
		// more than month, less than year
		else if (diffInHoursNow >= 4381 && diffInHoursNow <= 8760){
			difference = "year";
		}
		// more than a year
		else if (diffInHoursNow >= 8761){
			difference = "years";
		}
		return difference;
	}

	private int classifyAddedGenerics(String key, int addedGenerics, String pattern, Map.Entry<String, List<String>> pair) {
		if (pair.getKey().equals(key)){
			for (String s: pair.getValue()){
//				System.out.println("Full Pattern --> " + s);
//				System.out.println("Pattern --> " + pattern);
				if (s.contains(pattern)){
					//System.out.println("Matched " + pattern + " to " + s + "!");
					addedGenerics += 1;
				}
			}
		}
		return addedGenerics;
	}
	
	private boolean genericsClassified(String key, String pattern, Map.Entry<String, List<String>> pair){
		if (pair.getKey().equals(key)){
			for (String s: pair.getValue()){
//				System.out.println("Full Pattern --> " + s);
//				System.out.println("Pattern --> " + pattern);
				if (s.contains(pattern)){
					//System.out.println("Matched " + pattern + " to " + s + "!");
					return true;
				}
			}
		}
		
		return false;
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
	
	private int checkAddedGenerics(int added, String newHash, String diffText, String code){		
		// get part after last checksep; that's what we search diff for
	
		if (isGenericsAddition(diffText, code)){
			System.out.println("\n Generics pattern " + code + " was added at revision " + newHash + "\n");
			
//			System.out.println("******************** Diff Text ********************");
//			System.out.println(diffText);
			
			added +=1;
		}
		
		return added;
	}
	
	private boolean isGenericsAddition(String diff, String check){
		
		int count = 0;		

		count = StringUtils.countMatches(diff, check);
		
		if (count == 1){
			return true;
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
	
	
public void exceptionsAdditionDiff(String directory, ModelSourceFile file, List<String> checks, String oldH, String newH, ModelDeveloper developer, RevCommit timeDiff) {
		
		File repoDir = new File(directory);
		
		int addedThrowsMethods = 0;
		int addedTryStatements = 0;
		int addedCatchBlocks = 0;
		int addedCatchExceptions = 0;
		int addedMultiCatchBlocks = 0;
		int addedTryWithResources = 0;
		int addedFinallyBlocks = 0;
		int addedThrowStatements = 0;
		int addedExceptionClasses = 0;
		int addedCheckedExceptions = 0;
		int addedUncheckedExceptions = 0;
		
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
					
					BufferedReader br = new BufferedReader(new StringReader(diffText));
					String line = null;
					
					//System.out.println(diffText);
					
					while((line = br.readLine())!= null){ 
						line = line.trim();
						
						line = line.replaceAll("\t", " ");
						
						// iterate over parser checks for addition
						if (line.startsWith("+")){
							// remove duplicates from checks
							Set<String> hs = new HashSet<String>();
							hs.addAll(checks);
							checks.clear();
							checks.addAll(hs);
							
							for (String check: checks){
								
								//System.out.println("Current revision --> " + currentHash);
								//System.out.println("Previous revision --> " + previousHash);
								//System.out.println("Diff line --> " + line);
								//System.out.println("Usage pattern --> " + check);
								
								if (file.getThrowsMethods().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									//System.out.println("line:\n" + line);
									//System.out.println("pattern:\n" + pattern);
									
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedThrowsMethods = checkAddedExceptions(addedThrowsMethods, currentHash, diffText, pattern, file);
										
										if (throwsMethodDC == 1){
											String difference = checkDifference(timeDiff);
											throwsMethodDC +=1;													
											recency.put("throws methods", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}									
								} 
								if (file.getTryStatements().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedTryStatements = checkAddedExceptions(addedTryStatements, currentHash, diffText, pattern, file);
										
										if (tryStatementDC == 1){
											String difference = checkDifference(timeDiff);
											tryStatementDC +=1;													
											recency.put("try statements", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}
								} 
								if (file.getCatchBlocks().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedCatchBlocks = checkAddedExceptions(addedCatchBlocks, currentHash, diffText, pattern, file);
										
										if (file.getCatchExceptions().contains(check) && isExceptionsAddition(diffText, pattern)){
											addedCatchExceptions += 1;
										}
										
										if (catchBlockDC == 1){
											String difference = checkDifference(timeDiff);
											catchBlockDC +=1;													
											recency.put("catch blocks", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}
									
								} 
								if (file.getMultiCatchBlocks().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim().replace(" ", "");
									line = line.replace(" ", "");
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedMultiCatchBlocks = checkAddedExceptions(addedMultiCatchBlocks, currentHash, diffText, pattern, file);
										
										if (multiCatchBlockDC == 1){
											String difference = checkDifference(timeDiff);
											multiCatchBlockDC +=1;													
											recency.put("multi-catch blocks", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}
								} 
								if (file.getTryWithResources().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.lastIndexOf(CHECK_SEPERATOR));
									pattern = pattern.trim();
									// TODO check if resource was added as opposed to entire block
									String resource = check.substring(check.lastIndexOf(CHECK_SEPERATOR), check.length());
									
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedTryWithResources = checkAddedExceptions(addedTryWithResources, currentHash, diffText, pattern, file);
										
										if (tryWithResourceDC == 1){
											String difference = checkDifference(timeDiff);
											tryWithResourceDC +=1;													
											recency.put("try with resources", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}
								} 
								if (file.getFinallyBlocks().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedFinallyBlocks = checkAddedExceptions(addedFinallyBlocks, currentHash, diffText, pattern, file);
										
										if (finallyBlockDC == 1){
											String difference = checkDifference(timeDiff);
											finallyBlockDC +=1;													
											recency.put("finally blocks", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}									
 								} 
								if (file.getThrowStatements().contains(check)){
									String pattern = check.substring(check.indexOf(CHECK_SEPERATOR)+1, check.length());
									String p = pattern.trim();
									
									if (line.contains(p)){
										//System.out.println(pattern + " added here!");
										addedThrowStatements = checkAddedExceptions(addedThrowStatements, currentHash, diffText, pattern, file);
										
										if (throwStatementDC == 1){
											String difference = checkDifference(timeDiff);
											throwStatementDC +=1;													
											recency.put("throw statements", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									}
								} 
								if (file.getExceptionClasses().contains(check)){
									String pattern = check.substring(check.lastIndexOf(CHECK_SEPERATOR)+1, check.length());
									pattern = pattern.trim();
									
									if (line.contains(pattern)){
										//System.out.println(pattern + " added here!");
										addedExceptionClasses = checkAddedExceptions(addedExceptionClasses, currentHash, diffText, pattern, file);
										
										if (exceptionClassDC == 1){
											String difference = checkDifference(timeDiff);
											exceptionClassDC +=1;													
											recency.put("exception classes", difference);
										}
										
										if (isChecked == true){
											addedCheckedExceptions += 1;
											isChecked = false;
											
											if (checkedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												checkedExceptionDC +=1;													
												recency.put("checked exceptions", difference);
											}
										}
										
										if (isUnchecked == true){
											addedUncheckedExceptions += 1;
											isUnchecked = false;
											
											if (uncheckedExceptionDC == 1){
												String difference = checkDifference(timeDiff);
												uncheckedExceptionDC +=1;													
												recency.put("unchecked exceptions", difference);
											}
										}
									} 
									
								} 								
							}
						}						
					}
				}
				
				out.reset();
			}
						
			// add counts from analysis to developer counts (if within threshold)
			if (addedThrowsMethods > 0 && addedThrowsMethods <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedThrowsMethods(addedThrowsMethods);
				}
			}
			
			if (addedTryStatements > 0 && addedTryStatements <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedTryStatements(addedTryStatements);
				}
			}
			
			if (addedCatchBlocks > 0 && addedCatchBlocks <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedCatchBlocks(addedCatchBlocks);
				}
			}
			
			if (addedCatchExceptions > 0 && addedCatchExceptions <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedCatchExceptions(addedCatchExceptions);
				}
			}
			
			if (addedMultiCatchBlocks > 0 && addedMultiCatchBlocks <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedMultiCatchBlocks(addedMultiCatchBlocks);
				}
			}
			
			if (addedTryWithResources > 0 && addedTryWithResources <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedTryWithResources(addedTryWithResources);
				}
			}
			
			if (addedFinallyBlocks > 0 && addedFinallyBlocks <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedFinallyBlocks(addedFinallyBlocks);
				}
			}
			
			if (addedThrowStatements > 0 && addedThrowStatements <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedThrowStatements(addedThrowStatements);
				}
			}
			
			if (addedExceptionClasses > 0 && addedExceptionClasses <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedExceptionClasses(addedExceptionClasses);
				}
			}
			
			if (addedCheckedExceptions > 0 && addedCheckedExceptions <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedCheckedExceptions(addedCheckedExceptions);
				}
			}
			
			if (addedUncheckedExceptions > 0 && addedUncheckedExceptions <= 10){
				if (developer.getCommits().contains(currentHash)){
					developer.setAddedUncheckedExceptions(addedUncheckedExceptions);
				}
			}
			
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println("GitAPIException caught!");
		}
	}

public void variablesAdditionDiff(String directory, ModelSourceFile file, List<VariableData> datas, String oldH, String newH, ModelDeveloper developer, RevCommit timeDiff){
	File repoDir = new File(directory);
	
	int addedPrimitives = 0;
	int addedNonPrimitives = 0;
	int addedFields = 0;
	int addedLocalVariables = 0;
	int addedParameters = 0;
	int addedPublicVariables = 0;
	int addedPrivateVariables = 0;
	int addedProtectedVariables = 0;
	int addedStaticVariables = 0;
	int addedFinalVariables = 0;
	int addedTransientVariables = 0;
	int addedVolatileVariables = 0;
	
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
				
				BufferedReader br = new BufferedReader(new StringReader(diffText));
				String line = null;
				
				//System.out.println(diffText);
				
				while((line = br.readLine())!= null){ 
					line = line.trim();
					
					line = line.replaceAll("\t", " ");
					
					// iterate over parser checks for addition
					if (line.startsWith("+")){
						// remove duplicates from checks
						Set<VariableData> hs = new HashSet<VariableData>();
						hs.addAll(datas);
						datas.clear();
						datas.addAll(hs);
						
						for (VariableData data: datas){
							
//							System.out.println("Current revision --> " + currentHash);
//							System.out.println("Previous revision --> " + previousHash);
//							System.out.println("Diff line --> " + line);
//							System.out.println("Usage pattern --> " + check);
							
							// traverse primitive variables in file
							for (VariableData prim: file.getPrimitives()){
								// find data point of interest (if exists) in list of primitive variables
								if (prim.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									//System.out.println("line:\n" + line);
									//System.out.println("pattern:\n" + pattern);
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedPrimitives = checkAddedVariables(addedPrimitives, currentHash, diffText, source, file);
										
										if (primitiveDC == 1){
											String difference = checkDifference(timeDiff);
											primitiveDC +=1;													
											recency.put("primitive variables", difference);
										}
										
									}
								}
							}
							
							for (VariableData nonPrim: file.getNonPrimitives()){
								if (nonPrim.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedNonPrimitives = checkAddedVariables(addedNonPrimitives, currentHash, diffText, source, file);
										
										if (nonPrimitiveDC == 1){
											String difference = checkDifference(timeDiff);
											nonPrimitiveDC +=1;													
											recency.put("non primitive variables", difference);
										}
									}
								}
							}

							for (VariableData field: file.getFields()){
								if (field.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedFields = checkAddedVariables(addedFields, currentHash, diffText, source, file);
										
										if (fieldDC == 1){
											String difference = checkDifference(timeDiff);
											fieldDC +=1;													
											recency.put("fields", difference);
										}
									}
								}
							}							

							for (VariableData localVar: file.getLocalVariables()){
								if (localVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedLocalVariables = checkAddedVariables(addedLocalVariables, currentHash, diffText, source, file);
										
										if (localVariableDC == 1){
											String difference = checkDifference(timeDiff);
											localVariableDC +=1;													
											recency.put("local variables", difference);
										}
									}
								}
							}
							
							for (VariableData param: file.getParameters()){
								if (param.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedParameters = checkAddedVariables(addedParameters, currentHash, diffText, source, file);
										
										if (parameterDC == 1){
											String difference = checkDifference(timeDiff);
											parameterDC +=1;													
											recency.put("parameters", difference);
										}
									}
								}
							}
							
							for (VariableData publicVar: file.getPublicVariables()){
								if (publicVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedPublicVariables = checkAddedVariables(addedPublicVariables, currentHash, diffText, source, file);
										
										if (publicVariableDC == 1){
											String difference = checkDifference(timeDiff);
											publicVariableDC +=1;													
											recency.put("public variables", difference);
										}
									}
								}
							}
							
							for (VariableData privateVar: file.getPrivateVariables()){
								if (privateVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedPrivateVariables = checkAddedVariables(addedPrivateVariables, currentHash, diffText, source, file);
										
										if (privateVariableDC == 1){
											String difference = checkDifference(timeDiff);
											privateVariableDC +=1;													
											recency.put("private variables", difference);
										}
									}
								}
							}
							
							for (VariableData protectedVar: file.getProtectedVariables()){
								if (protectedVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedProtectedVariables = checkAddedVariables(addedProtectedVariables, currentHash, diffText, source, file);
										
										if (protectedVariableDC == 1){
											String difference = checkDifference(timeDiff);
											protectedVariableDC +=1;													
											recency.put("protected variables", difference);
										}
									}
								}
							}
							
							for (VariableData staticVar: file.getStaticVariables()){
								if (staticVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedStaticVariables = checkAddedVariables(addedStaticVariables, currentHash, diffText, source, file);
										
										if (staticVariableDC == 1){
											String difference = checkDifference(timeDiff);
											staticVariableDC +=1;													
											recency.put("static variables", difference);
										}
									}
								}
							}
							
							for (VariableData finalVar: file.getFinalVariables()){
								if (finalVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedFinalVariables = checkAddedVariables(addedFinalVariables, currentHash, diffText, source, file);
										
										if (finalVariableDC == 1){
											String difference = checkDifference(timeDiff);
											finalVariableDC +=1;													
											recency.put("final variables", difference);
										}
									}
								}
							}
							
							for (VariableData transientVar: file.getTransientVariables()){
								if (transientVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedTransientVariables = checkAddedVariables(addedTransientVariables, currentHash, diffText, source, file);
										
										if (transientVariableDC == 1){
											String difference = checkDifference(timeDiff);
											transientVariableDC +=1;													
											recency.put("transient variables", difference);
										}
									}
								}
							}
							
							for (VariableData volatileVar: file.getVolatileVariables()){
								if (volatileVar.getSrcLineStr().equals(data.getSrcLineStr())){
									String source = data.getSrcLineStr();
									source = source.trim();
									
									if (line.contains(source)){
										//System.out.println(pattern + " added here!");
										addedVolatileVariables = checkAddedVariables(addedVolatileVariables, currentHash, diffText, source, file);
										
										if (volatileVariableDC == 1){
											String difference = checkDifference(timeDiff);
											volatileVariableDC +=1;													
											recency.put("volatile variables", difference);
										}
									}
								}
							}
						}
					}						
				}
			}
			
			out.reset();
		}
					
		// add counts from analysis to developer counts (if within threshold)
		if (addedPrimitives > 0 && addedPrimitives <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedPrimitives(addedPrimitives);
			}
		}
		
		if (addedNonPrimitives > 0 && addedNonPrimitives <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedNonPrimitives(addedNonPrimitives);
			}
		}
		
		if (addedFields > 0 && addedFields <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedFields(addedFields);;
			}
		}
		
		if (addedLocalVariables > 0 && addedLocalVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedLocalVariables(addedLocalVariables);
			}
		}
		
		if (addedParameters > 0 && addedParameters <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedParameters(addedParameters);
			}
		}
		
		if (addedPublicVariables > 0 && addedPublicVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedPublicVariables(addedPublicVariables);
			}
		}
		
		if (addedPrivateVariables > 0 && addedPrivateVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedPrivateVariables(addedPrivateVariables);
			}
		}
		
		if (addedProtectedVariables > 0 && addedProtectedVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedProtectedVariables(addedProtectedVariables);
			}
		}
		
		if (addedStaticVariables > 0 && addedStaticVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedStaticVariables(addedStaticVariables);;
			}
		}
		
		if (addedFinalVariables > 0 && addedFinalVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedFinalVariables(addedFinalVariables);
			}
		}
		
		if (addedTransientVariables > 0 && addedTransientVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedTransientVariables(addedTransientVariables);
			}
		}
		
		if (addedVolatileVariables > 0 && addedVolatileVariables <= 10){
			if (developer.getCommits().contains(currentHash)){
				developer.setAddedVolatileVariables(addedVolatileVariables);
			}
		}
		
		
	} catch (IOException e1) {
		e1.printStackTrace();
	} catch (GitAPIException e) {
		System.out.println("GitAPIException caught!");
	}
}
	private int checkAddedVariables(int added, String newHash, String diffText, String pattern, ModelSourceFile file){
		if (isVariablesAddition(diffText, pattern)){
			System.out.println("Variable was added at revision " + newHash);
				
			added +=1;
		}
		return added;
	}
	
	private boolean isVariablesAddition(String diffText, String code){
		int count = 0;		

		diffText = diffText.replace(" ", "");
		code = code.replace(" ", "");
		
		count = StringUtils.countMatches(diffText, code);
		
		if (count == 1){
			return true;
		}			
		
		return false;
	}
	
	private int checkAddedExceptions(int added, String newHash, String diffText, String pattern, ModelSourceFile file){

		if (isExceptionsAddition(diffText, pattern)){
			System.out.println("Exceptions usage pattern was added at revision " + newHash);
				
			added +=1;
			
			for (String type : file.getCheckedExceptions()){
				if (type.contains(pattern)){
					isChecked = true;
				}
			}
			
			for (String type : file.getUncheckedExceptions()){
				if (type.contains(pattern)){
					isUnchecked = true;
				}
			}
		}
			
			
		return added;
	}
	
	private boolean isExceptionsAddition(String diffText, String code){
		int count = 0;		

		diffText = diffText.replace(" ", "");
		code = code.replace(" ", "");
		
		count = StringUtils.countMatches(diffText, code);
		
		if (count == 1){
			return true;
		}			
		
		return false;
	}


}