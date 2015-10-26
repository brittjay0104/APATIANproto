package util;

/**
 * Utility Class for storing Configuration Parameters
 * @author Rahul Pandita
 * 
 */
public class Configuration {
	public static String opFile;

	
	public Configuration (String developer, String repo){
		opFile = "output-" + developer + "-" + repo + ".txt";
	}
}
