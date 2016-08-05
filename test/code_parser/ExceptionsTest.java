package code_parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
	public void setUp(){
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
	
	/**
	 * Returns the expected result (from the inputfilename)
	 */
	private int getExpectedResult(){		
		String name = inputFile.getName();
		return Integer.parseInt(name.substring(name.indexOf('_')+1,//chop off head
								name.length()-5));					//chop of .java
	}

	// this is stupidly long; easier standard way to do this?
	private char[] fileContents() throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(inputFile);
		Reader reader = new InputStreamReader(in);

		List<Character> list = new LinkedList<>();

		int r;
		while ((r = reader.read()) != -1) {
			char ch = (char) r;
			list.add(ch);
		}

		in.close();

		char[] result = new char[list.size()];
		int i = 0;
		for (char c : list) {
			result[i++] = c;
		}
		return result;
	}
}
