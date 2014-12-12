import org.apache.log4j.*;
 
public class TestLog {
 
	/*
	 * get a static logger instance with name TestLog
	 */
	static Logger myLogger = Logger.getLogger(TestLog.class.getName());
	Appender myAppender;
	SimpleLayout myLayout;
 
	/* Constructor */
	public TestLog() {
 
		/*
		 * Set logger priority level programmatically. Though this is better done externally
		 */
		myLogger.setLevel(Level.ALL);
 
		/*
		 * Instantiate a layout and an appender, assign layout to appender
		 * programmatically
		 */
		myLayout = new SimpleLayout();
		myAppender = new ConsoleAppender(myLayout); // Appender is Interface
 
		/* Assign appender to the logger programmatically */
		myLogger.addAppender(myAppender);
 
	} // end constructor
 
	public void do_something(int a, float b) {
 
		/*
		 * This log request enabled and log statement logged, since INFO = INFO
		 */
		myLogger.info("The values of parameters passed to method  do_something are: "+ a + ", " + b);
 
		/* this log request is not enabled, since DEBUG &lt; INFO */ 		myLogger.debug("Operation performed successfully"); 		Object x=null; 		if (x == null) { 			/* 			 * this log request is enabled and log statement logged, since ERROR 			 * &gt; INFO
			 */
			myLogger.error("Value of X is null");
 
		}
	} // end do_something()
	public static void main(String []args){
		new TestLog().do_something(1, 3);
	}