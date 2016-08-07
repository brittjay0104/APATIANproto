import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StaticTryWithResources_5 {

	static {
		
		try(FileInputStream input = new FileInputStream("file.txt")) {
			input.read();

		} catch (FileNotFoundException e) {
		
		} catch (IOException e) {

		}
	} 
}

