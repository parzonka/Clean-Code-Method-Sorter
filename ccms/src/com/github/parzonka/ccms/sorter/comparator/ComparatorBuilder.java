package com.github.parzonka.ccms.sorter.comparator;

import static com.github.parzonka.ccms.preferences.PreferenceConstants.*;
import static com.github.parzonka.ccms.sorter.comparator.ComparatorFactory.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.preferences.IPreferences;
import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;
import com.github.parzonka.ccms.sorter.invocation.NodeOrdering;
import com.github.parzonka.ccms.sorter.invocation.NodeOrderingInterleaved;
import com.github.parzonka.ccms.sorter.invocation.NodeOrderingSimple;

/**
 * Encapsulates the mapping of {@link IPreferences} to the comparators produced
 * by the {@link ComparatorFactory}.
 *
 * @author Mateusz Parzonka
 *
 */
public class ComparatorBuilder {

    final private static Logger logger = LoggerFactory.getLogger(ComparatorBuilder.class);

    private final List<? extends CallGraphNode> callGraph;
    private final ASTNode ast;
    private final Set<Signature> knownSignatures;
    private final IPreferences preferences;

    public ComparatorBuilder(List<? extends CallGraphNode> callGraph, ASTNode ast, IPreferences preferences) {
	super();
	this.callGraph = callGraph;
	this.ast = ast;
	this.preferences = preferences;
	this.knownSignatures = new HashSet<Signature>();
	for (final CallGraphNode node : callGraph)
	    this.knownSignatures.add(node.getSignature());
	logger.trace("Created {}", this.getClass().getName());
    }

    public Comparator<Signature> getMethodOrderingComparator() {
	final StackableSignatureComparator comparator = new StackableSignatureComparator(this.knownSignatures);

	logger.debug("Start-points: {}", logCallGraph(this.callGraph));

	for (final String property : this.preferences.getMethodOrderingPreferences()) {
	    logger.debug("Adding comparator for [{}] to stackable comparator", property);
	    comparator.add(getComparator(property));
	}
	return comparator;
    }

    private static String logCallGraph(List<? extends CallGraphNode> aCallGraph) {
	final String LF = System.getProperty("line.separator");
	final StringBuilder sb = new StringBuilder();
	for (final CallGraphNode callGraphNode : aCallGraph) {
	    sb.append(callGraphNode.toString()).append(LF);
	}
	return sb.toString();
    }

    private Comparator<Signature> getComparator(String property) {

	if (property.equals(PRIORITY_INVOCATION_ORDER)) {

	    final NodeOrdering nodeOrdering = getNodeOrdering(this.preferences.isBeforeAfterRelation());
	    final boolean traversalStrategy = this.preferences.isInvocationStrategyDepthFirst();
	    return getInvocationComparator(nodeOrdering, traversalStrategy, this.callGraph);
	}

	else if (property.equals(PRIORITY_ACCESS_LEVEL))
	    return getAccessLevelComparator(this.ast);

	else if (property.equals(PRIORITY_CONSTRUCTOR))
	    return getConstructorComparator(this.ast);

	else if (property.equals(PRIORITY_FAN_OUT))
	    return getFanOutComparator(this.callGraph);

	else if (property.equals(PRIORITY_INITIALIZER_INVOCATION))
	    return getInitializerInvocationComparator(this.ast);

	else if (property.equals(PRIORITY_LEXICALITY))
	    return getLexicalComparator();

	else if (property.equals(PRIORITY_ROOTS))
	    return getRootSeparationComparator(this.callGraph);

	else if (property.equals(PRIORITY_SOURCE_POSITION))
	    return getSourcePositionComparator(this.ast);

	else
	    throw new IllegalArgumentException(property);
    }

    private NodeOrdering getNodeOrdering(boolean beforeAfterRelation) {
	if (beforeAfterRelation)
	    return new NodeOrderingInterleaved();
	return new NodeOrderingSimple();
    }

}