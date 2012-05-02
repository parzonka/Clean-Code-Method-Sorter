package com.github.parzonka.ccms.preferences;

import static com.github.parzonka.ccms.Utils.list;
import static com.github.parzonka.ccms.preferences.PreferenceConstants.*;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.github.parzonka.ccms.Activator;

/**
 * Reads the preferences from the {@link IPreferenceStore} and makes them
 * accessible as predicates via the plugins {@link IPreferences}-interface.
 *
 * @author Mateusz Parzonka
 *
 */
public class PreferenceManager implements IPreferences {

    final IPreferenceStore store;

    public PreferenceManager() {
	super();
	this.store = Activator.getDefault().getPreferenceStore();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "PreferenceManager [applyWorkingListHeuristics()="
		+ applyWorkingListHeuristics()
		+ ", isGetterSetterClustering()=" + isGetterSetterClustering()
		+ ", isInvocationStrategyDepthFirst()="
		+ isInvocationStrategyDepthFirst()
		+ ", isOverloadedMethodClustering()="
		+ isOverloadedMethodClustering()
		+ ", getMethodOrderingPreferences()="
		+ getMethodOrderingPreferences() + "]";
    }

    @Override
    public boolean applyWorkingListHeuristics() {
	return this.store.getString(INVOCATION_STARTPOINT_STRATEGY).equals(
		INVOCATION_STARTPOINT_STRATEGY_HEURISTIC);
    }

    @Override
    public boolean isBeforeAfterRelation() {
	return this.store.getBoolean(RESPECT_BEFORE_AFTER);
    }

    @Override
    public boolean isGetterSetterClustering() {
	return this.store.getBoolean(CLUSTER_GETTER_SETTER);
    }

    @Override
    public boolean isInvocationStrategyDepthFirst() {
	return this.store.getString(INVOCATION_ORDERING_STRATEGY).equals(
		INVOCATION_ORDERING_STRATEGY_DEPTH_FIRST);
    }

    @Override
    public boolean isOverloadedMethodClustering() {
	return this.store.getBoolean(CLUSTER_OVERLOADED_METHODS);
    }

    @Override
    public List<String> getMethodOrderingPreferences() {
	return list(this.store.getString(METHOD_ORDERING_PRIORITIES).split(DELIMITER));
    }

}
