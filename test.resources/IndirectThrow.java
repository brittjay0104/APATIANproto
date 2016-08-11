// uncheckedExceptions = 1
// throwStatements = 1
public class IndirectThrow {
	
	void m() {
		throw n();
	}
	
	RuntimeException n(){
		return new RuntimeException();
	}
}
