
public class LiverFunctionTest extends BiochemistryTest {

    // Constructor: sex-specific ranges
    public LiverFunctionTest(String testName, String unit,
                              double maleMin, double maleMax,
                              double femaleMin, double femaleMax) {
        super(testName, unit, maleMin, maleMax, femaleMin, femaleMax);
    }

    // Constructor: same range for both sexes
    public LiverFunctionTest(String testName, String unit, double min, double max) {
        super(testName, unit, min, max);
    }

    @Override
    public String interpret(double value, String sex) {
        String base = super.interpret(value, sex);
        if ("HIGH".equals(base)) return "HIGH — Possible Liver Dysfunction";
        if ("LOW".equals(base))  return "LOW  — Possible Malnutrition";
        return "NORMAL";
    }

    @Override
    public String getDisplayName() {
        return "[LFT] " + super.getDisplayName();
    }
}
