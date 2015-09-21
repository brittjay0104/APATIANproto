package developer_creator;

import java.util.ArrayList;

import code_parser.ModelRepository;


public class ModelDeveloper {

	private int removedNullCounts;
	private int addedNullCounts;
	private int removedNODPCounts;
	private int addedNODPCounts;
	private int removedCollCounts;
	private int addedCollCounts;
	private int removedOptCounts;
	private int addedOptCounts;
	private int removedCatchCounts;
	private int addedCatchCounts;
	
	private int derefCounts;
	private String name;
	private String username;
	public ArrayList<String> commits;
	public ArrayList<ModelRepository> repositories;
	public int linesAdded;
	public int linesRemoved;
	public String pseudoName;
	
	public ModelDeveloper (String id) {
		removedNullCounts = 0;
		addedNullCounts = 0;
		removedNODPCounts = 0;
		addedNODPCounts = 0;
		removedCollCounts = 0;
		addedCollCounts = 0;
		removedOptCounts = 0;
		addedOptCounts = 0;
		removedCatchCounts = 0;
		addedCatchCounts = 0;
		
		setDevName(id);
		commits = new ArrayList<String>();
		repositories = new ArrayList<ModelRepository>();
	}
	
	public ModelDeveloper() {
	}
	
	public void setPseudoName(String name){
		pseudoName = name;
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
	
	public int getRemovedNODPCounts() {
		return removedNODPCounts;
	}

	public void setRemovedNODPCounts(int removedNODP) {
		removedNODPCounts = removedNODPCounts + removedNODP;
	}

	public int getAddedNODPCounts() {
		return addedNODPCounts;
	}

	public void setAddedNODPCounts() {
		addedNODPCounts = addedNODPCounts + 1;
	}

	public int getRemovedCollCounts() {
		return removedCollCounts;
	}

	public void setRemovedCollCounts(int removedColls) {
		removedCollCounts = removedCollCounts + removedColls;
	}

	public int getAddedCollCounts() {
		return addedCollCounts;
	}

	public void setAddedCollCounts(int addedColls) {
		addedCollCounts = addedCollCounts + addedColls;
	}

	public int getRemovedOptCounts() {
		return removedOptCounts;
	}

	public void setRemovedOptCounts(int removedOpts) {
		removedOptCounts = removedOptCounts + removedOpts;
	}

	public int getAddedOptCounts() {
		return addedOptCounts;
	}

	public void setAddedOptCounts(int addedOpts) {
		addedOptCounts = addedOptCounts + addedOpts;
	}

	public int getRemovedCatchCounts() {
		return removedCatchCounts;
	}

	public void setRemovedCatchCounts(int removedCatchs) {
		removedCatchCounts = removedCatchCounts + removedCatchs;
	}

	public int getAddedCatchCounts() {
		return addedCatchCounts;
	}

	public void setAddedCatchCounts(int addedCatchs) {
		addedCatchCounts = addedCatchCounts + addedCatchs;
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
	
	public void setRemovedCounts(int num){
		removedNullCounts = removedNullCounts + num;
	}
	
	public void setAddedNullCounts(int num){
		addedNullCounts = addedNullCounts + num;
	}
	
	public void setDerefCount(int num){
		derefCounts = derefCounts + num;
	}
	
	public void incrementLinesAddedCount(){
		linesAdded += 1;
	}
	
	public void incrementLinesRemovedCount(){
		linesRemoved += 1; 
	}
	
	


}
