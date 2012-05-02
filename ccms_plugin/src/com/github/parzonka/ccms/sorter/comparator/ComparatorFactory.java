/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.comparator;

import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;
import com.github.parzonka.ccms.sorter.comparator.astextractor.AccessLevelComparatorExtractor;
import com.github.parzonka.ccms.sorter.comparator.astextractor.ConstructorComparatorExtractor;
import com.github.parzonka.ccms.sorter.comparator.astextractor.InitializerInvocationComparatorExtractor;
import com.github.parzonka.ccms.sorter.comparator.astextractor.SourcePositionComparatorExtractor;
import com.github.parzonka.ccms.sorter.invocation.InvocationSorter;
import com.github.parzonka.ccms.sorter.invocation.NodeOrdering;

/**
 * This class provides static methods that operate on collections are lists of
 * {@link CallGraphNode}s to return {@link Comparator}<String> objects, intended
 * to work with method signatures.
 *
 * @author Mateusz Parzonka
 *
 */
public class ComparatorFactory {

    private ComparatorFactory() {
	// not meant to be instantiated
    }

    public static Comparator<Signature> getAccessLevelComparator(ASTNode ast) {
	final AccessLevelComparatorExtractor accessLevelComparatorExtractor = new AccessLevelComparatorExtractor();
	ast.accept(accessLevelComparatorExtractor);
	return accessLevelComparatorExtractor.getComparator();
    }

    public static Comparator<Signature> getInvocationComparator(NodeOrdering nodeOrdering,
	    boolean traversalStrategyIsDepthFirst, List<? extends CallGraphNode> callGraphNodes) {

	final InvocationSorter invocationSorter = new InvocationSorter(nodeOrdering);
	final List<CallGraphNode> sortedNodes = traversalStrategyIsDepthFirst ? invocationSorter
		.getOrderedListDepthFirst(callGraphNodes) : invocationSorter.getOrderedListBreadthFirst(callGraphNodes);

	final SignatureComparator comparator = new SignatureComparator();

	int i = 0;
	for (final CallGraphNode node : sortedNodes) {
	    comparator.put(node.getSignature(), i++);
	}

	return comparator;

    }

    public static Comparator<Signature> getConstructorComparator(ASTNode ast) {
	final ConstructorComparatorExtractor extractor = new ConstructorComparatorExtractor();
	ast.accept(extractor);
	return extractor.getComparator();
    }

    /**
     * Returns a signature comparator which prefers methods that are called less
     * often.
     * <p>
     * Example: Methods that are called by no other method have the position 0,
     * methods that are called from 5 different methods have the position 5.
     * Note: Self-recursive methods are counted as being called by a method
     * (check if this makes sense...).
     *
     * @param callGraphNodes
     * @return
     */
    public static Comparator<Signature> getFanInComparator(Collection<CallGraphNode> callGraphNodes) {

	final Map<Signature, Integer> counts = new HashMap<Signature, Integer>();

	// initialize
	for (final CallGraphNode callGraphNode : callGraphNodes)
	    counts.put(callGraphNode.getSignature(), 0);

	for (final CallGraphNode callGraphNode : callGraphNodes) {
	    for (final CallGraphNode callee : callGraphNode.getCallees()) {
		counts.put(callee.getSignature(), counts.get(callee.getSignature()) + 1);
	    }
	}

	return new SignatureComparator(counts);
    }

    /**
     * Example: Method that calls some methods but is not called by itself has
     * the position 0, is sorted first. A method that calls 5 methods but is
     * called once itself, has the position 1/5. A method that is called some
     * times but does not call anybody has the maximum position, is sorted as
     * last method.
     *
     * @param callGraphNodes
     * @return
     */
    public static Comparator<Signature> getFanInFanOutRatioComparator(Collection<CallGraphNode> callGraphNodes) {

	final SignatureComparator comparator = new SignatureComparator();

	for (final CallGraphNode callGraphNode : callGraphNodes) {
	    final int calleesCount = callGraphNode.getCallees().size();
	    final int callersCount = callGraphNode.getCallers().size();

	    final double ratio = (calleesCount == 0) ? Double.MAX_VALUE : callersCount / (double) calleesCount;

	    comparator.put(callGraphNode.getSignature(), ratio);
	}

	return comparator;

    }

