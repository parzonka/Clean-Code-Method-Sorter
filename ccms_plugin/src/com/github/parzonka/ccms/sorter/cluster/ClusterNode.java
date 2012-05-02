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
import java.util.List;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

/**
 * Models a node aggregating {@link CallGraphNode} which form a subGraph to
 * allow sorting of subgraphs.
 *
 * @author Mateusz Parzonka
 *
 */
public class ClusterNode extends CallGraphNode {

    private final List<CallGraphNode> callees;
    private final List<CallGraphNode> callers;
    private List<CallGraphNode> clusteredNodes;

    public ClusterNode(List<CallGraphNode> callGraphNodes) {
	super(callGraphNodes.get(0).getSignature());
	this.clusteredNodes = callGraphNodes;
	if (callGraphNodes.size() < 1)
	    throw new IllegalArgumentException("A clustered call graph node should comprise at least 1 nodes.");
	this.callers = new ArrayList<CallGraphNode>();
	this.callees = new ArrayList<CallGraphNode>();
	mergeCallersAndCallees(callGraphNodes);
    }

    /**
     * @param callGraphNodes
     *            the callGraphNodes that are clustered in this ClusterNode.
     */
    public void setClusteredNodes(List<CallGraphNode> callGraphNodes) {
	this.clusteredNodes = callGraphNodes;
	mergeCallersAndCallees(callGraphNodes);
    }

    public List<CallGraphNode> getClusteredNodes() {
	return this.clusteredNodes;
    }

    private void mergeCallersAndCallees(List<CallGraphNode> callGraphNodes) {
	for (final CallGraphNode callGraphNode : callGraphNodes) {
	    for (final CallGraphNode caller : callGraphNode.getCallers())
		if (!this.callers.contains(caller))
		    this.callers.add(caller);
	    for (final CallGraphNode callee : callGraphNode.getCallees())
		if (!this.callees.contains(callee))
		    this.callees.add(callee);
	}

    }

    @Override
    public List<CallGraphNode> getCallees() {
	return this.callees;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (!(obj instanceof ClusterNode))
	    return false;
	final ClusterNode other = (ClusterNode) obj;
	if (this.clusteredNodes == null) {
	    if (other.clusteredNodes != null)
		return false;
	} else if (!this.clusteredNodes.equals(other.clusteredNodes))
	    return false;
	return true;
    }

    @Override
    public void addCallee(CallGraphNode callee) {
	throw new RuntimeException("Operation not supported!");
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((this.clusteredNodes == null) ? 0 : this.clusteredNodes.hashCode());
	return result;
    }

    @Override
    public List<CallGraphNode> getCallers() {
	return this.callers;
    }

    @Override
    public boolean removeCallee(CallGraphNode callee) {
	return this.callees.remove(callee);
    }

}
