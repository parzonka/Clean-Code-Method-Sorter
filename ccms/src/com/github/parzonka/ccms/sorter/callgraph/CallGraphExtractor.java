package com.github.parzonka.ccms.sorter.callgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.sorter.comparator.Signature;

/**
 * Visits all method declarations and extracts the lexical ordering for each
 * method.
 *
 * @author Mateusz Parzonka
 *
 */
public class CallGraphExtractor extends TopLevelASTVisitor {

    final private static Logger logger = LoggerFactory.getLogger(CallGraphExtractor.class);

    private CallGraphNode currentCaller;
    private int initializerCount = 0;
    private final Map<Signature, CallGraphNode> callGraphNodes;

    public CallGraphExtractor() {
	super();
	this.callGraphNodes = new HashMap<Signature, CallGraphNode>();
    }

    @Override
    public boolean visit(Initializer node) {
	final Signature signature = new Signature(this.initializerCount++);
	this.currentCaller = getNode(signature);
	logger.trace("{} visited", signature);
	return true;
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
	logger.trace("methodDeclaration: {}", methodDeclaration.getName().toString());
	this.currentCaller = getNode(new Signature(methodDeclaration));
	return true;
    }

    @Override
    public boolean visit(MethodInvocation methodInvocation) {
	logger.trace("methodInvocation: {}", methodInvocation.getName().toString());
	if (isInstanceInvocation(methodInvocation)) {

	    final Signature signature = new Signature(methodInvocation);
	    final CallGraphNode callee = getNode(signature);

	    final Signature currentCallerSignature = this.currentCaller != null ? this.currentCaller.getSignature()
		    : new Signature(0);

	    logger.trace("Caller [{}] invokes: [{}]", currentCallerSignature, methodInvocation.getName().toString());
	    logger.trace("Invocation resolved to signature [{}]", signature);

	    /*
	     * TODO currentCaller == null only happens with static field
	     * initializers before any method declaration in the source. Since
	     * static field initializers CAN appear somewhere after a method
	     * declaration, checking for currentCaller == null is no safe method
	     * to detect static field initializers. At the moment there is no
	     * fast solution visible. For the moment we just ignore static field
	     * initializers which call other methods.
	     */
	    if (this.currentCaller != null) {
		this.currentCaller.addCallee(callee);
	    }

	}
	return true;
    }

    private CallGraphNode getNode(Signature signature) {
	if (this.callGraphNodes.containsKey(signature)) {
	    return this.callGraphNodes.get(signature);
	} else {
	    final CallGraphNode callGraphNode = new CallGraphNode(signature);
	    this.callGraphNodes.put(signature, callGraphNode);
	    logger.trace("Created node with signature [{}]", signature);
	    return callGraphNode;
	}
    }

    /**
     * @param methodInvocation
     * @return
     */
    protected boolean isInstanceInvocation(MethodInvocation methodInvocation) {
	final IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

	boolean isInstanceInvocation = methodInvocation.getExpression() == null && methodBinding != null;
	if (methodBinding != null && !isInTopLevelType(methodBinding.getDeclaringClass())) {
	    logger.trace("{} not the toplevel type.", methodBinding.getDeclaringClass().getName().toString());
	    isInstanceInvocation = false;
	}
	logger.trace("Invocation {} is instanceInvocation: {}", methodInvocation.getName().toString(),
		isInstanceInvocation);
	return isInstanceInvocation;
    }

    /**
     * After this visitor has traversed the AST, clients can call this method to
     * retrieve the set of nodes which comprises the call graph.
     *
     * @return
     */
    public Collection<CallGraphNode> getCallGraph() {
	logger.info("CallGraphExtractor returns:");
	for (final CallGraphNode callGraphNode : this.callGraphNodes.values()) {
	    logger.info("Node: {}", callGraphNode.toString());
	}
	return this.callGraphNodes.values();
    }

}
