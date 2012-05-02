package com.github.parzonka.ccms.preferences;

public class PreferenceConstants {

    public static final String INVOCATION_ORDERING_STRATEGY = "Invocation ordering strategy";
    public static final String INVOCATION_ORDERING_STRATEGY_BREADTH_FIRST = "Breadth-first";
    public static final String INVOCATION_ORDERING_STRATEGY_DEPTH_FIRST = "Depth-first";

    public static final String INVOCATION_STARTPOINT_STRATEGY = "Invocation start-point strategy";
    public static final String INVOCATION_STARTPOINT_STRATEGY_USER = "Use existing order";
    public static final String INVOCATION_STARTPOINT_STRATEGY_HEURISTIC = "Apply heuristic";

    public static final String RESPECT_BEFORE_AFTER = "Respect before/after relation";
    public static final String WORKING_LIST_HEURISTICS = "Apply working list heuristics";

    public static final String CLUSTER_OVERLOADED_METHODS = "Cluster overloaded methods";
    public static final String CLUSTER_GETTER_SETTER = "Cluster getter and setter";

    public static final String METHOD_ORDERING_PRIORITIES = "Ordering priorities";
    public static final String PRIORITY_INVOCATION_ORDER = "Apply INVOCATION ordering";
    public static final String PRIORITY_INITIALIZER_INVOCATION = "Invoked by initializer";
    public static final String PRIORITY_ACCESS_LEVEL = "Separate by ACCESS LEVEL";
    public static final String PRIORITY_CONSTRUCTOR = "Separate CONSTRUCTORS and non-constructor methods";
    public static final String PRIORITY_LEXICALITY = "Apply LEXICAL ordering";
    public static final String PRIORITY_FAN_OUT = "Fan out";
    public static final String PRIORITY_ROOTS = "Invocation graph roots";
    public static final String PRIORITY_LEAFS = "Invocation graph leafs";
    public static final String PRIORITY_REACHABILITY = "Respect BEFORE/AFTER relation";
    public static final String PRIORITY_SOURCE_POSITION = "Keep ORIGINAL source position";

    public static final String DELIMITER = "#";

}
