import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRecord {

    private static final String DATA_FILE = "patient_records.csv";
    private static final String COL_SEP   = "|";
    private static final String TEST_SEP  = ";;";
    private static final String FIELD_SEP = "~";

    /*
     * CSV column layout v3 (12 columns, indices 0-11):
     *   0  name
     *   1  age
     *   2  sex
     *   3  timeLastMeal
     *   4  collectionDate
     *   5  collectionTime
     *   6  paymentMethod
     *   7  totalAmount
     *   8  amountPaid
     *   9  change
     *  10  tests  (TEST_SEP-delimited)
     *  11  paymentReference
     *
     * Each test entry (FIELD_SEP-delimited, 8 fields):
     *   0 testName | 1 result | 2 unit | 3 refRange
     *   4 interpretation | 5 price | 6 turnaroundHours | 7 expectedReadyTime
     *
     * Backward-compatible:
     *   v1 (7 cols)  - tests at index 6, no payment
     *   v2 (11 cols) - tests at index 10, no payRef
     *   v3 (12 cols) - tests at index 10, payRef at 11
     */

    // Save a patient session (append to file)
    public static void save(ClinicalLabPatient p) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(DATA_FILE, true));
            StringBuilder sb = new StringBuilder();
            sb.append(escape(p.getName()))                       .append(COL_SEP)
              .append(p.getAge())                                .append(COL_SEP)
              .append(p.getSex())                                .append(COL_SEP)
              .append(escape(p.getTimeLastMeal()))               .append(COL_SEP)
              .append(p.getCollectionDate())                     .append(COL_SEP)
              .append(p.getCollectionTime())                     .append(COL_SEP)
              .append(escape(p.getPaymentMethod()))              .append(COL_SEP)
              .append(String.format("%.2f", p.getTotalAmount())) .append(COL_SEP)
              .append(String.format("%.2f", p.getAmountPaid()))  .append(COL_SEP)
              .append(String.format("%.2f", p.getChange()))      .append(COL_SEP);

            List<String> entries = new ArrayList<>();
            for (ClinicalLabTest t : p.getCompletedTests()) {
                entries.add(
                    escape(t.getTestName())                          + FIELD_SEP +
                    t.getFormattedResult()                           + FIELD_SEP +
                    t.getUnit()                                      + FIELD_SEP +
                    t.getReferenceRange(p.getSex())                  + FIELD_SEP +
                    t.getInterpretation(p.getSex())                  + FIELD_SEP +
                    String.format("%.2f", t.getPrice())              + FIELD_SEP +
                    t.getTurnaroundHours()                           + FIELD_SEP +
                    t.getExpectedReadyTime(p.getCollectionDateTime())
                );
            }
            sb.append(String.join(TEST_SEP, entries));
            sb.append(COL_SEP).append(escape(p.getPaymentReference()));
            pw.println(sb);
        } catch (IOException e) {
            System.err.println("Warning: Could not save record - " + e.getMessage());
        } finally {
            if (pw != null) pw.close();
        }
    }

    // Load all raw rows from the CSV file
    public static List<String[]> loadAll() {
        List<String[]> rows = new ArrayList<>();
        File file = new File(DATA_FILE);
        if (!file.exists()) return rows;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    rows.add(line.split("\\|", -1));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load records - " + e.getMessage());
        } finally {
            if (br != null) {
                try { br.close(); } catch (IOException ignored) {}
            }
        }
        return rows;
    }

    // Detect which column index holds the test data
    static int testCol(String[] c) {
        if (c.length >= 11) return 10; // v2 or v3
        if (c.length >= 7)  return 6;  // v1
        return -1;
    }

    // Rows for the history JTable: [Name, Age, Sex, Date, Time, "N test(s)", Payment]
    public static List<String[]> getTableRows() {
        List<String[]> result = new ArrayList<>();
        for (String[] c : loadAll()) {
            if (c.length < 6) continue;
            int tc  = testCol(c);
            int cnt = 0;
            if (tc >= 0 && tc < c.length && !c[tc].trim().isEmpty()) {
                cnt = c[tc].split(";;").length;
            }
            String pay = (c.length >= 11) ? get(c, 6) : "N/A";
            result.add(new String[]{
                get(c, 0), get(c, 1), get(c, 2),
                get(c, 4), get(c, 5),
                cnt + " test(s)", pay
            });
        }
        return result;
    }

    // Detailed text for the Swing history viewer
    public static String getDetailedResult(int index) {
        List<String[]> all = loadAll();
        if (index < 0 || index >= all.size()) return "Record not found.";

        String[] c        = all.get(index);
        int      tc       = testCol(c);
        boolean  hasPay   = c.length >= 11;
        boolean  hasPayRef = c.length >= 12;

        String SEP = "--------------------------------------------------------------";
        String BOX = "==============================================================";
        StringBuilder sb = new StringBuilder();

        sb.append(BOX).append("\n");
        sb.append("   NUCOMP DIAGNOSTIC CORPORATION\n");
        sb.append("   CLINICAL CHEMISTRY LABORATORY\n");
        sb.append(BOX).append("\n\n");

        sb.append("PATIENT INFORMATION\n").append(SEP).append("\n");
        sb.append(String.format("  Name         : %s%n", get(c, 0)));
        sb.append(String.format("  Age          : %s%n", get(c, 1)));
        sb.append(String.format("  Sex          : %s%n", get(c, 2)));
        sb.append(String.format("  Last Meal    : %s%n", get(c, 3)));
        sb.append(String.format("  Date         : %s%n", get(c, 4)));
        sb.append(String.format("  Time         : %s%n", get(c, 5)));

        if (hasPay) {
            sb.append(String.format("  Payment      : %s%n", get(c, 6)));
            sb.append(String.format("  Total Due    : PhP %s%n", get(c, 7)));
            sb.append(String.format("  Amount Paid  : PhP %s%n", get(c, 8)));
            sb.append(String.format("  Change       : PhP %s%n", get(c, 9)));
        }
        if (hasPayRef) {
            String ref = get(c, 11);
            if (!ref.isEmpty()) {
                sb.append(String.format("  Reference No.: %s%n", ref));
            }
        }

        sb.append("\nTEST RESULTS\n").append(SEP).append("\n");
        sb.append(String.format("  %-26s %-14s %-8s %-22s %s%n",
            "Test", "Result", "TAT", "Expected Ready", "Interpretation"));
        sb.append("  ").append(SEP).append("\n");

        if (tc >= 0 && tc < c.length && !c[tc].trim().isEmpty()) {
            for (String entry : c[tc].split(";;")) {
                String[] f    = entry.split("~", -1);
                boolean  has8 = f.length >= 8;
                String   tat  = has8 ? formatTat(f[6]) : "N/A";
                String   rdy  = has8 ? f[7] : "N/A";
                if (f.length >= 5) {
                    sb.append(String.format("  %-26s %-14s %-8s %-22s %s%n",
                        f[0], f[1] + " " + f[2], tat, rdy, f[4]));
                }
            }
        }

        sb.append("\n").append(SEP).append("\n");
        sb.append("This result is for laboratory purposes only.\n");
        return sb.toString();
    }

    // Convert stored hours integer to a human-readable label
    public static String formatTat(String hoursStr) {
        try {
            int h = Integer.parseInt(hoursStr.trim());
            if (h < 24) return h + (h == 1 ? " hr" : " hrs");
            int d = h / 24;
            return d + (d == 1 ? " day" : " days");
        } catch (NumberFormatException e) {
            return hoursStr;
        }
    }

    // Package-accessible helper: get a column safely
    static String get(String[] arr, int i) {
        return (arr != null && i < arr.length && arr[i] != null)
               ? arr[i].trim() : "";
    }

    // Escape pipe and test separators from user input
    private static String escape(String val) {
        if (val == null) return "";
        return val.replace("|", "/").replace(";;", ";");
    }
}