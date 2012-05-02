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

import java.util.Collection;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.github.parzonka.ccms.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
	final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	store.setDefault(INVOCATION_ORDERING_STRATEGY,
		INVOCATION_ORDERING_STRATEGY_DEPTH_FIRST);
	store.setDefault(INVOCATION_STARTPOINT_STRATEGY,
		INVOCATION_STARTPOINT_STRATEGY_HEURISTIC);
	store.setDefault(CLUSTER_GETTER_SETTER, false);
	store.setDefault(RESPECT_BEFORE_AFTER, true);
	store.setDefault(CLUSTER_OVERLOADED_METHODS, false);
	store.setDefault(
		METHOD_ORDERING_PRIORITIES,
		join(getDefaultMethodOrderingPriorities(), "#"));
    }

    public static Collection<String> getDefaultMethodOrderingPriorities() {
	return list(
		PRIORITY_INVOCATION_ORDER, PRIORITY_ACCESS_LEVEL,
		PRIORITY_SOURCE_POSITION,
		PRIORITY_LEXICALITY);
    }

}
