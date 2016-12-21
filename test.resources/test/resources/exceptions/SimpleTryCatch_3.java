package test.resources.exceptions;
// tryStatements = 1
// catchBlocks = 1
// checkedExceptions = 1

import java.io.File;

public class SimpleTryCatch_3 {

	void m(){
		try{
			File f = new File("output.txt");
		}catch(Exception e){
			
		}
	}
}
