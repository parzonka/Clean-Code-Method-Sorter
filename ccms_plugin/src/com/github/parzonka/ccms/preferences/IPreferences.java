package com.github.parzonka.ccms.preferences;

import java.util.List;

/**
 * Constant definitions for plug-in preferences
 */
public interface IPreferences {

    public boolean isOverloadedMethodClustering();

    /**
     * @return true, if the working list has to be presorted
     */
    public boolean applyWorkingListHeuristics();

    public List<String> getMethodOrderingPreferences();

    /**
     * @return true, when getter/setters are to be clustered together.
     */
    public boolean isGetterSetterClustering();

    /**
     * @return true, if the invocation strategy is depth-first, false, when it
     *         is breadth-first
     */
    public boolean isInvocationStrategyDepthFirst();

    public boolean isBeforeAfterRelation();

}
