package com.github.parzonka.ccms.sorter.invocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

public class NodeOrderingInterleaved implements NodeOrdering {

    final private static Logger logger = LoggerFactory.getLogger(NodeOrderingInterleaved.class);

    private final List<CallGraphNode> orderedNodes;

    public NodeOrderingInterleaved() {
	super();
	this.orderedNodes = new LinkedList<CallGraphNode>();
    }

    /**
     * Inserts the given node v before a node w in the list only if w is
     * reachable from v. If not, v is inserted at the end of the list.
     *
     * @param node
     */
    @Override
    public void insert(CallGraphNode node) {

	if (contains(node))
	    return;


	for (int i = 0; i < this.orderedNodes.size(); i++) {
	    final CallGraphNode orderedNode = this.orderedNodes.get(i);
	    final Set<CallGraphNode> reachabilitySet = getReachabilitySet(node);
	    logger.trace("Node [{}] has r-set: {}", node, reachabilitySet);
	    final Set<CallGraphNode> otherReachabilitySet = getReachabilitySet(orderedNode);
	    logger.trace("Node [{}] has r-set: {}", orderedNode, otherReachabilitySet);
	    if (reachabilitySet.contains(orderedNode) && !otherReachabilitySet.contains(node)) {
		this.orderedNodes.add(i, node);
		logger.trace("Inserting [{}] before [{}].", node, orderedNode);
		return;
	    }
	    node.removeCallee(orderedNode);
	}
	this.orderedNodes.add(node);
	logger.trace("Inserting [{}] at the end of the sequence.", node);
    }

    @Override
    public boolean contains(CallGraphNode node) {
	return this.orderedNodes.contains(node);
    }

    /**
     * @param node
     * @return a set containing all nodes that are reachable from the given node
     */
    private static Set<CallGraphNode> getReachabilitySet(CallGraphNode node) {

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
	return reachabilitySet;
    }

    @Override
    public List<CallGraphNode> getList() {
	return this.orderedNodes;
    }

    public class ReachabilitiesWithoutBackEdges {

	final Map<CallGraphNode, Set<CallGraphNode>> node2reachabilitySet;

	public ReachabilitiesWithoutBackEdges() {
	    super();
	    this.node2reachabilitySet = new HashMap<CallGraphNode, Set<CallGraphNode>>();
	}

	public void add(CallGraphNode node, List<CallGraphNode> backNodes) {
	    final Set<CallGraphNode> rbset = getReachabilitySetWithoutBackEdges(node, backNodes);
	    logger.trace("Node [{}] has R_b-set: {}", node, rbset);
	    this.node2reachabilitySet.put(node, rbset);
	}

	public boolean isReachable(CallGraphNode node1, CallGraphNode node2) {

	    if (!this.node2reachabilitySet.containsKey(node1))
		return false;
	    else
		return this.node2reachabilitySet.get(node1).contains(node2);

	}

	/**
	 * @param node
	 * @return a set containing all nodes that are reachable from the given
	 *         node
	 */
	private Set<CallGraphNode> getReachabilitySetWithoutBackEdges(CallGraphNode node, List<CallGraphNode> backNodes) {

	    final Set<CallGraphNode> reachabilitySet = new HashSet<CallGraphNode>();
	    final Stack<CallGraphNode> stack = new Stack<CallGraphNode>();
	    stack.push(node);
	    while (!stack.empty()) {
		final CallGraphNode caller = stack.pop();
		reachabilitySet.add(caller);
		for (final CallGraphNode callee : caller.getCallees())
		    if (!reachabilitySet.contains(callee) && !backNodes.contains(callee)) {
			stack.push(callee);
		    }
	    }
	    return reachabilitySet;
	}

    }
}
