/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 *
 * @author Mateusz Parzonka
 * 
 */
public interface IMethodSorter {

    /**
     * Sorts the methods in a compilation unit.
     *
     * @param cu
     */
    public void sort(ICompilationUnit cu);

}
