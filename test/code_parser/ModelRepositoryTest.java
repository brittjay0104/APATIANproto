package code_parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Before;
import org.junit.Test;

import developer_creator.ModelDeveloper;

public class ModelRepositoryTest {

	private File repoPath;
	private Git git;
	private String directory;
//	private ArrayList<ModelSourceFile> sourceFiles;
	private Repository repo;
//	private ArrayList<RevCommit> revisions;


	private ModelRepository repository;

	private List<ModelDeveloper> developers;
	private List<String> devs;

	@Before
	public void setUp() throws Exception {

		repoPath = new File("./dummy-repo/.git");
		directory = "./dummy-repo";
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		repo = builder.setGitDir(repoPath).readEnvironment().findGitDir().build();

	}

	@Test
	public void testModelRepository() {
		try {
			repository = new ModelRepository(repoPath);

			assertEquals(repository.getRepoPath(), repoPath);

		} catch (RepositoryNotFoundException e) {
			fail("Repository not found");
		}

	}

	@Test
	public void testGetGitRepository() {
		repository.setGitRepository(git);

		assertEquals(repository.getGitRepository(), git);

	}

	@Test
	public void testGetRepo() {
		try {
			ModelRepository r = new ModelRepository(repoPath);
			r.setGitRepository(git);

			assertEquals(r.getGitRepository(), git);
		} catch (RepositoryNotFoundException e) {
			fail("Repository not found.");
		}

	}

	@Test
	public void testGetRepoPath() {

		assertEquals(repository.getRepoPath(), repoPath);
	}

	@Test
	public void testGetSourceFiles() {
		try {
			repository.setSourceFiles(directory);

			assertFalse(repository.getSourceFiles().isEmpty());

		} catch (IOException e) {
			fail("Operation failed or was interrupted.");
		}

	}

	@Test
	public void testGetRevisions() {
		//repository.setRepositoryRevisionHistory(git);

		assertFalse(repository.getRevisions().isEmpty());
	}

	@Test
	public void testSetFileRevisionHistory(){
		ModelSourceFile msf = repository.getSourceFiles().get(0);

		repository.setFileRevisionHistory(git, msf);

		assertFalse(msf.getCommits().isEmpty());
	}

	@Test
	public void testRevertAndAnalyze() {

		//repository.revertAndAnalyzeForNull(git, directory);

		assertFalse(repository.getDevelopers().isEmpty());




	}

}
