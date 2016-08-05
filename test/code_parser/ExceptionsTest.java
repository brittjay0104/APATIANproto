package code_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import junit.framework.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;
import node_visitor.ExceptionsVisitor;

@RunWith(Parameterized.class)
public class ExceptionsTest extends TestCase{

	@Parameter
	public File inputFile;
	private ModelSourceFile file;
	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;
	
	@Parameters(name="{0}")
    public static Collection<Object[]> params() {
    	
    	File[] javaFiles = new File("test.resources//").listFiles();
    	
    	Collection<Object[]> files = new ArrayList<Object[]>();
    	for(File jFile : javaFiles){
    		files.add(new File[] { jFile });
    	}
 
        return files;
    }

	@Before
	public void setUp() throws IOException{
		file = new ModelSourceFile(inputFile);
		
		ModelParser p = new ModelParser();
		
		p.parseForExceptions(file);
		
		assertTrue(inputFile.exists());
	}
	
	@Test
	public void testcase() throws IOException {
		
		char[] result = fileContents();
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(result);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
 
		ModelSourceFile file = new ModelSourceFile(inputFile);
		file.setSource(result);
		ExceptionsVisitor visitor = new ExceptionsVisitor(file);
		cu.accept(visitor);
		
		assertEquals(getExpectedResult(),visitor.findings());
	}
	
	@Test
	public void testExceptionDecl() {
		
		List<String> expectedResults = new ArrayList<String>();
		List<String> actualResults = new ArrayList<String>();
		
		expectedResults.add("public class ExceptionDecl_2 extends Exception");
		
		assertTrue(expectedResults.size() == file.getThrowsMethods().size());
		
		for (String result : file.getThrowsMethods()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualResults.add(result);
		}
		
		assertEquals(expectedResults.get(0), actualResults.get(0));
	}
	
	@Test
	public void testInitializerStaticTryCatch(){
		List<String> expectedTryResults = new ArrayList<String>();
		List<String> expectedCatchResults = new ArrayList<String>();
		List<String> actualTryResults = new ArrayList<String>();
		List<String> actualCatchResults = new ArrayList<String>();
		
		expectedTryResults.add("BufferedReader br = new BufferedReader(null);");
		expectedTryResults.add("File file = new File (\"static-try.txt\");");
		expectedCatchResults.add("catch (Exception e)");
		expectedCatchResults.add("catch(IOException e)");
		
		assertTrue(expectedTryResults.size() == file.getStaticTryStatements().size());
		assertTrue(expectedCatchResults.size() == file.getStaticCatchBlocks().size());
		
		for (String result : file.getStaticTryStatements()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualTryResults.add(result);
		}
		
		assertEquals(expectedTryResults.get(0), actualTryResults.get(0));
		
		for (String result : file.getStaticCatchBlocks()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualCatchResults.add(result);
		}
		
		assertEquals(expectedCatchResults.get(0), actualCatchResults.get(0));
	}
	
	@Test
	public void testMultiCatch(){
		List<String> expectedResults = new ArrayList<String>();
		List<String> actualResults = new ArrayList<String>();
		
		expectedResults.add("catch(IOException | SQLException e)");
		
		assertTrue(expectedResults.size() == file.getMultiCatchBlocks().size());
		
		for (String result : file.getMultiCatchBlocks()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualResults.add(result);
		}
		
		assertEquals(expectedResults.get(0), actualResults.get(0));
		
	}
	
	@Test
	public void testNothing() {
		List<String> expectedResults = new ArrayList<String>();
		List<String> actualResults = new ArrayList<String>();
		
		assertTrue(expectedResults.size() == file.getThrowsMethods().size());
		
		assertTrue(actualResults.size() == 0);
	}
	
	@Test
	public void testSimpleTryCatch() {
		List<String> expectedTryResults = new ArrayList<String>();
		List<String> expectedCatchResults = new ArrayList<String>();
		List<String> actualTryResults = new ArrayList<String>();
		List<String> actualCatchResults = new ArrayList<String>();
		
		expectedTryResults.add("File f = new File(\"output.txt\");");
		expectedCatchResults.add("catch(Exception e)");
		
		assertTrue(expectedTryResults.size() == file.getTryStatements().size());
		assertTrue(expectedCatchResults.size() == file.getCatchBlocks().size());
		
		for (String result : file.getTryStatements()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualTryResults.add(result);
		}
		
		for (String result : file.getCatchBlocks()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualCatchResults.add(result);
		}
		assertEquals(expectedTryResults.get(0), actualTryResults.get(0));
		assertEquals(expectedCatchResults.get(0), actualCatchResults.get(0));	
	}
	
	@Test 
	public void testThrowsMethod() {
		
		List<String> expectedResults = new ArrayList<String>();
		List<String> actualResults = new ArrayList<String>();
		
		expectedResults.add("public void exceptionTest() throws Exception");
		
		assertTrue(expectedResults.size() == file.getThrowsMethods().size());
		
		for (String result : file.getThrowsMethods()){
			result = result.substring(result.indexOf(CHECK_SEPERATOR), result.length());
			actualResults.add(result);
		}
		assertEquals(expectedResults.get(0), actualResults.get(0));
	}
	
	@Test
	public void testTryWithResources(){
		
	}
	
	/**
	 * Returns the expected result (from the inputfilename)
	 */
	private int getExpectedResult(){		
		String name = inputFile.getName();
		return Integer.parseInt(name.substring(name.indexOf('_')+1,//chop off head
								name.length()-5));					//chop of .java
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
