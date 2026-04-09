public class ThyroidTest extends BiochemistryTest {

    public enum ThyroidMarker { TSH, T3, T4 }

    private final ThyroidMarker marker;

    public ThyroidTest(String name, String unit,
                       double min, double max, ThyroidMarker marker) {
        super(name, unit, min, max);
        this.marker = marker;
    }

    @Override
    public String interpret(double value, String sex) {
        String base = super.interpret(value, sex);
        switch (marker) {
            case TSH:
                if ("HIGH".equals(base)) return "HIGH \u2014 Suggests Hypothyroidism";
                if ("LOW".equals(base))  return "LOW \u2014 Suggests Hyperthyroidism";
                break;
            case T3: case T4:
                if ("HIGH".equals(base)) return "HIGH \u2014 Suggests Hyperthyroidism";
                if ("LOW".equals(base))  return "LOW \u2014 Suggests Hypothyroidism";
                break;
        }
        return "NORMAL";
    }

    @Override
    public String getDisplayName() { return "[Thyroid] " + super.getDisplayName(); }
}
