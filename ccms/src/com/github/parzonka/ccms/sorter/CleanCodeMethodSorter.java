package com.github.parzonka.ccms.sorter;

import static com.github.parzonka.ccms.sorter.comparator.ComparatorFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.parzonka.ccms.engine.CompilationUnitSorter;
import com.github.parzonka.ccms.preferences.IPreferences;
import com.github.parzonka.ccms.preferences.PreferenceManager;
import com.github.parzonka.ccms.sorter.callgraph.ASTUtils;
import com.github.parzonka.ccms.sorter.callgraph.CallGraphExtractor;
import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;
import com.github.parzonka.ccms.sorter.callgraph.SubCallGraphExtractor;
import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode.CallGraphNodeComparator;
import com.github.parzonka.ccms.sorter.cluster.ClusterComparator;
import com.github.parzonka.ccms.sorter.cluster.ClusterGraphExtractor;
import com.github.parzonka.ccms.sorter.cluster.ClusterNode;
import com.github.parzonka.ccms.sorter.comparator.BodyDeclarationComparator;
import com.github.parzonka.ccms.sorter.comparator.ComparatorBuilder;
import com.github.parzonka.ccms.sorter.comparator.Signature;
import com.github.parzonka.ccms.sorter.comparator.StackableSignatureComparator;

/**
 * Implements the logic needed to orchestrate all calculations needed to find
 * the right ordering of methods. Consumes an {@link ICompilationUnit} which is
 * to be sorted and invokes the sorting operation in the JDT-API.
 *
 * @author Mateusz Parzonka
 *
 */
public class CleanCodeMethodSorter implements IMethodSorter {

    final private static Logger logger = LoggerFactory.getLogger(CleanCodeMethodSorter.class);

    private final IPreferences preferences;
    private ASTNode ast;
    private List<CallGraphNode> callGraph;
    private Set<Signature> knownSignatures;

    /**
     * @param preferences
     *            provides preferences used in this class instance.
     */
    public CleanCodeMethodSorter(IPreferences preferences) {
	super();
	this.preferences = preferences;
    }

    /**
     * Instantiation uses the User-configurable preferences stored in the
     * Eclipse {@link PreferenceStore}.
     */
    public CleanCodeMethodSorter() {
	super();
	this.preferences = new PreferenceManager();
    }

