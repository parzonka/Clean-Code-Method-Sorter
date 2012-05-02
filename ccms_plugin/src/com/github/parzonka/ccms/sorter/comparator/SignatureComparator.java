/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.sorter.comparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SignatureComparator implements Comparator<Signature> {

    private final Map<Signature, Double> sig2order;
    private final boolean defaultSortPositionFlag;

    /**
     * Default instances sort unknown signatures AFTER all known signatures.
     */
    public SignatureComparator() {
	this(false);
    }

    /**
     * The given flag value has effect when a signature is not known to the
     * comparator.
     *
     * @param defaultSortPositionFlag
     *            when true, not found signatures are sorted BEFORE the known
     *            signatures. When set to false, not found signatures are sorted
     *            AFTER known signatures.
     *
     */
    public SignatureComparator(boolean defaultSortPositionFlag) {
	super();
	this.sig2order = new HashMap<Signature, Double>();
	this.defaultSortPositionFlag = defaultSortPositionFlag;
    }

    public SignatureComparator(Map<Signature, Integer> signature2position) {
	super();
	this.sig2order = new HashMap<Signature, Double>();
	for (final Signature key : signature2position.keySet())
	    this.sig2order.put(key, new Double(signature2position.get(key)));
	this.defaultSortPositionFlag = false;
    }

    public void put(String signature, int order) {
	this.sig2order.put(new Signature(signature), new Double(order));
    }

    public void put(String signature, double order) {
	this.sig2order.put(new Signature(signature), order);
    }

    public void put(Signature signature, double order) {
	this.sig2order.put(signature, order);
    }

    public void put(Signature signature, int order) {
	this.sig2order.put(signature, new Double(order));
    }

    @Override
    public int compare(Signature signature1, Signature signature2) {
	final Double value1 = this.sig2order.get(signature1);
	final Double value2 = this.sig2order.get(signature2);

	if (value1 == null && value2 == null)
	    return 0;
	else if (value1 == null)
	    return this.defaultSortPositionFlag ? -1 : 1;
	else if (value2 == null)
	    return this.defaultSortPositionFlag ? 1 : -1;
	else {
	    if (value1 - value2 < 0D)
		return -1;
	    else if (value1 - value2 > 0D)
		return 1;
	    else
		return 0;
	}
    }

}
