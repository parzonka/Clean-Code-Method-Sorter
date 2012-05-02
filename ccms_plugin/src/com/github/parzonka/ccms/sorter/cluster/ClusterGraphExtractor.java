/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;
import com.github.parzonka.ccms.sorter.callgraph.TopLevelASTVisitor;
import com.github.parzonka.ccms.sorter.comparator.Signature;

/**
 * Aggregates getters/setters and overloaded methods into {@link ClusterNode}
 * which form a clusterGraph. Each cluster-representant may be part in only one
 * cluster.
 * <p>
 * TODO: The algorithmic structure is not easy generalizable, architecture
 * should be refactored in further builds.
 *
 * @author Mateusz Parzonka
 *
 */
public class ClusterGraphExtractor extends TopLevelASTVisitor {

    final private static Logger logger = LoggerFactory.getLogger(ClusterGraphExtractor.class);

    private final boolean clusterGetterSetters;
    private final boolean clusterOverloadedMethods;
    private final Map<Signature, CallGraphNode> signature2node;
    private final List<MethodDeclaration> methods;
    private final Set<MethodDeclaration> alreadyClusteredMethods;
    private final List<ClusterNode> clusteredGraph;

    public ClusterGraphExtractor(List<CallGraphNode> callGraph, boolean clusterGetterSetters,
	    boolean clusterOverloadedMethods) {
	this.clusterGetterSetters = clusterGetterSetters;
	this.clusterOverloadedMethods = clusterOverloadedMethods;
	this.signature2node = getSignature2NodeMapping(callGraph);
	this.methods = new ArrayList<MethodDeclaration>();
	this.alreadyClusteredMethods = new HashSet<MethodDeclaration>();
	this.clusteredGraph = new ArrayList<ClusterNode>();

    }

    private HashMap<Signature, CallGraphNode> getSignature2NodeMapping(List<CallGraphNode> callGraph) {
	final HashMap<Signature, CallGraphNode> signature2node = new HashMap<Signature, CallGraphNode>();
	for (final CallGraphNode callGraphNode : callGraph)
	    signature2node.put(callGraphNode.getSignature(), callGraphNode);
	return signature2node;
    }

    /**
     * @return the clusteredGraph
     */
    public List<ClusterNode> getClusteredGraph() {

	for (final MethodDeclaration method : this.methods) {

	    if (isNotClusteredYet(method)) {
		if (this.clusterGetterSetters && hasSetterPattern(method))
		    searchGetter(method);
		else if (this.clusterGetterSetters && hasGetterPattern(method))
		    searchSetter(method);
		else
		    handleNormalAndOverloadedMethods(method);
	    }
	}
	return this.clusteredGraph;
    }

    /**
     * @param method
     * @return
     */
    private boolean isNotClusteredYet(MethodDeclaration method) {
	return !this.alreadyClusteredMethods.contains(method);
    }

    /**
     * A method is a setter when it starts with "set" and has only one
     * parameter.
     *
     * @param methodDeclaration
     * @return
     */
    private boolean hasSetterPattern(MethodDeclaration methodDeclaration) {
	logger.trace("method: {} starts with \"set\":{}", methodDeclaration.getName().getIdentifier(),
		methodDeclaration.getName().getIdentifier().startsWith("set"));
	if (!methodDeclaration.getName().getIdentifier().startsWith("set"))
	    return false;
	@SuppressWarnings("unchecked")
	final List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
	if (parameters.size() != 1)
	    return false;
	return true;
    }

    private void searchGetter(MethodDeclaration potentialSetter) {
	final List<CallGraphNode> clusteredNodes = new ArrayList<CallGraphNode>();
	addToClusteredNodes(potentialSetter, clusteredNodes);
	for (final MethodDeclaration potentialGetter : this.methods) {

	    final String signature = Signature.getMethodSignature(potentialGetter);
	    logger.trace("{}: notClustered={}", signature, isNotClusteredYet(potentialGetter));
	    logger.trace("{}: hasGetterPattern={}", signature, hasGetterPattern(potentialGetter));
	    logger.trace("{}: matchingGettersAndSetters={}", signature,
		    matchingGetterAndSetter(potentialGetter, potentialSetter));

	    if (isNotClusteredYet(potentialGetter) && hasGetterPattern(potentialGetter)
		    && matchingGetterAndSetter(potentialGetter, potentialSetter)) {
		addToClusteredNodes(potentialGetter, clusteredNodes);
		logger.trace("Adding Getter [{}]", potentialGetter.getName());
	    }
	}
	addToClusteredGraph(clusteredNodes);

    }

