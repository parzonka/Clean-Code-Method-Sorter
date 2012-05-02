/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.invocation;

import java.util.List;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

public interface NodeOrdering {

    public void insert(CallGraphNode node);

    public List<CallGraphNode> getList();

    public boolean contains(CallGraphNode node);

}
