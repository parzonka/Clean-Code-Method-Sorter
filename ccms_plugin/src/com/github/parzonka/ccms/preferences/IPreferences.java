/**
 * Copyright (c) 2011 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
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
