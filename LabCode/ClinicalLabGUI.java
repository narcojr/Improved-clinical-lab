
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicalLabGUI extends JFrame {

    //  Color Palette
    static final Color NAVY       = new Color(15,  52,  96);
    static final Color DARK_NAVY  = new Color(8,   30,  63);
    static final Color GOLD       = new Color(212, 175, 55);
    static final Color LIGHT_GOLD = new Color(255, 245, 200);
    static final Color WHITE      = Color.WHITE;
    static final Color LIGHT_BLUE = new Color(230, 240, 255);
    static final Color ROW_ALT    = new Color(240, 246, 255);

    // Fonts
    static final Font F_TITLE    = new Font("Arial", Font.BOLD,  28);
    static final Font F_SUBTITLE = new Font("Arial", Font.BOLD,  13);
    static final Font F_LABEL    = new Font("Arial", Font.BOLD,  13);
    static final Font F_INPUT    = new Font("Arial", Font.PLAIN, 13);
    static final Font F_BUTTON   = new Font("Arial", Font.BOLD,  13);
    static final Font F_CHECK    = new Font("Arial", Font.PLAIN, 12);
    static final Font F_RANGE    = new Font("Arial", Font.ITALIC,11);
    static final Font F_MONO     = new Font("Monospaced", Font.PLAIN, 12);
    static final Font F_CAT      = new Font("Arial", Font.BOLD,  12);

    //  Patient Info Fields 
    private JTextField      nameField, ageField, lastMealField;
    private JTextField      dateField, timeField;
    private JComboBox<String> sexBox;

    //  Test Lists (category grouping)
    private final List<ClinicalLabTest> biochemTests  = new ArrayList<>();
    private final List<ClinicalLabTest> cardiacTests  = new ArrayList<>();
    private final List<ClinicalLabTest> lftTests      = new ArrayList<>();
    private final List<ClinicalLabTest> thyroidTests  = new ArrayList<>();
    private final List<ClinicalLabTest> allTests      = new ArrayList<>();

    // Parallel UI lists — index matches allTests exactly
    private final List<JCheckBox>  checkBoxes  = new ArrayList<>();
    private final List<JTextField> inputFields = new ArrayList<>();

    //  History Tab 
    private JTable            historyTable;
    private DefaultTableModel historyModel;

    // 
    public ClinicalLabGUI() {
        buildTests();
        buildFrame();
        loadHistory();
    }

    // TEST INITIALIZATION
    private void buildTests() {

        // ── Standard Biochemistry ─────────────────────────────
        biochemTests.add(new BiochemistryTest("FBS",               "mg/dL",   74,    100));
        biochemTests.add(new BiochemistryTest("RBS",               "mg/dL",   70,    140));
        biochemTests.add(new BiochemistryTest("Total Cholesterol",  "mg/dL",  150,    200));
        biochemTests.add(new BiochemistryTest("HDL",               "mg/dL",   35,     80,   42,   88));
        biochemTests.add(new BiochemistryTest("LDL",               "mg/dL",   50,    130));
        biochemTests.add(new BiochemistryTest("Triglycerides",     "mg/dL",   60,    165,   40,  140));
        biochemTests.add(new BiochemistryTest("Creatinine",        "mg/dL",    0.9,   1.3,   0.6,  1.2));
        biochemTests.add(new BiochemistryTest("Uric Acid",         "mg/dL",    3.5,   7.2,   2.6,  6.0));
        biochemTests.add(new BiochemistryTest("BUN",               "mg/dL",    6.0,  20.0));
        biochemTests.add(new BiochemistryTest("Sodium",            "mEq/L",  135,    145));
        biochemTests.add(new BiochemistryTest("Potassium",         "mEq/L",    3.5,   5.0));
        biochemTests.add(new BiochemistryTest("Chloride",          "mEq/L",   96,    110));
        biochemTests.add(new BiochemistryTest("Total Calcium",     "mg/dL",    8.6,  10.28));
        biochemTests.add(new BiochemistryTest("Ionized Calcium",   "mg/dL",    4.4,   5.2));
        biochemTests.add(new BiochemistryTest("HbA1c",             "%",        4.0,   5.6));   // NEW
        biochemTests.add(new BiochemistryTest("Vitamin D (25-OH)", "ng/mL",   30,    100));   // NEW
        biochemTests.add(new BiochemistryTest("Magnesium",         "mEq/L",    1.7,   2.2));   // NEW
        biochemTests.add(new BiochemistryTest("Phosphorus",        "mg/dL",    2.5,   4.5));   // NEW

        // ── Cardiac & Enzyme Markers ──────────────────────────
        cardiacTests.add(new CardiacMarkerTest("AST/SGOT",   "U/L",    46));
        cardiacTests.add(new CardiacMarkerTest("ALT/SGPT",   "U/L",    49));
        cardiacTests.add(new CardiacMarkerTest("Troponin I", "ng/mL",  0.04));  // NEW
        cardiacTests.add(new CardiacMarkerTest("CK-MB",      "ng/mL",  3.6));   // NEW
        cardiacTests.add(new BiochemistryTest( "LDH",        "U/L",  140,  280));// NEW
        cardiacTests.add(new CardiacMarkerTest("CRP",        "mg/L",   1.0));   // NEW

        // ── Liver Function Tests ──────────────────────────────
        lftTests.add(new LiverFunctionTest("Total Bilirubin",  "mg/dL", 0.2, 1.2));  // NEW
        lftTests.add(new LiverFunctionTest("Direct Bilirubin", "mg/dL", 0.0, 0.3));  // NEW
        lftTests.add(new LiverFunctionTest("Albumin",          "g/dL",  3.5, 5.0));  // NEW
        lftTests.add(new LiverFunctionTest("Total Protein",    "g/dL",  6.0, 8.3));  // NEW
        lftTests.add(new LiverFunctionTest("ALP",              "U/L",   44,  147));  // NEW

        // ── Thyroid Function Tests ─────────────────────────────
        thyroidTests.add(new ThyroidTest("TSH",     "mIU/L", 0.4, 4.0,  ThyroidTest.ThyroidMarker.TSH)); // NEW
        thyroidTests.add(new ThyroidTest("Free T3", "pg/mL", 2.3, 4.2,  ThyroidTest.ThyroidMarker.T3));  // NEW
        thyroidTests.add(new ThyroidTest("Free T4", "ng/dL", 0.8, 1.8,  ThyroidTest.ThyroidMarker.T4));  // NEW

        // Merge into one ordered list — order MUST match UI order below
        allTests.addAll(biochemTests);
        allTests.addAll(cardiacTests);
        allTests.addAll(lftTests);
        allTests.addAll(thyroidTests);
    }

    // ─────────────────────────────────────────────────────────
    // FRAME SETUP
    // ─────────────────────────────────────────────────────────
    private void buildFrame() {
        setTitle("NUCOMP Diagnostic Corporation — Clinical Chemistry System");
        setSize(1350, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTabs(),      BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    // HEADER PANEL
    // ─────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(DARK_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 25, 14, 25));

        JLabel company = new JLabel("NUCOMP DIAGNOSTIC CORPORATION");
        company.setFont(F_TITLE);
        company.setForeground(GOLD);

        JLabel sub = new JLabel(
            "Clinical Chemistry Laboratory System  |  " + LocalDate.now());
        sub.setFont(F_SUBTITLE);
        sub.setForeground(WHITE);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(DARK_NAVY);
        left.add(company);
        left.add(sub);

        // Live digital clock on the right
        JLabel clock = new JLabel(LocalTime.now().withNano(0).toString());
        clock.setFont(new Font("Arial", Font.BOLD, 22));
        clock.setForeground(LIGHT_GOLD);
        clock.setHorizontalAlignment(SwingConstants.RIGHT);
        new Timer(1000, e ->
            clock.setText(LocalTime.now().withNano(0).toString())
        ).start();

        header.add(left,  BorderLayout.WEST);
        header.add(clock, BorderLayout.EAST);
        return header;
    }

    // ─────────────────────────────────────────────────────────
    // TABBED PANE
    // ─────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("🧪  New Patient",      buildNewPatientTab());
        tabs.addTab("📋  Patient History",  buildHistoryTab());
        return tabs;
    }

    // ─────────────────────────────────────────────────────────
    // NEW PATIENT TAB
    // ─────────────────────────────────────────────────────────
    private JPanel buildNewPatientTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        tab.setBackground(LIGHT_BLUE);
        tab.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));
        tab.add(buildPatientInfoPanel(), BorderLayout.NORTH);
        tab.add(buildTestScrollPane(),   BorderLayout.CENTER);
        tab.add(buildActionButtons(),    BorderLayout.SOUTH);
        return tab;
    }

    // ── Patient Info Form ─────────────────────────────────────
    private JPanel buildPatientInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 10, 8));
        panel.setBackground(WHITE);
        panel.setBorder(styledBorder("  Patient Information  "));

        nameField     = inputField();
        ageField      = inputField();
        lastMealField = inputField();
        sexBox        = new JComboBox<>(new String[]{"MALE", "FEMALE"});
        sexBox.setFont(F_INPUT);

        dateField = inputField();
        dateField.setText(LocalDate.now().toString());
        dateField.setEditable(false);
        dateField.setBackground(LIGHT_GOLD);

        timeField = inputField();
        timeField.setText(LocalTime.now().withNano(0).toString());
        timeField.setEditable(false);
        timeField.setBackground(LIGHT_GOLD);

        // Keep time field updated every second
        new Timer(1000, e ->
            timeField.setText(LocalTime.now().withNano(0).toString())
        ).start();

        // Row 1
        panel.add(label("Patient Name:"));        panel.add(nameField);
        panel.add(label("Date of Collection:"));   panel.add(dateField);
        // Row 2
        panel.add(label("Age:"));                 panel.add(ageField);
        panel.add(label("Time of Collection:"));   panel.add(timeField);
        // Row 3
        panel.add(label("Patient Sex:"));          panel.add(sexBox);
        panel.add(label("Time of Last Meal:"));    panel.add(lastMealField);

        return panel;
    }

    // ── Test Selection — GridLayout with category headers ─────
    private JScrollPane buildTestScrollPane() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 0, 2));
        panel.setBackground(WHITE);
        panel.setBorder(styledBorder("  Clinical Chemistry Tests  "));

        checkBoxes.clear();
        inputFields.clear();

        // Column header row
        panel.add(navyCell("Test Name",                          GOLD));
        panel.add(navyCell("Result",                             GOLD));
        panel.add(navyCell("Reference Range (♂ Male / ♀ Female)", GOLD));

        // Add category sections — ORDER must match allTests list
        addCategory(panel, "📊  STANDARD BIOCHEMISTRY",         biochemTests);
        addCategory(panel, "❤   CARDIAC & ENZYME MARKERS",      cardiacTests);
        addCategory(panel, "🫀  LIVER FUNCTION TESTS (LFT)",    lftTests);
        addCategory(panel, "🦋  THYROID FUNCTION TESTS (TFT)",  thyroidTests);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // Adds a navy header row + alternating test rows to the grid
    private void addCategory(JPanel panel, String title,
                              List<ClinicalLabTest> tests) {
        // Category header — 3 cells wide (one per column)
        panel.add(navyCell("  " + title, WHITE));
        panel.add(navyCell("",           WHITE));
        panel.add(navyCell("",           WHITE));

        for (int i = 0; i < tests.size(); i++) {
            ClinicalLabTest test = tests.get(i);
            Color bg = (i % 2 == 0) ? WHITE : ROW_ALT;

            JCheckBox cb = new JCheckBox(test.getDisplayName());
            cb.setFont(F_CHECK);
            cb.setBackground(bg);
            cb.setOpaque(true);

            JTextField tf = new JTextField();
            tf.setFont(F_INPUT);
            tf.setBackground(bg);

            JLabel ref = new JLabel(
                "  ♂ " + test.getReferenceRange("MALE") +
                "     ♀ " + test.getReferenceRange("FEMALE"));
            ref.setFont(F_RANGE);
            ref.setForeground(new Color(40, 70, 130));
            ref.setOpaque(true);
            ref.setBackground(bg);

            panel.add(cb);
            panel.add(tf);
            panel.add(ref);

            checkBoxes.add(cb);   // index matches allTests
            inputFields.add(tf);
        }
    }

    // ── Action Buttons Bar ────────────────────────────────────
    private JPanel buildActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(NAVY);

        JButton genBtn   = button("⚗  Generate Result",   GOLD,       DARK_NAVY);
        JButton expBtn   = button("📊  Export to Excel",   WHITE,      NAVY);
        JButton clearBtn = button("🗑  Clear Form",         LIGHT_BLUE, NAVY);

        genBtn.addActionListener(  e -> generateResult());
        expBtn.addActionListener(  e -> quickExport());
        clearBtn.addActionListener(e -> clearForm());

        panel.add(genBtn);
        panel.add(expBtn);
        panel.add(clearBtn);
        return panel;
    }

    // ─────────────────────────────────────────────────────────
    // PATIENT HISTORY TAB
    // ─────────────────────────────────────────────────────────
    private JPanel buildHistoryTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        tab.setBackground(LIGHT_BLUE);
        tab.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));

        String[] cols = {"Patient Name", "Age", "Sex", "Date", "Time", "Tests"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(historyModel);
        historyTable.setFont(F_INPUT);
        historyTable.setRowHeight(26);
        historyTable.setSelectionBackground(GOLD);
        historyTable.setSelectionForeground(DARK_NAVY);
        historyTable.setGridColor(new Color(200, 215, 240));

        // Styled table header
        JTableHeader th = historyTable.getTableHeader();
        th.setFont(F_LABEL);
        th.setBackground(NAVY);
        th.setForeground(WHITE);

        // Alternating row colors
        historyTable.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(styledBorder("  Patient History  "));
        tab.add(scroll, BorderLayout.CENTER);

        // History action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(NAVY);

        JButton viewBtn    = button("🔍  View Details",     GOLD,       DARK_NAVY);
        JButton exportBtn  = button("📊  Export to Excel",  WHITE,      NAVY);
        JButton refreshBtn = button("🔄  Refresh",          LIGHT_BLUE, NAVY);

        viewBtn.addActionListener(   e -> viewHistoryDetail());
        exportBtn.addActionListener( e -> exportHistoryRecord());
        refreshBtn.addActionListener(e -> loadHistory());

        btnPanel.add(viewBtn);
        btnPanel.add(exportBtn);
        btnPanel.add(refreshBtn);
        tab.add(btnPanel, BorderLayout.SOUTH);

        return tab;
    }

    // ─────────────────────────────────────────────────────────
    // STATUS BAR
    // ─────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DARK_NAVY);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 15));

        JLabel left = new JLabel(
            "NUCOMP Clinical Chemistry Laboratory System  |  by COMSCI");
        left.setForeground(new Color(160, 190, 220));
        left.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel right = new JLabel("v2.0  |  " + LocalDate.now().getYear());
        right.setForeground(GOLD);
        right.setFont(new Font("Arial", Font.BOLD, 11));

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ═════════════════════════════════════════════════════════
    // BUSINESS LOGIC
    // ═════════════════════════════════════════════════════════

    private void generateResult() {
        try {
            String name   = nameField.getText().trim();
            String ageStr = ageField.getText().trim();

            if (name.isEmpty())   throw new Exception("Patient name is required.");
            if (ageStr.isEmpty()) throw new Exception("Patient age is required.");

            int    age    = Integer.parseInt(ageStr);
            String sex    = sexBox.getSelectedItem().toString();
            String meal   = lastMealField.getText().trim();

            ClinicalLabPatient patient =
                new ClinicalLabPatient(name, age, sex, meal);

            boolean anySelected = false;
            for (int i = 0; i < allTests.size(); i++) {
                if (!checkBoxes.get(i).isSelected()) continue;
                anySelected = true;

                String val = inputFields.get(i).getText().trim();
                if (val.isEmpty())
                    throw new Exception(
                        "Enter a result for: " + allTests.get(i).getTestName());

                allTests.get(i).setResult(Double.parseDouble(val));
                patient.addTest(allTests.get(i));
            }

            if (!anySelected)
                throw new Exception("Please select at least one test.");

            PatientRecord.save(patient);
            loadHistory();
            showResultDialog(patient);   // clears test objects after use

        } catch (NumberFormatException ex) {
            error("Age and test results must be numeric values.");
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    // Shows the result pop-up with an option to export to Excel
    private void showResultDialog(ClinicalLabPatient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════╗\n");
        sb.append("║         NUCOMP DIAGNOSTIC CORPORATION                    ║\n");
        sb.append("║         CLINICAL CHEMISTRY LABORATORY                    ║\n");
        sb.append("╚══════════════════════════════════════════════════════════╝\n\n");
        sb.append(patient).append("\n\n");
        sb.append("TEST RESULTS\n");
        sb.append("──────────────────────────────────────────────────────────\n");
        sb.append(String.format("  %-28s %-14s %-20s %s%n",
                "Test", "Result", "Reference", "Interpretation"));
        sb.append("  ────────────────────────────────────────────────────────\n");

        for (ClinicalLabTest t : patient.getCompletedTests()) {
            sb.append(String.format("  %-28s %-14s %-20s %s%n",
                    t.getTestName(),
                    t.getFormattedResult() + " " + t.getUnit(),
                    t.getReferenceRange(patient.getSex()),
                    t.getInterpretation(patient.getSex())));
        }

        sb.append("\n──────────────────────────────────────────────────────────\n");
        sb.append("This result is for laboratory purposes only.\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(F_MONO);
        area.setEditable(false);
        area.setBackground(new Color(250, 253, 255));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(740, 460));

        Object[] options = {"📊  Open in Excel", "Close"};
        int choice = JOptionPane.showOptionDialog(
                this, scroll, "Laboratory Result",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);

        // Export FIRST, then clear
        if (choice == 0) {
            try {
                ExcelExporter.exportAndOpen(patient);
            } catch (Exception ex) {
                error("Could not open in Excel:\n" + ex.getMessage());
            }
        }

        // Clear test values so objects are ready for the next patient
        allTests.forEach(ClinicalLabTest::clearResult);
    }

    // Direct export from form without showing the result dialog
    private void quickExport() {
        try {
            String name   = nameField.getText().trim();
            String ageStr = ageField.getText().trim();
            if (name.isEmpty() || ageStr.isEmpty())
                throw new Exception("Fill in patient name and age first.");

            int    age = Integer.parseInt(ageStr);
            String sex = sexBox.getSelectedItem().toString();

            ClinicalLabPatient patient =
                new ClinicalLabPatient(name, age, sex, lastMealField.getText().trim());

            boolean any = false;
            for (int i = 0; i < allTests.size(); i++) {
                if (!checkBoxes.get(i).isSelected()) continue;
                String val = inputFields.get(i).getText().trim();
                if (val.isEmpty()) continue;
                allTests.get(i).setResult(Double.parseDouble(val));
                patient.addTest(allTests.get(i));
                any = true;
            }

            if (!any) throw new Exception("No test results to export.");

            ExcelExporter.exportAndOpen(patient);
            allTests.forEach(ClinicalLabTest::clearResult);

        } catch (Exception ex) {
            error("Export failed: " + ex.getMessage());
        }
    }

    private void viewHistoryDetail() {
        int row = historyTable.getSelectedRow();
        if (row < 0) { error("Select a record from the history table."); return; }

        JTextArea area = new JTextArea(PatientRecord.getDetailedResult(row));
        area.setFont(F_MONO);
        area.setEditable(false);
        area.setBackground(new Color(250, 253, 255));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(740, 460));

        JOptionPane.showMessageDialog(this, scroll,
                "Patient Record Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportHistoryRecord() {
        int row = historyTable.getSelectedRow();
        if (row < 0) { error("Select a record to export."); return; }
        try {
            ExcelExporter.exportFromHistory(row);
        } catch (Exception ex) {
            error("Export failed: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        ageField.setText("");
        lastMealField.setText("");
        sexBox.setSelectedIndex(0);
        checkBoxes.forEach(cb -> cb.setSelected(false));
        inputFields.forEach(tf -> tf.setText(""));
        allTests.forEach(ClinicalLabTest::clearResult);
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        PatientRecord.getTableRows().forEach(historyModel::addRow);
    }

    // ═════════════════════════════════════════════════════════
    // UI FACTORY HELPERS
    // ═════════════════════════════════════════════════════════

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_LABEL);
        lbl.setForeground(DARK_NAVY);
        return lbl;
    }

    private JTextField inputField() {
        JTextField tf = new JTextField();
        tf.setFont(F_INPUT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170, 195, 235)),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)));
        return tf;
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(F_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color hover = bg.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // Creates a navy-colored label cell for column/category headers
    private JLabel navyCell(String text, Color fg) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(F_CAT);
        lbl.setForeground(fg);
        lbl.setOpaque(true);
        lbl.setBackground(NAVY);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        return lbl;
    }

    private Border styledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD, 2),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), NAVY);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(
                this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════
    // ENTRY POINT
    // ═════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ClinicalLabGUI();
        });
    }
}
