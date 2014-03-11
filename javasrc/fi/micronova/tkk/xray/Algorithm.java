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
    JavaDE("JavaDE","JavaDE",true,true),
    JavaCovDE("JavaCovDE","JavaCovDE",true,true),
    DE("DE","DE",true,false),
    CovDE("CovDE","CovDE",true,false);

    /** Octave name */
    public final String octName;
    public final boolean isDE;
    public final boolean isJava;
    private final String name;
    Algorithm(String octName, String name, boolean isDE, boolean isJava) {
        this.octName = octName;
        this.name = name;
        this.isDE = isDE;
        this.isJava = isJava;
    }
    public String toString() {
        return name;
    }
};

