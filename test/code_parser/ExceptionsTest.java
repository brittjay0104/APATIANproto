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
		int es = getExpectedOutput().size();
		int as = visitor.fullFindings().size();
		
		assertEquals(getExpectedResult(),visitor.findings());
		//(getExpectedOutput().size(), visitor.fullFindings().size());
		// TODO this need to be updated - compare findings one by one (loop)?
		//assertEquals(getExpectedOutput(), visitor.fullFindings());
	}
	
	/**
	 * Returns the expected result (from the inputfilename)
	 */
	private int getExpectedResult(){		
		String name = inputFile.getName();
		return Integer.parseInt(name.substring(name.indexOf('_')+1,//chop off head
								name.length()-5));					//chop of .java
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
			String output = line.substring(line.indexOf("-")+1, line.length()).trim();
			
			expectedOutput.add(output);
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
