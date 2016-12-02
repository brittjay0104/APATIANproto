package node_visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import code_parser.ModelSourceFile;

/**
 * Class to visit Variable Decelerations and Extract <i>STUFF</i> 
 * 
 * //TODO Fix description
 * 
 * @author Rahul Pandita
 *
 */
public class VariablesVisitor extends AbstractVisitor {

	public VariablesVisitor(ModelSourceFile file) {
		super(file);
	}

	private List<VariableData> allVariableData = new ArrayList<>();

	@Override
	public boolean visit(FieldDeclaration node) {
		processVariableDeclerationFragments(node.fragments(), node.toString(), node.getModifiers(), true);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {

		processVariableDeclerationFragments(node.fragments(), node.toString(), node.getModifiers(), false);
		return super.visit(node);
	}

	private void processVariableDeclerationFragments(List<?> fragments, String srcLineStr, int modifier, boolean classMember) {
		
		String name;
		String type;
		VariableDeclarationFragment decFragment;

		for (Object fragObject : fragments) {
			if (fragObject instanceof VariableDeclarationFragment) {
				decFragment = (VariableDeclarationFragment) fragObject;
				name = decFragment.getName().getIdentifier();
				type = decFragment.getName().resolveTypeBinding().getName();
				allVariableData.add(new VariableData(modifier, name, type, srcLineStr, classMember));
			}
		}
	}

	public List<VariableData> getAllVariableData() {
		return allVariableData;
	}

}
