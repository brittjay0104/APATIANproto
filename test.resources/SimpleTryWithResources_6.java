// try - test = br.readLine();
// try with resource - try (BufferedReader br = new BufferedReader(new FileReader(new File("file.txt"))))
// catch - catch (FileNotFoundException e)
// catch - catch (IOException e)
// checked - catch (FileNotFoundException e) 
// checked - catch (IOException e)

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SimpleTryWithResources_6 {
	
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
