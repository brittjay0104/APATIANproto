package developer_creator;

import java.util.ArrayList;

import code_parser.ModelRepository;


public class ModelDeveloper {

	private int removedNullCounts;
	private int addedNullCounts;
	private int derefCounts;
	private String name;
	private String username;
	public ArrayList<String> commits;
	public ArrayList<ModelRepository> repositories;
	public int linesAdded;
	public int linesRemoved;
	
	public ModelDeveloper (String id) {
		removedNullCounts = 0;
		addedNullCounts = 0;
		setDevName(id);
		commits = new ArrayList<String>();
		repositories = new ArrayList<ModelRepository>();
	}
	
	public ModelDeveloper() {
	}
	
	public void addRepository(ModelRepository repo){
		repositories.add(repo);
	}
	
	public ArrayList<ModelRepository> getRepositories(){
		return repositories;
	}
	
	public void setUserName (String name){
		username = name;
	}
	
	public String getUserName(){
		return username;
	}

	public void setDevName(String name) {
		this.name = name;
	}
	
	public String getDevName(){
		return name;
	}
	
	public void addCommit(String hash){
		commits.add(hash);
	}
	
	public ArrayList<String> getCommits(){
		return commits;
	}
	
	public int getRemovedNullCounts(){
		return removedNullCounts;
	}
	
	public int getAddedNullCounts(){
		return addedNullCounts;
	}
	
	public int getDerefCount(){
		return derefCounts;
	}
	
	public int getLinesAdded(){
		return linesAdded;
	}
	
	public int getLinesRemoved(){
		return linesRemoved;
	}
	
	public void incrementRemovedNullCounts(){
		removedNullCounts += 1;
	}
	
	public void addToRemovedCounts(int num){
		removedNullCounts = removedNullCounts + num;
	}
	
	public void incrementAddedNullCounts(){
		addedNullCounts += 1;
	}
	
	public void addToAddedNullCounts(int num){
		addedNullCounts = addedNullCounts + num;
	}
	
	public void incrementDerefCount(){
		derefCounts += 1;
	}	
	
	public void addToDerefCount(int num){
		derefCounts = derefCounts + num;
	}
	
	public void incrementLinesAddedCount(){
		linesAdded += 1;
	}
	
	public void incrementLinesRemovedCount(){
		linesRemoved += 1; 
	}
	
	


}
