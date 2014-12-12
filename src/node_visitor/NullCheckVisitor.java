package node_visitor;

import static code_parser.ModelRepository.CHECK_SEPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import code_parser.ModelSourceFile;


public class NullCheckVisitor extends ASTVisitor{

	ArrayList<String> nullChecks = new ArrayList<String>();
	// null methods passed to parser after analysis
	List <String> nullMethods = new ArrayList<String>();

	// null check and next line of code
	ArrayList<String> ifAndNext;

	String removedMethod;
	String removedStartPos;
	private char[] source;


	/**
	 * Constructor for visitor that analyzes code for null checks
	 */
	public NullCheckVisitor(ModelSourceFile file){
		ifAndNext = new ArrayList<String>();
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

	@SuppressWarnings("unchecked")
	public boolean visit(InfixExpression node){
		
		Expression leftOperand = node.getLeftOperand();
		Operator op = node.getOperator();
		Expression rightOperand = node.getRightOperand();

		if (op.equals(Operator.EQUALS) || op.equals(Operator.NOT_EQUALS)){
			if (leftOperand instanceof NullLiteral || rightOperand instanceof NullLiteral) {

				// trying to get if statement (store with location and method - or instead of?)
				ASTNode parent = node.getParent();
				
				if (getMethodDeclaration(parent) != null){
					// simple if statements
					if (parent instanceof IfStatement){
						Statement then = ((IfStatement) parent).getThenStatement();

						Statement firstLineInThenBlock = null;
						if (then instanceof Block) {
							List<Statement> blockStatements = ((Block)then).statements();
							if (!blockStatements.isEmpty())
								firstLineInThenBlock = blockStatements.get(0);
						} else {
							//probably an ExpressionStatement
							firstLineInThenBlock = then;
						}

						String originalNullCheckSource = findSourceForNode(node);
						String firstLineInIfSource = findSourceForNode(firstLineInThenBlock);
						String decMethod = findSourceForNode(getMethodDeclaration(parent).getName());


						//System.out.println("Found null check with source `"+originalNullCheckSource + '`' + " in method " + decMethod);

						String check = originalNullCheckSource + CHECK_SEPERATOR + firstLineInIfSource;
						String carryOverCheck =  decMethod + CHECK_SEPERATOR + originalNullCheckSource; 
						
						if (!(nullChecks.contains(carryOverCheck))){
							nullChecks.add(carryOverCheck);
						}

						if (!(ifAndNext.contains(check))){
							ifAndNext.add(check);
						}

						return true;

					}
					// complex if statements
					else if (parent instanceof InfixExpression){
						System.out.println(findSourceForNode(parent));
						
						// TODO HERE - account for possiblity of assert statement in method dec too!
						if (getIfStatement(parent) != null) {
							IfStatement ifParent = getIfStatement(parent);
							
							Statement then = ((IfStatement) ifParent).getThenStatement();
							
							Statement firstLineInThenBlock = null;
							if (then instanceof Block) {
								List<Statement> blockStatements = ((Block)then).statements();
								if (!blockStatements.isEmpty())
									firstLineInThenBlock = blockStatements.get(0);
							} else {
								//probably an ExpressionStatement
								firstLineInThenBlock = then;
							}
							
							String originalNullCheckSource = findSourceForNode(node);
							String firstLineInIfSource = findSourceForNode(firstLineInThenBlock);
							String decMethod = findSourceForNode(getMethodDeclaration(parent).getName());
							
							
							//System.out.println("Found null check with source `"+originalNullCheckSource + '`' + " in method " + decMethod);
							
							String carryOverCheck =  decMethod + CHECK_SEPERATOR + originalNullCheckSource; 
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
							
							String check = originalNullCheckSource + CHECK_SEPERATOR + firstLineInIfSource;
							if (!(ifAndNext.contains(check))){
								ifAndNext.add(check);
							}
							
							
							return true;							
						} else if (getAssertStatement(parent) != null){
							//AssertStatement assertParent = getAssertStatement(parent);
							
							String originalNullCheckSource = findSourceForNode(node);
							String typeDec = findSourceForNode(getTypeDeclaration(parent).getName());
							
							String carryOverCheck = typeDec + CHECK_SEPERATOR + originalNullCheckSource;
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
						}

					}
					// return statements
					else if (parent instanceof ConditionalExpression){
						
						//System.out.println(originalNullCheckSource);

						if (getReturnStatement(parent) != null) {
							ReturnStatement returnNode = getReturnStatement(parent);
							
							String originalNullCheckSource = findSourceForNode(node);
							String decMethod = findSourceForNode(getMethodDeclaration(parent).getName());
							String fullReturnStatment = findSourceForNode(returnNode);
							
							
							//System.out.println("Found null check with source `"+originalNullCheckSource + '`' + " in method " + decMethod);
							
							String carryOverCheck =  decMethod + CHECK_SEPERATOR + originalNullCheckSource; 
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
							
							
							String check = originalNullCheckSource + CHECK_SEPERATOR + fullReturnStatment;
							if (!(ifAndNext.contains(check))){
								ifAndNext.add(check);
							}
							
							return true;						
						} else if (getMethodInvocation(parent) != null) {
							
							MethodInvocation invocNode = (MethodInvocation) getMethodInvocation(parent);
							
							String originalNullCheckSource = findSourceForNode(node);
							String decMethod = findSourceForNode(getMethodDeclaration(parent).getName());
							String fullInvocation = findSourceForNode(invocNode);
							
							String carryOverCheck = decMethod + CHECK_SEPERATOR + fullInvocation;
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
							
							
							String check = originalNullCheckSource + CHECK_SEPERATOR + fullInvocation;
							if (!(ifAndNext.contains(check))){
								ifAndNext.add(check);
							}
							
							return true;
							
						}
					}
				} else if (getTypeDeclaration(parent) != null) {
					
					// simple if statements
					if (parent instanceof IfStatement){
						Statement then = ((IfStatement) parent).getThenStatement();

						Statement firstLineInThenBlock = null;
						if (then instanceof Block) {
							List<Statement> blockStatements = ((Block)then).statements();
							if (!blockStatements.isEmpty())
								firstLineInThenBlock = blockStatements.get(0);
						} else {
							//probably an ExpressionStatement
							firstLineInThenBlock = then;
						}

						String originalNullCheckSource = findSourceForNode(node);
						String firstLineInIfSource = findSourceForNode(firstLineInThenBlock);
						String typeDec = findSourceForNode(getTypeDeclaration(parent).getName());
		

						//System.out.println("Found null check with source `"+originalNullCheckSource + '`' + " in method " + decMethod);

						String check = originalNullCheckSource + CHECK_SEPERATOR + firstLineInIfSource;
						String carryOverCheck =  typeDec + CHECK_SEPERATOR + originalNullCheckSource; 
						
						if (!(nullChecks.contains(carryOverCheck))){
							nullChecks.add(carryOverCheck);
						}

						if (!(ifAndNext.contains(check))){
							ifAndNext.add(check);
						}

						return true;

					}
					// complex if statements
					else if (parent instanceof InfixExpression){
						
						if (getIfStatement(parent) != null){
							IfStatement ifParent = getIfStatement(parent);
							
							Statement then = ((IfStatement) ifParent).getThenStatement();
							
							Statement firstLineInThenBlock = null;
							if (then instanceof Block) {
								List<Statement> blockStatements = ((Block)then).statements();
								if (!blockStatements.isEmpty())
									firstLineInThenBlock = blockStatements.get(0);
							} else {
								//probably an ExpressionStatement
								firstLineInThenBlock = then;
							}
							
							String originalNullCheckSource = findSourceForNode(node);
							String firstLineInIfSource = findSourceForNode(firstLineInThenBlock);
							String typeDec = findSourceForNode(getTypeDeclaration(parent).getName());
							
							
							//System.out.println("Found null check with source `"+originalNullCheckSource + '`' + " in method " + decMethod);
							
							String carryOverCheck =  typeDec + CHECK_SEPERATOR + originalNullCheckSource; 
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
							
							String check = originalNullCheckSource + CHECK_SEPERATOR + firstLineInIfSource;
							if (!(ifAndNext.contains(check))){
								ifAndNext.add(check);
							}
							
						} else if (getAssertStatement(parent) != null){
							//AssertStatement assertParent = getAssertStatement(parent);
							
							String originalNullCheckSource = findSourceForNode(node);
							String typeDec = findSourceForNode(getTypeDeclaration(parent).getName());
							
							String carryOverCheck = typeDec + CHECK_SEPERATOR + originalNullCheckSource;
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
						}


						return true;

					}
					// return statements
					else if (parent instanceof ConditionalExpression){
						
						//System.out.println(originalNullCheckSource);

						if (getReturnStatement(parent) != null) {
							ReturnStatement returnNode = getReturnStatement(parent);
							
							String originalNullCheckSource = findSourceForNode(node);
							String typeDec = findSourceForNode(getTypeDeclaration(parent).getName());
							String fullReturnStatment = findSourceForNode(returnNode);
							
							
							//System.out.println("Found null check with source `"+originalNullCheckSource + '`' + " in method " + decMethod);
							
							String carryOverCheck =  typeDec + CHECK_SEPERATOR + originalNullCheckSource; 
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
							
							
							String check = originalNullCheckSource + CHECK_SEPERATOR + fullReturnStatment;
							if (!(ifAndNext.contains(check))){
								ifAndNext.add(check);
							}
							
							return true;						
						} else if (getMethodInvocation(parent) != null) {
							
							MethodInvocation invocNode = (MethodInvocation) getMethodInvocation(parent);
							
							String originalNullCheckSource = findSourceForNode(node);
							String typeDec = findSourceForNode(getTypeDeclaration(parent).getName());
							String fullInvocation = findSourceForNode(invocNode);
							
							String carryOverCheck = typeDec + CHECK_SEPERATOR + fullInvocation;
							
							if (!(nullChecks.contains(carryOverCheck))){
								nullChecks.add(carryOverCheck);
							}
							
							
							String check = originalNullCheckSource + CHECK_SEPERATOR + fullInvocation;
							if (!(ifAndNext.contains(check))){
								ifAndNext.add(check);
							}
							
							return true;
							
						}
					}
				}

			}

		}
		


		return super.visit(node);
	}
	
	private TypeDeclaration getTypeDeclaration(ASTNode node){
		if (node.getParent() != null){
			return node instanceof TypeDeclaration ? (TypeDeclaration) node : getTypeDeclaration(node.getParent());
		}
		
		return null;
	}

	private MethodInvocation getMethodInvocation(ASTNode node) {
		if (node.getParent() != null){
			return node instanceof MethodInvocation ? (MethodInvocation) node : getMethodInvocation(node.getParent());
		}
		
		return null;
	}


	private IfStatement getIfStatement(ASTNode node) {
		if (node.getParent() != null){
			return node instanceof IfStatement ? (IfStatement)node : getIfStatement(node.getParent());			
		}
		
		return null;
	}
	
	private AssertStatement getAssertStatement(ASTNode node){
		if (node.getParent() != null){
			return node instanceof AssertStatement ? (AssertStatement)node: getAssertStatement(node.getParent());
		}
		
		return null;
	}

	private ReturnStatement getReturnStatement(ASTNode node){
		if (node.getParent() != null){
			return node instanceof ReturnStatement ? (ReturnStatement)node : getReturnStatement(node.getParent());
		}
		
		return null;
	}


	/**
	 *
	 * Returns list of NullCheck items from the NullCheckVisitor
	 *
	 * @return
	 */
	public ArrayList<String> getNullChecks() {
		return nullChecks;
	}

	public ArrayList<String> getIfAndNext() {
		return ifAndNext;
	}

	public List<String> getNullMethods(){
		return nullMethods;
	}
}
