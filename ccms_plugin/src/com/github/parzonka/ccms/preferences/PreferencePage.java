/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.github.parzonka.ccms.preferences;

import static com.github.parzonka.ccms.Utils.join;
import static com.github.parzonka.ccms.Utils.list;
import static com.github.parzonka.ccms.preferences.PreferenceConstants.*;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.parzonka.ccms.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PreferencePage extends FieldEditorPreferencePage implements
	IWorkbenchPreferencePage {

    public PreferencePage() {
	super(GRID);
	setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {

	addField(new SimpleListEditor(METHOD_ORDERING_PRIORITIES,
		METHOD_ORDERING_PRIORITIES, getFieldEditorParent()));

	addRadioGroupField(INVOCATION_STARTPOINT_STRATEGY,
		INVOCATION_STARTPOINT_STRATEGY_USER,
		INVOCATION_STARTPOINT_STRATEGY_HEURISTIC);

	addRadioGroupField(INVOCATION_ORDERING_STRATEGY,
		INVOCATION_ORDERING_STRATEGY_BREADTH_FIRST,
		INVOCATION_ORDERING_STRATEGY_DEPTH_FIRST);

	addBooleanField(CLUSTER_OVERLOADED_METHODS);

	addBooleanField(CLUSTER_GETTER_SETTER);

	addBooleanField(RESPECT_BEFORE_AFTER);

    }

    private void addRadioGroupField(String fieldName, String option1,
	    String option2) {
	addField(new RadioGroupFieldEditor(fieldName, fieldName, 1,
		new String[][] { { option1, option1 }, { option2, option2 } },
		getFieldEditorParent()));
    }

    private void addBooleanField(String field) {
	addField(new BooleanFieldEditor(field, field, getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    public class SimpleListEditor extends ListEditor {

	public SimpleListEditor(String name, String labelText, Composite parent) {
	    super(name, labelText, parent);
	    getAddButton().setVisible(false);
	    getRemoveButton().setVisible(false);
	    getDownButton().moveAbove(getRemoveButton());
	    getUpButton().moveAbove(getDownButton());
	}

	@Override
	protected String createList(String[] items) {
	    return join(list(items), DELIMITER);
	}

	@Override
	protected String getNewInputObject() {
	    return null;
	}

	@Override
	protected String[] parseString(String stringList) {
	    return stringList.split(DELIMITER);
	}

    }

}