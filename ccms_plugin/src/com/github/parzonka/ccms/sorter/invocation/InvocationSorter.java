package com.github.parzonka.ccms.sorter.invocation;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

public class InvocationSorter {

    private final NodeOrdering nodeOrdering;

    public InvocationSorter(NodeOrdering nodeOrdering) {
	this.nodeOrdering = nodeOrdering;
    }

    /**
     * Traverses children breadth first.
     *
     * @param nodes
     * @return
     */
    public List<CallGraphNode> getOrderedListBreadthFirst(List<? extends CallGraphNode> nodes) {

	final Queue<CallGraphNode> queue = new LinkedList<CallGraphNode>();

	for (CallGraphNode node : nodes) {
	    if (this.nodeOrdering.contains(node)) {
		continue;
	    }
	    queue.offer(node);
	    while (!queue.isEmpty()) {
		node = queue.remove();
		if (this.nodeOrdering.contains(node)) {
		    continue;
		}
		queue.addAll(node.getCallees());
		this.nodeOrdering.insert(node);
	    }
	}
	return this.nodeOrdering.getList();
    }

    /**
     * Traverses children depth first.
     * 
     * @param nodes
     * @return
     */
    public List<CallGraphNode> getOrderedListDepthFirst(List<? extends CallGraphNode> nodes) {

	final Stack<CallGraphNode> stack = new Stack<CallGraphNode>();

	for (CallGraphNode node : nodes) {
	    if (this.nodeOrdering.contains(node)) {
		continue;
	    }
	    stack.push(node);
	    while (!stack.isEmpty()) {
		node = stack.pop();
		if (this.nodeOrdering.contains(node)) {
		    continue;
		}
		pushCalleesOnStackInReverseOrder(node.getCallees(), stack);
		this.nodeOrdering.insert(node);
	    }
	}
	return this.nodeOrdering.getList();
    }

    private static void pushCalleesOnStackInReverseOrder(final List<CallGraphNode> callees, final Stack<CallGraphNode> stack) {
	for (int i = callees.size() - 1; i >= 0; i--) {
	    stack.push(callees.get(i));
	}
    }

}
