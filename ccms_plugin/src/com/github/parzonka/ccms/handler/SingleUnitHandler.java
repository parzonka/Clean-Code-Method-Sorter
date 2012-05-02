/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.github.parzonka.ccms.sorter.CleanCodeMethodSorter;
import com.github.parzonka.ccms.sorter.callgraph.ASTUtils;

/**
 * Handles an event from the active workbench window.
 * 
 * @author Mateusz Parzonka
 *
 */
public class SingleUnitHandler extends AbstractHandler {

    private final CleanCodeMethodSorter sorter;

    public SingleUnitHandler() {
	sorter = new CleanCodeMethodSorter();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
	IWorkbenchWindow window = HandlerUtil
		.getActiveWorkbenchWindowChecked(event);
	ICompilationUnit cu = ASTUtils.getCompilationUnit(window);
	sorter.sort(cu);
	return null;
    }
}
