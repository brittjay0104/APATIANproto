// try - int data = input.read();
// try with resource- try(FileInputStream input = new FileInputStream("file.txt"))
// catch - catch (FileNotFoundException e) 
// catch - catch (IOException e)
// checked - catch (FileNotFoundException e) 
// checked - catch (IOException e)

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

