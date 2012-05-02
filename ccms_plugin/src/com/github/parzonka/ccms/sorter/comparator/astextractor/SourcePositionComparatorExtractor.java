/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.comparator.astextractor;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.github.parzonka.ccms.sorter.callgraph.TopLevelASTVisitor;
import com.github.parzonka.ccms.sorter.comparator.Signature;
import com.github.parzonka.ccms.sorter.comparator.SignatureComparator;

/**
 *
 * @author Mateusz Parzonka
 *
 */
public class SourcePositionComparatorExtractor extends TopLevelASTVisitor {

    private final SignatureComparator comparator;
    private int pos = 0;

    public SourcePositionComparatorExtractor() {
	super();
	this.comparator = new SignatureComparator(true);
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
	final Signature signature = new Signature(methodDeclaration);
	this.comparator.put(signature, this.pos++);
	return false;
    }

    public Comparator<Signature> getSourcePositionComparator() {
	return this.comparator;
    }

}
