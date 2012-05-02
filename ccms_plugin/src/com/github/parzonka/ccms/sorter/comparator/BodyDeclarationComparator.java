package com.github.parzonka.ccms.sorter.comparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.util.CompilationUnitSorter;
import org.eclipse.jdt.internal.corext.codemanipulation.SortMembersOperation.DefaultJavaElementComparator;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.MembersOrderPreferenceCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comparator that sorts all {@link BodyDeclaration}s that are methods by an
 * ordering associated with their unique method signature. This ordering is
 * pre-calculated and passed at instantiation. All other BodyDeclarations are
 * tried to be sorted preserving their old relative order.
 * <p>
 * Based on {@link DefaultJavaElementComparator}.
 *
 * @author Mateusz Parzonka
 */
public class BodyDeclarationComparator implements Comparator<BodyDeclaration> {

    final private static Logger logger = LoggerFactory.getLogger(BodyDeclarationComparator.class);

    private final MembersOrderPreferenceCache fMemberOrderCache;
    private final Comparator<Signature> methodDeclarationComparator;
    private final Set<Signature> knownMethodSignatures;

    public BodyDeclarationComparator(Comparator<Signature> methodDeclarationComparator,
	    Set<Signature> knownMethodSignatures) {
	this.fMemberOrderCache = JavaPlugin.getDefault().getMemberOrderPreferenceCache();
	this.methodDeclarationComparator = methodDeclarationComparator;
	this.knownMethodSignatures = knownMethodSignatures;
    }

    /**
     * @return the knownMethodSignatures
     */
    public Set<Signature> getKnownMethodSignatures() {
	return this.knownMethodSignatures;
    }

    public Comparator<Signature> getMethodDeclarationComparator() {
	return this.methodDeclarationComparator;
    }

    /**
     * This comparator follows the contract defined in
     * CompilationUnitSorter.sort.
     *
     * @see Comparator#compare(java.lang.Object, java.lang.Object)
     * @see CompilationUnitSorter#sort(int,
     *      org.eclipse.jdt.core.ICompilationUnit, int[], java.util.Comparator,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public int compare(BodyDeclaration bodyDeclaration1, BodyDeclaration bodyDeclaration2) {
	if (isSortPreserved(bodyDeclaration1) && isSortPreserved(bodyDeclaration2)) {
	    final int preservedOrder = preserveRelativeOrder(bodyDeclaration1, bodyDeclaration2);
	    logger.trace("Keeping preserved order: [{}] : [{}] = " + preservedOrder, bodyDeclaration1.toString(),
		    bodyDeclaration2.toString());
	    return preservedOrder;
	}

	final int cat1 = category(bodyDeclaration1);
	final int cat2 = category(bodyDeclaration2);

	if (cat1 != cat2) {
	    final int categoryOrder = cat1 - cat2;
	    logger.trace("Keeping category order: [{}] : [{}] = " + categoryOrder, bodyDeclaration1.toString(),
		    bodyDeclaration2.toString());
	    return categoryOrder;
	}

	if (bodyDeclaration1.getNodeType() == ASTNode.METHOD_DECLARATION) {
	    final MethodDeclaration method1 = (MethodDeclaration) bodyDeclaration1;
	    final MethodDeclaration method2 = (MethodDeclaration) bodyDeclaration2;

	    final Signature signature1 = new Signature(method1);
	    final Signature signature2 = new Signature(method2);

	    if (this.knownMethodSignatures.contains(signature1) && this.knownMethodSignatures.contains(signature2)) {
		final int compare = this.methodDeclarationComparator.compare(signature1, signature2);
		logger.trace("Comparing methods [{}] : [{}] = " + compare, signature1, signature2);
		if (compare == 0)
		    logger.warn("No absolute compare value between [{}] and [{}]", signature1, signature2);
		return compare;
	    }
	    logger.warn("A method signature was not known!");
	    logger.warn("{} known={}", signature1, this.knownMethodSignatures.contains(signature1));
	    logger.warn("{} known={}", signature2, this.knownMethodSignatures.contains(signature2));
	}
	final int relativeOrder = preserveRelativeOrder(bodyDeclaration1, bodyDeclaration2);
	logger.trace("Keeping relative order: [{}] : [{}]= " + relativeOrder, bodyDeclaration1.toString(),
		bodyDeclaration2.toString());
	return relativeOrder;
    }

    private boolean isSortPreserved(BodyDeclaration bodyDeclaration) {
	switch (bodyDeclaration.getNodeType()) {
	case ASTNode.FIELD_DECLARATION:
	case ASTNode.ENUM_CONSTANT_DECLARATION:
	case ASTNode.INITIALIZER:
	case ASTNode.TYPE_DECLARATION:
	    return true;
	default:
	    return false;
	}
    }

    private int preserveRelativeOrder(BodyDeclaration bodyDeclaration1, BodyDeclaration bodyDeclaration2) {
	final int value1 = ((Integer) bodyDeclaration1.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
	final int value2 = ((Integer) bodyDeclaration2.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
	return value1 - value2;
    }

    private int category(BodyDeclaration bodyDeclaration) {
	switch (bodyDeclaration.getNodeType()) {
	case ASTNode.METHOD_DECLARATION: {
	    final MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
	    if (method.isConstructor()) {
		return getMemberCategory(MembersOrderPreferenceCache.CONSTRUCTORS_INDEX);
	    } else
		return getMemberCategory(MembersOrderPreferenceCache.METHOD_INDEX);
	}
	case ASTNode.FIELD_DECLARATION: {
	    return getMemberCategory(MembersOrderPreferenceCache.FIELDS_INDEX);
	}
	case ASTNode.INITIALIZER: {
	    final int flags = ((Initializer) bodyDeclaration).getModifiers();
	    if (Modifier.isStatic(flags))
		return getMemberCategory(MembersOrderPreferenceCache.STATIC_INIT_INDEX);
	    else
		return getMemberCategory(MembersOrderPreferenceCache.INIT_INDEX);
	}
	case ASTNode.TYPE_DECLARATION:
	case ASTNode.ENUM_DECLARATION:
	case ASTNode.ANNOTATION_TYPE_DECLARATION:
	    return getMemberCategory(MembersOrderPreferenceCache.TYPE_INDEX);
	case ASTNode.ENUM_CONSTANT_DECLARATION:
	    return getMemberCategory(MembersOrderPreferenceCache.ENUM_CONSTANTS_INDEX);
	case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
	    return getMemberCategory(MembersOrderPreferenceCache.METHOD_INDEX);

	}
	throw new IllegalStateException();
    }

    private int getMemberCategory(int kind) {
	return this.fMemberOrderCache.getCategoryIndex(kind);
    }

    @Override
    public String toString() {
	final String LF = System.getProperty("line.separator");
	final StringBuilder sb = new StringBuilder();
	sb.append("Final ordering of [" + this.knownMethodSignatures.size() + "] known signatures:").append(LF);
	final List<Signature> orderedSignatures = new ArrayList<Signature>();
	orderedSignatures.addAll(this.knownMethodSignatures);
	Collections.sort(orderedSignatures, this.methodDeclarationComparator);
	int i = 0;
	for (final Signature signature : orderedSignatures) {
	    sb.append(String.format("[%d] %s", ++i, signature)).append(LF);
	}
	return sb.toString();
    }

}
