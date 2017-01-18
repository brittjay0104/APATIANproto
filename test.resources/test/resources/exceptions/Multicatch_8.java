package test.resources.exceptions;
// tryStatements = 1
// catchBlocks = 1
// checkedExceptions = 4
// uncheckedExceptions = 1
// multiCatchBlocks = 1
// throwStatements = 2

import java.io.IOException;
import java.sql.SQLException;

public class Multicatch_8 {
	void m(){
		try{
			if(1==1)
				throw new IOException();
			throw new SQLException();
			
		} catch(IOException | SQLException | RuntimeException e){
			
		} 
	}
}
