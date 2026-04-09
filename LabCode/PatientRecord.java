import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRecord {

    private static final String DATA_FILE  = "patient_records.csv";
    // Column separator — pipe avoids conflicts with commas in values
    private static final String COL_SEP    = "|";
    private static final String TEST_SEP   = ";;";
    private static final String FIELD_SEP  = "~";

    /*
     * CSV column layout (v2 format — 11 columns, index 0-10):
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
     *
     * Each test entry (FIELD_SEP-delimited, 8 fields):
     *   0  testName
     *   1  formattedResult
     *   2  unit
     *   3  referenceRange
     *   4  interpretation
     *   5  price
     *   6  turnaroundHours
     *   7  expectedReadyTime
     *
     * Old v1 format had 7 columns (0-6), tests at index 6 with 5 fields each.
     * Both formats are handled on load.
     */

    // ── Save ──────────────────────────────────────────────────────
    public static void save(ClinicalLabPatient p) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE, true))) {

            StringBuilder sb = new StringBuilder();
            sb.append(escape(p.getName()))            .append(COL_SEP)
              .append(p.getAge())                     .append(COL_SEP)
              .append(p.getSex())                     .append(COL_SEP)
              .append(escape(p.getTimeLastMeal()))    .append(COL_SEP)
              .append(p.getCollectionDate())           .append(COL_SEP)
              .append(p.getCollectionTime())           .append(COL_SEP)
              .append(escape(p.getPaymentMethod()))   .append(COL_SEP)
              .append(String.format("%.2f", p.getTotalAmount()))  .append(COL_SEP)
              .append(String.format("%.2f", p.getAmountPaid()))   .append(COL_SEP)
              .append(String.format("%.2f", p.getChange()))       .append(COL_SEP);

            List<String> entries = new ArrayList<>();
            for (ClinicalLabTest t : p.getCompletedTests()) {
                entries.add(
                    escape(t.getTestName())                             + FIELD_SEP +
                    t.getFormattedResult()                              + FIELD_SEP +
                    t.getUnit()                                         + FIELD_SEP +
                    t.getReferenceRange(p.getSex())                     + FIELD_SEP +
                    t.getInterpretation(p.getSex())                     + FIELD_SEP +
                    String.format("%.2f", t.getPrice())                 + FIELD_SEP +
                    t.getTurnaroundHours()                              + FIELD_SEP +
                    t.getExpectedReadyTime(p.getCollectionDateTime())
                );
            }
            sb.append(String.join(TEST_SEP, entries));
            pw.println(sb);

        } catch (IOException e) {
            System.err.println("Warning: Could not save record — " + e.getMessage());
        }
    }

    // ── Load All Raw Rows ─────────────────────────────────────────
    public static List<String[]> loadAll() {
        List<String[]> rows = new ArrayList<>();
        File file = new File(DATA_FILE);
        if (!file.exists()) return rows;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty())
                    rows.add(line.split("\\|", -1));
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load records — " + e.getMessage());
        }
        return rows;
    }

    // ── Table Rows for History JTable ─────────────────────────────
    // Returns: [Name, Age, Sex, Date, Time, "N test(s)", PaymentMethod]
    public static List<String[]> getTableRows() {
        List<String[]> result = new ArrayList<>();
        for (String[] cols : loadAll()) {
            if (cols.length < 6) continue;

            boolean newFmt = cols.length >= 11;
            int testCol = newFmt ? 10 : (cols.length >= 7 ? 6 : -1);
            int count   = 0;
            if (testCol >= 0 && testCol < cols.length
                    && !cols[testCol].trim().isEmpty()) {
                count = cols[testCol].split(";;").length;
            }
            String payment = newFmt ? get(cols, 6) : "N/A";

            result.add(new String[]{
                get(cols, 0), get(cols, 1), get(cols, 2),
                get(cols, 4), get(cols, 5),
                count + " test(s)", payment
            });
        }
        return result;
    }

    // ── Detailed Text for Swing History Viewer ────────────────────
    public static String getDetailedResult(int index) {
        List<String[]> all = loadAll();
        if (index < 0 || index >= all.size()) return "Record not found.";

        String[] c = all.get(index);
        boolean  newFmt  = c.length >= 11;
        int      testCol = newFmt ? 10 : (c.length >= 7 ? 6 : -1);

        StringBuilder sb = new StringBuilder();
        sb.append("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2557\n");
        sb.append("\u2551        NUCOMP DIAGNOSTIC CORPORATION                 \u2551\n");
        sb.append("\u2551        CLINICAL CHEMISTRY LABORATORY                 \u2551\n");
        sb.append("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u255D\n\n");
        sb.append("PATIENT INFORMATION\n");
        sb.append("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\n");
        sb.append(String.format("  Name      : %s%n", get(c, 0)));
        sb.append(String.format("  Age       : %s%n", get(c, 1)));
        sb.append(String.format("  Sex       : %s%n", get(c, 2)));
        sb.append(String.format("  Last Meal : %s%n", get(c, 3)));
        sb.append(String.format("  Date      : %s%n", get(c, 4)));
        sb.append(String.format("  Time      : %s%n", get(c, 5)));
        if (newFmt) {
            sb.append(String.format("  Payment   : %s%n", get(c, 6)));
            sb.append(String.format("  Total Due : PhP %s%n", get(c, 7)));
            sb.append(String.format("  Paid      : PhP %s%n", get(c, 8)));
            sb.append(String.format("  Change    : PhP %s%n", get(c, 9)));
        }

        sb.append("\nTEST RESULTS\n");
        sb.append("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\n");
        sb.append(String.format("  %-26s %-14s %-20s %-10s %s%n",
            "Test", "Result", "Reference", "TAT", "Interpretation"));
        sb.append("  \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\n");

        if (testCol >= 0 && testCol < c.length && !c[testCol].trim().isEmpty()) {
            for (String entry : c[testCol].split(";;")) {
                String[] f = entry.split("~", -1);
                boolean  nf = f.length >= 8;
                String   tatLabel = nf ? formatTat(f[6]) : "N/A";
                if (f.length >= 5) {
                    sb.append(String.format("  %-26s %-14s %-20s %-10s %s%n",
                        f[0],
                        f[1] + " " + f[2],
                        f[3],
                        tatLabel,
                        f[4]));
                    if (nf)
                        sb.append(String.format("    %-58s Ready: %s%n", "", f[7]));
                }
            }
        }

        sb.append("\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\n");
        sb.append("This result is for laboratory purposes only.\n");
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private static String get(String[] arr, int i) {
        return (arr != null && i < arr.length && arr[i] != null)
               ? arr[i].trim() : "N/A";
    }

    private static String escape(String val) {
        if (val == null) return "";
        // Replace pipe characters that would break the column split
        return val.replace("|", "/");
    }

    static String formatTat(String hoursStr) {
        try {
            int h = Integer.parseInt(hoursStr.trim());
            if (h < 24) return h + (h == 1 ? " hr" : " hrs");
            int d = h / 24;
            return d + (d == 1 ? " day" : " days");
        } catch (NumberFormatException e) {
            return hoursStr;
        }
    }
}
