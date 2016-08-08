// try - if (1==1)
// throw - throw new IOException(); , throw new SQLException();
// multicatch - catch (IOException | SQLException e)
// checked - throw new IOException(); , throw new SQLException(); , catch(IOException | SQLException e), catch(IOException | SQLException e)

import java.io.IOException;
import java.sql.SQLException;

public class Multicatch_8 {

	void m(){
		try{
			if(1==1)
				throw new IOException();
			throw new SQLException();
		}catch(IOException | SQLException e){
			
		}
	}
}
