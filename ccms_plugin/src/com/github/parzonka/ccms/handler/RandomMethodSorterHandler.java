package com.github.parzonka.ccms.handler;

import com.github.parzonka.ccms.sorter.IMethodSorter;
import com.github.parzonka.ccms.sorter.RandomMethodSorter;

/**
 * Shuffles methods randomly. TODO to be deleted in a final version.
 *
 * @author Mateusz Parzonka
 *
 */
public class RandomMethodSorterHandler extends BatchProcessingHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipselabs.recommenders.cleancode.methodsorter.handler.
     * BatchProcessingHandler#getMethodSorter()
     */
    @Override
    protected IMethodSorter getMethodSorter() {
	return new RandomMethodSorter();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipselabs.recommenders.cleancode.methodsorter.handler.
     * BatchProcessingHandler#getMessage()
     */
    @Override
    protected String getMessage() {
	return "Methods shuffled randomly in " + sortedClassesCount
		+ " classes. \n";
    }

}
