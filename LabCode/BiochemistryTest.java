public class BiochemistryTest extends ClinicalLabTest {

    protected double maleMin, maleMax;
    protected double femaleMin, femaleMax;

    /** Different ranges per sex */
    public BiochemistryTest(String name, String unit,
                            double maleMin,   double maleMax,
                            double femaleMin, double femaleMax) {
        super(name, unit);
        this.maleMin   = maleMin;
        this.maleMax   = maleMax;
        this.femaleMin = femaleMin;
        this.femaleMax = femaleMax;
    }

    /** Same range for both sexes */
    public BiochemistryTest(String name, String unit, double min, double max) {
        this(name, unit, min, max, min, max);
    }

    @Override
    public String getReferenceRange(String sex) {
        double lo = "FEMALE".equalsIgnoreCase(sex) ? femaleMin : maleMin;
        double hi = "FEMALE".equalsIgnoreCase(sex) ? femaleMax : maleMax;
        return lo + " - " + hi;
    }

    @Override
    public String interpret(double value, String sex) {
        double lo = "FEMALE".equalsIgnoreCase(sex) ? femaleMin : maleMin;
        double hi = "FEMALE".equalsIgnoreCase(sex) ? femaleMax : maleMax;
        if (value < lo) return "LOW";
        if (value > hi) return "HIGH";
        return "NORMAL";
    }
}