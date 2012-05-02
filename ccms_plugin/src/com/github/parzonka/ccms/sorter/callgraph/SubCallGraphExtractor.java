/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.callgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.github.parzonka.ccms.sorter.comparator.Signature;

/**
 * Extracts a callGraph consisting only of nodes and edges between nodes in the
 * filterNodes-collection.
 *
 * @author Mateusz Parzonka
 *
 */
public class SubCallGraphExtractor extends CallGraphExtractor {

    private final Set<Signature> filterSignatures;

    public SubCallGraphExtractor(Collection<CallGraphNode> filterNodes) {
	super();
	this.filterSignatures = new HashSet<Signature>();
	for (final CallGraphNode callGraphNode : filterNodes)
	    this.filterSignatures.add(callGraphNode.getSignature());
    }

    @Override
    public boolean visit(MethodInvocation methodInvocation) {
	if (this.filterSignatures.contains(new Signature(methodInvocation)))
	    return super.visit(methodInvocation);
	return false;
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
	if (this.filterSignatures.contains(new Signature(methodDeclaration)))
	    return super.visit(methodDeclaration);
	return false;
    }

}
