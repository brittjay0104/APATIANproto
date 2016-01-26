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
	
	private int addedGenericFields;
	private int removedGenericFields;
	private int addedGenericMethods;
	private int addedGenericInvocs;
	private int addedGenericParams;
	private int addedGenericVarDecs;
	private int removedGenericVarDecs;
	
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
		
		addedGenericFields = 0;
		removedGenericFields = 0;
		addedGenericInvocs = 0;
		addedGenericMethods = 0;
		addedGenericParams = 0;
		addedGenericVarDecs = 0;
		removedGenericVarDecs = 0;
		
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

	public void setAddedNODPCounts(int addedNODP) {
		addedNODPCounts = addedNODPCounts + addedNODP;
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
	
	public void setAddedGenericFields(int addedFields){
		addedGenericFields = addedGenericFields + addedFields;
	}
	
	public void setAddedGenericMethods(int addedMethods){
		addedGenericMethods = addedGenericMethods + addedMethods;
	}
	
	public void setAddedGenericInvocs(int addedInvocs){
		addedGenericInvocs = addedGenericInvocs + addedInvocs;
	}
	
	public void setAddedGenericParams(int addedParams){
		addedGenericParams = addedGenericParams + addedParams;
	}
	
	public void setAddedGenericVarDecs(int addedVarDecs){
		addedGenericVarDecs = addedGenericVarDecs + addedVarDecs;
	}
	
	public void setRemovedGenericFields(int removedFields){
		removedGenericFields = removedGenericFields + removedFields;
	}
	
	public void setRemovedGenericVarDecs (int removedVarDecs){
		removedGenericVarDecs = removedGenericVarDecs + removedVarDecs;
	}
		
	public int getAddedGenericsFieldsCount(){
		return addedGenericFields;
	}
	
	public int getRemovedGenericsFieldsCount(){
		return removedGenericFields;
	}
	
	public int getAddedGenericsMethodsCount(){
		return addedGenericMethods;
	}

	public int getAddedGenericsInvocsCount(){
		return addedGenericInvocs;
	}
	
	public int getAddedGenericParamsCount(){
		return addedGenericParams;
	}
	
	public int getAddedGenericVarDecsCount(){
		return addedGenericVarDecs;
	}
	
	public int getRemovedGenericVarDecsCount(){
		return removedGenericVarDecs;
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
