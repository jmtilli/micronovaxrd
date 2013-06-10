package fi.micronova.tkk.xray;
/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their Octave and human-readable names.
 */
public enum FitnessFunction {
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
    relchi2("relchi2fitnessfunction","Mixed relative / chi-squared"),
    logfitness("logfitnessfunction","p-norm in logarithmic space");

    /** Octave name */
    public final String octName;
    private final String name;
    FitnessFunction(String octName, String name) {
        this.octName = octName;
        this.name = name;
    }
    public String toString() {
        return name;
    }
};
