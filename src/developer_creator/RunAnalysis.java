package developer_creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import node_visitor.GenericsVisitor;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

import code_parser.ModelParser;
import code_parser.ModelRepository;
import code_parser.ModelSourceFile;
import util.Configuration;

public class RunAnalysis {

	public static String repoName = "";
	public static String userName = "";
	public static String developerName = "";
	

	public static void main(String[] args) throws Exception {
		
//		ModelSourceFile f = new ModelSourceFile(new File("Generics.java"));
////		ModelSourceFile f = new ModelSourceFile(new File("src/util/Configuration.java"));
//		
//		ModelParser p = new ModelParser();
//		
//		p.parseForGenerics(f);
		


		File repos = new File("repos.txt");
		
		InputStream is = null;
		OutputStream os = null;
		
		// read in list of repos; analyze for each line
		Scanner sc = new Scanner (repos);
		
		try {
			
			while (sc.hasNextLine()){
				String line = sc.nextLine();
				
				// master branch
				if (StringUtils.countMatches(line, ",") == 2 || StringUtils.countMatches(line, ",") == 1){
					
					RunAnalysis.setRepoName(line.substring(0, line.indexOf(",")));
					RunAnalysis.setDeveloperName(line.substring(line.lastIndexOf(",")+2, line.length()));
					RunAnalysis.setUserName(line.substring(line.indexOf(",")+2, line.lastIndexOf(",")));
					
					//RunAnalysis.setUserName(line.substring(line.indexOf(",")+2, line.length()));
					
					String dName = RunAnalysis.getDeveloperName();
					String rName = RunAnalysis.getRepoName();
					String uName = RunAnalysis.getUserName();
					
					Configuration config = new Configuration(dName, rName);
					String opFile = config.getOpFile();
					
					String opFile2 = "";
					File f1;
					File f2;
					
					if (line.contains("/")){
						opFile2 = opFile.replace("/", "-");
						System.setOut(new PrintStream(new FileOutputStream(opFile2)));
						
						// create files to populate after analysis
						f1 = new File(opFile2);
						f2 = new File("./archived-output/02-08-2016/" + opFile2);
						
						is = new FileInputStream(f1);
						os = new FileOutputStream(f2);
						
						byte[] buffer = new byte[1024];
						
						System.out.println(rName + " -- " + uName + " -- " + dName);
						
						runAnalysis("");
						
						// move file to folder for archived output
						int length;
						//copy file contents in bytes
						while ((length = is.read(buffer)) > 0){
							os.write(buffer, 0, length);
						}
						
						f1.delete();
						
						System.out.println("File copied successfully!");
					}
					
//					System.setOut(new PrintStream(new FileOutputStream(opFile)));
//					
//					f1 = new File(opFile);
//					f2 = new File("./archived-output/02-08-2016/" + opFile);
//					
//					is = new FileInputStream(f1);
//					os = new FileOutputStream(f2);
//					
//					byte[] buffer = new byte[1024];
//					
//					System.out.println(rName + " -- " + uName + " -- " + dName);
//					
//					runAnalysis("");
//					
//					// move file to folder for archived output
//					int length;
//					//copy file contents in bytes
//					while ((length = is.read(buffer)) > 0){
//						os.write(buffer, 0, length);
//					}
//					
//					f1.delete();
//					
//					System.out.println("File copied successfully!");
				}

				// TODO other branches of current master branch (3 ','s)
				// clone with url then checkout branch to analyze?
			}

			sc.close();
			is.close();  
			os.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}

	}
	
	private static void runAnalysis(String branch) throws FileNotFoundException,
			IOException, InterruptedException, RepositoryNotFoundException {
		

		//String github_url = "https://github.com/" + userName + "/" + repoName + ".git";
		String github_url = "https://github.com/" + repoName.trim() + ".git";
		System.out.println(github_url);
		String gitCloneCmd = "";
//		if (!(branch.equals(""))){
//			gitCloneCmd = "git clone -b " + branch + " " + github_url; 
//		}
		gitCloneCmd = "git clone " + github_url;
		System.out.println(gitCloneCmd);
		
		//String localRepoDir = "." + File.separator + repoName + File.separator;
		String localRepoDir = "." + File.separator + repoName.substring(repoName.indexOf("/"), repoName.length()) + File.separator;
		System.out.println(localRepoDir);
		String repoLocalFile = localRepoDir + ".git";
		System.out.println(repoLocalFile);
		
		Runtime rt = Runtime.getRuntime();

		ModelDeveloper dev = new ModelDeveloper(developerName);
		dev.setUserName(userName);
		// TODO add pseudo name to reporting?

		File directory = new File(localRepoDir);

		clearDirectory(directory);
		directory.delete();

		Process p3 = rt.exec(gitCloneCmd);

		System.out.println(p3.waitFor());

		System.out.println("Project cloned!");

		// set repository history
		ModelRepository repository = new ModelRepository(new File(repoLocalFile));
		Git gitHub = repository.getGitRepository();

		if (repository.setRepositoryRevisionHistory(gitHub, dev) != null) {

			// set source files for each directory
			repository.setSourceFiles(localRepoDir);

			// set history for each file
			for (ModelSourceFile f : repository.getSourceFiles()) {
				repository.setFileRevisionHistory(gitHub, f);
			}

			// Analyze ASTs for all revisions for usage patterns (and addition)
			System.out.println("\n ************ ANALYZING FOR USAGE PATTERN ADDITION ************\n");
			repository.revertAndAnalyzeForPatternAddition(gitHub, localRepoDir, dev, repoName);
			
//			clearDirectory(directory);
//			directory.delete();
//			
//			Process p4 = rt.exec(gitCloneCmd);
//			System.out.println(p4.waitFor());
//			
//			repository.setSourceFiles(localRepoDir);
//			
//			// Analyze all revisions for removal of usage patterns 
//			System.out.println("\n ************ ANALYZING FOR USAGE PATTERN REMOVAL ************\n");
//			repository.revertAndAnalyzeForPatternRemoval(gitHub, localRepoDir, dev, repoName);
		}
	}
	
	private static void setUserName(String u){
		userName = u;
	}
	
	private static void setRepoName(String r){
		repoName = r;
	}
	private static void setDeveloperName(String d){
		developerName = d;
	}
	
	private static String getUserName(){
		return userName;
	}
	private static String getRepoName(){
		return repoName;
	}
	
	private static String getDeveloperName(){
		return developerName;
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