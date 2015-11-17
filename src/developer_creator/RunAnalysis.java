package developer_creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

import code_parser.ModelRepository;
import code_parser.ModelSourceFile;
import util.Configuration;

public class RunAnalysis {

	public static String repoName = "L-Puzzle_Solver";
	public static String userName = "kjlubick";
	public static String developerName = "Kevin Lubick";
	public static String github_url = "https://github.com/" + userName + "/" + repoName + ".git";
	public static String gitCloneCmd = "git clone " + github_url;
	public static String localRepoDir = "." + File.separator + repoName + File.separator;
	public static String repoLocalFile = localRepoDir + ".git";
	
	

	public static void main(String[] args) throws Exception {
		
		File f1 = new File("repos.txt");
		Configuration config = new Configuration(developerName, repoName);
		String opFile = config.getOpFile();
		
		System.setOut(new PrintStream(new FileOutputStream(opFile)));
		
		InputStream is = new FileInputStream(opFile);
		
		try {
			Scanner sc = new Scanner (f1);
			
			while (sc.hasNextLine()){
				String line = sc.nextLine();
				
				// master branch
				if (StringUtils.countMatches(line, ",") == 2){
					
					repoName = line.substring(0, line.indexOf(","));
					userName = line.substring(line.indexOf(",")+2, line.lastIndexOf(","));
					developerName = line.substring(line.lastIndexOf(",")+2, line.length());
					
					System.out.println(repoName + " -- " + userName + " -- " + developerName);
					
					runAnalysis();
					
					// TODO move file to folder for output
					
				}
				
				// other branches of current master branch (3 ','s)
				// clone with url then checkout branch to analyze?
			}
			
			sc.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static void runAnalysis() throws FileNotFoundException,
			IOException, InterruptedException, RepositoryNotFoundException {
		

		Runtime rt = Runtime.getRuntime();

		ModelDeveloper dev = new ModelDeveloper(developerName);
		dev.setUserName(userName);
		// TODO add pseudo name to reporting?

		File directory = new File(localRepoDir);

		clearDirectory(directory);
		directory.delete();

		Process p3 = rt.exec(gitCloneCmd);

		System.out.println(p3.waitFor());

		// System.out.println("Project cloned!");

		// set repository history
		ModelRepository repository = new ModelRepository(new File(repoLocalFile));
		Git gitHub = repository.getGitRepository();

		if (repository.setRepositoryRevisionHistory(gitHub, dev) != null) {
//						ArrayList<RevCommit> commits = repository.getRevisions();
//						for (RevCommit commit: commits){
//							System.out.println(ObjectId.toString(commit.getId()));
//						}

			// set source files for each directory
			repository.setSourceFiles(localRepoDir);

			// set history for each file
			for (ModelSourceFile f : repository.getSourceFiles()) {
				repository.setFileRevisionHistory(gitHub, f);
			}

			// Analyze ASTs for all revisions for usage patterns (and addition)
			System.out.println("\n ************ ANALYZING FOR USAGE PATTERN ADDITION ************\n");
			repository.revertAndAnalyzeForNullAddition(gitHub, localRepoDir, dev, repoName);
			
			Process p4 = rt.exec(gitCloneCmd);
			System.out.println(p4.waitFor());
			
			repository.setSourceFiles(localRepoDir);
			
			// Analyze all revisions for removal of usage patterns 
			System.out.println("\n ************ ANALYZING FOR USAGE PATTERN REMOVAL ************\n");
			repository.revertAndAnalyzeForNullRemoval(gitHub, localRepoDir, dev, repoName);
		}
	}

	// File users = new File("test-github-users.txt");
	// FileReader in1 = new FileReader(users);
	// BufferedReader br = new BufferedReader(in1);
	//
	// String line;
	// while ((line=br.readLine()) != null){
	// Runtime rt = Runtime.getRuntime();
	// String username = line;
	// //dummyRepoRun(rt);
	//
	// // get all repositories for a given Github account/username
	// //String username = "mpeterson2";
	// ModelDeveloper dev = new ModelDeveloper();
	// dev.setUserName(username);
	// TODO can I just set the username and name here rather than automating it?
	// --> use it to check to make sure commit belongs to dev
	// Process p1 = rt.exec("C:/SW/CommandLine/GnuWin32/bin/wget
	// --no-check-certificate -O user.txt https://api.github.com/users/" +
	// username);
	// // Process p1 = rt.exec("curl -o user.txt https://api.github.com/users/"
	// + username);
	// System.out.println(p1.waitFor());
	//
	// FileReader in2 = new FileReader(new File("user.txt"));
	// BufferedReader br1 = new BufferedReader(in2);
	//
	// String line1;
	// while ((line1=br1.readLine()) != null){
	// if (line1.contains("name")){
	// String developer = line1.substring(line1.indexOf(":") + 3,
	// line1.length()-2);
	// System.out.println(developer);
	// dev.setDevName(developer);
	// }
	// }
	//
	// if (dev.getDevName() == null){
	// FileReader in5 = new FileReader(new File("user.txt"));
	// BufferedReader br5 = new BufferedReader(in5);
	//
	// String line5;
	// while ((line5=br5.readLine()) != null){
	// if (line5.contains("login")){
	// String loginName = line5.substring(line5.indexOf(":") + 3,
	// line5.length()-2);
	// System.out.println(loginName);
	// dev.setDevName(loginName);
	//
	// }
	// }
	//
	// br5.close();
	// }
	//
	// br1.close();
	//
	//
	// Process p2 = rt.exec("C:/SW/CommandLine/GnuWin32/bin/wget
	// --no-check-certificate -O output.txt
	// https://api.github.com/search/repositories?q=language:java+user:" +
	// username);
	// //Process p2 = rt.exec("curl -o output.txt
	// https://api.github.com/search/repositories?q=language:java+user:" +
	// username);
	// System.out.println(p2.waitFor());
	//
	// FileReader in3 = new FileReader(new File("output.txt"));
	// ArrayList<String> urls = new ArrayList<String>();
	// BufferedReader br3 = new BufferedReader(in3);
	//
	// String line2;
	// while ((line2 = br3.readLine()) != null){
	// if (line2.contains("clone_url")){
	// String url = line2.substring(line2.indexOf("http"),
	// line2.lastIndexOf("\""));
	// System.out.println(url);
	// urls.add(url);
	// }
	// }
	//
	// br3.close();
	//
	//
	// FileReader in4 = new FileReader(new File("output.txt"));
	// ArrayList<String> repoNames = new ArrayList<String>();
	// BufferedReader br4 = new BufferedReader(in4);
	//
	// String line3;
	// while ((line3 = br4.readLine()) != null){
	// if (line3.contains("full_name")){
	// String repoName = line3.substring(line3.indexOf("/")+1,
	// line3.lastIndexOf("\""));
	// System.out.println(repoName);
	// repoNames.add(repoName);
	// }
	// }
	//
	//
	// br4.close();
	//
	//
	// String repoName = "";
	//
	// // clone from list of urls (also gather repoNames for reporting -- should
	// match up with number of urls)
	// for (int i=0; i < urls.size(); i++){
	// if (repoNames.get(i) != null){
	// repoName = repoNames.get(i);
	// }
	//
	// // for cloning
	// String cloneURL = urls.get(i);
	//
	//
	// System.out.println(cloneURL);
	// // for creating repository
	// String directory = "." + cloneURL.substring(cloneURL.lastIndexOf("/"),
	// cloneURL.length());
	// System.out.println(directory);
	// File repoDir = new File(directory);
	//
	// clearOutDirectory(repoDir);
	// repoDir.delete();
	//
	// Process p3 = rt.exec("git clone " + cloneURL);
	// System.out.println(p3.waitFor());
	//
	// String repo = directory + "/.git";
	// File repoGit = new File(repo);
	//
	// //set repository history
	// ModelRepository repository = new ModelRepository(repoGit);
	// Git gitHub = repository.getGitRepository();
	//
	// if (repository.setRepositoryRevisionHistory(gitHub, dev) != null){
	// repository.setRepositoryRevisionHistory(gitHub, dev);
	//
	// //set source files for each directory
	// repository.setSourceFiles(directory);
	//
	// //set history for each file
	// for (ModelSourceFile f: repository.getSourceFiles()) {
	// repository.setFileRevisionHistory(gitHub, f);
	// }
	//
	// //Analyze ASTs for all revisions (right now for null checks)
	// repository.revertAndAnalyzeForNull(gitHub, directory, dev, repoName);
	//
	// }
	// }
	// }
	//
	// br.close();

	/**
	 * Deletes a directory.
	 * @param directory
	 * @return {@code true} if directory has been deleted otherwise {@code false}
	 */
	public static boolean clearDirectory(File directory) {
		if (!directory.exists() || (directory.isDirectory() && directory.listFiles().length == 0)) {
			return true;
		}
		return recursivelyClearDirectory(directory);
	}

	private static boolean recursivelyClearDirectory(File directory) {
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				if (!recursivelyClearDirectory(f))
					return false;
			}
			if (!f.delete()) {
				return false;
			}
		}
		return directory.listFiles().length == 0;
	}

}