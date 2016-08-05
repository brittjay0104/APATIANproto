import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SimpleTryWithResources_5 {
	
	public String createTry() {
		String test = "";
		try (BufferedReader br = new BufferedReader(new FileReader(new File("file.txt")))) {
			test = br.readLine();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}
		return test;
	}

}
