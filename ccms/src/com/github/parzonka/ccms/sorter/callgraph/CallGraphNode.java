package com.github.parzonka.ccms.sorter.callgraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.github.parzonka.ccms.sorter.comparator.Signature;

/**
 * Represents a node in a call graph.
 *
 * @author Mateusz Parzonka
 *
 */
public class CallGraphNode implements Comparable<CallGraphNode> {

    private final Signature signature;
    private final List<CallGraphNode> callees;
    private final List<CallGraphNode> callers;

    public CallGraphNode(Signature signature) {
	super();
	this.signature = signature;
	this.callees = new ArrayList<CallGraphNode>();
	this.callers = new ArrayList<CallGraphNode>();
    }

    /**
     * Not to be instantiated by clients, internal use.
     */
    CallGraphNode() {
	super();
	this.signature = new Signature();
	this.callees = null;
	this.callers = null;
    }

    public CallGraphNode(String signature) {
	this(new Signature(signature));
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof CallGraphNode)) {
	    return false;
	}
	final CallGraphNode other = (CallGraphNode) obj;
	if (this.callees == null && other.callees != null) {
	    return false;
	}
	if (this.callees != null && other.callees == null) {
	    return false;
	}
	if (this.callees != null && other.callees != null && !signaturesEqual(this.callees, other.callees)) {
	    return false;
	}
	if (this.signature == null) {
	    if (other.signature != null) {
		return false;
	    }
	} else if (!this.signature.equals(other.signature)) {
	    return false;
	}
	return true;
    }

    private static boolean signaturesEqual(List<CallGraphNode> callees1, List<CallGraphNode> callees2) {
	if (callees1.size() != callees2.size()) {
	    return false;
	} else {
	    for (int i = 0; i < callees1.size(); i++) {
		if (!callees1.get(i).getSignature().equals(callees2.get(i).getSignature())) {
		    return false;
		}
	    }
	}
	return true;
    }

    public List<CallGraphNode> getCallers() {
	return this.callers;
    }

    @Override
    public String toString() {
	return this.signature.toString();
	// final StringBuilder sb = new StringBuilder();
	// sb.append(this.signature);
	// sb.append(" -> [");
	// if (!this.callees.isEmpty()) {
	// final Iterator<CallGraphNode> iter = this.callees.iterator();
	// sb.append(iter.next().signature);
	// while (iter.hasNext()) {
	// sb.append(", ");
	// sb.append(iter.next().signature);
	// }
	// }
	// sb.append("]");
	// return sb.toString();
    }

    public Signature getSignature() {
	return this.signature;
    }

    @Override
    public int compareTo(CallGraphNode o) {
	return this.signature.compareTo(o.signature);
    }

    @Override
    public int hashCode() {
	return this.signature.hashCode();
    }

    public List<CallGraphNode> getCallees() {
	return this.callees;
    }

    /**
     * Callees are stored in the order of their invocation. Successive
     * invocations are ignored, thus callees not added to the list.
     *
     * @param callee
     */
    public void addCallee(CallGraphNode callee) {
	if (!this.callees.contains(callee)) {
	    this.callees.add(callee);
	    callee.callers.add(this);
	}
    }

    public boolean removeCallee(CallGraphNode callee) {
	return this.callees.remove(callee);
    }

    public static class CallGraphNodeComparator implements Comparator<CallGraphNode> {

	private final Comparator<Signature> signatureComparator;

	public CallGraphNodeComparator(Comparator<Signature> signatureComparator) {
	    super();
	    this.signatureComparator = signatureComparator;
	}

	@Override
	public int compare(CallGraphNode node1, CallGraphNode node2) {
	    return this.signatureComparator.compare(node1.getSignature(), node2.getSignature());
	}
    }

}