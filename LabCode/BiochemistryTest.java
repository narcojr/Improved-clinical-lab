
public class BiochemistryTest extends ClinicalLabTest {

    protected double maleMin, maleMax;
    protected double femaleMin, femaleMax;

    // Constructor: different ranges per sex
    public BiochemistryTest(String testName, String unit,
                            double maleMin,   double maleMax,
                            double femaleMin, double femaleMax) {
        super(testName, unit);
        this.maleMin   = maleMin;
        this.maleMax   = maleMax;
        this.femaleMin = femaleMin;
        this.femaleMax = femaleMax;
    }

    // Constructor: same range for both sexes
    public BiochemistryTest(String testName, String unit, double min, double max) {
        this(testName, unit, min, max, min, max);
    }

    @Override
    public String getReferenceRange(String sex) {
        double lo = "FEMALE".equalsIgnoreCase(sex) ? femaleMin : maleMin;
        double hi = "FEMALE".equalsIgnoreCase(sex) ? femaleMax : maleMax;
        return lo + " – " + hi;
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
