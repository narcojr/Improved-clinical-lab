import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRecord {

    private static final String DATA_FILE  = "patient_records.csv";
    private static final String COL_SEP    = "|";
    private static final String TEST_SEP   = ";;";
    private static final String FIELD_SEP  = "~";

    // ── Save one patient session (append to file) 
    public static void save(ClinicalLabPatient p) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE, true))) {

            StringBuilder sb = new StringBuilder();
            sb.append(p.getName()).append(COL_SEP)
              .append(p.getAge()).append(COL_SEP)
              .append(p.getSex()).append(COL_SEP)
              .append(p.getTimeLastMeal()).append(COL_SEP)
              .append(p.getCollectionDate()).append(COL_SEP)
              .append(p.getCollectionTime()).append(COL_SEP);

            List<String> entries = new ArrayList<>();
            for (ClinicalLabTest t : p.getCompletedTests()) {
                entries.add(
                    t.getTestName()                       + FIELD_SEP +
                    t.getFormattedResult()                + FIELD_SEP +
                    t.getUnit()                           + FIELD_SEP +
                    t.getReferenceRange(p.getSex())       + FIELD_SEP +
                    t.getInterpretation(p.getSex())
                );
            }
            sb.append(String.join(TEST_SEP, entries));
            pw.println(sb);

        } catch (IOException e) {
            System.err.println("Warning: Could not save record — " + e.getMessage());
        }
    }

    // ── Load every raw row from file ──────────────────────────
    public static List<String[]> loadAll() {
        List<String[]> rows = new ArrayList<>();
        File file = new File(DATA_FILE);
        if (!file.exists()) return rows;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(line.split("\\|", -1));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load records — " + e.getMessage());
        }
        return rows;
    }

    // ── Rows formatted for the history JTable ─────────────────
    // Returns: [Name, Age, Sex, Date, Time, "N test(s)"]
    public static List<String[]> getTableRows() {
        List<String[]> result = new ArrayList<>();
        for (String[] cols : loadAll()) {
            if (cols.length < 6) continue;
            int count = 0;
            if (cols.length > 6 && !cols[6].trim().isEmpty()) {
                count = cols[6].split(";;").length;
            }
            result.add(new String[]{
                cols[0], cols[1], cols[2], cols[4], cols[5], count + " test(s)"
            });
        }
        return result;
    }

    // ── Detailed formatted text for one record by index ───────
    public static String getDetailedResult(int index) {
        List<String[]> all = loadAll();
        if (index < 0 || index >= all.size()) return "Record not found.";

        String[] c = all.get(index);
        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════════╗\n");
        sb.append("║        NUCOMP DIAGNOSTIC CORPORATION                 ║\n");
        sb.append("║        CLINICAL CHEMISTRY LABORATORY                 ║\n");
        sb.append("╚══════════════════════════════════════════════════════╝\n\n");
        sb.append("PATIENT INFORMATION\n");
        sb.append("──────────────────────────────────────────────────────\n");
        sb.append(String.format("  Name      : %s%n", get(c, 0)));
        sb.append(String.format("  Age       : %s%n", get(c, 1)));
        sb.append(String.format("  Sex       : %s%n", get(c, 2)));
        sb.append(String.format("  Last Meal : %s%n", get(c, 3)));
        sb.append(String.format("  Date      : %s%n", get(c, 4)));
        sb.append(String.format("  Time      : %s%n", get(c, 5)));
        sb.append("\nTEST RESULTS\n");
        sb.append("──────────────────────────────────────────────────────\n");
        sb.append(String.format("  %-28s %-12s %-20s %s%n",
                "Test", "Result", "Reference", "Interpretation"));
        sb.append("  ────────────────────────────────────────────────────\n");

        if (c.length > 6 && !c[6].trim().isEmpty()) {
            for (String entry : c[6].split(";;")) {
                String[] p = entry.split("~", -1);
                if (p.length >= 5) {
                    sb.append(String.format("  %-28s %-12s %-20s %s%n",
                            p[0], p[1] + " " + p[2], p[3], p[4]));
                }
            }
        }

        sb.append("\n──────────────────────────────────────────────────────\n");
        sb.append("This result is for laboratory purposes only.\n");
        return sb.toString();
    }

    private static String get(String[] arr, int i) {
        return (arr != null && i < arr.length) ? arr[i] : "";
    }
}
