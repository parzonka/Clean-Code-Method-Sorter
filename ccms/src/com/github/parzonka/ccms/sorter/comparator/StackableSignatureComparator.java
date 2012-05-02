package com.github.parzonka.ccms.sorter.comparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;

/**
 * A comparison of strings is resolved by traversing through different layers of
 * comparators. In each layer the StackableComparator tries to resolve to a
 * ordering. If the strings are considered to be in no ordering relation, the
 * next layer is visited ("tie-breaking").
 *
 * @author Mateusz Parzonka
 *
 */
public class StackableSignatureComparator implements Comparator<Signature> {

    final private static Logger logger = LoggerFactory.getLogger(StackableSignatureComparator.class);

    private final List<Comparator<Signature>> comparators;
    private final Set<Signature> knownSignatures;

    public StackableSignatureComparator(Set<Signature> knownSignatures) {
	super();
	this.knownSignatures = knownSignatures;
	this.comparators = new ArrayList<Comparator<Signature>>();
    }

    /**
     * @param callGraph
     *            All signatures in this callGraph will be known to the
     *            Comparator. when a signature is not known it is considered
     *            unordered to any other signature, the comparator will return
     *            0.
     */
    public StackableSignatureComparator(Collection<CallGraphNode> callGraph) {
	super();
	this.knownSignatures = new HashSet<Signature>();
	for (final CallGraphNode node : callGraph)
	    this.knownSignatures.add(node.getSignature());
	this.comparators = new ArrayList<Comparator<Signature>>();
    }

    public StackableSignatureComparator(Set<Signature> knownStrings, Comparator<Signature>... comparators) {
	super();
	this.knownSignatures = knownStrings;
	this.comparators = new ArrayList<Comparator<Signature>>();
	for (final Comparator<Signature> comparator : comparators)
	    this.comparators.add(comparator);
    }

    @Override
    public int compare(Signature signature1, Signature signature2) {
	if (this.knownSignatures.contains(signature1) && this.knownSignatures.contains(signature2)) {
	    final Iterator<Comparator<Signature>> compIter = this.comparators.iterator();
	    final StringBuilder sb = new StringBuilder();
	    while (compIter.hasNext()) {
		final int compResult = compIter.next().compare(signature1, signature2);
		sb.append(compResult).append(", ");
		if (compResult != 0) {
		    return compResult;
		}
	    }
	    logger.warn("No ordering between [{}] and [{}]! CompResults: " + sb.toString(), signature1, signature2);
	    logger.warn("We have {} comparators in this stackable comparator.", this.comparators.size());
	    return 0;
	}
	return 0;
    }

    /**
     * Adds a string comparator as lowest layer in the comparator stack.
     *
     * @param comparator
     */
    public void add(Comparator<Signature> comparator) {
	logger.debug("Adding comparator #{}", this.comparators.size());
	this.comparators.add(comparator);
    }

    public void add(int index, Comparator<Signature> comparator) {
	this.comparators.add(index, comparator);
    }

}
