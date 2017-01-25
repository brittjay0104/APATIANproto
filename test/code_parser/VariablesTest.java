package code_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;
import node_visitor.AbstractVisitor;
import node_visitor.VariableData;
import node_visitor.VariablesVisitor;

@RunWith(Parameterized.class)
public class VariablesTest extends TestCase{

	//TODO throw error if no assertions in test cases
	
	@Parameter
	public File inputFile;
	
	@Parameters(name="{0}")
    public static Collection<Object[]> params() {
    	
    	File[] javaFiles = new File("test.resources/test/resources/variables//").listFiles();
    	
    	Collection<Object[]> files = new ArrayList<Object[]>();
    	for(File jFile : javaFiles){
    		files.add(new File[] { jFile });
    	}
 
        return files;
    }

	@Before
	public void setUp() throws IOException{		
		assertTrue(inputFile.exists());
	}
	
	@Test
	public void testcase() throws IOException {
		
		char[] result = fileContents();
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(result);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setUnitName(inputFile.getName());
		
		String [] sources = {"test.resources/test/resources/variables/"};
		String[] classpath = {"ap.jar"};
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		
		ModelSourceFile file = new ModelSourceFile(inputFile);
		file.setSource(result);
		VariablesVisitor visitor = new VariablesVisitor(file);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
		cu.accept(visitor);

		for(String data : getExpectedOutput()){
			String[] split = data.split(" ");
			
			int actual = actual(visitor, split);
			
			assertEquals("Expected: " + data, Integer.parseInt(split[3]) , actual);
		}
	
	}
	
	@Test
	public void testcase_2(){
		
	}
	
	private int actual (AbstractVisitor visitor, String[] split){
		Class<?> c = visitor.getClass();

		Field f = null;
		try {
			f = c.getDeclaredField(split[1]);
		} catch (NoSuchFieldException _) {
			throw new RuntimeException("Test framework couldn't find field " + split[1]);
		} catch (SecurityException _){
			fail("Test framework couldn't access field " + split[0]);
		}
		int actualResult = -1;
		try {
			f.setAccessible(true);
			actualResult = ((List<String>) f.get(visitor)).size();
		} catch (IllegalArgumentException | IllegalAccessException _) {
			throw new RuntimeException("Test framework couldn't access field " + split[0]);
		}
		return actualResult;
	}
	
	/**
	 * Returns the expected output (from the comment in inputfile)
	 * @throws FileNotFoundException, IOException
	 */
	private List<String> getExpectedOutput() throws FileNotFoundException, IOException {
		List<String> expectedOutput = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(inputFile);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		
		while ((line = br.readLine()) != null && line.startsWith("//")){
			//String output = line.substring(line.indexOf("-")+1, line.length()).trim();
			
			expectedOutput.add(line);
		}
		
		br.close();
		
		return expectedOutput;
	}
	

	private char[] fileContents() throws FileNotFoundException, IOException {
		StringBuffer sb = new StringBuffer();
		for(String s: readFile(inputFile.getCanonicalPath()))
		{
			sb.append(s);
			sb.append("\n");
		}
		
		String src = sb.toString();
		
		char[] result = src.toCharArray();
		
		return result;
	}
	
	public static List<String> readFile(String file) {
		List<String> retList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				retList.add(line);
			}
		} catch (Exception e) {
			retList = new ArrayList<String>();
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					retList = new ArrayList<String>();
					e.printStackTrace();
				}
			}
		}
		return retList;
	}
}