package com.github.parzonka.ccms.sorter.comparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

/**
 * Comparator intended to work on method signatures, based on a acyclic
 * reachability graph. When method b is reachable from method a, then method a
 * is sorted before method b. When method a is reachable from b and vice-versa,
 * the comparator returns 0. When a is not reachable from b and vice-versa, the
 * comparator returns 0 as well.
 *
 * @author Mateusz Parzonka
 *
 */
public class ReachabilityComparator implements Comparator<Signature> {

    final private static Logger logger = LoggerFactory.getLogger(ReachabilityComparator.class);

    private final Map<CallGraphNode, Set<CallGraphNode>> reachabilitySets;
    private final Map<Signature, Set<Signature>> reachabilitySignatureSets;

    public ReachabilityComparator(Collection<? extends CallGraphNode> callGraph) {

	this.reachabilitySets = new HashMap<CallGraphNode, Set<CallGraphNode>>();
	this.reachabilitySignatureSets = new HashMap<Signature, Set<Signature>>();

	for (final CallGraphNode caller : callGraph) {
	    createReachabilitySet(caller);
	}
	createReachabilitySignatureSets(callGraph);
    }

    private void createReachabilitySet(CallGraphNode node) {

	final Set<CallGraphNode> reachabilitySet = new HashSet<CallGraphNode>();
	final Stack<CallGraphNode> stack = new Stack<CallGraphNode>();
	stack.push(node);
	while (!stack.empty()) {
	    final CallGraphNode caller = stack.pop();
	    reachabilitySet.add(caller);
	    for (final CallGraphNode callee : caller.getCallees())
		if (!reachabilitySet.contains(callee)) {
		    stack.push(callee);
		}
	}
	this.reachabilitySets.put(node, reachabilitySet);
    }

    /**
     * Creates reachabilitySignatureSets based on reachabilitySets, needed for
     * comparison based on strings.
     */
    private void createReachabilitySignatureSets(Collection<? extends CallGraphNode> callGraph) {
	for (final CallGraphNode node : callGraph) {
	    final Set<Signature> reachabilitySignatureSet = new HashSet<Signature>();
	    for (final CallGraphNode reachableNode : this.reachabilitySets.get(node))
		reachabilitySignatureSet.add(reachableNode.getSignature());
	    this.reachabilitySignatureSets.put(node.getSignature(), reachabilitySignatureSet);
	}
    }

    @Override
    public int compare(Signature signature1, Signature signature2) {

	int compare;

	final Set<Signature> reachabilitySignatureSet1 = this.reachabilitySignatureSets.get(signature1);
	if (reachabilitySignatureSet1 == null)
	    throw new IllegalStateException("ReachabilitySet not existent for method: " + signature1);
	final boolean IsReachable1to2 = reachabilitySignatureSet1.contains(signature2);

	final Set<Signature> reachabilitySignatureSet2 = this.reachabilitySignatureSets.get(signature2);
	if (reachabilitySignatureSet2 == null)
	    throw new IllegalStateException("ReachabilitySet not existent for method: " + signature2);
	final boolean IsReachable2to1 = reachabilitySignatureSet2.contains(signature1);

	if (IsReachable1to2 && IsReachable2to1) {
	    compare = 0;
	} else if (IsReachable1to2) {
	    compare = -1;
	} else if (IsReachable2to1) {
	    compare = 1;
	} else {
	    compare = 0;
    	}
	logger.trace("ReachabilityOrdering [{}] : [{}] = " + compare, signature1, signature2);
        return compare;

    }

}
