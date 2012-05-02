package com.github.parzonka.ccms.sorter.comparator.astextractor;

import java.util.Comparator;

import com.github.parzonka.ccms.sorter.comparator.Signature;

public interface IComparatorExtractor {

    public Comparator<Signature> getComparator();

}
