package code_parser;

import java.io.File;
import java.util.ArrayList;

import developer_creator.ModelDeveloper;

public class ModelCommit {

	private String developerId;
	private ModelDeveloper developer;
	private String commitHash;
	private File analysisResults;
	private ArrayList<String> ifAndNext;

	public ModelCommit(String developerId, String commitHash) {
		super();
		this.developerId = developerId;
		developer = new ModelDeveloper(developerId);
		this.commitHash = commitHash;
		ifAndNext = new ArrayList<String>();
	}

	public String getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(String developerId) {
		this.developerId = developerId;
	}
	
	public ModelDeveloper getDeveloper(String devId) {
		return developer;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	public File getAnalysisResults() {
		return analysisResults;
	}

	public void setAnalysisResults(File analysisResults) {
		this.analysisResults = analysisResults;
	}
	
	public void addIfAndNextStatement(String s){
		ifAndNext.add(s);
	}
	
	public ArrayList<String> getIfAndNext(){
		return ifAndNext;
	}

}
