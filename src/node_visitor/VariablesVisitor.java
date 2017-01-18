package node_visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import code_parser.ModelSourceFile;
import node_visitor.VariableData.VariableType;

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
		processVariableDeclerationFragments(node.fragments(), node.toString(), node.getModifiers(), VariableType.FIELD);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		processVariableDeclerationFragments(node.fragments(), node.toString(), node.getModifiers() , VariableType.LOCAL);
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		SingleVariableDeclaration param;
		for(Object obj : node.parameters()){
			if(obj instanceof SingleVariableDeclaration){
				param = (SingleVariableDeclaration) obj;
				if (param.getType().isPrimitiveType()){
					allVariableData.add(new VariableData(param.getModifiers(), param.getName().getIdentifier(), param.getName().resolveBinding().getName(), node.toString(), VariableType.PARAMETER, true));
				} else {
					allVariableData.add(new VariableData(param.getModifiers(), param.getName().getIdentifier(), param.getName().resolveBinding().getName(), node.toString(), VariableType.PARAMETER, false));					
				}
			}
			else
			{
				System.err.println("Unexpected Type!!");
			}
				
		}
		return super.visit(node);
	}

	private void processVariableDeclerationFragments(List<?> fragments, String srcLineStr, int modifier, VariableType variableType) {
		
		String name;
		String type;
		VariableDeclarationFragment decFragment;

		for (Object fragObject : fragments) {
			if (fragObject instanceof VariableDeclarationFragment) {
				decFragment = (VariableDeclarationFragment) fragObject;
				name = decFragment.getName().getIdentifier();
				type = decFragment.getName().resolveTypeBinding().getName();
				boolean isPrimitive = decFragment.getName().resolveTypeBinding().isPrimitive();
				allVariableData.add(new VariableData(modifier, name, type, srcLineStr, variableType, isPrimitive));
			}
			else
			{
				System.err.println("Unexpected Type !");
			}
		}
	}

	public List<VariableData> getAllVariableData() {
		return allVariableData;
	}
}
