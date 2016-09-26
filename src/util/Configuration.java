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
	
	public Configuration (String developer, String repo){
		if (developer.contains("\"")){
			developer = developer.replace("\"", "-");
		}
		opFile = "output-" + developer + "-" + repo + ".txt";
	}
	
	
	public String getOpFile(){		
		return opFile;
	}

}
