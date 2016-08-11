// tryWithResources = 1
// catchBlocks = 1
// checkedExceptions = 1

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
