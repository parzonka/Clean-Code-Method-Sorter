package com.github.parzonka.ccms.sorter.cluster;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.parzonka.ccms.sorter.callgraph.CallGraphNode;
import com.github.parzonka.ccms.sorter.comparator.Signature;
import com.github.parzonka.ccms.sorter.comparator.SignatureComparator;

/**
 * This comparator wraps a signature comparator providing a layer to deal with
 * clusters (and pass non-cluster relationships to the wrapped comparator)
 *
 * @author Mateusz Parzonka
 *
 */
public class ClusterComparator implements Comparator<Signature> {

    private final Map<Signature, SignatureCluster> mapping;
    private final Comparator<Signature> signatureComparator;
    private final Set<Signature> knownSignatures;

    /**
     * @param signatureComparator
     * @param clusterNodes
     */
    public ClusterComparator(Comparator<Signature> signatureComparator, Collection<ClusterNode> clusterNodes) {
	super();
	this.mapping = new HashMap<Signature, SignatureCluster>();
	this.signatureComparator = signatureComparator;
	this.knownSignatures = new HashSet<Signature>();
	for (final ClusterNode clusterNode : clusterNodes) {
	    createSignatureCluster(clusterNode);
	    for (final CallGraphNode node : clusterNode.getClusteredNodes()) {
		this.knownSignatures.add(node.getSignature());
	    }
	}
    }

    /**
     * Creates a signature cluster (mapping of a set of signatures to a main
     * signature).
     *
     * @param clusterNode
     */
    private void createSignatureCluster(ClusterNode clusterNode) {
	final SignatureCluster signatureCluster = new SignatureCluster(clusterNode);
	for (final CallGraphNode node : clusterNode.getClusteredNodes()) {
	    this.mapping.put(node.getSignature(), signatureCluster);
	}
    }

    @Override
    public int compare(Signature signature1, Signature signature2) {
	if (this.knownSignatures.contains(signature1) && this.knownSignatures.contains(signature2)) {
	    final SignatureCluster cluster1 = this.mapping.get(signature1);
	    final SignatureCluster cluster2 = this.mapping.get(signature2);
	    if (cluster1 == cluster2)
		return cluster1.compare(signature1, signature2);
	    else
		return this.signatureComparator.compare(cluster1.getMainSignature(), cluster2.getMainSignature());
	}
	return 0;
    }

    /**
     * Models a cluster using a signature comparator containing all signatures
     * in the cluster.
     *
     * @author Mateusz Parzonka
     *
     */
    class SignatureCluster implements Comparator<Signature> {

	private final SignatureComparator comp;
	private final Signature mainSignature;

	public SignatureCluster(ClusterNode clusterNode) {
	    super();
	    this.comp = new SignatureComparator();
	    int i = 0;
	    this.mainSignature = clusterNode.getSignature();
	    for (final CallGraphNode node : clusterNode.getClusteredNodes())
		this.comp.put(node.getSignature(), i++);
	}

	@Override
	public int compare(Signature signature1, Signature signature2) {
	    return this.comp.compare(signature1, signature2);
	}

	/**
	 * @return the signature of the first node
	 */
	public Signature getMainSignature() {
	    return this.mainSignature;
	}

    }

}
