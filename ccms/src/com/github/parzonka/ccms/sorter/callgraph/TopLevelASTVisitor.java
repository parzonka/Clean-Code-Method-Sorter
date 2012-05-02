package com.github.parzonka.ccms.sorter.callgraph;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for ASTvisitors restricted to the top-level.
 *
 * @author Mateusz Parzonka
 *
 */
public abstract class TopLevelASTVisitor extends ASTVisitor {

    final private static Logger logger = LoggerFactory.getLogger(TopLevelASTVisitor.class);

    private ITypeBinding topLevelType = null;
    private CompilationUnit root;

    public TopLevelASTVisitor() {
	super();
    }

    @Override
    public boolean visit(TypeDeclaration node) {
	logger.trace("TypeDeclaration: {}", node.getName());
	final ITypeBinding type = node.resolveBinding();
	
	// capture the top level declaring class of the compilation unit
	if (this.topLevelType == null && type != null && type.isTopLevel() && !type.isAnonymous()) {
	    this.topLevelType = type;
	    logger.trace("TopLevelType := {}", type.getName().toString());
	}
	// nested classes are not traversed
	if (type != null && type.isClass() && type.isNested()) {
	    logger.trace("Is nested class");
	    return false;
	}

	return super.visit(node);
    }

    @Override
    public boolean visit(CompilationUnit node) {
	this.root = node;
	return super.visit(node);
    }

    protected CompilationUnit getRoot() {
	return this.root;
    }

    /**
     * Returns true if this typeBinding is declared in the topLevelType.
     *
     * @param typeBinding
     * @return
     */
    protected boolean isInTopLevelType(ITypeBinding typeBinding) {
	return typeBinding != null && typeBinding.getKey().equals(this.topLevelType.getKey());
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
	return false;
    }

}
