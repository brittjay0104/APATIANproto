import java.util.List;

public class SimpleFieldDeclTest extends Exception {
	public static String test;
	
	public void test1 (String paramSimple) {
		
	}
	
	public void test2(String...paramVarargs){
		
	}
	
	public void test3(String[] param1of2, List<String> param2of2){
		
	}
	
	public void test4(){
		String local ="";
		SimpleFieldDeclTest local1of2, local2of2;
	}
}