    @Override
    public void sort(ICompilationUnit cu) {
	this.ast = ASTUtils.getAST(cu);
	this.callGraph = createCallGraph();
	this.knownSignatures = getKnownSignatures();

	logger.debug("Starting CleanCodeMethodSorter");

	if (this.preferences.applyWorkingListHeuristics()) {
	    applyWorkingListHeuristics();
	} else {
	    useSourcePositionOrder();
	}

	final Comparator<Signature> signatureComparator = getSignatureComparator();

	final Comparator<BodyDeclaration> comparator = getBodyDeclarationComparator(signatureComparator);
	try {
	    CompilationUnitSorter.sort(AST.JLS3, cu, null, comparator, 0, null);
	} catch (final JavaModelException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Creates a callGraph by traversing the current AST
     *
     * @param ast
     * @return the callGraph
     */
    private List<CallGraphNode> createCallGraph() {
	final CallGraphExtractor extractor = new CallGraphExtractor();
	this.ast.accept(extractor);
	final List<CallGraphNode> callGraph = new ArrayList<CallGraphNode>(extractor.getCallGraph());
	return callGraph;
    }

    /**
     * Sort the given callGraph using the current preferences.
     *
     * @param subGraph
     * @param ast
     *            AST which was used when creating this graph or its superGraph
     */
    public void sort(List<CallGraphNode> subGraph, ASTNode ast) {
	this.ast = ast;
	this.callGraph = subGraph;
	this.knownSignatures = getKnownSignatures();

	if (this.preferences.applyWorkingListHeuristics()) {
	    applyWorkingListHeuristics();
	} else {
	    useSourcePositionOrder();
	}

	final Comparator<CallGraphNode> comparator = new CallGraphNodeComparator(getSignatureComparator());
	Collections.sort(subGraph, comparator);
    }

    private Set<Signature> getKnownSignatures() {
	final HashSet<Signature> knownSignatures = new HashSet<Signature>();
	for (final CallGraphNode node : this.callGraph) {
	    knownSignatures.add(node.getSignature());
	}
	return knownSignatures;
    }

    /**
     * Sorts the callGraph (aka a list of {@link CallGraphNode} aka
     * "WorkingList") following some empirically unverified heuristics. The
     * working list provides an list of start-points for the invocation order.
     */
    private void applyWorkingListHeuristics() {

	final StackableSignatureComparator comp = new StackableSignatureComparator(this.knownSignatures);
	comp.add(getInitializerInvocationComparator(this.ast));
	comp.add(getConstructorComparator(this.ast));
	// comp.add(getLeafSeparationComparator(callGraph));
	comp.add(getRootSeparationComparator(this.callGraph));
	comp.add(getAccessLevelComparator(this.ast));
	comp.add(getFanOutComparator(this.callGraph));
	comp.add(getSourcePositionComparator(this.ast));
	Collections.sort(this.callGraph, new CallGraphNodeComparator(comp));
    }

    /**
     * Sorts the callGraph (aka a list of {@link CallGraphNode} aka
     * "WorkingList") following the order found in the source, hoping the
     * developer had some clues about sorting his/her methods already. The
     * working list provides an list of start-points for the invocation order.
     */
    private void useSourcePositionOrder() {
	Collections.sort(this.callGraph, new CallGraphNodeComparator(getSourcePositionComparator(this.ast)));
    }

    /**
     * @return a signatureComparator representing the sorting information needed
     *         to sort the methods in the CU *except* clustering of methods.
     */
    private Comparator<Signature> getSignatureComparator() {

	final ComparatorBuilder builder = new ComparatorBuilder(this.callGraph, this.ast, this.preferences);
	return builder.getMethodOrderingComparator();

    }

    /**
     * Creates the comparator which is passed to the Eclipse sorting API.
     *
     * @param signatureComparator
     *            represents the ordering which is derived from invovation
     *            ordering (and reachability). May be wrapped into a
     *            {@link ClusterComparator} when clustering is activated in the
     *            preferences.
     * @return the final comparator used for sorting the CU.
     */

    private Comparator<BodyDeclaration> getBodyDeclarationComparator(Comparator<Signature> signatureComparator) {
	Comparator<Signature> finalMethodSignatureComparator;
	if (this.preferences.isOverloadedMethodClustering() || this.preferences.isGetterSetterClustering()) {
	    finalMethodSignatureComparator = getClusterComparator(signatureComparator);
	} else {
	    finalMethodSignatureComparator = signatureComparator;
	}
	logFinalOrderingOfKnownSignatures(finalMethodSignatureComparator);
	return new BodyDeclarationComparator(finalMethodSignatureComparator, this.knownSignatures);
    }

    /**
     * @param signatureComparator
     *            used to sort the subgraphs (clusters) in the callGraph.
     * @return a signature comparator that takes clusters into account
     */
    private Comparator<Signature> getClusterComparator(Comparator<Signature> signatureComparator) {
	final ClusterGraphExtractor clusterGraphExtractor = new ClusterGraphExtractor(this.callGraph,
		this.preferences.isGetterSetterClustering(), this.preferences.isOverloadedMethodClustering());
	this.ast.accept(clusterGraphExtractor);
	final List<ClusterNode> clusterGraph = clusterGraphExtractor.getClusteredGraph();
	sortSubgraphs(clusterGraph);
	return new ClusterComparator(signatureComparator, clusterGraph);
    }

    private void logFinalOrderingOfKnownSignatures(Comparator<Signature> comparator) {
	logger.info("Final ordering of [{}] known signatures:", this.knownSignatures.size());
	final List<Signature> orderedSignatures = new ArrayList<Signature>();
	orderedSignatures.addAll(this.knownSignatures);
	Collections.sort(orderedSignatures, comparator);
	logger.info("Preferences: [{}]", this.preferences.toString());
	int i = 0;
	for (final Signature signature : orderedSignatures) {
	    logger.info("[{}] {}", ++i, signature);
	}
    }

    /**
     * Sort all CallGraphNode-clusters in all {@link ClusterNode}s in the given
     * clusterGraph.
     *
     * @param clusterGraph
     */
    private void sortSubgraphs(List<ClusterNode> clusterGraph) {
	logger.trace("Sorting clusters (subgraphs):");
	for (final ClusterNode clusteredNode : clusterGraph) {
	    final List<CallGraphNode> clusteredNodes = clusteredNode.getClusteredNodes();
	    logger.trace("Subgraph [{}]", clusteredNodes);
	    if (clusteredNodes.size() > 1) {
		final List<CallGraphNode> subGraph = getSubgraph(clusteredNodes);
		new CleanCodeMethodSorter(this.preferences).sort(subGraph, this.ast);
		clusteredNode.setClusteredNodes(subGraph);
	    }
	}
    }

    /**
     * Creates a invocation-subGraph consisting only of nodes and edges between
     * the given collection of nodes.
     *
     * @param nodes
     * @return a list of {@link CallGraphNode}s connected to invocation graph.
     *         The nodes are sorted following source position order.
     *
     * */
    private List<CallGraphNode> getSubgraph(Collection<CallGraphNode> nodes) {
	final CallGraphExtractor subCallGraphExtractor = new SubCallGraphExtractor(nodes);
	this.ast.accept(subCallGraphExtractor);
	return new ArrayList<CallGraphNode>(subCallGraphExtractor.getCallGraph());

    }

}
