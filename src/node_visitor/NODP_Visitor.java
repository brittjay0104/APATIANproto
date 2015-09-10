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
	
	
	List<String> NODPs = new ArrayList<String>();
	List<String> fields =  new ArrayList<String>();
	private char[] source;
	
	public NODP_Visitor(ModelSourceFile file){
		this.source = file.getSource();
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
					String NODPVar = type + "-" + var;
					
					// Add field that could be used in NODP
					//System.out.println(NODPVar);
					if (!fields.contains(NODPVar)){
						fields.add(NODPVar);						
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
			if (ret.contains(field)){
				MethodDeclaration md = getMethodDeclaration(node);
				String m = md.modifiers().toString();
				if (m.contains("static")){
//					System.out.println("Static method that returns " + field);
//					System.out.println(field + " might be used for NODP!");
					
					// Strong possibility NODP in play
					NODPs.add(field);
				}
				
			}
		}
		
		return true;
	}
	
}
