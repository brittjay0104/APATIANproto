// tryStatements = 1
// catchBlocks = 1
// checkedExceptions = 4
// multiCatchBlocks = 1
// throwStatements = 2

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Multicatch_8 {
	public List<String> tryStatements;
	public List<String> throwStatements;
	public List<String> multiCatchBlocks;
	public List<String> checkedExceptions;
	
	void m(){
		try{
			if(1==1)
				throw new IOException();
			throw new SQLException();
		}catch(IOException | SQLException e){
			
		}
	}
	
	public static void populateField()
	{
		
	}
}
