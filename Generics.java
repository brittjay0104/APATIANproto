public class Box<T> {

  private T t;
  private Object o; 

  public Box(T k){
	  // code
  }
  
  public void add(T t) {
    this.t = t;
  }

  public T get() {
	  return new Func1<Throwable, Event<T>>();
  }
  
  public Observable<Event<T>> call (){
	  
  }
  
  public <T> void method(T item){
	  List<T> list = new ArrayList();
  }
  
  // determines the largest of three Comparable objects
   public static <T extends Comparable<T>> T maximum(T x, T y, T z)
   {                      
      T max = x; // assume x is initially the largest       
      if ( y.compareTo( max ) > 0 ){
         max = y; // y is the largest so far
      }
      if ( z.compareTo( max ) > 0 ){
         max = z; // z is the largest now                 
      }
      return max; // returns the largest object   
   }
   
   public static double sum(List<? extends Number> list){
       double sum = 0;
       for(Number n : list){
           sum += n.doubleValue();
       }
       return sum;
   }
   
   public static void printData(List<?> list){
       for(Object obj : list){
           System.out.print(obj + "::");
       }
   }

  public static void main(String[] args) {
	  
     Box<Integer> integerBox = new Box<Integer>();
     Box<String> stringBox = new Box<String>();
    
     integerBox.add(new Integer(10));
     stringBox.add(new String("Hello World"));

     System.out.printf("Integer Value :%d\n\n", integerBox.get());
     System.out.printf("String Value :%s\n", stringBox.get());
     
     System.out.printf( "Max of %d, %d and %d is %d\n\n", 
             3, 4, 5, maximum( 3, 4, 5 ) );

	System.out.printf( "Maxm of %.1f,%.1f and %.1f is %.1f\n\n",
	             6.6, 8.8, 7.7, maximum( 6.6, 8.8, 7.7 ) );
	
	System.out.printf( "Max of %s, %s and %s is %s\n","pear",
			"apple", "orange", maximum( "pear", "apple", "orange" ) );
	
	 this.<Number>method(new Integer(100));
	 
	 List<String> anEmptyStringList = Collections.<String>emptyList();

  }
}
