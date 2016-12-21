package test.resources.exceptions;
// tryWithResources = 1
// catchBlocks = 2
// checkedExceptions = 2

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StaticTryWithResources_6 {

	static {
		
		try(FileInputStream input = new FileInputStream("file.txt")) {
			int data = input.read();

		} catch (FileNotFoundException e) {
		
		} catch (IOException e) {

		}
	} 
}

