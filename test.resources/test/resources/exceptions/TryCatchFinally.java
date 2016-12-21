package test.resources.exceptions;
// tryStatements = 2
// catchBlocks = 2
// finallyBlocks = 1
// checkedExceptions = 2

import java.io.FileReader;
import java.io.IOException;

public class TryCatchFinally {
	
    public void openFile(){
        FileReader reader = null;
        try {
            reader = new FileReader("someFile");
            int i=0;
            while(i != -1){
                i = reader.read();
                System.out.println((char) i );
            }
        } catch (IOException e) {
            //do something clever with the exception
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    //do something clever with the exception
                }
            }
            System.out.println("--- File End ---");
        }
    }

}
