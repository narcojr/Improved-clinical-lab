public abstract class ClinicalLabTest {

    protected final String testName;
    protected final String unit;
    protected Double result;   // null = not yet entered

    public ClinicalLabTest(String testName, String unit) {
        this.testName = testName;
        this.unit     = unit;
        this.result   = null;
    }

    // ── Abstract methods — each subclass defines these ─────────────
    public abstract String getReferenceRange(String sex);
    public abstract String interpret(double value, String sex);

    // ── Concrete shared methods ────────────────────────────────────
    public String getTestName()    { return testName; }
    public String getUnit()        { return unit; }
    public Double getResult()      { return result; }

    public void setResult(double value) { this.result = value; }
    public void clearResult()           { this.result = null;  }

    public boolean hasResult() { return result != null; }

    public String getFormattedResult() {
        if (result == null) return "N/A";
        // Drop trailing .0 for whole numbers, keep decimals otherwise
        return (result == Math.floor(result) && !Double.isInfinite(result))
               ? String.valueOf(result.intValue())
               : String.valueOf(result);
    }

    public String getInterpretation(String sex) {
        return (result == null) ? "N/A" : interpret(result, sex);
    }

    // Override in subclasses that add a display prefix (e.g. [LFT])
    public String getDisplayName() { return testName; }

    @Override
    public String toString() {
        return String.format("%s: %s %s  [%s]  → %s",
            testName,
            getFormattedResult(), unit,
            getReferenceRange("MALE"),
            result == null ? "N/A" : interpret(result, "MALE"));
    }
}
