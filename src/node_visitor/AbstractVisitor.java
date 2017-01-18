package node_visitor;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import code_parser.ModelSourceFile;

public abstract class AbstractVisitor extends ASTVisitor {

	//TODO only one subclass currently; should have more
	private char[] source;
	public static final char CHECK_SEPERATOR = Character.MAX_VALUE;

	public AbstractVisitor(ModelSourceFile file) {
		this.source = file.source;
	}

	protected String findSourceForNode(ASTNode node) {
		try {
			return new String(Arrays.copyOfRange(source, node.getStartPosition(), node.getStartPosition() + node.getLength()));
		}
		catch (Exception e) {
			System.err.println("OMG PROBLEM MAKING SOURCE FOR "+node);
			return "";
		}
	}

	protected String signatureOfParent(ASTNode node) {
		Initializer init = getInitializer(node);
		MethodDeclaration md = getMethodDeclaration(node);
		
		String signature;
		if (md != null){
			signature = md.getName().toString();
		}else if (init != null){
			StringBuffer sb = new StringBuffer();
			for (Object i : init.modifiers()){
				sb.append(findSourceForNode((ASTNode)i));
			}
			signature = sb.toString();
		}else{
			throw new RuntimeException("Unknown container for " + node.getClass());
		}
		return signature;
	}

	private MethodDeclaration getMethodDeclaration(ASTNode node) {
		if (node.getParent() != null){
			return node instanceof MethodDeclaration ? (MethodDeclaration)node : getMethodDeclaration(node.getParent());			
		}
		
		return null;
	}

	private Initializer getInitializer(ASTNode node) {
		if (node.getParent() != null){
			return node instanceof Initializer ? (Initializer)node : getInitializer(node.getParent());			
		}
		
		return null;
	}
}