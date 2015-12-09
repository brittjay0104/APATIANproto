package util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Utility Class for storing Configuration Parameters
 * @author Rahul Pandita
 * 
 */
public class Configuration {
	public String opFile;
	
	
	public Configuration(){
		this.method(new Integer(100));
		
		this.<Number>method(new Integer(500));
	}
	
	public Configuration (String developer, String repo){
		opFile = "output-" + developer + "-" + repo + ".txt";
	}
	
	
	public String getOpFile(){		
		return opFile;
	}
	
	public <T> void method(T item){
		List<T> list = new ArrayList<T>();
	}
	
}
