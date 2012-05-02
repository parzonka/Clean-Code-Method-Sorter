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
