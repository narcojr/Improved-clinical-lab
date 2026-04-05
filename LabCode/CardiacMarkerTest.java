
public class CardiacMarkerTest extends ClinicalLabTest {

    private final double threshold;

    public CardiacMarkerTest(String testName, String unit, double threshold) {
        super(testName, unit);
        this.threshold = threshold;
    }

    @Override
    public String getReferenceRange(String sex) {
        return "< " + threshold;   // same for both sexes
    }

    @Override
    public String interpret(double value, String sex) {
        return value > threshold ? "HIGH ⚠" : "NORMAL";
    }
}
