package test.resources.exceptions;
// uncheckedExceptions = 1

public class InitializerThrows {

	{
		if(1==1)
			throw new RuntimeException();
	}
}
