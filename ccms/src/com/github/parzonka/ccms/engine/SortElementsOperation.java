/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Blewitt - alex_blewitt@yahoo.com https://bugs.eclipse.org/bugs/show_bug.cgi?id=171066
 *******************************************************************************/
package com.github.parzonka.ccms.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.util.CompilationUnitSorter;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelOperation;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.sorter.comparator.BodyDeclarationComparator;
import com.github.parzonka.ccms.sorter.comparator.Signature;

/**
 * Version of org.eclipse.jdt.internal.core.SortElementsOperation including
 * logging.
 */
public class SortElementsOperation extends JavaModelOperation {
    public static final String CONTAINS_MALFORMED_NODES = "malformed"; //$NON-NLS-1$

    final private static Logger logger = LoggerFactory.getLogger(SortElementsOperation.class);

    private final Comparator<BodyDeclaration> comparator;
    private final int[] positions;
    private final int apiLevel;

    /**
     * Constructor for SortElementsOperation.
     *
     * @param level
     *            the AST API level; one of the AST LEVEL constants
     * @param elements
     * @param positions
     * @param comparator
     */
    public SortElementsOperation(int level, IJavaElement[] elements, int[] positions,
	    Comparator<BodyDeclaration> comparator) {
	super(elements);
	this.comparator = comparator;
	this.positions = positions;
	this.apiLevel = level;

	// logger.info(comparator.toString());
    }

    /**
     * Returns the amount of work for the main task of this operation for
     * progress reporting.
     */
    protected int getMainAmountOfWork() {
	return this.elementsToProcess.length;
    }

    boolean checkMalformedNodes(ASTNode node) {
	final Object property = node.getProperty(CONTAINS_MALFORMED_NODES);
	if (property == null)
	    return false;
	return ((Boolean) property).booleanValue();
    }

    protected boolean isMalformed(ASTNode node) {
	return (node.getFlags() & ASTNode.MALFORMED) != 0;
    }

    /**
     * @see org.eclipse.jdt.internal.core.JavaModelOperation#executeOperation()
     */
    @Override
    protected void executeOperation() throws JavaModelException {
	try {
	    beginTask(Messages.operation_sortelements, getMainAmountOfWork());
	    final CompilationUnit copy = (CompilationUnit) this.elementsToProcess[0];
	    final ICompilationUnit unit = copy.getPrimary();
	    final IBuffer buffer = copy.getBuffer();
	    if (buffer == null) {
		return;
	    }
	    final char[] bufferContents = buffer.getCharacters();
	    final String result = processElement(unit, bufferContents);
	    if (!CharOperation.equals(result.toCharArray(), bufferContents)) {
		copy.getBuffer().setContents(result);
	    }
	    worked(1);
	} finally {
	    done();
	}
    }

