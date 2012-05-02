package com.github.parzonka.ccms.sorter.invocation;

import java.util.List;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

public interface NodeOrdering {

    public void insert(CallGraphNode node);

    public List<CallGraphNode> getList();

    public boolean contains(CallGraphNode node);

}
