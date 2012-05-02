/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.util.CompilationUnitSorter;

import com.github.parzonka.ccms.sorter.callgraph.ASTUtils;
import com.github.parzonka.ccms.sorter.callgraph.CallGraphExtractor;
import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;
import com.github.parzonka.ccms.sorter.comparator.ComparatorFactory;
import com.github.parzonka.ccms.sorter.comparator.Signature;

/**
 * Sorts methods randomly for evaluation purposes.
 * 
 * @author Mateusz Parzonka
 * 
 */
public class RandomMethodSorter implements IMethodSorter {

    public RandomMethodSorter() {
	super();

    }

    @Override
    public void sort(ICompilationUnit cu) {

	try {
	    CompilationUnitSorter.sort(3, cu, null, createComparator(cu), 0, null);
	} catch (final JavaModelException e) {
	    throw new RuntimeException(e);
	}

    }

    private Comparator<BodyDeclaration> createComparator(ICompilationUnit cu) {

	final ASTNode ast = ASTUtils.getAST(cu);
	final List<CallGraphNode> callGraph = getCallGraph(ast);

	final Comparator<Signature> methodSignatureComparator = ComparatorFactory.getRandomComparator(callGraph);

	return new SignatureComparatorWrapper(methodSignatureComparator, callGraph);
    }

    /**
     * @param ast
     * @return
     */
    private List<CallGraphNode> getCallGraph(ASTNode ast) {
	final CallGraphExtractor extractor = new CallGraphExtractor();
	ast.accept(extractor);
	final List<CallGraphNode> callGraph = new ArrayList<CallGraphNode>(extractor.getCallGraph());
	return callGraph;
    }

    /**
     * Comparator that sorts MethodDeclarations by an ordering associated with
     * their unique key. This ordering is pre-calculated and passed at
     * instantiation. All other declarations are sorted preserving their old
     * relative order.
     */
    public class SignatureComparatorWrapper implements Comparator<BodyDeclaration> {

	private final Comparator<Signature> comparator;
	private final Set<Signature> knownSignatures;

	public SignatureComparatorWrapper(Comparator<Signature> comparator, Collection<CallGraphNode> knownMethods) {
	    super();
	    this.comparator = comparator;

	    this.knownSignatures = new HashSet<Signature>();
	    for (final CallGraphNode node : knownMethods)
		this.knownSignatures.add(node.getSignature());
	}

	@Override
	public int compare(BodyDeclaration bodyDeclaration1, BodyDeclaration bodyDeclaration2) {

	    if (isNonConstructorMethod(bodyDeclaration1) && isNonConstructorMethod(bodyDeclaration2)) {
		final Signature signature1 = new Signature((MethodDeclaration) bodyDeclaration1);
		final Signature signature2 = new Signature((MethodDeclaration) bodyDeclaration2);
		if (this.knownSignatures.contains(signature1) && this.knownSignatures.contains(signature2))
		    return this.comparator.compare(signature1, signature2);
	    }

	    return preserveRelativeOrder(bodyDeclaration1, bodyDeclaration2);
	}

	private boolean isNonConstructorMethod(BodyDeclaration bodyDeclaration) {
	    return bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION
		    && !((MethodDeclaration) bodyDeclaration).isConstructor();
	}

	private int preserveRelativeOrder(BodyDeclaration bodyDeclaration1, BodyDeclaration bodyDeclaration2) {
	    final int value1 = ((Integer) bodyDeclaration1.getProperty(CompilationUnitSorter.RELATIVE_ORDER))
		    .intValue();
	    final int value2 = ((Integer) bodyDeclaration2.getProperty(CompilationUnitSorter.RELATIVE_ORDER))
		    .intValue();
	    return value1 - value2;
	}

    }

}
