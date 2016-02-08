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
	
	private int addedSimpleGenerics;
	private int addedSimpleFields;
	private int addedSimpleVariables;
	private int addedSimpleMethods;
	private int addedSimpleReturn;
	
	private int addedAdvancedGenerics;
	private int addedAdvancedClasses;
	private int addedAdvancedFields;
	private int addedAdvancedMethods;
	private int addedAdvancedReturn;
	private int addedAdvancedNested;
	private int addedAdvancedParameters;
	private int addedAdvancedBounds;
	private int addedAdvancedWildcard;
	private int addedAdvancedDiamond;
	
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
		
		addedSimpleGenerics = 0;
		addedSimpleFields = 0;
		addedSimpleVariables = 0;
		addedSimpleMethods = 0;
		addedSimpleReturn = 0;
		
		addedAdvancedGenerics = 0;
		addedAdvancedClasses = 0;
		addedAdvancedFields = 0;
		addedAdvancedMethods = 0;
		addedAdvancedReturn = 0;
		addedAdvancedNested = 0;
		addedAdvancedParameters = 0;
		addedAdvancedBounds = 0;
		addedAdvancedWildcard = 0;
		addedAdvancedDiamond = 0;
		
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
	
	public void setAddedSimpleGenerics(int addedGenerics){
		addedSimpleGenerics = addedSimpleGenerics + addedGenerics;
	}
	
	public void setAddedSimpleFields (int addedGenerics){
		addedSimpleFields = addedSimpleFields + addedGenerics;
	}
	
	public void setAddedSimpleVariables (int addedGenerics){
		addedSimpleVariables = addedSimpleVariables + addedGenerics;
	}
	
	public void setAddedSimpleMethods (int addedGenerics){
		addedSimpleMethods = addedSimpleMethods + addedGenerics;
	}
	
	public void setAddedSimpleReturn (int addedGenerics){
		addedSimpleReturn = addedSimpleReturn + addedGenerics;
	}
	
	public void setAddedAdvancedGenerics (int addedGenerics){
		addedAdvancedGenerics = addedAdvancedGenerics + addedGenerics;
	}
	
	public void setAddedAdvancedClasses (int addedGenerics){
		addedAdvancedClasses = addedAdvancedClasses + addedGenerics;
	}
	
	public void setAddedAdvancedFields (int addedGenerics){
		addedAdvancedFields = addedAdvancedFields + addedGenerics;
	}
	
	public void setAddedAdvancedMethods (int addedGenerics){
		addedAdvancedMethods = addedAdvancedMethods + addedGenerics;
	}
	
	public void setAddedAdvancedReturn (int addedGenerics){
		addedAdvancedReturn = addedAdvancedReturn + addedGenerics;
	}
	
	public void setAddedAdvancedNested (int addedGenerics){
		addedAdvancedNested = addedAdvancedNested + addedGenerics;
	}
	
	public void setAddedAdvancedParameters (int addedGenerics){
		addedAdvancedParameters = addedAdvancedParameters + addedGenerics;
	}
	
	public void setAddedAdvancedBounds (int addedGenerics){
		addedAdvancedBounds = addedAdvancedBounds + addedGenerics;
	}
	
	public void setAddedAdvancedWildcard (int addedGenerics){
		addedAdvancedWildcard = addedAdvancedWildcard + addedGenerics;
	}
	
	public void setAddedAdvancedDiamond (int addedGenerics){
		addedAdvancedDiamond = addedAdvancedDiamond + addedGenerics;
	}
	
	public void setAddedGenerics(int addedGenerics){
		allAddedGenerics = allAddedGenerics + addedGenerics;
	}
	
	public int getAllAddedGenerics(){
		return allAddedGenerics;
	}
	
	public int getAddedSimpleGenerics(){
		return addedSimpleGenerics;
	}
	
	public int getAddedSimpleFields(){
		return addedSimpleFields;
	}
	
	public int getAddedSimpleVariables(){
		return addedSimpleVariables;
	}
	
	public int getAddedSimpleMethods(){
		return addedSimpleMethods;
	}
	
	public int getAddedSimpleReturn(){
		return addedSimpleReturn;
	}
	
	public int getAddedAdvancedGenerics(){
		return addedAdvancedGenerics;
	}
	
	public int getAddedAdvancedClasses(){
		return addedAdvancedClasses;
	}
	
	public int getAddedAdvancedFields(){
		return addedAdvancedFields;
	}
	
	public int getAddedAdvancedMethods(){
		return addedAdvancedMethods;
	}
	
	public int getAddedAdvancedReturn(){
		return addedAdvancedReturn;
	}
	
	public int getAddedAdvancedNested(){
		return addedAdvancedNested;
	}
	
	public int getAddedAdvancedParameters(){
		return addedAdvancedParameters;
	}
	
	public int getAddedAdvancedBounds(){
		return addedAdvancedBounds;
	}
	
	public int getAddedAdvancedWildcard(){
		return addedAdvancedWildcard;
	}
	
	public int getAddedAdvancedDiamond(){
		return addedAdvancedDiamond;
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