    /**
     * A method is a getter when it starts with "get" and is parameterless.
     *
     * @param methodDeclaration
     * @return
     */
    private boolean hasGetterPattern(MethodDeclaration methodDeclaration) {
	if (!methodDeclaration.getName().getIdentifier().startsWith("get"))
	    return false;
	@SuppressWarnings("unchecked")
	final List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
	if (parameters.size() != 0)
	    return false;
	return true;
    }

    private void searchSetter(MethodDeclaration potentialGetter) {
	final List<CallGraphNode> clusteredNodes = new ArrayList<CallGraphNode>();
	addToClusteredNodes(potentialGetter, clusteredNodes);
	for (final MethodDeclaration potentialSetter : this.methods) {

	    final String signature = Signature.getMethodSignature(potentialSetter);
	    logger.trace("{}: notClustered={}", signature, isNotClusteredYet(potentialSetter));
	    logger.trace("{}: hasSetterPattern={}", signature, hasGetterPattern(potentialSetter));
	    logger.trace("{}: matchingGettersAndSetters={}", signature,
		    matchingGetterAndSetter(potentialGetter, potentialSetter));

	    if (isNotClusteredYet(potentialSetter) && hasSetterPattern(potentialSetter)
		    && matchingGetterAndSetter(potentialGetter, potentialSetter)) {
		addToClusteredNodes(potentialSetter, clusteredNodes);
		logger.trace("Adding Setter [{}]", potentialGetter.getName());
	    }
	}
	addToClusteredGraph(clusteredNodes);
    }

    private void handleNormalAndOverloadedMethods(MethodDeclaration methodDeclaration) {

	final List<CallGraphNode> clusteredNodes = new ArrayList<CallGraphNode>();
	addToClusteredNodes(methodDeclaration, clusteredNodes);

	for (final MethodDeclaration otherMethodDeclaration : this.methods) {
	    logger.trace("Methodnames match={}", methodNamesMatch(methodDeclaration, otherMethodDeclaration));
	    if (this.clusterOverloadedMethods && methodNamesMatch(methodDeclaration, otherMethodDeclaration)) {
		logger.trace("clustering overloaded: {} : {}", methodDeclaration.getName().toString(),
			otherMethodDeclaration.getName().toString());
		addToClusteredNodes(otherMethodDeclaration, clusteredNodes);
	    }
	}

	addToClusteredGraph(clusteredNodes);
    }

    /**
     * @param methodDeclaration
     * @param clusteredNodes
     */
    private void addToClusteredNodes(MethodDeclaration methodDeclaration, List<CallGraphNode> clusteredNodes) {
	logger.trace("Adding node [{}]", (Signature.getMethodSignature(methodDeclaration)));
	clusteredNodes.add(this.signature2node.get(new Signature(methodDeclaration)));
	this.alreadyClusteredMethods.add(methodDeclaration);
    }

    /**
     * A method is a getter when it starts with "get" and is parameterless.
     *
     * @param methodDeclaration
     * @return
     */
    private boolean matchingGetterAndSetter(MethodDeclaration getter, MethodDeclaration setter) {
	if (getter.getName().getIdentifier().length() < 3)
	    return false;
	if (setter.getName().getIdentifier().length() < 3)
	    return false;
	final String getterName = getter.getName().getIdentifier().substring(3);
	final String setterName = setter.getName().getIdentifier().substring(3);
	if (!getterName.equals(setterName)) {
	    return false;

	}
	if (!returnTypeEqualsParameterType(getter, setter)) {
	    return false;
	}
	return true;
    }

    private void addToClusteredGraph(List<CallGraphNode> clusteredNodes) {
	this.clusteredGraph.add(new ClusterNode(clusteredNodes));
    }

    /**
     * @param methodDeclaration
     * @param otherMethod
     * @return
     */
    private boolean methodNamesMatch(MethodDeclaration methodDeclaration, MethodDeclaration otherMethod) {
	return isNotClusteredYet(otherMethod)
		&& methodDeclaration.getName().toString().equals(otherMethod.getName().toString());
    }

    /**
     * @param methodDeclaration1
     *            return type of this method is taken into account.
     * @param MethodDeclaration2
     *            the type of the first parameter is taken into account
     * @return
     */
    private static boolean returnTypeEqualsParameterType(MethodDeclaration methodDeclaration1,
	    MethodDeclaration MethodDeclaration2) {
	if (MethodDeclaration2.parameters().size() == 0)
	    return false;
	return ((SingleVariableDeclaration) MethodDeclaration2.parameters().get(0)).getType().toString()
		.equals(methodDeclaration1.getReturnType2().toString());
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
	if (this.signature2node.containsKey(new Signature(methodDeclaration))) {
	    this.methods.add(methodDeclaration);
	}
	return false;
    }

}
