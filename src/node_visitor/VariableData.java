package node_visitor;

import org.eclipse.jdt.core.dom.Modifier;

public class VariableData {
	
	public static enum  VariableType {
		FIELD,
		LOCAL,
		PARAMETER
	}

	private int modifiers;
	
	private String name;
	
	private String type;
	
	private boolean isPrimitive;
	
	private String srcLineStr;
	
	private VariableType variableType;
	
	
	public VariableData(int modifiers, String name, String type, String srcLineStr, VariableType variableType, boolean primitive) {
		this.modifiers = modifiers;
		this.srcLineStr = srcLineStr;
		this.type = type;
		this.isPrimitive = primitive;
		this.name = name;
		this.variableType  = variableType;
		
	}
	
	public int getModifiers() {
		return modifiers;
	}

	public void setModifier(int modifiers) {
		this.modifiers = modifiers;
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
	
	public boolean getIsPrimitive(){
		return isPrimitive;
	}
	
	public void setIsPrimitive(boolean primitive){
		this.isPrimitive = primitive;
	}

	public String getSrcLineStr() {
		return srcLineStr;
	}

	public void setSrcLineStr(String srcLineStr) {
		this.srcLineStr = srcLineStr;
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public void setVariableType(VariableType variableType) {
		this.variableType = variableType;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer buff = new StringBuffer();
		buff.append(Modifier.isAbstract(modifiers)?"abstract ":"");
		buff.append(Modifier.isFinal(modifiers)?"final ":"");
		buff.append(Modifier.isNative(modifiers)?"native ":"");
		buff.append(Modifier.isPrivate(modifiers)?"private ":"");
		buff.append(Modifier.isProtected(modifiers)?"protected ":"");
		buff.append(Modifier.isPublic(modifiers)?"public ":"");
		buff.append(Modifier.isStatic(modifiers)?"static ":"");
		buff.append(Modifier.isStrictfp(modifiers)?"strictfp ":"");
		buff.append(Modifier.isSynchronized(modifiers)?"synchronized ":"");
		buff.append(Modifier.isTransient(modifiers)?"transient ":"");
		buff.append(Modifier.isVolatile(modifiers)?"volatile ":"");
		//"\u03D5" represents absence of modifiers
		buff.append((buff.toString().trim().length()==0)?"\u03D5":"");
		buff.append(", ").append(type).append(", ").append(name).append(", ").append("\n\t").append(srcLineStr);
		return buff.toString();
	}
}
