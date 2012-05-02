package com.github.parzonka.ccms.sorter.comparator.astextractor;

import java.util.Comparator;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import com.github.parzonka.ccms.sorter.callgraph.TopLevelASTVisitor;
import com.github.parzonka.ccms.sorter.comparator.Signature;
import com.github.parzonka.ccms.sorter.comparator.SignatureComparator;

/**
 * Returns a signature comparator for sorting by access modifier. <br>
 * Ordering: <code>public, protected, default, private</code>.
 *
 * @author Mateusz Parzonka
 *
 */
public class AccessLevelComparatorExtractor extends TopLevelASTVisitor
	implements IComparatorExtractor {

    private final SignatureComparator comparator;

    public AccessLevelComparatorExtractor() {
	super();
	comparator = new SignatureComparator(true);
    }

    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
	final Signature signature = new Signature(methodDeclaration);
	comparator.put(signature, getAccessLevel(methodDeclaration));
	return false;
    }

    private static int getAccessLevel(MethodDeclaration method) {
	int modifiers = method.getModifiers();
	if (Modifier.isPublic(modifiers))
	    return 0;
	if (Modifier.isProtected(modifiers))
	    return 1;
	if (Modifier.isPrivate(modifiers))
	    return 3;
	else
	    return 2;
    }

    @Override
    public Comparator<Signature> getComparator() {
	return comparator;
    }

}
