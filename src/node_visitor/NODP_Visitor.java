package node_visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import code_parser.ModelSourceFile;

public class NODP_Visitor extends ASTVisitor{
	
	
	List<String> NOPDs = new ArrayList<String>();
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
	
	public boolean visit (FieldDeclaration node){
		
		List mods = node.modifiers();
		int count = 0;

		for (int i = 0; i < mods.size(); i++){
			String modifier = mods.get(i).toString();
			//System.out.println(modifier);
			
			if (modifier.equals("static") || modifier.equals("private") || modifier.equals("final")){
				count += 1;
			}
		}
		
		if (count == 3){
			System.out.println("Potential Singleton!");	
		}
		
		String type = node.getType().toString();
		if (type.toLowerCase().contains("null")){
			System.out.println("Potential NODP!");
		}
		
		List decs = node.fragments();
		
		for (int i = 0; i < decs.size(); i++){
			System.out.println(decs.get(i));
		}
		
		return true;
	}
	
	public boolean visit(MethodDeclaration node){
		
		return true;
	}
	
}
