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
	
	private int allAddedGenerics;
	
	private int addedTypeArgumentMethods;
	private int addedWildcard;
	private int addedTypeDeclarations;
	private int addedTypeParameterMethods;
	private int addedTypeParameterFields;
	private int addedDiamond;
	private int addedMethodInvocations;
	private int addedClassInstantiation;
	private int addedNested;
	private int addedBounds;
	
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
		
		allAddedGenerics = 0;
		
		
		addedTypeArgumentMethods = 0;
		addedWildcard = 0;
		addedTypeDeclarations = 0;
		addedTypeParameterMethods = 0;
		addedTypeParameterFields = 0;
		addedDiamond = 0;
		addedMethodInvocations = 0;
		addedClassInstantiation = 0;
		addedNested = 0;
		addedBounds = 0;
		
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

	public void setAddedTypeArgumentMethods(int addedGenerics){
		addedTypeArgumentMethods = addedTypeArgumentMethods + addedGenerics;
	}
	
	public void setAddedWildcard(int addedGenerics){
		addedWildcard = addedWildcard + addedGenerics;
	}
	
	public void setAddedTypeDeclarations(int addedGenerics){
		addedTypeDeclarations = addedTypeDeclarations + addedGenerics;
	}
	
	public void setAddedTypeParameterMethods(int addedGenerics){
		addedTypeParameterMethods = addedTypeParameterMethods + addedGenerics;
	}
	
	public void setAddedTypeParameterFields(int addedGenerics){
		addedTypeParameterFields = addedTypeParameterFields + addedGenerics;
	}
	
	public void setAddedDiamond(int addedGenerics){
		addedDiamond = addedDiamond + addedGenerics;
	}
	
	public void setAddedMethodInvocation(int addedGenerics){
		addedMethodInvocations = addedMethodInvocations + addedGenerics;
	}
	
	public void setAddedClassInstantiations(int addedGenerics){
		addedClassInstantiation = addedClassInstantiation + addedGenerics;
	}
	
	public void setAddedNested(int addedGenerics){
		addedNested = addedNested + addedGenerics;
	}
	
	public void setAddedBounds(int addedGenerics){
		addedBounds = addedBounds + addedGenerics;
	}
	
	public void setAddedGenerics(int addedGenerics){
		allAddedGenerics = allAddedGenerics + addedGenerics;
	}
	
	public int getAllAddedGenerics(){
		return allAddedGenerics;
	}
	
	public int getAddedTypeArgumentMethods(){
		return addedTypeArgumentMethods;
	}
	
	public int getAddedWildcard(){
		return addedWildcard;
	}
	
	public int getAddedTypeDecs(){
		return addedTypeDeclarations;
	}
	
	public int getAddedTypeParamMethods(){
		return addedTypeParameterMethods;
	}
	
	public int getAddedTypeParamFields(){
		return addedTypeParameterFields;
	}
	
	public int getAddedDiamond(){
		return addedDiamond;
	}
	
	public int getAddedMethodInvocs(){
		return addedMethodInvocations;
	}
	
	public int getAddedClassInstances(){
		return addedClassInstantiation;
	}
	
	public int getAddedNested(){
		return addedNested;
	}
	
	public int getAddedBounds(){
		return addedBounds;
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
