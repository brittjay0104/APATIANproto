package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.reflect.MethodDelegate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;

import code_parser.ModelSourceFile;

public class NODP_Visitor extends ASTVisitor{
	
	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;
	
	List<String> NODPs = new ArrayList<String>();
	List<String> fields =  new ArrayList<String>();
	private char[] source;
	String currentRev;
	
	public NODP_Visitor(ModelSourceFile file, String currentRevision){
		this.source = file.getSource();
		currentRev = currentRevision;

	}
	
	private String findSourceForNode(ASTNode node) {
		try {
			return new String(Arrays.copyOfRange(source, node.getStartPosition(), node.getStartPosition() + node.getLength()));
		}
		catch (Exception e) {
			System.err.println("OMG PROBLEM MAKING SOURCE FOR "+node);
			return "";
		}
	}
	
	public MethodDeclaration getMethodDeclaration(ASTNode node){		
		if (node.getParent() != null){
			return node instanceof MethodDeclaration ? (MethodDeclaration)node : getMethodDeclaration(node.getParent());			
		}
		
		return null;
	}
	
	public boolean visit (FieldDeclaration node){
		
		String fieldDec = findSourceForNode(node);
		//System.out.println(fieldDec);
		
		if (fieldDec.contains("static") && fieldDec.contains("private") && fieldDec.contains("final")){
			//System.out.println("Potential Singleton!");
			String type = node.getType().toString();
			if (type.toLowerCase().contains("null")){
				//System.out.println("Potential NODP!");
				
				List decs = node.fragments();
				
				for (int i = 0; i < decs.size(); i++){
					String dec = decs.get(i).toString();			
					String var = dec.substring(0, dec.indexOf("="));
					
					
					//System.out.println(var);
					
					//Add type to field for uniqueness (required for Singleton/NODP)
					String NODPvar = type + CHECK_SEPERATOR + var;
					
					//System.out.println(NODPvar);
					if (!(fields.contains(NODPvar))){
						//System.out.println(NODPvar + "added!");
						fields.add(NODPvar);
					}
				}
			}
		}
					
		return true;
	}
	
	public boolean visit(ReturnStatement node){
		
		String ret = node.toString();
		
		//System.out.println(ret);
		for (int i =0; i < fields.size(); i++){
			String field = fields.get(i);
			String var = field.substring(field.indexOf(CHECK_SEPERATOR)+1, field.length());
			
			//System.out.println("Return variable: " + var);
			
			if (ret.contains(var)){
				MethodDeclaration md = getMethodDeclaration(node);
				String m = md.modifiers().toString();
				if (m.contains("static")){
					//System.out.println("Static method that returns " + field);
					//System.out.println(field + " might be used for NODP!");
					
					// Strong possibility NODP in play
					
					String check = field + CHECK_SEPERATOR + ret;
					fields.set(i, check);
					if (!NODPs.contains(check)){
						NODPs.add(check);						
					}
					
					
//					if (nodp.getAttrCount() < 2){
//						System.out.println("Increment count -- potential NODP!");
//						int count = nodp.incrAttrCount();
//						
//						if (count == 2){
//							System.out.println("Full implementation flagged at rev " + currentRev);
//							nodp.setRevAdded(currentRev);						
//						}						
//					}
					
				}
				
			}
		}
		
		return true;
	}
	
	public List<String> getNODPs(){
		return NODPs;
	}
	
	
}
