package developer_creator;

import developer_creator.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import node_visitor.NoNullCheckVisitor;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import code_parser.ModelParser;
import code_parser.ModelRepository;
import code_parser.ModelSourceFile;

public class RunAnalysis {

		public static void main(String[] args) throws Exception {
			
//			File users = new File("test-github-users.txt");
//			FileReader in1 = new FileReader(users);
//			BufferedReader br = new BufferedReader(in1);
//			
//			String line;
//			while ((line=br.readLine()) != null){
//				Runtime rt = Runtime.getRuntime();
//				String username = line;
//				//dummyRepoRun(rt);
//				
//				// get all repositories for a given Github account/username
//				//String username = "mpeterson2";
//				ModelDeveloper dev = new ModelDeveloper();
//				dev.setUserName(username);
			// TODO can I just set the username and name here rather than automating it? --> use it to check to make sure commit belongs to dev 
//				Process p1 = rt.exec("C:/SW/CommandLine/GnuWin32/bin/wget --no-check-certificate -O user.txt https://api.github.com/users/" + username);
//				// Process p1 = rt.exec("curl -o user.txt https://api.github.com/users/" + username);
//				System.out.println(p1.waitFor());
//				
//				FileReader in2 = new FileReader(new File("user.txt"));
//				BufferedReader br1 = new BufferedReader(in2);
//				
//				String line1;
//				while ((line1=br1.readLine()) != null){
//					if (line1.contains("name")){
//						String developer = line1.substring(line1.indexOf(":") + 3, line1.length()-2);
//						System.out.println(developer);
//						dev.setDevName(developer);
//					} 
//				}
//				
//				if (dev.getDevName() == null){
//					FileReader in5 = new FileReader(new File("user.txt"));
//					BufferedReader br5 = new BufferedReader(in5);
//					
//					String line5;
//					while ((line5=br5.readLine()) != null){
//						if (line5.contains("login")){
//							String loginName = line5.substring(line5.indexOf(":") + 3, line5.length()-2);
//							System.out.println(loginName);
//							dev.setDevName(loginName);								
//							
//						}						
//					}
//					
//					br5.close();
//				}
//				
//				br1.close();
//				
//				
//				Process p2 = rt.exec("C:/SW/CommandLine/GnuWin32/bin/wget --no-check-certificate -O output.txt https://api.github.com/search/repositories?q=language:java+user:" + username);
//				//Process p2 = rt.exec("curl -o output.txt https://api.github.com/search/repositories?q=language:java+user:" + username);
//				System.out.println(p2.waitFor());
//				
//				FileReader in3 = new FileReader(new File("output.txt"));
//				ArrayList<String> urls = new ArrayList<String>();
//				BufferedReader br3 = new BufferedReader(in3);
//				
//				String line2;
//				while ((line2 = br3.readLine()) != null){
//					if (line2.contains("clone_url")){
//						String url = line2.substring(line2.indexOf("http"), line2.lastIndexOf("\""));
//						System.out.println(url);
//						urls.add(url);
//					}
//				}
//				
//				br3.close();
//				
//				
//				FileReader in4 = new FileReader(new File("output.txt"));
//				ArrayList<String> repoNames = new ArrayList<String>();
//				BufferedReader br4 = new BufferedReader(in4);
//				
//				String line3;
//				while ((line3 = br4.readLine()) != null){
//					if (line3.contains("full_name")){
//						String repoName = line3.substring(line3.indexOf("/")+1, line3.lastIndexOf("\""));
//						System.out.println(repoName);
//						repoNames.add(repoName);
//					}
//				}
//				
//				
//				br4.close();
//				
//				
//				String repoName = "";
//
//				// clone from list of urls (also gather repoNames for reporting -- should match up with number of urls)
//				for (int i=0; i < urls.size(); i++){
//					if (repoNames.get(i) != null){
//						repoName = repoNames.get(i);						
//					}
//					
//					// for cloning
//					String cloneURL = urls.get(i);
//					
//					
//					System.out.println(cloneURL);
//					// for creating repository
//					String directory = "." + cloneURL.substring(cloneURL.lastIndexOf("/"), cloneURL.length());
//					System.out.println(directory);
//					File repoDir = new File(directory);
//					
//					clearOutDirectory(repoDir);
//					repoDir.delete();
//					
//					Process p3 = rt.exec("git clone " + cloneURL);
//					System.out.println(p3.waitFor());
//					
//					String repo = directory + "/.git";
//					File repoGit = new File(repo);
//					
//					//set repository history
//					ModelRepository repository = new ModelRepository(repoGit);
//					Git gitHub = repository.getGitRepository();
//					
//					if (repository.setRepositoryRevisionHistory(gitHub, dev) != null){
//						repository.setRepositoryRevisionHistory(gitHub, dev);
//						
//						//set source files for each directory
//						repository.setSourceFiles(directory);
//						
//						//set history for each file
//						for (ModelSourceFile f: repository.getSourceFiles()) {
//							repository.setFileRevisionHistory(gitHub, f);
//						}
//						
//						//Analyze ASTs for all revisions (right now for null checks)
//						repository.revertAndAnalyzeForNull(gitHub, directory, dev, repoName);
//						
//					}
//				}
//			}
//			
//			br.close();


			System.setOut(new PrintStream(new FileOutputStream("console-output.txt")));
			
			Runtime rt = Runtime.getRuntime();
			
			String repo = "./dx/.git";
			File repoGit = new File(repo);
			ModelDeveloper dev = new ModelDeveloper("Brian McCallister");
			dev.setUserName("brianm");
			String repoName = "dx";
			String dir = "./dx/";
			File directory = new File (dir);
			
			clearOutDirectory(directory);
			directory.delete();
			
			Process p3 = rt.exec("git clone https://github.com/brianm/dx.git");
			System.out.println(p3.waitFor());
			
			//set repository history
			ModelRepository repository = new ModelRepository(repoGit);
			Git gitHub = repository.getGitRepository();
			
			if (repository.setRepositoryRevisionHistory(gitHub, dev) != null){
				repository.setRepositoryRevisionHistory(gitHub, dev);
				
				//ArrayList<RevCommit> commits = repository.getRevisions();

				//set source files for each directory
				repository.setSourceFiles(dir);
				
				//set history for each file
				for (ModelSourceFile f: repository.getSourceFiles()) {
					repository.setFileRevisionHistory(gitHub, f);
				}
								
				//Analyze ASTs for all revisions (right now for null checks)
				repository.revertAndAnalyzeForNull(gitHub, dir, dev, repoName);
		}	
				
				

		}
		


