import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StaticTryCatch_3 {

	static {
		try{
			File file = new File ("static-try.txt");
			file.createNewFile();
		}catch(IOException e){
			
		}
	}
}