    /**
     * Calculates the required text edits to sort the <code>unit</code>
     *
     * @param group
     * @return the edit or null if no sorting is required
     */
    public TextEdit calculateEdit(org.eclipse.jdt.core.dom.CompilationUnit unit, TextEditGroup group)
	    throws JavaModelException {
	if (this.elementsToProcess.length != 1)
	    throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS));

	if (!(this.elementsToProcess[0] instanceof ICompilationUnit))
	    throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES,
		    this.elementsToProcess[0]));

	try {
	    beginTask(Messages.operation_sortelements, getMainAmountOfWork());

	    final ICompilationUnit cu = (ICompilationUnit) this.elementsToProcess[0];
	    final String content = cu.getBuffer().getContents();
	    final ASTRewrite rewrite = sortCompilationUnit(unit, group);
	    if (rewrite == null) {
		return null;
	    }

	    final Document document = new Document(content);
	    return rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
	} finally {
	    done();
	}
    }

    /**
     * Method processElement.
     *
     * @param unit
     * @param source
     */
    private String processElement(ICompilationUnit unit, char[] source) {
	final Document document = new Document(new String(source));
	final CompilerOptions options = new CompilerOptions(unit.getJavaProject().getOptions(true));
	final ASTParser parser = ASTParser.newParser(this.apiLevel);
	parser.setCompilerOptions(options.getMap());
	parser.setSource(source);
	parser.setKind(ASTParser.K_COMPILATION_UNIT);
	parser.setResolveBindings(false);
	final org.eclipse.jdt.core.dom.CompilationUnit ast = (org.eclipse.jdt.core.dom.CompilationUnit) parser
		.createAST(null);

	final ASTRewrite rewriter = sortCompilationUnit(ast, null);
	if (rewriter == null)
	    return document.get();

	final TextEdit edits = rewriter.rewriteAST(document, unit.getJavaProject().getOptions(true));

	RangeMarker[] markers = null;
	if (this.positions != null) {
	    markers = new RangeMarker[this.positions.length];
	    for (int i = 0, max = this.positions.length; i < max; i++) {
		markers[i] = new RangeMarker(this.positions[i], 0);
		insert(edits, markers[i]);
	    }
	}
	try {
	    edits.apply(document, TextEdit.UPDATE_REGIONS);
	    if (this.positions != null) {
		for (int i = 0, max = markers.length; i < max; i++) {
		    this.positions[i] = markers[i].getOffset();
		}
	    }
	} catch (final BadLocationException e) {
	    // ignore
	}
	return document.get();
    }

    private ASTRewrite sortCompilationUnit(org.eclipse.jdt.core.dom.CompilationUnit ast, final TextEditGroup group) {
	ast.accept(new ASTVisitor() {
	    @Override
	    public boolean visit(org.eclipse.jdt.core.dom.CompilationUnit compilationUnit) {
		final List<AbstractTypeDeclaration> types = compilationUnit.types();
		for (final Iterator<AbstractTypeDeclaration> iter = types.iterator(); iter.hasNext();) {
		    final AbstractTypeDeclaration typeDeclaration = iter.next();
		    typeDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER,
			    new Integer(typeDeclaration.getStartPosition()));
		    compilationUnit.setProperty(CONTAINS_MALFORMED_NODES, Boolean.valueOf(isMalformed(typeDeclaration)));
		}
		return true;
	    }

	    @Override
	    public boolean visit(AnnotationTypeDeclaration annotationTypeDeclaration) {
		final List bodyDeclarations = annotationTypeDeclaration.bodyDeclarations();
		for (final Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
		    final BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
		    bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER,
			    new Integer(bodyDeclaration.getStartPosition()));
		    annotationTypeDeclaration.setProperty(CONTAINS_MALFORMED_NODES,
			    Boolean.valueOf(isMalformed(bodyDeclaration)));
		}
		return true;
	    }

	    @Override
	    public boolean visit(AnonymousClassDeclaration anonymousClassDeclaration) {
		final List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		for (final Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
		    final BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
		    bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER,
			    new Integer(bodyDeclaration.getStartPosition()));
		    anonymousClassDeclaration.setProperty(CONTAINS_MALFORMED_NODES,
			    Boolean.valueOf(isMalformed(bodyDeclaration)));
		}
		return true;
	    }

	    @Override
	    public boolean visit(TypeDeclaration typeDeclaration) {
		final List bodyDeclarations = typeDeclaration.bodyDeclarations();
		for (final Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
		    final BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
		    bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER,
			    new Integer(bodyDeclaration.getStartPosition()));
		    typeDeclaration.setProperty(CONTAINS_MALFORMED_NODES, Boolean.valueOf(isMalformed(bodyDeclaration)));
		}
		return true;
	    }

	    @Override
	    public boolean visit(EnumDeclaration enumDeclaration) {
		final List bodyDeclarations = enumDeclaration.bodyDeclarations();
		for (final Iterator iter = bodyDeclarations.iterator(); iter.hasNext();) {
		    final BodyDeclaration bodyDeclaration = (BodyDeclaration) iter.next();
		    bodyDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER,
			    new Integer(bodyDeclaration.getStartPosition()));
		    enumDeclaration.setProperty(CONTAINS_MALFORMED_NODES, Boolean.valueOf(isMalformed(bodyDeclaration)));
		}
		final List enumConstants = enumDeclaration.enumConstants();
		for (final Iterator iter = enumConstants.iterator(); iter.hasNext();) {
		    final EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) iter.next();
		    enumConstantDeclaration.setProperty(CompilationUnitSorter.RELATIVE_ORDER, new Integer(
			    enumConstantDeclaration.getStartPosition()));
		    enumDeclaration.setProperty(CONTAINS_MALFORMED_NODES,
			    Boolean.valueOf(isMalformed(enumConstantDeclaration)));
		}
		return true;
	    }
	});

	final ASTRewrite rewriter = ASTRewrite.create(ast.getAST());
	final boolean[] hasChanges = new boolean[] { false };

	ast.accept(new ASTVisitor() {

	    /**
	     * This
	     *
	     * @param elements
	     * @param listRewrite
	     */
	    private void sortElements(List<BodyDeclaration> elements, ListRewrite listRewrite) {
		if (elements.size() == 0)
		    return;

		final List<BodyDeclaration> myCopy = new ArrayList<BodyDeclaration>();
		myCopy.addAll(elements);

		// orderexperiment
		final List<Signature> methods = new ArrayList<Signature>();
		for (final BodyDeclaration bd : myCopy) {
		    if (bd.getNodeType() == ASTNode.METHOD_DECLARATION) {
			methods.add(new Signature((MethodDeclaration) bd));
		    }
		}
		Collections.sort(methods, ((BodyDeclarationComparator) SortElementsOperation.this.comparator)
			.getMethodDeclarationComparator());
		logger.info("Sorting of methods based on appearance:");
		int j = 0;
		for (final Signature sig : methods) {
		    logger.info("Method [{}] : {}", ++j, sig);
		}

		Collections.sort(myCopy, SortElementsOperation.this.comparator);

		logger.info("Final sorting order just before the AST-Rewrite:");
		for(final BodyDeclaration bd : myCopy) {
		    if (bd.getNodeType() == ASTNode.METHOD_DECLARATION) {
			logger.info("{}", new Signature((MethodDeclaration) bd));
		    } else {
			logger.info("{}", bd.toString());
		    }
		}

		for (int i = 0; i < elements.size(); i++) {
		    final BodyDeclaration oldNode = elements.get(i);

		    final BodyDeclaration newNode = myCopy.get(i);

		    if (oldNode != newNode) {
			if (oldNode.getNodeType() == ASTNode.METHOD_DECLARATION
				&& newNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
			    final Signature oldMethodSignature = new Signature((MethodDeclaration) oldNode);
			    final Signature newMethodSignature = new Signature((MethodDeclaration) newNode);
			    logger.trace("Swapping [{}] for [{}]", oldMethodSignature, newMethodSignature);
			} else {
			    logger.trace("Swapping [{}] for [{}]", oldNode.getNodeType(), newNode.getNodeType());
			}
			listRewrite.replace(oldNode, rewriter.createMoveTarget(newNode), group);
			hasChanges[0] = true;
		    }
		}
	    }

	    @Override
	    public boolean visit(org.eclipse.jdt.core.dom.CompilationUnit compilationUnit) {
		if (checkMalformedNodes(compilationUnit)) {
		    logger.warn("Malformed nodes. Aborting sorting of current element.");
		    return true;
		}

		sortElements(compilationUnit.types(), rewriter.getListRewrite(compilationUnit,
			org.eclipse.jdt.core.dom.CompilationUnit.TYPES_PROPERTY));
		return true;
	    }

	    @Override
	    public boolean visit(AnnotationTypeDeclaration annotationTypeDeclaration) {
		if (checkMalformedNodes(annotationTypeDeclaration)) {
		    logger.warn("Malformed nodes. Aborting sorting of current element.");
		    return true;
		}

		sortElements(annotationTypeDeclaration.bodyDeclarations(), rewriter.getListRewrite(
			annotationTypeDeclaration, AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY));
		return true;
	    }

	    @Override
	    public boolean visit(AnonymousClassDeclaration anonymousClassDeclaration) {
		if (checkMalformedNodes(anonymousClassDeclaration)) {
		    logger.warn("Malformed nodes. Aborting sorting of current element.");
		    return true;
		}

		sortElements(anonymousClassDeclaration.bodyDeclarations(), rewriter.getListRewrite(
			anonymousClassDeclaration, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY));
		return true;
	    }

	    @Override
	    public boolean visit(TypeDeclaration typeDeclaration) {
		if (checkMalformedNodes(typeDeclaration)) {
		    logger.warn("Malformed nodes. Aborting sorting of current element.");
		    return true;
		}

		sortElements(typeDeclaration.bodyDeclarations(),
			rewriter.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY));
		return true;
	    }

	    @Override
	    public boolean visit(EnumDeclaration enumDeclaration) {
		if (checkMalformedNodes(enumDeclaration)) {
		    return true; // abort sorting of current element
		}

		sortElements(enumDeclaration.bodyDeclarations(),
			rewriter.getListRewrite(enumDeclaration, EnumDeclaration.BODY_DECLARATIONS_PROPERTY));
		sortElements(enumDeclaration.enumConstants(),
			rewriter.getListRewrite(enumDeclaration, EnumDeclaration.ENUM_CONSTANTS_PROPERTY));
		return true;
	    }
	});

	if (!hasChanges[0])
	    return null;

	return rewriter;
    }

    /**
     * Possible failures:
     * <ul>
     * <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the
     * operation is <code>null</code></li>.
     * <li>INVALID_ELEMENT_TYPES - the supplied elements are not an instance of
     * IWorkingCopy</li>.
     * </ul>
     *
     * @return IJavaModelStatus
     */
    @Override
    public IJavaModelStatus verify() {
	if (this.elementsToProcess.length != 1) {
	    return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	if (this.elementsToProcess[0] == null) {
	    return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
	}
	if (!(this.elementsToProcess[0] instanceof ICompilationUnit)
		|| !((ICompilationUnit) this.elementsToProcess[0]).isWorkingCopy()) {
	    return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this.elementsToProcess[0]);
	}
	return JavaModelStatus.VERIFIED_OK;
    }

    public static void insert(TextEdit parent, TextEdit edit) {
	if (!parent.hasChildren()) {
	    parent.addChild(edit);
	    return;
	}
	final TextEdit[] children = parent.getChildren();
	// First dive down to find the right parent.
	for (int i = 0; i < children.length; i++) {
	    final TextEdit child = children[i];
	    if (covers(child, edit)) {
		insert(child, edit);
		return;
	    }
	}
	// We have the right parent. Now check if some of the children have to
	// be moved under the new edit since it is covering it.
	for (int i = children.length - 1; i >= 0; i--) {
	    final TextEdit child = children[i];
	    if (covers(edit, child)) {
		parent.removeChild(i);
		edit.addChild(child);
	    }
	}
	parent.addChild(edit);
    }

    private static boolean covers(TextEdit thisEdit, TextEdit otherEdit) {
	if (thisEdit.getLength() == 0) {
	    return false;
	}

	final int thisOffset = thisEdit.getOffset();
	final int thisEnd = thisEdit.getExclusiveEnd();
	if (otherEdit.getLength() == 0) {
	    final int otherOffset = otherEdit.getOffset();
	    return thisOffset <= otherOffset && otherOffset < thisEnd;
	} else {
	    final int otherOffset = otherEdit.getOffset();
	    final int otherEnd = otherEdit.getExclusiveEnd();
	    return thisOffset <= otherOffset && otherEnd <= thisEnd;
	}
    }
}