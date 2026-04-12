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

    // Colors
    static final Color NAVY       = new Color(15,  52,  96);
    static final Color DARK_NAVY  = new Color(8,   30,  63);
    static final Color GOLD       = new Color(212, 175, 55);
    static final Color LIGHT_GOLD = new Color(255, 245, 200);
    static final Color WHITE      = Color.WHITE;
    static final Color LIGHT_BLUE = new Color(230, 240, 255);
    static final Color ROW_ALT    = new Color(240, 246, 255);
    static final Color WARN_RED   = new Color(210,  50,  50);
    static final Color WARN_BG    = new Color(255, 235, 235);

    // Fonts
    static final Font F_TITLE  = new Font("Arial", Font.BOLD,  26);
    static final Font F_SUB    = new Font("Arial", Font.BOLD,  12);
    static final Font F_LABEL  = new Font("Arial", Font.BOLD,  12);
    static final Font F_INPUT  = new Font("Arial", Font.PLAIN, 12);
    static final Font F_BUTTON = new Font("Arial", Font.BOLD,  12);
    static final Font F_CHECK  = new Font("Arial", Font.PLAIN, 11);
    static final Font F_RANGE  = new Font("Arial", Font.ITALIC,10);
    static final Font F_MONO   = new Font("Monospaced", Font.PLAIN, 12);
    static final Font F_CAT    = new Font("Arial", Font.BOLD,  11);

    // Patient info fields
    private JTextField        nameField, ageField, lastMealField;
    private JTextField        dateField, timeField;
    private JComboBox<String> sexBox;

    // Test lists
    private final List<ClinicalLabTest> biochemTests = new ArrayList<>();
    private final List<ClinicalLabTest> cardiacTests = new ArrayList<>();
    private final List<ClinicalLabTest> lftTests     = new ArrayList<>();
    private final List<ClinicalLabTest> thyroidTests = new ArrayList<>();
    private final List<ClinicalLabTest> allTests     = new ArrayList<>();

    // Parallel UI lists - index matches allTests exactly
    private final List<JCheckBox>  checkBoxes  = new ArrayList<>();
    private final List<JTextField> inputFields = new ArrayList<>();

    // FBS <-> RBS mutual exclusion indices
    private int idxFBS = -1;
    private int idxRBS = -1;

    // History tab
    private JTable            historyTable;
    private DefaultTableModel historyModel;

    public ClinicalLabGUI() {
        buildTests();
        buildFrame();
        loadHistory();
    }

    // ================================================================
    // TEST INITIALIZATION  (price = PhP, turnaround = hours)
    // ================================================================
    private void buildTests() {

        // --- Standard Biochemistry ---
        biochemTests.add(bio("FBS",               "mg/dL",  74,    100,         150,  2));
        biochemTests.add(bio("RBS",               "mg/dL",  70,    140,         150,  2));
        biochemTests.add(bio("Total Cholesterol", "mg/dL", 150,    200,         250,  4));
        biochemTests.add(bioSex("HDL",            "mg/dL",  35, 80, 42, 88,     300,  4));
        biochemTests.add(bio("LDL",               "mg/dL",  50,    130,         300,  4));
        biochemTests.add(bioSex("Triglycerides",  "mg/dL",  60,165, 40,140,     300,  4));
        biochemTests.add(bioSex("Creatinine",     "mg/dL", 0.9,1.3, 0.6,1.2,   180,  2));
        biochemTests.add(bioSex("Uric Acid",      "mg/dL", 3.5,7.2, 2.6,6.0,   180,  2));
        biochemTests.add(bio("BUN",               "mg/dL",   6.0,  20.0,        180,  2));
        biochemTests.add(bio("Sodium",            "mEq/L", 135,    145,         250,  4));
        biochemTests.add(bio("Potassium",         "mEq/L",   3.5,   5.0,        250,  4));
        biochemTests.add(bio("Chloride",          "mEq/L",  96,    110,         250,  4));
        biochemTests.add(bio("Total Calcium",     "mg/dL",   8.6,  10.28,       250,  4));
        biochemTests.add(bio("Ionized Calcium",   "mg/dL",   4.4,   5.2,        280,  4));
        biochemTests.add(bio("HbA1c",             "%",        4.0,   5.6,        600, 24));
        biochemTests.add(bio("Vitamin D (25-OH)", "ng/mL",  30,    100,        1800, 48));
        biochemTests.add(bio("Magnesium",         "mEq/L",   1.7,   2.2,        250,  4));
        biochemTests.add(bio("Phosphorus",        "mg/dL",   2.5,   4.5,        250,  4));

        // --- Cardiac & Enzyme Markers ---
        cardiacTests.add(cardiac("AST/SGOT",   "U/L",    46,     250, 4));
        cardiacTests.add(cardiac("ALT/SGPT",   "U/L",    49,     250, 4));
        cardiacTests.add(cardiac("Troponin I", "ng/mL",   0.04,  950, 1));
        cardiacTests.add(cardiac("CK-MB",      "ng/mL",   3.6,   750, 2));
        cardiacTests.add(bio(    "LDH",        "U/L",   140, 280,  350, 4));
        cardiacTests.add(cardiac("CRP",        "mg/L",    1.0,   600, 4));

        // --- Liver Function Tests ---
        lftTests.add(lft("Total Bilirubin",  "mg/dL", 0.2, 1.2, 250, 4));
        lftTests.add(lft("Direct Bilirubin", "mg/dL", 0.0, 0.3, 250, 4));
        lftTests.add(lft("Albumin",          "g/dL",  3.5, 5.0, 250, 4));
        lftTests.add(lft("Total Protein",    "g/dL",  6.0, 8.3, 250, 4));
        lftTests.add(lft("ALP",              "U/L",  44,  147,  300, 4));

        // --- Thyroid Function Tests ---
        thyroidTests.add(tft("TSH",     "mIU/L", 0.4, 4.0, ThyroidTest.ThyroidMarker.TSH, 700, 24));
        thyroidTests.add(tft("Free T3", "pg/mL", 2.3, 4.2, ThyroidTest.ThyroidMarker.T3,  700, 24));
        thyroidTests.add(tft("Free T4", "ng/dL", 0.8, 1.8, ThyroidTest.ThyroidMarker.T4,  700, 24));

        allTests.addAll(biochemTests);
        allTests.addAll(cardiacTests);
        allTests.addAll(lftTests);
        allTests.addAll(thyroidTests);

        for (int i = 0; i < allTests.size(); i++) {
            String n = allTests.get(i).getTestName();
            if ("FBS".equals(n)) idxFBS = i;
            if ("RBS".equals(n)) idxRBS = i;
        }
    }

    // Test factory helpers
    private ClinicalLabTest bio(String n, String u, double min, double max,
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
    private ClinicalLabTest cardiac(String n, String u, double thr,
                                     double price, int tat) {
        CardiacMarkerTest t = new CardiacMarkerTest(n, u, thr);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }
    private ClinicalLabTest lft(String n, String u, double min, double max,
                                  double price, int tat) {
        LiverFunctionTest t = new LiverFunctionTest(n, u, min, max);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }
    private ClinicalLabTest tft(String n, String u, double min, double max,
                                  ThyroidTest.ThyroidMarker marker,
                                  double price, int tat) {
        ThyroidTest t = new ThyroidTest(n, u, min, max, marker);
        t.setPrice(price); t.setTurnaroundHours(tat); return t;
    }

    // ================================================================
    // FRAME SETUP
    // ================================================================
    private void buildFrame() {
        setTitle("NUCOMP Diagnostic Corporation - Clinical Chemistry System");
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

    // ================================================================
    // HEADER
    // ================================================================
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout(10, 0));
        hdr.setBackground(DARK_NAVY);
        hdr.setBorder(BorderFactory.createEmptyBorder(14, 25, 14, 25));

        JLabel company = new JLabel("NUCOMP DIAGNOSTIC CORPORATION");
        company.setFont(F_TITLE);
        company.setForeground(GOLD);

        JLabel sub = new JLabel(
            "Clinical Chemistry Laboratory System  |  " + LocalDate.now());
        sub.setFont(F_SUB);
        sub.setForeground(WHITE);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(DARK_NAVY);
        left.add(company);
        left.add(sub);

        // Live clock
        JLabel clock = new JLabel(LocalTime.now().withNano(0).toString(),
                                  SwingConstants.RIGHT);
        clock.setFont(new Font("Arial", Font.BOLD, 22));
        clock.setForeground(LIGHT_GOLD);
        Timer t = new Timer(1000, e -> clock.setText(
            LocalTime.now().withNano(0).toString()));
        t.setInitialDelay(0);
        t.start();

        hdr.add(left,  BorderLayout.WEST);
        hdr.add(clock, BorderLayout.EAST);
        return hdr;
    }

    // ================================================================
    // TABS
    // ================================================================
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("New Patient",     buildNewPatientTab());
        tabs.addTab("Patient History", buildHistoryTab());
        return tabs;
    }

    // ================================================================
    // NEW PATIENT TAB
    // ================================================================
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

        Timer tt = new Timer(1000, e -> timeField.setText(
            LocalTime.now().withNano(0).toString()));
        tt.setInitialDelay(0);
        tt.start();

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

        // Column headers
        panel.add(navyCell("Test Name",                   GOLD));
        panel.add(navyCell("Result",                      GOLD));
        panel.add(navyCell("Reference | TAT | Price(PhP)",GOLD));

        // FBS / RBS notice banner
        JLabel notice = new JLabel(
            "  NOTE: FBS and RBS are mutually exclusive - selecting one deselects the other.");
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

        addCategory(panel, "-- STANDARD BIOCHEMISTRY --",        biochemTests);
        addCategory(panel, "-- CARDIAC & ENZYME MARKERS --",     cardiacTests);
        addCategory(panel, "-- LIVER FUNCTION TESTS (LFT) --",   lftTests);
        addCategory(panel, "-- THYROID FUNCTION TESTS (TFT) --", thyroidTests);

        wireMutualExclusion();

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /**
     * FBS (Fasting Blood Sugar) and RBS (Random Blood Sugar) are mutually exclusive.
     * FBS requires 8-12 hours fasting. RBS is a non-fasting random sample.
     * No laboratory orders both on the same request.
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
        Timer flashTimer = new Timer(1200, e -> {
            cb.setBackground(WHITE);
            tf.setBackground(WHITE);
        });
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    private void addCategory(JPanel panel, String title, List<ClinicalLabTest> tests) {
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
                "  M: " + test.getReferenceRange("MALE") +
                "  F: " + test.getReferenceRange("FEMALE") +
                "  | " + test.getTurnaroundLabel() +
                "  | PhP " + String.format("%,.0f", test.getPrice()));
            ref.setFont(F_RANGE);
            ref.setForeground(new Color(30, 60, 120));
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
        JButton genBtn   = button("Generate Result", GOLD,       DARK_NAVY);
        JButton pdfBtn   = button("Quick PDF",       WHITE,      NAVY);
        JButton clearBtn = button("Clear Form",      LIGHT_BLUE, NAVY);
        genBtn.addActionListener(  e -> generateResult());
        pdfBtn.addActionListener(  e -> quickPdf());
        clearBtn.addActionListener(e -> clearForm());
        panel.add(genBtn);
        panel.add(pdfBtn);
        panel.add(clearBtn);
        return panel;
    }

    // ================================================================
    // PATIENT HISTORY TAB
    // ================================================================
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

        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if (!sel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(styledBorder("  Patient History  "));
        tab.add(scroll, BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bp.setBackground(NAVY);
        JButton viewBtn = button("View Details", GOLD,       DARK_NAVY);
        JButton pdfBtn  = button("Export PDF",   WHITE,      NAVY);
        JButton refBtn  = button("Refresh",      LIGHT_BLUE, NAVY);
        viewBtn.addActionListener(e -> viewHistoryDetail());
        pdfBtn.addActionListener( e -> exportHistoryPdf());
        refBtn.addActionListener( e -> loadHistory());
        bp.add(viewBtn);
        bp.add(pdfBtn);
        bp.add(refBtn);
        tab.add(bp, BorderLayout.SOUTH);
        return tab;
    }

    // ================================================================
    // STATUS BAR
    // ================================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(DARK_NAVY);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 15));
        JLabel left = new JLabel(
            "NUCOMP Clinical Chemistry Laboratory System  |  by COMSCI");
        left.setForeground(new Color(160, 190, 220));
        left.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel right = new JLabel("v3.0  |  " + LocalDate.now().getYear());
        right.setForeground(GOLD);
        right.setFont(new Font("Arial", Font.BOLD, 11));
        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ================================================================
    // BUSINESS LOGIC
    // ================================================================

    private void generateResult() {

        // 1. Validate patient header fields
        String name = nameField.getText().trim();
        if (name.isEmpty()) { error("Patient name is required."); return; }

        String ageStr = ageField.getText().trim();
        if (ageStr.isEmpty()) { error("Patient age is required."); return; }
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 0 || age > 150) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            error("Age must be a whole number between 0 and 150."); return;
        }

        String sex  = (String) sexBox.getSelectedItem();
        String meal = lastMealField.getText().trim();

        // 2. Validate test selections and result values
        boolean any = false;
        for (int i = 0; i < allTests.size(); i++) {
            if (!checkBoxes.get(i).isSelected()) continue;
            any = true;
            String val = inputFields.get(i).getText().trim();
            if (val.isEmpty()) {
                error("Enter a result value for: " + allTests.get(i).getTestName());
                return;
            }
            try {
                double d = Double.parseDouble(val);
                if (d < 0) {
                    error("Result cannot be negative for: " + allTests.get(i).getTestName());
                    return;
                }
            } catch (NumberFormatException ex) {
                error("Result must be a number for: " + allTests.get(i).getTestName());
                return;
            }
        }
        if (!any) { error("Please select at least one test."); return; }

        // 3. Build patient and add selected tests
        ClinicalLabPatient patient = new ClinicalLabPatient(name, age, sex, meal);
        for (int i = 0; i < allTests.size(); i++) {
            if (!checkBoxes.get(i).isSelected()) continue;
            allTests.get(i).setResult(
                Double.parseDouble(inputFields.get(i).getText().trim()));
            patient.addTest(allTests.get(i));
        }

        // 4. Show payment dialog
        if (!showPaymentDialog(patient)) {
            allTests.forEach(ClinicalLabTest::clearResult);
            return;
        }

        // 5. Save, refresh history, show result
        PatientRecord.save(patient);
        loadHistory();
        showResultDialog(patient);
        allTests.forEach(ClinicalLabTest::clearResult);
    }

    // ================================================================
    // PAYMENT DIALOG
    // ================================================================
    private boolean showPaymentDialog(ClinicalLabPatient patient) {
        double subtotal = patient.getSubtotal();

        JDialog dlg = new JDialog(this, "Payment", true);
        dlg.setSize(530, 660);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        // Header bar
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(DARK_NAVY);
        hdr.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel ht = new JLabel("PAYMENT  --  " + patient.getName().toUpperCase());
        ht.setFont(new Font("Arial", Font.BOLD, 14));
        ht.setForeground(GOLD);
        hdr.add(ht);
        dlg.add(hdr, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(12, 18, 8, 18));

        // Test items table
        content.add(sectionLbl("  TEST ITEMS & PRICES"));
        content.add(Box.createVerticalStrut(6));

        JPanel items = new JPanel(new GridLayout(0, 2, 0, 1));
        items.setBackground(WHITE);
        items.add(hdrCell("  Test Name"));
        items.add(hdrCell("  Price (PhP)"));

        boolean alt = false;
        for (ClinicalLabTest t : patient.getCompletedTests()) {
            Color  bg = alt ? ROW_ALT : WHITE;
            JLabel nl = itemCell("  " + t.getTestName(), bg, SwingConstants.LEFT);
            JLabel pl = itemCell(String.format("  %,.2f  ", t.getPrice()), bg,
                                  SwingConstants.RIGHT);
            items.add(nl);
            items.add(pl);
            alt = !alt;
        }
        JLabel stL = itemCell("  SUBTOTAL", LIGHT_GOLD, SwingConstants.LEFT);
        stL.setFont(new Font("Arial", Font.BOLD, 11));
        JLabel stV = itemCell(String.format("  %,.2f  ", subtotal), LIGHT_GOLD,
                               SwingConstants.RIGHT);
        stV.setFont(new Font("Arial", Font.BOLD, 11));
        items.add(stL);
        items.add(stV);

        JScrollPane is = new JScrollPane(items);
        is.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        is.setPreferredSize(new Dimension(460, 180));
        content.add(is);
        content.add(Box.createVerticalStrut(14));

        // Payment form
        content.add(sectionLbl("  PAYMENT OPTIONS"));
        content.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setBackground(WHITE);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        String[]          methods   = {"CASH", "GCASH", "MAYA", "PHILHEALTH"};
        JComboBox<String> methodBox = new JComboBox<>(methods);
        methodBox.setFont(F_INPUT);
        grid.add(label("Payment Method:"));
        grid.add(methodBox);

        // PhilHealth note
        JLabel philNote = new JLabel("  PhilHealth subsidy of PhP 500.00 applied");
        philNote.setFont(new Font("Arial", Font.ITALIC, 11));
        philNote.setForeground(new Color(0, 120, 0));
        philNote.setVisible(false);
        grid.add(philNote);
        grid.add(new JLabel());

        // Reference number (shown for non-cash)
        JLabel refLbl = new JLabel("Reference / Transaction No.:");
        refLbl.setFont(F_LABEL);
        refLbl.setForeground(DARK_NAVY);
        JTextField refField = new JTextField();
        refField.setFont(F_INPUT);
        refField.setToolTipText(
            "GCash ref no., Maya transaction no., or card last 4 digits");
        JPanel refLblPanel = wrapComp(refLbl);
        JPanel refFldPanel = wrapComp(refField);
        refLblPanel.setVisible(false);
        refFldPanel.setVisible(false);
        grid.add(refLblPanel);
        grid.add(refFldPanel);

        // Hint text
        JLabel refHint = new JLabel(" ");
        refHint.setFont(new Font("Arial", Font.ITALIC, 10));
        refHint.setForeground(new Color(80, 80, 140));
        grid.add(refHint);
        grid.add(new JLabel());

        // Total due
        JLabel totalDueLbl = new JLabel(
            String.format("  TOTAL DUE:  PhP %,.2f", subtotal));
        totalDueLbl.setFont(new Font("Arial", Font.BOLD, 14));
        totalDueLbl.setForeground(DARK_NAVY);
        grid.add(totalDueLbl);
        grid.add(new JLabel());

        // Amount tendered (cash only)
        JLabel tenderLbl = new JLabel("Amount Tendered (PhP):");
        tenderLbl.setFont(F_LABEL);
        tenderLbl.setForeground(DARK_NAVY);
        JTextField tenderField = new JTextField("0.00");
        tenderField.setFont(F_INPUT);
        JPanel tenderLblPanel = wrapComp(tenderLbl);
        JPanel tenderFldPanel = wrapComp(tenderField);
        grid.add(tenderLblPanel);
        grid.add(tenderFldPanel);

        grid.add(label("Change:"));
        JLabel changeLbl = new JLabel("PhP 0.00");
        changeLbl.setFont(new Font("Arial", Font.BOLD, 13));
        changeLbl.setForeground(new Color(0, 110, 0));
        grid.add(changeLbl);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("Arial", Font.ITALIC, 11));
        errLbl.setForeground(WARN_RED);
        grid.add(errLbl);
        grid.add(new JLabel());

        content.add(grid);
        dlg.add(new JScrollPane(content), BorderLayout.CENTER);

        // Shared state
        final double[]  totalDue = {subtotal};
        final boolean[] ok       = {false};

        // Recalculate whenever method or tender changes
        Runnable update = () -> {
            String  method = (String) methodBox.getSelectedItem();
            boolean isCash = "CASH".equals(method);
            boolean isPhil = "PHILHEALTH".equals(method);
            boolean showRef = !isCash;

            double due  = isPhil ? Math.max(0, subtotal - 500.0) : subtotal;
            totalDue[0] = due;
            totalDueLbl.setText(String.format("  TOTAL DUE:  PhP %,.2f", due));
            philNote.setVisible(isPhil);

            // Show / hide reference field
            refLblPanel.setVisible(showRef);
            refFldPanel.setVisible(showRef);

            if ("GCASH".equals(method))
                refHint.setText("  e.g. GCash Reference Number (13 digits)");
            else if ("MAYA".equals(method))
                refHint.setText("  e.g. Maya Transaction Reference");
            else if ("PHILHEALTH".equals(method))
                refHint.setText("  e.g. PhilHealth Member ID or Claim No.");
            else
                refHint.setText(" ");

            // Cash shows tender + change, others do not
            tenderLblPanel.setVisible(isCash);
            tenderFldPanel.setVisible(isCash);

            if (isCash) {
                tenderField.setEnabled(true);
                try {
                    double tendered = Double.parseDouble(
                        tenderField.getText().trim().replace(",", ""));
                    double chg = tendered - due;
                    changeLbl.setText(String.format("PhP %,.2f", Math.max(0, chg)));
                    changeLbl.setForeground(chg >= 0
                        ? new Color(0, 110, 0) : WARN_RED);
                } catch (NumberFormatException ex) {
                    changeLbl.setText("PhP 0.00");
                }
            } else {
                tenderField.setText(String.format("%.2f", due));
                tenderField.setEnabled(false);
                changeLbl.setText("PhP 0.00");
                changeLbl.setForeground(new Color(0, 110, 0));
            }

            dlg.revalidate();
            dlg.repaint();
        };

        methodBox.addActionListener(e -> update.run());
        tenderField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { update.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { update.run(); }
            @Override public void changedUpdate(DocumentEvent e) { update.run(); }
        });
        update.run();

        // Buttons
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        bp.setBackground(NAVY);
        JButton cancelBtn  = button("Cancel",          LIGHT_BLUE, NAVY);
        JButton confirmBtn = button("Confirm Payment", GOLD,       DARK_NAVY);

        cancelBtn.addActionListener(e -> dlg.dispose());

        confirmBtn.addActionListener(e -> {
            errLbl.setText(" ");
            try {
                String method   = (String) methodBox.getSelectedItem();
                double due      = totalDue[0];
                double tendered;
                String ref      = refField.getText().trim();

                // Non-cash requires a reference number
                if (!"CASH".equals(method) && ref.isEmpty()) {
                    throw new Exception(
                        "Please enter the Reference / Transaction No. for " + method + ".");
                }

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
                            "Amount tendered (PhP %.2f) is less than total due (PhP %.2f).",
                            tendered, due));
                } else {
                    tendered = due;
                }

                patient.setPaymentMethod(method);
                patient.setPaymentReference(ref);
                patient.setTotalAmount(due);
                patient.setAmountPaid(tendered);
                patient.setChange(tendered - due);
                ok[0] = true;
                dlg.dispose();

            } catch (Exception ex) {
                errLbl.setText("  WARNING: " + ex.getMessage());
            }
        });

        bp.add(cancelBtn);
        bp.add(confirmBtn);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true); // blocks until disposed
        return ok[0];
    }

    // ================================================================
    // RESULT PREVIEW DIALOG
    // ================================================================
    private void showResultDialog(ClinicalLabPatient patient) {
        String SEP = "--------------------------------------------------------------";
        StringBuilder sb = new StringBuilder();
        sb.append("==============================================================\n");
        sb.append("   NUCOMP DIAGNOSTIC CORPORATION\n");
        sb.append("   CLINICAL CHEMISTRY LABORATORY\n");
        sb.append("==============================================================\n\n");
        sb.append(patient).append("\n");
        sb.append(String.format("Payment       : %s%n", patient.getPaymentMethod()));
        String ref = patient.getPaymentReference();
        if (ref != null && !ref.isEmpty())
            sb.append(String.format("Reference No. : %s%n", ref));
        sb.append(String.format("Total Due     : PhP %,.2f%n", patient.getTotalAmount()));
        sb.append(String.format("Amount Paid   : PhP %,.2f%n", patient.getAmountPaid()));
        sb.append(String.format("Change        : PhP %,.2f%n", patient.getChange()));
        sb.append("\nTEST RESULTS\n").append(SEP).append("\n");
        sb.append(String.format("  %-26s %-14s %-8s %-22s %s%n",
            "Test", "Result", "TAT", "Expected Ready", "Interpretation"));
        sb.append("  ").append(SEP).append("\n");
        for (ClinicalLabTest t : patient.getCompletedTests()) {
            sb.append(String.format("  %-26s %-14s %-8s %-22s %s%n",
                t.getTestName(),
                t.getFormattedResult() + " " + t.getUnit(),
                t.getTurnaroundLabel(),
                t.getExpectedReadyTime(patient.getCollectionDateTime()),
                t.getInterpretation(patient.getSex())));
        }
        sb.append("\n").append(SEP).append("\n");
        sb.append("This result is for laboratory purposes only.\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(F_MONO);
        area.setEditable(false);
        area.setBackground(new Color(250, 253, 255));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(820, 480));

        Object[] opts = {"Save as PDF", "Close"};
        int choice = JOptionPane.showOptionDialog(this, scroll, "Laboratory Result",
            JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, opts, opts[1]);

        if (choice == 0) {
            try {
                PdfExporter.exportAndOpen(patient);
            } catch (Exception ex) {
                error("PDF export failed:\n" + ex.getMessage());
            }
        }
    }

    // ================================================================
    // QUICK PDF
    // ================================================================
    private void quickPdf() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { error("Patient name is required."); return; }
        String ageStr = ageField.getText().trim();
        if (ageStr.isEmpty()) { error("Patient age is required."); return; }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            error("Age must be a whole number."); return;
        }
        String sex = (String) sexBox.getSelectedItem();
        ClinicalLabPatient patient =
            new ClinicalLabPatient(name, age, sex, lastMealField.getText().trim());
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
                error("Non-numeric result for: " + allTests.get(i).getTestName());
                allTests.forEach(ClinicalLabTest::clearResult);
                return;
            }
        }
        if (!any) { error("No test results entered."); return; }
        if (!showPaymentDialog(patient)) {
            allTests.forEach(ClinicalLabTest::clearResult);
            return;
        }
        try {
            PdfExporter.exportAndOpen(patient);
        } catch (Exception ex) {
            error("Quick PDF failed:\n" + ex.getMessage());
        }
        allTests.forEach(ClinicalLabTest::clearResult);
    }

    // ================================================================
    // HISTORY ACTIONS
    // ================================================================
    private void viewHistoryDetail() {
        int row = historyTable.getSelectedRow();
        if (row < 0) { error("Select a record from the history table."); return; }
        JTextArea area = new JTextArea(PatientRecord.getDetailedResult(row));
        area.setFont(F_MONO);
        area.setEditable(false);
        area.setBackground(new Color(250, 253, 255));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(820, 480));
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

    // ================================================================
    // UI FACTORY HELPERS
    // ================================================================

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(DARK_NAVY);
        return l;
    }

    private JLabel sectionLbl(String text) {
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
        l.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return l;
    }

    private JLabel itemCell(String text, Color bg, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        return l;
    }

    /** Wrap any component in a plain panel so it can be shown/hidden as a grid cell */
    private JPanel wrapComp(Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.add(c, BorderLayout.CENTER);
        return p;
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
            BorderFactory.createLineBorder(GOLD, 2), title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), NAVY);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    // ENTRY POINT
    // ================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            ClinicalLabGUI gui = new ClinicalLabGUI();
            // setVisible is called inside buildFrame()
        });
    }
}