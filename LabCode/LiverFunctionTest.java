public class LiverFunctionTest extends BiochemistryTest {

    public LiverFunctionTest(String name, String unit,
                              double maleMin, double maleMax,
                              double femaleMin, double femaleMax) {
        super(name, unit, maleMin, maleMax, femaleMin, femaleMax);
    }

    public LiverFunctionTest(String name, String unit, double min, double max) {
        super(name, unit, min, max);
    }

    @Override
    public String interpret(double value, String sex) {
        String base = super.interpret(value, sex);
        if ("HIGH".equals(base)) return "HIGH \u2014 Possible Liver Dysfunction";
        if ("LOW".equals(base))  return "LOW \u2014 Possible Malnutrition";
        return "NORMAL";
    }

    @Override
    public String getDisplayName() { return "[LFT] " + super.getDisplayName(); }
}
