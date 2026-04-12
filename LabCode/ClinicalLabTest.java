import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ClinicalLabTest {

    private static final DateTimeFormatter READY_FMT =
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    protected final String testName;
    protected final String unit;
    protected Double       result;
    private   double       price;
    private   int          turnaroundHours;

    public ClinicalLabTest(String testName, String unit) {
        this.testName        = testName;
        this.unit            = unit;
        this.result          = null;
        this.price           = 0.0;
        this.turnaroundHours = 4;
    }

    public abstract String getReferenceRange(String sex);
    public abstract String interpret(double value, String sex);

    public String  getTestName()        { return testName; }
    public String  getUnit()            { return unit; }
    public Double  getResult()          { return result; }
    public double  getPrice()           { return price; }
    public int     getTurnaroundHours() { return turnaroundHours; }

    public void setResult(double v)       { this.result          = v; }
    public void clearResult()             { this.result          = null; }
    public void setPrice(double p)        { this.price           = p; }
    public void setTurnaroundHours(int h) { this.turnaroundHours = h; }

    public boolean hasResult() { return result != null; }

    public String getFormattedResult() {
        if (result == null) return "N/A";
        if (result == Math.floor(result) && !Double.isInfinite(result))
            return String.valueOf(result.intValue());
        return String.format("%.2f", result);
    }

    public String getInterpretation(String sex) {
        return (result == null) ? "N/A" : interpret(result, sex);
    }

    public String getTurnaroundLabel() {
        if (turnaroundHours < 24)
            return turnaroundHours + (turnaroundHours == 1 ? " hr" : " hrs");
        int days = turnaroundHours / 24;
        return days + (days == 1 ? " day" : " days");
    }

    public String getExpectedReadyTime(LocalDateTime cdt) {
        return cdt.plusHours(turnaroundHours).format(READY_FMT);
    }

    public String getDisplayName() { return testName; }

    @Override
    public String toString() {
        return String.format("%-28s %s %s", testName, getFormattedResult(), unit);
    }
}