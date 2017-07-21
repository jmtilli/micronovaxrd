package fi.micronova.tkk.xray;
/** Enumeration of the supported algorithms.
 *
 * All the supported algorithms and their Octave and human-readable names.
 */
public enum FitnessFunction {
    relchi2transform("relchi2transformfitnessfunction","p-norm in MR/chi^2 space"),
    relchi2("relchi2fitnessfunction","Mixed relative / chi-squared"),
    logfitness("logfitnessfunction","p-norm in logarithmic space"),
    sqrtfitness("sqrtfitnessfunction","p-norm in sqrt-space"),
    chi2("chi2fitnessfunction","chi-squared");

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
