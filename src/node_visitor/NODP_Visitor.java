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
	
	List<NODP> NODPs = new ArrayList<NODP>();
	//List<String> fields =  new ArrayList<String>();
	private char[] source;
	String currentRev;
	List<NODP> currentNODPs = new ArrayList<NODP>();
	
	public NODP_Visitor(ModelSourceFile file, String currentRevision, List<NODP> fileNODPs){
		this.source = file.getSource();
		currentRev = currentRevision;
		
		if (!fileNODPs.isEmpty()){
			for (NODP n: fileNODPs){
				NODPs.add(n);
			}			
		}
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
				System.out.println("Potential NODP!");
				
				List decs = node.fragments();
				
				for (int i = 0; i < decs.size(); i++){
					String dec = decs.get(i).toString();			
					String var = dec.substring(0, dec.indexOf("="));
					
					
					//System.out.println(var);
					
					//Add type to field for uniqueness (required for Singleton/NODP)
					NODP f = new NODP(var, type);
					
					// if object with same field name and type already in there, flag it
					// TODO -- here is where we might check for removal as well! if it's already there look at count number...? 
					// Or at higher level (file level?) with saved list (compare values)
					
					currentNODPs.add(f);
					
					boolean there = false;
					
					for (NODP n: NODPs){
						if (n.equals(f)){
							System.out.println("NODP already exists!");
							there = true;
						}
					}

					// Increment count; part 1 of having an NODP implemented 
					// Add NODP object that could be used in NODP
					if (!there){
						System.out.println("NODP field added!");
						f.incrAttrCount();
						NODPs.add(f);
					}
				}
			}
		}
					
		return true;
	}
	
	public boolean visit(ReturnStatement node){
		
		String ret = node.toString();
		
		//System.out.println(ret);
		for (int i =0; i < NODPs.size(); i++){
			NODP nodp = NODPs.get(i);
			
			String var = nodp.getField();
			System.out.println("Return variable: " + var);
			
			if (ret.contains(var)){
				MethodDeclaration md = getMethodDeclaration(node);
				String m = md.modifiers().toString();
				if (m.contains("static")){
					//System.out.println("Static method that returns " + field);
					//System.out.println(field + " might be used for NODP!");
					
					// Strong possibility NODP in play
					// here is where count would change to 2 (meaning both pieces are present)
					
					if (nodp.getAttrCount() < 2){
						System.out.println("Increment count -- potential NODP!");
						int count = nodp.incrAttrCount();
						
						if (count == 2){
							System.out.println("Full implementation flagged at rev " + currentRev);
							nodp.setRevAdded(currentRev);						
						}						
					}
					
				}
				
			}
		}
		
		return true;
	}
	
	public List<NODP> getNODPs(){
		return NODPs;
	}
	
	public List<NODP> getCurrentNODPs()	{
		return currentNODPs;
	}
	
	
}
