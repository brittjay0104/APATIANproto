package node_visitor;

import java.awt.List;
//import java.util.Optional;

public class NullObjectPattern_test extends List {
	
	private static final NullObjectPattern_test instance = new NullObjectPattern_test();
	  private NullObjectPattern_test() { }

	  public static NullObjectPattern_test Singleton()
	  {
	    return instance; 
	  }

	  public List getTail()
	  {
	    return this;
	  }
	  
	    public static void main(String[] args) 
	    {
	        // TODO Auto-generated method stub
	        try
	        {
	                        
	            for (int i = 0; i < 10; i++) 
	            {
	               // do something
	            	java.util.List<String> a = java.util.Collections.emptyList();

//	            	Optional<Integer> possible = Optional.of(5);
//	            	possible.isPresent(); // returns true
//	            	possible.get(); // returns 5
	          
	            }

	            // Create loop;
	            // nodes[9].next = nodes[3];
	            //Boolean abc= Check_Circular(nodes[0]);
	            //System.out.print(abc);
	        }
	        catch(NullPointerException e)
	        {
	            System.out.print("NullPointerException caught");
	        }

	    }

}
