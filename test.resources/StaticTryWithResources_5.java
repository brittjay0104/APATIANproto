import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// try - try(FileInputStream input = new FileInputStream("file.txt"))
// catch - catch (FileNotFoundException e) , catch (IOException e)
// checked - catch (FileNotFoundException e) , catch (IOException e)
public class StaticTryWithResources_5 {

	static {
		
		try(FileInputStream input = new FileInputStream("file.txt")) {
			input.read();

		} catch (FileNotFoundException e) {
		
		} catch (IOException e) {

		}
	} 
}

