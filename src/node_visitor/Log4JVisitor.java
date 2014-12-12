package node_visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class Log4JVisitor extends ASTVisitor {
	
	List<String> loggerUsage = new ArrayList<String>();
	
	List<String> loggerMethods = new ArrayList<String>();
	int loggerMethodsInvoked;
	
	public Log4JVisitor(){
		loggerMethods.add("getLogger");
		loggerMethods.add("setLevel");
		loggerMethods.add("addAppender");
		loggerMethods.add("info");
		loggerMethods.add("error");
		loggerMethods.add("warn");
	}
	
	public boolean visit (MethodInvocation node){
		
		if (loggerMethods.contains(node.getName().toString())){
			
			String methodCall = node.getName().toString() + "-" + node.getStartPosition();
			loggerUsage.add(methodCall);
			
			loggerMethodsInvoked += 1;
		}
		
		return super.visit(node);
	}
	
	public List<String> getMethodCalls(){
		return loggerUsage;
	}
	
	public int getLoggerMethodsCount(){
		return loggerMethodsInvoked;
	}

}
