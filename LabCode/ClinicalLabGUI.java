import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClinicalLabGUI extends JFrame {

    // ── Color Palette ──────────────────────────────────────────────
    static final Color NAVY       = new Color(15,  52,  96);
    static final Color DARK_NAVY  = new Color(8,   30,  63);
    static final Color GOLD       = new Color(212, 175, 55);
    static final Color LIGHT_GOLD = new Color(255, 245, 200);
    static final Color WHITE      = Color.WHITE;
    static final Color LIGHT_BLUE = new Color(230, 240, 255);
    static final Color ROW_ALT    = new Color(240, 246, 255);
    static final Color WARN_RED   = new Color(220,  60,  60);
    static final Color WARN_BG    = new Color(255, 235, 235);

    // ── Fonts ──────────────────────────────────────────────────────
    static final Font F_TITLE    = new Font("Arial", Font.BOLD,  28);
    static final Font F_SUBTITLE = new Font("Arial", Font.BOLD,  13);
    static final Font F_LABEL    = new Font("Arial", Font.BOLD,  13);
    static final Font F_INPUT    = new Font("Arial", Font.PLAIN, 13);
    static final Font F_BUTTON   = new Font("Arial", Font.BOLD,  13);
    static final Font F_CHECK    = new Font("Arial", Font.PLAIN, 12);
    static final Font F_RANGE    = new Font("Arial", Font.ITALIC,11);
    static final Font F_MONO     = new Font("Monospaced", Font.PLAIN, 12);
    static final Font F_CAT      = new Font("Arial", Font.BOLD,  12);

    // ── Patient Info Fields ────────────────────────────────────────
    private JTextField        nameField, ageField, lastMealField;
    private JTextField        dateField, timeField;
    private JComboBox<String> sexBox;

    // ── Test Lists ─────────────────────────────────────────────────
    private final List<ClinicalLabTest> biochemTests  = new ArrayList<>();
    private final List<ClinicalLabTest> cardiacTests  = new ArrayList<>();
    private final List<ClinicalLabTest> lftTests      = new ArrayList<>();
    private final List<ClinicalLabTest> thyroidTests  = new ArrayList<>();
    private final List<ClinicalLabTest> allTests      = new ArrayList<>();

    private final List<JCheckBox>  checkBoxes  = new ArrayList<>();
    private final List<JTextField> inputFields = new ArrayList<>();

    // ── Mutual-exclusion indices (FBS ↔ RBS) ──────────────────────
    private int idxFBS = -1;
    private int idxRBS = -1;

    // ── History Tab ────────────────────────────────────────────────
    private JTable            historyTable;
    private DefaultTableModel historyModel;

    // ──────────────────────────────────────────────────────────────
    public ClinicalLabGUI() {
        buildTests();
        buildFrame();
        loadHistory();
    }

    // ══════════════════════════════════════════════════════════════
    // TEST INITIALIZATION  (price = PhP, tat = hours)
    // ══════════════════════════════════════════════════════════════
    private void buildTests() {

        // ── Standard Biochemistry ──────────────────────────────────
        biochemTests.add(bio("FBS",               "mg/dL",  74,   100,         150,  2));
        biochemTests.add(bio("RBS",               "mg/dL",  70,   140,         150,  2));
        biochemTests.add(bio("Total Cholesterol",  "mg/dL", 150,  200,         250,  4));
        biochemTests.add(bioSex("HDL","mg/dL",35,80,42,88,                     300,  4));
        biochemTests.add(bio("LDL",               "mg/dL",  50,   130,         300,  4));
        biochemTests.add(bioSex("Triglycerides","mg/dL",60,165,40,140,         300,  4));
        biochemTests.add(bioSex("Creatinine","mg/dL",0.9,1.3,0.6,1.2,         180,  2));
        biochemTests.add(bioSex("Uric Acid","mg/dL",3.5,7.2,2.6,6.0,          180,  2));
        biochemTests.add(bio("BUN",               "mg/dL",   6.0, 20.0,        180,  2));
        biochemTests.add(bio("Sodium",            "mEq/L", 135,   145,         250,  4));
        biochemTests.add(bio("Potassium",         "mEq/L",   3.5,  5.0,        250,  4));
        biochemTests.add(bio("Chloride",          "mEq/L",  96,   110,         250,  4));
        biochemTests.add(bio("Total Calcium",     "mg/dL",   8.6, 10.28,       250,  4));
        biochemTests.add(bio("Ionized Calcium",   "mg/dL",   4.4,  5.2,        280,  4));
        biochemTests.add(bio("HbA1c",             "%",        4.0,  5.6,        600, 24));
        biochemTests.add(bio("Vitamin D (25-OH)", "ng/mL",  30,   100,        1800, 48));
        biochemTests.add(bio("Magnesium",         "mEq/L",   1.7,  2.2,        250,  4));
        biochemTests.add(bio("Phosphorus",        "mg/dL",   2.5,  4.5,        250,  4));

        // ── Cardiac & Enzyme Markers ───────────────────────────────
        cardiacTests.add(cardiac("AST/SGOT",   "U/L",   46,     250,  4));
        cardiacTests.add(cardiac("ALT/SGPT",   "U/L",   49,     250,  4));
        cardiacTests.add(cardiac("Troponin I", "ng/mL",  0.04,  950,  1));
        cardiacTests.add(cardiac("CK-MB",      "ng/mL",  3.6,   750,  2));
        cardiacTests.add(bio(    "LDH",        "U/L",  140, 280,      350,  4));
        cardiacTests.add(cardiac("CRP",        "mg/L",   1.0,   600,  4));

        // ── Liver Function Tests ───────────────────────────────────
        lftTests.add(lft("Total Bilirubin",  "mg/dL", 0.2, 1.2,  250, 4));
        lftTests.add(lft("Direct Bilirubin", "mg/dL", 0.0, 0.3,  250, 4));
        lftTests.add(lft("Albumin",          "g/dL",  3.5, 5.0,  250, 4));
        lftTests.add(lft("Total Protein",    "g/dL",  6.0, 8.3,  250, 4));
        lftTests.add(lft("ALP",              "U/L",  44,  147,   300, 4));

        // ── Thyroid Function Tests ─────────────────────────────────
        thyroidTests.add(tft("TSH",     "mIU/L", 0.4, 4.0, ThyroidTest.ThyroidMarker.TSH, 700, 24));
        thyroidTests.add(tft("Free T3", "pg/mL", 2.3, 4.2, ThyroidTest.ThyroidMarker.T3,  700, 24));
        thyroidTests.add(tft("Free T4", "ng/dL", 0.8, 1.8, ThyroidTest.ThyroidMarker.T4,  700, 24));

        allTests.addAll(biochemTests);
        allTests.addAll(cardiacTests);
        allTests.addAll(lftTests);
        allTests.addAll(thyroidTests);

        // Locate FBS / RBS for mutual exclusion
        for (int i = 0; i < allTests.size(); i++) {
            String n = allTests.get(i).getTestName();
            if ("FBS".equals(n)) idxFBS = i;
            if ("RBS".equals(n)) idxRBS = i;
        }
    }

    // ── Test factory helpers ───────────────────────────────────────
    private ClinicalLabTest bio(String n, String u,
                                 double min, double max,
                                 double price, int tat) {
        BiochemistryTest t = new BiochemistryTest(n, u, min, max);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }

    private ClinicalLabTest bioSex(String n, String u,
                                    double mMin, double mMax,
                                    double fMin, double fMax,
                                    double price, int tat) {
        BiochemistryTest t = new BiochemistryTest(n, u, mMin, mMax, fMin, fMax);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }

    private ClinicalLabTest cardiac(String n, String u,
                                     double threshold,
                                     double price, int tat) {
        CardiacMarkerTest t = new CardiacMarkerTest(n, u, threshold);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }

    private ClinicalLabTest lft(String n, String u,
                                  double min, double max,
                                  double price, int tat) {
        LiverFunctionTest t = new LiverFunctionTest(n, u, min, max);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }

    private ClinicalLabTest tft(String n, String u,
                                  double min, double max,
                                  ThyroidTest.ThyroidMarker marker,
                                  double price, int tat) {
        ThyroidTest t = new ThyroidTest(n, u, min, max, marker);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }

    // ══════════════════════════════════════════════════════════════
    // FRAME SETUP
    // ══════════════════════════════════════════════════════════════
    private void buildFrame() {
        setTitle("NUCOMP Diagnostic Corporation \u Clinical Chemistry System");
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

    // ══════════════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout(10, 0));
        hdr.setBackground(DARK_NAVY);
        hdr.setBorder(BorderFactory.createEmptyBorder(14, 25, 14, 25));

        JLabel company = new JLabel("NU BACOLOD DIAGNOSTIC CORPORATION");
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

        JLabel clock = new JLabel(LocalTime.now().withNano(0).toString(),
                                  SwingConstants.RIGHT);
        clock.setFont(new Font("Arial", Font.BOLD, 22));
        clock.setForeground(LIGHT_GOLD);

        Timer clockTimer = new Timer(1000,
            e -> clock.setText(LocalTime.now().withNano(0).toString()));
        clockTimer.setInitialDelay(0);
        clockTimer.start();

        hdr.add(left,  BorderLayout.WEST);
        hdr.add(clock, BorderLayout.EAST);
        return hdr;
    }

    // ══════════════════════════════════════════════════════════════
    // TABS
    // ══════════════════════════════════════════════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("\uD83E\uDDEA  New Patient",     buildNewPatientTab());
        tabs.addTab("\uD83D\uDCCB  Patient History", buildHistoryTab());
        return tabs;
    }

    // ══════════════════════════════════════════════════════════════
    // NEW PATIENT TAB
    // ══════════════════════════════════════════════════════════════
    private JPanel buildNewPatientTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        tab.setBackground(LIGHT_BLUE);
        tab.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));
        tab.add(buildPatientInfoPanel(), BorderLayout.NORTH);
        tab.add(buildTestScrollPane(),   BorderLayout.CENTER);
        tab.add(buildActionButtons(),    BorderLayout.SOUTH);
        return tab;
    }

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

        Timer tmr = new Timer(1000,
            e -> timeField.setText(LocalTime.now().withNano(0).toString()));
        tmr.setInitialDelay(0);
        tmr.start();

        panel.add(label("Patient Name:"));       panel.add(nameField);
        panel.add(label("Date of Collection:")); panel.add(dateField);
        panel.add(label("Age:"));                panel.add(ageField);
        panel.add(label("Time of Collection:")); panel.add(timeField);
        panel.add(label("Patient Sex:"));        panel.add(sexBox);
        panel.add(label("Time of Last Meal:"));  panel.add(lastMealField);
        return panel;
    }

    private JScrollPane buildTestScrollPane() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 0, 2));
        panel.setBackground(WHITE);
        panel.setBorder(styledBorder("  Clinical Chemistry Tests  "));

        checkBoxes.clear();
        inputFields.clear();

        panel.add(navyCell("Test Name",                           GOLD));
        panel.add(navyCell("Result",                              GOLD));
        panel.add(navyCell("Reference Range (\u2642 Male / \u2640 Female)", GOLD));

        // FBS ↔ RBS mutual-exclusion banner
        JLabel notice = new JLabel(
            "  \u26A0  FBS and RBS are mutually exclusive \u2014 selecting one deselects the other.");
        notice.setFont(new Font("Arial", Font.ITALIC, 11));
        notice.setForeground(WARN_RED);
        notice.setOpaque(true);
        notice.setBackground(WARN_BG);
        notice.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        JPanel noticeWrap = new JPanel(new BorderLayout());
        noticeWrap.setBackground(WARN_BG);
        noticeWrap.add(notice, BorderLayout.WEST);
        panel.add(noticeWrap);
        panel.add(filler(WARN_BG));
        panel.add(filler(WARN_BG));

        addCategory(panel, "\uD83D\uDCCA  STANDARD BIOCHEMISTRY",       biochemTests);
        addCategory(panel, "\u2764   CARDIAC & ENZYME MARKERS",          cardiacTests);
        addCategory(panel, "\uD83E\uDE80  LIVER FUNCTION TESTS (LFT)",  lftTests);
        addCategory(panel, "\uD83E\uDD8B  THYROID FUNCTION TESTS (TFT)",thyroidTests);

        wireMutualExclusion();

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /**
     * FBS (Fasting Blood Sugar) and RBS (Random Blood Sugar) are mutually
     * exclusive: FBS requires 8-12 h fasting while RBS is a non-fasting
     * random sample.
     */
    private void wireMutualExclusion() {
        if (idxFBS < 0 || idxRBS < 0) return;
        JCheckBox cbFBS = checkBoxes.get(idxFBS);
        JCheckBox cbRBS = checkBoxes.get(idxRBS);

        cbFBS.addItemListener(e -> {
            if (cbFBS.isSelected() && cbRBS.isSelected()) {
                cbRBS.setSelected(false);
                inputFields.get(idxRBS).setText("");
                flashRow(cbRBS, inputFields.get(idxRBS));
            }
        });
        cbRBS.addItemListener(e -> {
            if (cbRBS.isSelected() && cbFBS.isSelected()) {
                cbFBS.setSelected(false);
                inputFields.get(idxFBS).setText("");
                flashRow(cbFBS, inputFields.get(idxFBS));
            }
        });
    }

    private void flashRow(JCheckBox cb, JTextField tf) {
        cb.setBackground(WARN_BG);
        tf.setBackground(WARN_BG);
        Timer t = new Timer(1200, e -> {
            cb.setBackground(WHITE);
            tf.setBackground(WHITE);
        });
        t.setRepeats(false);
        t.start();
    }

    private void addCategory(JPanel panel, String title,
                              List<ClinicalLabTest> tests) {
        panel.add(navyCell("  " + title, WHITE));
        panel.add(navyCell("", WHITE));
        panel.add(navyCell("", WHITE));

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
                "  \u2642 " + test.getReferenceRange("MALE") +
                "     \u2640 " + test.getReferenceRange("FEMALE") +
                "     |  TAT: " + test.getTurnaroundLabel() +
                "   |  PhP " + String.format("%,.0f", test.getPrice()));
            ref.setFont(F_RANGE);
            ref.setForeground(new Color(40, 70, 130));
            ref.setOpaque(true);
            ref.setBackground(bg);

            panel.add(cb);
            panel.add(tf);
            panel.add(ref);
            checkBoxes.add(cb);
            inputFields.add(tf);
        }
    }

    private JPanel buildActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(NAVY);

        JButton genBtn   = button("\u2697  Generate Result",  GOLD,       DARK_NAVY);
        JButton pdfBtn   = button("\uD83D\uDCC4  Quick PDF",   WHITE,      NAVY);
        JButton clearBtn = button("\uD83D\uDDD1  Clear Form",  LIGHT_BLUE, NAVY);

        genBtn.addActionListener(  e -> generateResult());
        pdfBtn.addActionListener(  e -> quickPdf());
        clearBtn.addActionListener(e -> clearForm());

        panel.add(genBtn);
        panel.add(pdfBtn);
        panel.add(clearBtn);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════
    // HISTORY TAB
    // PATIENT HISTORY
    // ══════════════════════════════════════════════════════════════
    private JPanel buildHistoryTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        tab.setBackground(LIGHT_BLUE);
        tab.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));

        String[] cols = {"Patient Name","Age","Sex","Date","Time","Tests","Payment"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(historyModel);
        historyTable.setFont(F_INPUT);
        historyTable.setRowHeight(26);
        historyTable.setSelectionBackground(GOLD);
        historyTable.setSelectionForeground(DARK_NAVY);
        historyTable.setGridColor(new Color(200, 215, 240));

        JTableHeader th = historyTable.getTableHeader();
        th.setFont(F_LABEL);
        th.setBackground(NAVY);
        th.setForeground(WHITE);

        historyTable.setDefaultRenderer(Object.class,
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                    if (!sel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                    return c;
                }
            });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(styledBorder("  Patient History  "));
        tab.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(NAVY);

        JButton viewBtn    = button("\uD83D\uDD0D  View Details",   GOLD,       DARK_NAVY);
        JButton pdfBtn     = button("\uD83D\uDCC4  Export PDF",     WHITE,      NAVY);
        JButton refreshBtn = button("\uD83D\uDD04  Refresh",        LIGHT_BLUE, NAVY);

        viewBtn.addActionListener(   e -> viewHistoryDetail());
        pdfBtn.addActionListener(    e -> exportHistoryPdf());
        refreshBtn.addActionListener(e -> loadHistory());

        btnPanel.add(viewBtn);
        btnPanel.add(pdfBtn);
        btnPanel.add(refreshBtn);
        tab.add(btnPanel, BorderLayout.SOUTH);
        return tab;
    }

    // ══════════════════════════════════════════════════════════════
    // STATUS BAR
    // ══════════════════════════════════════════════════════════════
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DARK_NAVY);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 15));

        JLabel left = new JLabel(
            "NUCOMP Clinical Chemistry Laboratory System  |  NARCO VILLANDO JR.");
        left.setForeground(new Color(160, 190, 220));
        left.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel right = new JLabel("v3.0  |  " + LocalDate.now().getYear());
        right.setForeground(GOLD);
        right.setFont(new Font("Arial", Font.BOLD, 11));

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    // BUSINESS LOGIC
    // ══════════════════════════════════════════════════════════════

    private void generateResult() {
        try {
            // ── 1. Validate patient info ───────────────────────────
            String name   = nameField.getText().trim();
            String ageStr = ageField.getText().trim();
            if (name.isEmpty())
                throw new Exception("Patient name is required.");
            if (ageStr.isEmpty())
                throw new Exception("Patient age is required.");

            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 0 || age > 150)
                    throw new Exception("Age must be between 0 and 150.");
            } catch (NumberFormatException ex) {
                throw new Exception("Age must be a whole number.");
            }

            String sex  = sexBox.getSelectedItem().toString();
            String meal = lastMealField.getText().trim();

            // ── 2. Validate test selections ────────────────────────
            boolean anySelected = false;
            for (int i = 0; i < allTests.size(); i++) {
                if (!checkBoxes.get(i).isSelected()) continue;
                anySelected = true;
                String val = inputFields.get(i).getText().trim();
                if (val.isEmpty())
                    throw new Exception(
                        "Enter a result for: " + allTests.get(i).getTestName());
                try {
                    double d = Double.parseDouble(val);
                    if (d < 0)
                        throw new Exception(
                            "Result cannot be negative for: "
                            + allTests.get(i).getTestName());
                } catch (NumberFormatException ex) {
                    throw new Exception(
                        "Result must be numeric for: "
                        + allTests.get(i).getTestName());
                }
            }
            if (!anySelected)
                throw new Exception("Please select at least one test.");

            // ── 3. Build patient + add tests ───────────────────────
            ClinicalLabPatient patient =
                new ClinicalLabPatient(name, age, sex, meal);
            for (int i = 0; i < allTests.size(); i++) {
                if (!checkBoxes.get(i).isSelected()) continue;
                allTests.get(i).setResult(
                    Double.parseDouble(inputFields.get(i).getText().trim()));
                patient.addTest(allTests.get(i));
            }

            // ── 4. Payment dialog ──────────────────────────────────
            double subtotal = patient.getSubtotal();
            if (!showPaymentDialog(patient, subtotal)) {
                allTests.forEach(ClinicalLabTest::clearResult);
                return; // user cancelled
            }

            // ── 5. Save + show result ──────────────────────────────
            PatientRecord.save(patient);
            loadHistory();
            showResultDialog(patient);
            allTests.forEach(ClinicalLabTest::clearResult);

        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    // ── Payment Dialog ─────────────────────────────────────────────
    private boolean showPaymentDialog(ClinicalLabPatient patient, double subtotal) {
        final boolean[] confirmed = {false};
        final double[]  totalDue  = {subtotal};

        JDialog dlg = new JDialog(this, "Payment", true);
        dlg.setSize(500, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(DARK_NAVY);
        hdr.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel htitle = new JLabel(
            "\uD83D\uDCB3  PAYMENT  \u2014  " + patient.getName().toUpperCase());
        htitle.setFont(new Font("Arial", Font.BOLD, 14));
        htitle.setForeground(GOLD);
        hdr.add(htitle);
        dlg.add(hdr, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(12, 18, 8, 18));

        // Test items grid and prices
        content.add(sectionLabel("  TEST ITEMS  &  PRICES"));
        content.add(Box.createVerticalStrut(6));

        JPanel items = new JPanel(new GridLayout(0, 2, 0, 1));
        items.setBackground(WHITE);

        JLabel ih1 = hdrCell("  Test");
        JLabel ih2 = hdrCell("Price (PhP)  ");
        ih2.setHorizontalAlignment(SwingConstants.RIGHT);
        items.add(ih1);
        items.add(ih2);

        boolean alt = false;
        for (ClinicalLabTest t : patient.getCompletedTests()) {
            Color bg = alt ? ROW_ALT : WHITE;
            JLabel nl = new JLabel("  " + t.getTestName());
            nl.setFont(new Font("Arial", Font.PLAIN, 11));
            nl.setOpaque(true); nl.setBackground(bg);
            nl.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

            JLabel pl = new JLabel(
                String.format("%,.2f  ", t.getPrice()),
                SwingConstants.RIGHT);
            pl.setFont(new Font("Arial", Font.PLAIN, 11));
            pl.setOpaque(true); pl.setBackground(bg);
            pl.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

            items.add(nl);
            items.add(pl);
            alt = !alt;
        }

        // Subtotal row
        JLabel stL = new JLabel("  SUBTOTAL");
        stL.setFont(new Font("Arial", Font.BOLD, 11));
        stL.setOpaque(true); stL.setBackground(LIGHT_GOLD);
        stL.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        items.add(stL);

        JLabel stA = new JLabel(String.format("%,.2f  ", subtotal), SwingConstants.RIGHT);
        stA.setFont(new Font("Arial", Font.BOLD, 11));
        stA.setOpaque(true); stA.setBackground(LIGHT_GOLD);
        stA.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        items.add(stA);

        JScrollPane itemScroll = new JScrollPane(items);
        itemScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        itemScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 180));
        content.add(itemScroll);
        content.add(Box.createVerticalStrut(14));

        // Payment options
        content.add(sectionLabel("  PAYMENT OPTIONS"));
        content.add(Box.createVerticalStrut(8));

        JPanel payGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        payGrid.setBackground(WHITE);
        payGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        String[] methods = {"CASH", "GCASH", "MAYA", "PHILHEALTH"};
        JComboBox<String> methodBox = new JComboBox<>(methods);
        methodBox.setFont(F_INPUT);
        payGrid.add(label("Payment Method:"));
        payGrid.add(methodBox);

        JLabel philNote = new JLabel("  \u2714 PhilHealth Subsidy: \u2212 PhP 500.00");
        philNote.setFont(new Font("Arial", Font.ITALIC, 11));
        philNote.setForeground(new Color(0, 128, 0));
        philNote.setVisible(false);
        payGrid.add(philNote);
        payGrid.add(new JLabel());

        JLabel totalDueLabel = new JLabel(
            String.format("  TOTAL DUE:  PhP %,.2f", subtotal));
        totalDueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalDueLabel.setForeground(DARK_NAVY);
        payGrid.add(totalDueLabel);
        payGrid.add(new JLabel());

        payGrid.add(label("Amount Tendered (PhP):"));
        JTextField tenderField = new JTextField("0.00");
        tenderField.setFont(F_INPUT);
        payGrid.add(tenderField);

        payGrid.add(label("Change:"));
        JLabel changeAmt = new JLabel("PhP 0.00");
        changeAmt.setFont(new Font("Arial", Font.BOLD, 13));
        changeAmt.setForeground(new Color(0, 110, 0));
        payGrid.add(changeAmt);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("Arial", Font.ITALIC, 11));
        errLbl.setForeground(WARN_RED);
        payGrid.add(errLbl);
        payGrid.add(new JLabel());

        content.add(payGrid);
        dlg.add(new JScrollPane(content), BorderLayout.CENTER);

        // ── Dynamic update logic ───────────────────────────────────
        Runnable update = () -> {
            String  method = (String) methodBox.getSelectedItem();
            boolean isPhil = "PHILHEALTH".equals(method);
            boolean isCash = "CASH".equals(method);
            double  due    = isPhil ? Math.max(0, subtotal - 500) : subtotal;
            totalDue[0]    = due;
            totalDueLabel.setText(String.format("  TOTAL DUE:  PhP %,.2f", due));
            philNote.setVisible(isPhil);

            if (isCash) {
                tenderField.setEnabled(true);
                try {
                    double tendered = Double.parseDouble(
                        tenderField.getText().trim().replace(",", ""));
                    double chg = tendered - due;
                    changeAmt.setText(String.format("PhP %,.2f", Math.max(0, chg)));
                    changeAmt.setForeground(
                        chg >= 0 ? new Color(0, 110, 0) : WARN_RED);
                } catch (NumberFormatException ex) {
                    changeAmt.setText("PhP 0.00");
                }
            } else {
                tenderField.setText(String.format("%.2f", due));
                tenderField.setEnabled(false);
                changeAmt.setText("PhP 0.00");
                changeAmt.setForeground(new Color(0, 110, 0));
            }
        };

        methodBox.addActionListener(e -> update.run());

        tenderField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { update.run(); }
            public void removeUpdate(DocumentEvent e)  { update.run(); }
            public void changedUpdate(DocumentEvent e) { update.run(); }
        });

        update.run(); // initialize

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        btnPanel.setBackground(NAVY);

        JButton cancelBtn  = button("\u2716  Cancel",          LIGHT_BLUE, NAVY);
        JButton confirmBtn = button("\u2714  Confirm Payment",  GOLD,       DARK_NAVY);

        cancelBtn.addActionListener(e -> dlg.dispose());

        confirmBtn.addActionListener(e -> {
            try {
                errLbl.setText(" ");
                String method   = (String) methodBox.getSelectedItem();
                double due      = totalDue[0];
                double tendered;

                if ("CASH".equals(method)) {
                    String raw = tenderField.getText().trim().replace(",", "");
                    if (raw.isEmpty())
                        throw new Exception("Please enter the amount tendered.");
                    try {
                        tendered = Double.parseDouble(raw);
                    } catch (NumberFormatException ex) {
                        throw new Exception("Amount tendered must be a valid number.");
                    }
                    if (tendered < 0)
                        throw new Exception("Amount tendered cannot be negative.");
                    if (tendered < due)
                        throw new Exception(String.format(
                            "Tendered (PhP %.2f) is less than total due (PhP %.2f).",
                            tendered, due));
                } else {
                    tendered = due;
                }

                patient.setPaymentMethod(method);
                patient.setTotalAmount(due);
                patient.setAmountPaid(tendered);
                patient.setChange(tendered - due);

                confirmed[0] = true;
                dlg.dispose();

            } catch (Exception ex) {
                errLbl.setText("  \u26A0 " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true); // blocks until disposed
        return confirmed[0];
    }

    // ── Result Preview Dialog ──────────────────────────────────────
    private void showResultDialog(ClinicalLabPatient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2557\n");
        sb.append("\u2551       NUCOMP DIAGNOSTIC CORPORATION                      \u2551\n");
        sb.append("\u2551       CLINICAL CHEMISTRY LABORATORY                      \u2551\n");
        sb.append("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550" +
                  "\u2550\u2550\u255D\n\n");
        sb.append(patient).append("\n");
        sb.append(String.format("Payment       : %s%n", patient.getPaymentMethod()));
        sb.append(String.format("Total Due     : PhP %,.2f%n", patient.getTotalAmount()));
        sb.append(String.format("Amount Paid   : PhP %,.2f%n", patient.getAmountPaid()));
        sb.append(String.format("Change        : PhP %,.2f%n", patient.getChange()));
        sb.append("\nTEST RESULTS\n");
        sb.append("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\n");
        sb.append(String.format("  %-26s %-14s %-8s %-22s %s%n",
            "Test","Result","TAT","Expected Ready","Interpretation"));
        sb.append("  \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\n");

        for (ClinicalLabTest t : patient.getCompletedTests()) {
            sb.append(String.format("  %-26s %-14s %-8s %-22s %s%n",
                t.getTestName(),
                t.getFormattedResult() + " " + t.getUnit(),
                t.getTurnaroundLabel(),
                t.getExpectedReadyTime(patient.getCollectionDateTime()),
                t.getInterpretation(patient.getSex())));
        }

        sb.append("\n\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
                  "\u2500\u2500\n");
        sb.append("This result is for laboratory purposes only.\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(F_MONO);
        area.setEditable(false);
        area.setBackground(new Color(250, 253, 255));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(800, 480));

        Object[] opts = {"\uD83D\uDCC4  Save as PDF", "Close"};
        int choice = JOptionPane.showOptionDialog(
            this, scroll, "Laboratory Result",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null, opts, opts[1]);

        if (choice == 0) {
            try {
                PdfExporter.exportAndOpen(patient);
            } catch (Exception ex) {
                error("PDF export failed:\n" + ex.getMessage());
            }
        }
    }

    // ── Quick PDF (without re-showing result dialog) ───────────────
    private void quickPdf() {
        try {
            String name   = nameField.getText().trim();
            String ageStr = ageField.getText().trim();
            if (name.isEmpty())
                throw new Exception("Patient name is required.");
            if (ageStr.isEmpty())
                throw new Exception("Patient age is required.");

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                throw new Exception("Age must be a whole number.");
            }

            String sex = sexBox.getSelectedItem().toString();
            ClinicalLabPatient patient =
                new ClinicalLabPatient(name, age, sex,
                    lastMealField.getText().trim());

            boolean any = false;
            for (int i = 0; i < allTests.size(); i++) {
                if (!checkBoxes.get(i).isSelected()) continue;
                String val = inputFields.get(i).getText().trim();
                if (val.isEmpty()) continue;
                try {
                    allTests.get(i).setResult(Double.parseDouble(val));
                    patient.addTest(allTests.get(i));
                    any = true;
                } catch (NumberFormatException ex) {
                    throw new Exception(
                        "Non-numeric result for: " + allTests.get(i).getTestName());
                }
            }
            if (!any) throw new Exception("No test results entered.");

            double subtotal = patient.getSubtotal();
            if (!showPaymentDialog(patient, subtotal)) {
                allTests.forEach(ClinicalLabTest::clearResult);
                return;
            }

            PdfExporter.exportAndOpen(patient);
            allTests.forEach(ClinicalLabTest::clearResult);

        } catch (Exception ex) {
            error("Quick PDF failed: " + ex.getMessage());
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
        scroll.setPreferredSize(new Dimension(780, 480));

        JOptionPane.showMessageDialog(this, scroll,
            "Patient Record Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportHistoryPdf() {
        int row = historyTable.getSelectedRow();
        if (row < 0) { error("Select a record to export."); return; }
        try {
            PdfExporter.exportFromHistory(row);
        } catch (Exception ex) {
            error("PDF export failed:\n" + ex.getMessage());
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

    // ══════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(DARK_NAVY);
        return l;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(DARK_NAVY);
        l.setOpaque(true);
        l.setBackground(LIGHT_BLUE);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, GOLD),
            BorderFactory.createEmptyBorder(4, 6, 4, 4)));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private JLabel hdrCell(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 11));
        l.setForeground(WHITE);
        l.setOpaque(true);
        l.setBackground(NAVY);
        l.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return l;
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
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private JLabel navyCell(String text, Color fg) {
        JLabel l = new JLabel("  " + text);
        l.setFont(F_CAT);
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(NAVY);
        l.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        return l;
    }

    private JPanel filler(Color bg) {
        JPanel p = new JPanel();
        p.setBackground(bg);
        return p;
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

    // ENTRY POINT
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
