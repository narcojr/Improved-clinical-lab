public class CardiacMarkerTest extends ClinicalLabTest {

    private final double threshold;

    public CardiacMarkerTest(String name, String unit, double threshold) {
        super(name, unit);
        this.threshold = threshold;
    }

    @Override
    public String getReferenceRange(String sex) { return "< " + threshold; }

    @Override
    public String interpret(double value, String sex) {
        return value > threshold ? "HIGH \u26A0" : "NORMAL";
    }
}
