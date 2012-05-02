/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.github.parzonka.ccms.sorter.CleanCodeMethodSorter;
import com.github.parzonka.ccms.sorter.IMethodSorter;
import com.github.parzonka.ccms.sorter.callgraph.ASTUtils;

/**
 *
 * @author Mateusz Parzonka
 *
 */
public class BatchProcessingHandler extends AbstractHandler {

    private IMethodSorter fMethodSorter;
    protected int sortedClassesCount;
    private ExecutionEvent event;

    public BatchProcessingHandler() {
	super();
	fMethodSorter = getMethodSorter();
    }

    protected IMethodSorter getMethodSorter() {
	return new CleanCodeMethodSorter();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

	sortedClassesCount = 0;
	this.event = event;

	ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
	if (currentSelection instanceof IStructuredSelection) {

	    IStructuredSelection structuredSelection = (IStructuredSelection) HandlerUtil
		    .getCurrentSelection(event);
	    IJavaElement[] elements = getJavaElements(structuredSelection);
	    sort(elements);
	} else {
	    IWorkbenchWindow window = HandlerUtil
		    .getActiveWorkbenchWindowChecked(event);
	    ICompilationUnit cu = ASTUtils.getCompilationUnit(window);
	    sort(cu);
	}

	MessageDialog.openInformation(
		HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
		"Clean Code Method Sorter", getMessage());

	return null;
    }

    /**
     * Converts the given structured selection into an array of Java elements.
     * structured selection is not of type <code>IJavaElement</code> it is not
     * included into the list.
     *
     * @param selection
     *            the selection
     * @return the Java element contained in the selection
     */
    private static IJavaElement[] getJavaElements(IStructuredSelection selection) {

	List<IJavaElement> result = new ArrayList<IJavaElement>();
	Iterator<?> iter = selection.iterator();
	while (iter.hasNext()) {
	    Object element = iter.next();
	    if (element instanceof IJavaElement)
		result.add((IJavaElement) element);
	}
	return result.toArray(new IJavaElement[result.size()]);
    }

    /**
     * @param event
     * @param store
     * @param elements
     * @throws ExecutionException
     */
    private void sort(IJavaElement[] elements) throws ExecutionException {

	for (IJavaElement iJavaElement : elements) {

	    if (iJavaElement instanceof CompilationUnit) {
		sort((CompilationUnit) iJavaElement);

	    } else if (iJavaElement instanceof PackageFragmentRoot) {
		sort((PackageFragmentRoot) iJavaElement);

	    } else if (iJavaElement instanceof PackageFragment) {
		sort((PackageFragment) iJavaElement);

	    } else if (iJavaElement instanceof JavaProject) {
		sort((JavaProject) iJavaElement);

	    } else {
		MessageDialog.openInformation(HandlerUtil
			.getActiveWorkbenchWindow(event).getShell(),
			"Clean Code Method Sorter",
			iJavaElement.getClass().getCanonicalName() + "\n"
				+ iJavaElement.getElementName());
	    }
	}
    }

    private void sort(IPackageFragmentRoot packageFragmentRoot)
	    throws ExecutionException {
	try {
	    sort(packageFragmentRoot.getChildren());
	} catch (JavaModelException e) {
	    throw new ExecutionException(e.getMessage());
	}
    }

    private void sort(PackageFragment packageFragment) throws ExecutionException {
	try {
	    sort(packageFragment.getCompilationUnits());
	} catch (JavaModelException e) {
	    throw new ExecutionException(e.getMessage());
	}
    }

    private void sort(IJavaProject iJavaElement) throws ExecutionException {
	try {
	    for (IPackageFragmentRoot root : iJavaElement
		    .getAllPackageFragmentRoots()) {
		if (root instanceof PackageFragmentRoot)
		    sort(root);
	    }
	} catch (JavaModelException e) {
	    throw new ExecutionException(e.getMessage());
	}
    }

    /**
     * Sorts a single CompilationUnit.
     *
     * @param cu
     * @throws ExecutionException
     */
    private void sort(ICompilationUnit cu) throws ExecutionException {
	try {
	    cu.becomeWorkingCopy(null);
	    fMethodSorter.sort(cu);
	    sortedClassesCount++;
	    cu.commitWorkingCopy(true, null);
	    cu.discardWorkingCopy();
	} catch (JavaModelException e) {
	    throw new ExecutionException(e.getMessage());
	}
    }

    protected String getMessage() {
	StringBuilder sb = new StringBuilder();
	sb.append("Methods sorted in " + sortedClassesCount + " classes.");
	return sb.toString();
    }

}
