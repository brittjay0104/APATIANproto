package code_parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class ModelSourceFileTest {

	private ModelRepository model;
	private ModelSourceFile file;

	private File repositoryPath;
	private Repository repo;
	private File testFile;
	private RevCommit testCommit;
	private Git git;
	private String testMethod;

	@Before
	public void setUp() throws Exception {
		try {
			model = new ModelRepository(new File("./JSON-java/.git"));
			file = new ModelSourceFile();
			testFile = new File("CDL.java");
			repositoryPath = new File("./JSON-java/.git");
			testMethod = "rowToArray";
		} catch (RepositoryNotFoundException e) {
			fail("Repo not found.");
		}
	}

	@Test
	public void testSourceFile() {

		final File file = null;

		ModelSourceFile msf = new ModelSourceFile();

		assertEquals(msf.getSourceFile(), file);
	}

	@Test
	public void testSourceFileFile() {

		ModelSourceFile msf = new ModelSourceFile(testFile);

		assertEquals(msf.getSourceFile(), testFile);
	}

	@Test
	public void testSetSource() {
		try {
			String src = ModelParser.readFiletoString(file.getSourceFile().getCanonicalPath());
			char[] charSrc = src.toCharArray();
			file.setSource(charSrc);

			assertEquals(file.getSource(), charSrc);

		} catch (IOException e) {
			fail("Operation failed or was interrupted.");
		}

	}

	@Test
	public void testSetSourceFile() {
		File f = new File("CDL.java");
		file.setSourceFile(f);

		assertEquals(file.getSourceFile(), f);
	}

	@Test
	public void testSetRepository() {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			repo = builder.setGitDir(repositoryPath).readEnvironment().findGitDir().build();
			file.setRepository(repo);

			assertEquals(file.getRepository(), repo);
		} catch (IOException e) {
			fail("Operation failed or was interrupted.");
		}

	}

	@Test
	public void testSetName(){
		String filename = testFile.getName();

		file.setName(filename);

		assertEquals(file.getName(), filename);
	}

	@Test
	public void testAddCommit() {
		git = new Git(repo);
		//model.setRepositoryRevisionHistory(git);
		testCommit = model.getRevisions().get(0);
		file.addCommit(testCommit);

		assertEquals(file.getCommits().get(0), testCommit);
	}

	@Test
	public void testAddMethod(){
		file.addMethod(testMethod);

		assertEquals(file.getNullMethods().get(0), testMethod);
	}

	@Test
	public void testRemoveMethodIndex(){
		int index = file.getNullMethods().indexOf(testMethod);

		file.removeMethod(index);

		assertEquals(file.getNullMethods().get(index), null);
	}

	@Test
	public void testRemoveMethodString(){
		file.addMethod(testMethod);
		int index = file.getNullMethods().indexOf(testMethod);

		file.removeMethod(testMethod);

		assertEquals(file.getNullMethods().get(index), null);
	}


//	@Test
//	public void testGetRevisionHistory() {
//
//		model.setFileRevisionHistory(model.getGitRepository(),file);
//		ArrayList<ModelCommit> commits = file.getCommits();
//
//		ArrayList<ModelCommit> expectedCommits = new ArrayList<ModelCommit>();
//		expectedCommits.add(new ModelCommit("", ""));
//
//		assertEquals(expectedCommits.size(), commits.size());
//		for (int i = 0; i < expectedCommits.size(); i++) {
//			assertEquals(expectedCommits.get(i).getDeveloperId(), commits.get(i).getDeveloperId());
//			assertEquals(expectedCommits.get(i).getCommitHash(), commits.get(i).getCommitHash());
//		}
//
//
//
//
//	}

}
