package com.github.parzonka.ccms.sorter.callgraph;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Some utilities for working with compilation units, ASTs and selected
 * ASTNodes.
 *
 * @author Mateusz Parzonka
 *
 */
public class ASTUtils {

    private ASTUtils() {
	// not meant to be instantiated
    }

    public static ASTNode getAST(ICompilationUnit compilationUnit, IJavaProject project) {
	final ASTParser parser = ASTParser.newParser(AST.JLS3);
	parser.setKind(ASTParser.K_COMPILATION_UNIT);
	parser.setSource(compilationUnit);
	parser.setResolveBindings(true);
	parser.setBindingsRecovery(true);
	parser.setProject(project);
	return parser.createAST(null);
    }

    /**
     * Returns an ASTNode for a ICompilationUnit usually representing the
     * Java-AST of a complete java source-code file.
     *
     * @param compilationUnit
     * @return
     */
    public static ASTNode getAST(ICompilationUnit compilationUnit) {
	final ASTParser parser = ASTParser.newParser(AST.JLS3);
	parser.setKind(ASTParser.K_COMPILATION_UNIT);
	parser.setSource(compilationUnit);
	parser.setResolveBindings(true);
	parser.setBindingsRecovery(false);
	return parser.createAST(null);
    }

    /**
     * Returns a ICompilationUnit when the active editor in the workbench is an
     * instance of CompilationUnitEditor. Returns null otherwise.
     *
     * @param workbenchWindow
     * @return the cu in the editor
     */
    public static ICompilationUnit getCompilationUnit(IWorkbenchWindow workbenchWindow) {
	CompilationUnitEditor cuEditor;
	final IWorkbench workbench = workbenchWindow.getWorkbench();
	final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
	final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
	final IEditorPart activeEditor = activePage.getActiveEditor();
	if (activeEditor instanceof CompilationUnitEditor)
	    cuEditor = (CompilationUnitEditor) activeEditor;
	else
	    return null;
	final IJavaElement input = SelectionConverter.getInput(cuEditor);
	if (input instanceof CompilationUnit) {
	    return (CompilationUnit) input;
	} else
	    return null;
    }

}
