import java.text.DecimalFormat;

public abstract class ClinicalLabTest {

    protected String  testName;
    protected String  unit;
    protected double  resultValue;
    protected boolean resultEntered;

    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    // ── Constructor ──────────────────────────────────────────
    public ClinicalLabTest(String testName, String unit) {
        this.testName      = testName;
        this.unit          = unit;
        this.resultEntered = false;
        this.resultValue   = 0.0;
    }

    // ── Abstract — subclasses must implement ─────────────────
    public abstract String getReferenceRange(String sex);
    public abstract String interpret(double value, String sex);

    // ── Template Method — do not override ────────────────────
    public final String getInterpretation(String sex) {
        if (!resultEntered) return "—";
        return interpret(resultValue, sex);
    }

    // ── Setters ──────────────────────────────────────────────
    public void setResult(double value) {
        this.resultValue   = value;
        this.resultEntered = true;
    }

    public void clearResult() {
        this.resultValue   = 0.0;
        this.resultEntered = false;
    }

    // ── Getters ──────────────────────────────────────────────
    public String  getTestName()      { return testName; }
    public String  getUnit()          { return unit; }
    public double  getResultValue()   { return resultValue; }
    public boolean isResultEntered()  { return resultEntered; }

    public String getDisplayName() {
        return testName + " (" + unit + ")";
    }

    public String getFormattedResult() {
        return resultEntered ? FORMAT.format(resultValue) : "";
    }
}
