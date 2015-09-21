package node_visitor;

public class NODP {
	
	String field;
	String type;
	int attrCount;
	String revAdded;
	String revRemoved;
	
	public NODP(String field, String type){
		this.field = field;
		this.type = type;
		attrCount = 0;
		revAdded = "";
		revRemoved = "";
	}
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int incrAttrCount(){
		attrCount += 1;
		
		return attrCount;
	}
	
	public int decrAttrCount(){
		attrCount -= 1;
		
		return attrCount;
	}
	
	public int getAttrCount(){
		return attrCount;
	}
	
	public void setRevAdded(String rev){
		revAdded = rev;
	}
	
	public String getRevAdded(){
		return revAdded;
	}
	
	public void setRevRemoved(String rev){
		revRemoved = rev;
	}
	
	public String getRevRemoved(){
		return revRemoved;
	}

	public boolean equals (NODP n){
		
		if (this.getClass() == n.getClass() && this.getField().equals(n.getField()) && this.getType().equals(n.getType())){
			return true;			
		}
		
		return false;
	}

}
