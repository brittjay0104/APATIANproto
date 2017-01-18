package test.resources.exceptions;
// tryStatements = 1
// catchBlocks = 1
// finallyBlocks = 1
// uncheckedExceptions = 1

public class InitializerTryCatchFinally {

	{
		 try{  
		   int data=25/5;  
		   System.out.println(data);  
		} catch(NullPointerException e){
			System.out.println(e);
		} finally {
			System.out.println("finally block is always executed");
		}  
	}  
}
