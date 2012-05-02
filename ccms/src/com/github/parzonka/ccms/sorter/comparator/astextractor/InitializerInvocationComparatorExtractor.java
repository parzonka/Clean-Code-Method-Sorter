package com.github.parzonka.ccms.sorter.comparator.astextractor;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.Initializer;

import com.github.parzonka.ccms.sorter.callgraph.TopLevelASTVisitor;
import com.github.parzonka.ccms.sorter.comparator.Signature;
import com.github.parzonka.ccms.sorter.comparator.SignatureComparator;

/**
 * This comparator orders all initializers before non initializers. The
 * initializers are sorted in order of appearance.
 *
 * @author Mateusz Parzonka
 *
 */
public class InitializerInvocationComparatorExtractor extends TopLevelASTVisitor implements IComparatorExtractor {

    private final SignatureComparator comparator;
    private int initializerCount = 0;

    public InitializerInvocationComparatorExtractor() {
	super();
	this.comparator = new SignatureComparator();
    }

    @Override
    public boolean visit(Initializer node) {
	this.comparator.put(new Signature(this.initializerCount), this.initializerCount);
	this.initializerCount++;
	return false;
    }

    @Override
    public Comparator<Signature> getComparator() {
	return this.comparator;
    }

}
