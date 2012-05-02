package com.github.parzonka.ccms.sorter.comparator.astextractor;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.github.parzonka.ccms.sorter.callgraph.TopLevelASTVisitor;
import com.github.parzonka.ccms.sorter.comparator.Signature;
import com.github.parzonka.ccms.sorter.comparator.SignatureComparator;

/**
 * This visitor extracts a comparator which orders constructors before other
 * methods.
 *
 * @author Mateusz Parzonka
 *
 */
public class ConstructorComparatorExtractor extends TopLevelASTVisitor
	implements IComparatorExtractor {

    private final SignatureComparator comparator;

    public ConstructorComparatorExtractor() {
	super();
	this.comparator = new SignatureComparator();
    }

    @Override
    public Comparator<Signature> getComparator() {
	return this.comparator;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
	if (node.isConstructor())
	    this.comparator.put(new Signature(node), 0);
	return false;
    }

}
