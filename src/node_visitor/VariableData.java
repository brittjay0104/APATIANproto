package node_visitor;

import org.eclipse.jdt.core.dom.Modifier;

public class VariableData {
	
	private int modifier;
	
	private String name;
	
	private String type;
	
	private String srcLineStr;
	
	private boolean classMember;
	
	
	
	public VariableData(int modifier, String name, String type, String srcLineStr, boolean classMember) {
		this.modifier = modifier;
		this.srcLineStr = srcLineStr;
		this.type = type;
		this.name = name;
		this.classMember = classMember;
		
	}
	
	public int getModifier() {
		return modifier;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSrcLineStr() {
		return srcLineStr;
	}

	public void setSrcLineStr(String srcLineStr) {
		this.srcLineStr = srcLineStr;
	}

	public boolean isClassMember() {
		return classMember;
	}

	public void setClassMember(boolean classMember) {
		this.classMember = classMember;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer buff = new StringBuffer();
		buff.append(Modifier.isAbstract(modifier)?"abstract ":"");
		buff.append(Modifier.isFinal(modifier)?"final ":"");
		buff.append(Modifier.isNative(modifier)?"native ":"");
		buff.append(Modifier.isPrivate(modifier)?"private ":"");
		buff.append(Modifier.isProtected(modifier)?"protected ":"");
		buff.append(Modifier.isPublic(modifier)?"public ":"");
		buff.append(Modifier.isStatic(modifier)?"static ":"");
		buff.append(Modifier.isStrictfp(modifier)?"strictfp ":"");
		buff.append(Modifier.isSynchronized(modifier)?"synchronized ":"");
		buff.append(Modifier.isTransient(modifier)?"transient ":"");
		buff.append(Modifier.isVolatile(modifier)?"volatile ":"");
		//"\u03D5" represents absence of modifiers
		buff.append((buff.toString().trim().length()==0)?"\u03D5":"");
		buff.append(", ").append(type).append(", ").append(name).append(", ").append("\n\t").append(srcLineStr);
		return buff.toString();
	}
}
