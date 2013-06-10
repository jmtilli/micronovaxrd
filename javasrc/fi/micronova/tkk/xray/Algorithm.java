package fi.micronova.tkk.xray;
/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their Octave and human-readable names.
 */
public enum Algorithm {
    /*
    CovGA("CovGA","CovGA"),
    HGA("HGA","HGA"),
    JGA("JGA","JGA"),
    XGA("XGA","XGA"),
    CGA("CGA","CGA"),
    MGA("MGA","MGA");
    */
    /*
    ICAGA("ICAGA","ICAGA",false),
    PCAGA("PCAGA","PCAGA",false),
    CGA("CGA","CGA",false),
    */
    DE("DE","DE",true),
    CovDE("CovDE","CovDE",true);

    /** Octave name */
    public final String octName;
    public final boolean isDE;
    private final String name;
    Algorithm(String octName, String name, boolean isDE) {
        this.octName = octName;
        this.name = name;
        this.isDE = isDE;
    }
    public String toString() {
        return name;
    }
};

