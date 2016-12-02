package code_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import node_visitor.VariableData;
import node_visitor.VariablesVisitor;

@RunWith(Parameterized.class)
public class VariablesTest extends TestCase{

	//TODO throw error if no assertions in test cases
	
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
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setUnitName(inputFile.getName());
		
		String [] sources = {"test.resources/"};
		String[] classpath = {"ap.jar"};
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		
		ModelSourceFile file = new ModelSourceFile(inputFile);
		file.setSource(result);
		VariablesVisitor visitor = new VariablesVisitor(file);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
		cu.accept(visitor);

		for(VariableData data : visitor.getAllVariableData()){
			System.out.println(data);
		}
	
	}
	
	@Test
	public void testcase_2(){
		
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
