// try - br.read();
// try with resource - try (BufferedReader br = new BufferedReader(new FileReader("C:\\testing.txt")))
// catch - catch (IOException e)
// checked - catch (IOException e) 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class InitializerTryWithResources_4 {

	{
		try (BufferedReader br = new BufferedReader(new FileReader("C:\\testing.txt")))
		{
			br.read();
		} catch (IOException e) {
			
		}
	}
}