		private static void dummyRepoRun(Runtime rt) throws IOException, InterruptedException, RepositoryNotFoundException {
			String directory = "./dummy-repo2";
			File repoDir = new File(directory);

			clearOutDirectory(repoDir);
			repoDir.delete();

			Process p = rt.exec("git clone https://github.com/brittjay0104/dummy-repo2.git");
			System.out.println(p.waitFor());

			String repository = "./dummy-repo2/.git";
			File repoGit = new File(repository);

			String developer = "Brittany Johnson";
			ModelDeveloper dev = new ModelDeveloper(developer);
			
			String repoName = "dummy-repo2";


			// Set repository history (all commits for the repository)
			ModelRepository repo = new ModelRepository(repoGit);
			Git gitHub = repo.getGitRepository();

			repo.setRepositoryRevisionHistory(gitHub, dev);


			// Set source files for directory/repository
			repo.setSourceFiles(directory);

			// Set history for each file in directory
			for (ModelSourceFile f: repo.getSourceFiles()) {
				repo.setFileRevisionHistory(gitHub, f);
			}


			//Analyze ASTs for all revisions (right now for null checks)
			repo.revertAndAnalyzeForNull(gitHub, directory, dev, repoName);
		}


		public static boolean clearOutDirectory(File rootDirectory)
		{
			if (!rootDirectory.exists() || (rootDirectory.isDirectory() && rootDirectory.listFiles().length == 0))
			{
				return true;
			}
			return recursivelyClearDirectory(rootDirectory);
		}

		private static boolean recursivelyClearDirectory(File parentDirectory)
		{
			for (File f : parentDirectory.listFiles())
			{
				if (f.isDirectory())
				{
					if (!recursivelyClearDirectory(f))
						return false;
				}
				if (!f.delete())
				{
					return false;
				}
			}
			return parentDirectory.listFiles().length == 0;
		}


		

}