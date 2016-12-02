package node_visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import code_parser.ModelSourceFile;

/**
 * Class to visit Variable Decelerations and Extract <i>STUFF</i>
 * //TODO Fix description  
 * @author Rahul Pandita
 *
 */
public class VariablesVisitor extends AbstractVisitor{

	public VariablesVisitor(ModelSourceFile file) {
		super(file);
	}
	
	public List<FieldDeclaration> allFieldDeclerations = new ArrayList<>();
	public List<String> globalVariableList = new ArrayList<>();
	
	
	@Override
	public boolean visit(FieldDeclaration node) {
		return super.visit(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return super.visit(node);
	}

}
