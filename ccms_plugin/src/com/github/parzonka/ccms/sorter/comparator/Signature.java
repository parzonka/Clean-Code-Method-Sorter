package com.github.parzonka.ccms.sorter.comparator;

import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;

/**
 * Models a method signature. Signature can be created from
 * {@link MethodDeclaration}s and from {@link MethodInvocation}s providing a
 * common point of reference which does not depend on concrete instances of
 * MethodDeclaration and MethodInvocation
 *
 * @author Mateusz Parzonka
 *
 */
public class Signature implements Comparable<Signature> {

    private final String methodSignature;

    public Signature(MethodDeclaration methodDeclaration) {
	this.methodSignature = getMethodSignature(methodDeclaration);
    }

    public Signature(MethodInvocation methodInvocation) {
	this.methodSignature = getMethodSignature(methodInvocation);
    }

    public Signature(String signature) {
	this.methodSignature = signature;
    }

    public Signature() {
	this.methodSignature = "";
    }

    public Signature(int i) {
	this.methodSignature = getInitializerSignature(i);
    }

    public static String getMethodSignature(MethodDeclaration method) {

	final StringBuilder sb = new StringBuilder();

	@SuppressWarnings("unchecked")
	final List<SingleVariableDeclaration> parameters = method.parameters();

	sb.append(method.getName().getIdentifier());
	sb.append("(");

	for (int i = 0; i < parameters.size(); i++) {
	    // TODO ugly idiom
	    sb.append(ASTNodes.asString(parameters.get(i).getType()).split("<")[0]);
	    if (i < parameters.size() - 1) {
		sb.append(", ");
	    }
	}

	sb.append(")");

	return sb.toString();

    }

    @Override
    public int hashCode() {
	return this.methodSignature.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Signature))
	    return false;
	final Signature other = (Signature) obj;
	if (this.methodSignature == null) {
	    if (other.methodSignature != null)
		return false;
	} else if (!this.methodSignature.equals(other.methodSignature))
	    return false;
	return true;
    }

    public static String getMethodSignature(MethodInvocation method) {

	final StringBuilder sb = new StringBuilder();

	final IMethodBinding methodBinding = method.resolveMethodBinding();

	if (methodBinding == null) {
	    return null;
	}

	final ITypeBinding[] parameters = methodBinding.getParameterTypes();

	sb.append(method.getName());
	sb.append("(");

	for (int i = 0; i < parameters.length; i++) {
	    sb.append(parameters[i].getName().split("<")[0]);
	    if (i < parameters.length - 1) {
		sb.append(", ");
	    }
	}
	sb.append(")");

	return sb.toString();
    }

    public static String getInitializerSignature(int i) {
	return "#INITIALIZER#_" + i;
    }

    @Override
    public int compareTo(Signature otherSignature) {
	return this.methodSignature.compareTo(otherSignature.methodSignature);
    }

    @Override
    public String toString() {
	return this.methodSignature;
    }

}