    /**
     * Returns a signature comparator which prefers methods that call multiple
     * other methods.
     * <p>
     * Example: A method that calls 5 other methods has the position -5, a
     * method which calls 0 other methods has the position 0.
     *
     * @param callGraphNodes
     * @return
     */
    public static Comparator<Signature> getFanOutComparator(Collection<? extends CallGraphNode> callGraphNodes) {

	final SignatureComparator comparator = new SignatureComparator();

	for (final CallGraphNode callGraphNode : callGraphNodes) {
	    comparator.put(callGraphNode.getSignature(), -callGraphNode.getCallees().size());
	}

	return comparator;
    }

    /**
     * This comparator orders all initializers before non initializers. The
     * initializers are sorted in order of appearance.
     *
     * @param ast
     * @return a signature comparator
     */
    public static Comparator<Signature> getInitializerInvocationComparator(ASTNode ast) {
	final InitializerInvocationComparatorExtractor extractor = new InitializerInvocationComparatorExtractor();
	ast.accept(extractor);
	return extractor.getComparator();
    }

    public static Comparator<Signature> getInvocationOrderComparator(List<? extends CallGraphNode> callGraphNodes) {

	final SignatureComparator comparator = new SignatureComparator();

	final Queue<CallGraphNode> roots = new LinkedList<CallGraphNode>(callGraphNodes);
	final Queue<CallGraphNode> q = new LinkedList<CallGraphNode>();
	final Set<CallGraphNode> visited = new HashSet<CallGraphNode>();

	CallGraphNode callGraphNode;
	while (!roots.isEmpty()) {
	    callGraphNode = roots.remove();
	    int counter = 0;
	    if (visited.contains(callGraphNode)) {
		continue;
	    }
	    q.offer(callGraphNode);
	    while (!q.isEmpty()) {
		callGraphNode = q.remove();
		if (visited.contains(callGraphNode)) {
		    continue;
		}
		comparator.put(callGraphNode.getSignature(), counter++);
		visited.add(callGraphNode);
		q.addAll(callGraphNode.getCallees());
	    }
	}
	return comparator;
    }

    public static Comparator<Signature> getLeafSeparationComparator(Collection<CallGraphNode> callGraph) {

	final SignatureComparator comparator = new SignatureComparator();

	for (final CallGraphNode callGraphNode : callGraph) {
	    if (callGraphNode.getCallees().size() == 0)
		comparator.put(callGraphNode.getSignature(), 1);
	    else
		comparator.put(callGraphNode.getSignature(), 0);
	}

	return comparator;
    }

    public static Comparator<Signature> getLexicalComparator() {

	return new Comparator<Signature>() {

	    @Override
	    public int compare(Signature signature1, Signature signature2) {
		return signature1.compareTo(signature2);
	    }
	};
    }

    public static Comparator<Signature> getNullComparator() {

	return new Comparator<Signature>() {

	    @Override
	    public int compare(Signature signature1, Signature signature2) {
		return 0;
	    }
	};
    }

    public static Comparator<Signature> getRandomComparator(Collection<CallGraphNode> callGraph) {

	final Random random = new Random();
	final SignatureComparator comparator = new SignatureComparator();

	for (final CallGraphNode node : callGraph)
	    comparator.put(node.getSignature(), random.nextInt());

	return comparator;
    }

    public static Comparator<Signature> getReachabilityComparator(Collection<? extends CallGraphNode> callGraph) {

	return new ReachabilityComparator(callGraph);
    }

    public static Comparator<Signature> getRootSeparationComparator(Collection<? extends CallGraphNode> callGraph) {

	final SignatureComparator comparator = new SignatureComparator();

	for (final CallGraphNode callGraphNode : callGraph) {
	    if (callGraphNode.getCallers().size() == 0)
		comparator.put(callGraphNode.getSignature(), -1);
	    else
		comparator.put(callGraphNode.getSignature(), 0);
	}

	return comparator;
    }

    public static Comparator<Signature> getSourcePositionComparator(ASTNode ast) {
	final SourcePositionComparatorExtractor sourcePositionExtractor = new SourcePositionComparatorExtractor();
	ast.accept(sourcePositionExtractor);
	return sourcePositionExtractor.getSourcePositionComparator();
    }

    public static Comparator<Signature> getVisibilityComparator(Collection<CallGraphNode> callGraph) {

	return new ReachabilityComparator(callGraph);
    }

}
