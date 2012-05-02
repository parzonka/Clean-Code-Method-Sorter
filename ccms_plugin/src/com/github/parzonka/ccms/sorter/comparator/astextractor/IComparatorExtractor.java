/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.comparator.astextractor;

import java.util.Comparator;

import com.github.parzonka.ccms.sorter.comparator.Signature;

public interface IComparatorExtractor {

    public Comparator<Signature> getComparator();

}
