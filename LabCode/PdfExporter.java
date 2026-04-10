import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

public class PdfExporter {

    private static final String EXPORT_DIR = "results";

    // ── iText Colors ──────────────────────────────────────────────
    private static final BaseColor C_DARK_NAVY = new BaseColor(8,   30,  63);
    private static final BaseColor C_NAVY      = new BaseColor(15,  52,  96);
    private static final BaseColor C_GOLD      = new BaseColor(212, 175, 55);
    private static final BaseColor C_LIGHT     = new BaseColor(230, 240, 255);
    private static final BaseColor C_ROW_ALT   = new BaseColor(240, 246, 255);
    private static final BaseColor C_HIGH_BG   = new BaseColor(255, 235, 235);
    private static final BaseColor C_LOW_BG    = new BaseColor(255, 255, 210);
    private static final BaseColor C_OK_BG     = new BaseColor(235, 255, 235);

    // ── iText Fonts ───────────────────────────────────────────────
    private static final Font F_COMPANY  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  14, C_GOLD);
    private static final Font F_TAGLINE  = FontFactory.getFont(FontFactory.HELVETICA,         8, BaseColor.WHITE);
    private static final Font F_DATE_HDR = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10, C_GOLD);
    private static final Font F_SECTION  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10, C_NAVY);
    private static final Font F_COL_HDR  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    8, BaseColor.WHITE);
    private static final Font F_BODY     = FontFactory.getFont(FontFactory.HELVETICA,          8, BaseColor.BLACK);
    private static final Font F_BOLD     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    8, BaseColor.BLACK);
    private static final Font F_SMALL    = FontFactory.getFont(FontFactory.HELVETICA,          7, BaseColor.GRAY);
    private static final Font F_HIGH     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    8, new BaseColor(200,  0,  0));
    private static final Font F_LOW      = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    8, new BaseColor(160,120,  0));
    private static final Font F_OK       = FontFactory.getFont(FontFactory.HELVETICA_BOLD,    8, new BaseColor(  0,130,  0));
    private static final Font F_TOTAL    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10, C_NAVY);
    private static final Font F_FOOTER   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, BaseColor.GRAY);

    // ═════════════════════════════════════════════════════════════
    // this is freaking pain in the ass to make thanks god there's alot of resources in github
    // PUBLIC ENTRY POINTS
    // ═════════════════════════════════════════════════════════════

    /** Export a live patient session and open the PDF. */
    public static void exportAndOpen(ClinicalLabPatient patient) throws Exception {
        ensureDir();
        String path = buildPath(patient.getName(), patient.getCollectionDate().toString());
        generateFromPatient(path, patient);
        openFile(new File(path));
    }

    /** Export a saved history record by index and open the PDF. */
    public static void exportFromHistory(int index) throws Exception {
        List<String[]> all = PatientRecord.loadAll();
        if (index < 0 || index >= all.size())
            throw new Exception("Record not found at index " + index + ".");
        ensureDir();
        String[] c    = all.get(index);
        String   path = buildPath(
            PatientRecord.formatTat(get(c, 0).isEmpty() ? "record" : get(c, 0)) + "_hist",
            get(c, 4));
        generateFromRaw(path, c);
        openFile(new File(path));
    }

    // ═════════════════════════════════════════════════════════════
    // GENERATION — LIVE PATIENT
    // ═════════════════════════════════════════════════════════════

    private static void generateFromPatient(String path, ClinicalLabPatient p)
            throws Exception {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        try (FileOutputStream fos = new FileOutputStream(path)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            LocalDateTime cdt = p.getCollectionDateTime();

            addLetterhead(doc);
            addSectionTitle(doc, "PATIENT INFORMATION");
            addPatientInfoTable(doc, p);
            addSectionTitle(doc, "LABORATORY TEST RESULTS");
            addLiveTestTable(doc, p, cdt);
            addSectionTitle(doc, "PAYMENT SUMMARY");
            addLivePaymentTable(doc, p);
            addFooter(doc);

        } catch (DocumentException de) {
            throw new Exception("PDF error: " + de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    // ═════════════════════════════════════════════════════════════
    // GENERATION — RAW CSV ROW (history export)
    // ═════════════════════════════════════════════════════════════

    private static void generateFromRaw(String path, String[] c) throws Exception {
        boolean newFmt  = c.length >= 11;
        int     testCol = newFmt ? 10 : (c.length >= 7 ? 6 : -1);

        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        try (FileOutputStream fos = new FileOutputStream(path)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            addLetterhead(doc);

            // Patient info
            addSectionTitle(doc, "PATIENT INFORMATION");
            PdfPTable info = new PdfPTable(4);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{18, 32, 18, 32});
            info.setSpacingBefore(4);
            info.setSpacingAfter(8);
            addInfoRow(info, "Name",      get(c, 0));
            addInfoRow(info, "Age",       get(c, 1));
            addInfoRow(info, "Sex",       get(c, 2));
            addInfoRow(info, "Last Meal", get(c, 3));
            addInfoRow(info, "Date",      get(c, 4));
            addInfoRow(info, "Time",      get(c, 5));
            doc.add(info);

            // Tests
            addSectionTitle(doc, "LABORATORY TEST RESULTS");
            doc.add(buildRawTestTable(c, testCol));

            // Payment (new format only)
            if (newFmt) {
                addSectionTitle(doc, "PAYMENT SUMMARY");
                addRawPaymentTable(doc, c, testCol);
            }

            addFooter(doc);

        } catch (DocumentException de) {
            throw new Exception("PDF error: " + de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    // ═════════════════════════════════════════════════════════════
    // SECTION BUILDERS
    // ═════════════════════════════════════════════════════════════

    private static void addLetterhead(Document doc) throws DocumentException {
        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{68, 32});
        tbl.setSpacingAfter(12);

        // Left — company name
        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(C_DARK_NAVY);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(12);
        left.addElement(new Paragraph("NUCOMP DIAGNOSTIC CORPORATION", F_COMPANY));
        left.addElement(new Paragraph(
            "Clinical Chemistry Laboratory System  |  NARCO VILLANDO JR.", F_TAGLINE));
        tbl.addCell(left);

        // Right — date + label
        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(C_NAVY);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(12);
        Paragraph dateP = new Paragraph(java.time.LocalDate.now().toString(), F_DATE_HDR);
        dateP.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(dateP);
        Paragraph recP = new Paragraph("OFFICIAL RECEIPT", F_TAGLINE);
        recP.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(recP);
        tbl.addCell(right);

        doc.add(tbl);
    }

    private static void addPatientInfoTable(Document doc, ClinicalLabPatient p)
            throws DocumentException {
        PdfPTable tbl = new PdfPTable(4);
        tbl.setWidthPercentage(100);
        tbl.setWidths(new float[]{18, 32, 18, 32});
        tbl.setSpacingBefore(4);
        tbl.setSpacingAfter(8);
        addInfoRow(tbl, "Name",      p.getName());
        addInfoRow(tbl, "Age",       String.valueOf(p.getAge()));
        addInfoRow(tbl, "Sex",       p.getSex());
        addInfoRow(tbl, "Last Meal", p.getTimeLastMeal());
        addInfoRow(tbl, "Date",      p.getCollectionDate().toString());
        addInfoRow(tbl, "Time",      p.getCollectionTime().toString());
        doc.add(tbl);
    }

    private static void addLiveTestTable(Document doc, ClinicalLabPatient p,
                                          LocalDateTime cdt) throws DocumentException {
        String[] hdrs   = {"Test", "Result", "Reference Range",
                           "Status", "TAT", "Expected Ready"};
        float[]  widths = {24f, 14f, 20f, 16f, 10f, 16f};

        PdfPTable tbl = new PdfPTable(hdrs.length);
        tbl.setWidthPercentage(100);
        tbl.setWidths(widths);
        tbl.setSpacingBefore(4);
        tbl.setSpacingAfter(8);
        tbl.setHeaderRows(1);

        for (String h : hdrs) addColHeader(tbl, h);

        boolean alt = false;
        for (ClinicalLabTest t : p.getCompletedTests()) {
            BaseColor bg     = alt ? C_ROW_ALT : BaseColor.WHITE;
            String    status = t.getInterpretation(p.getSex());
            addBodyCell(tbl, t.getTestName(),                              bg, Element.ALIGN_LEFT);
            addBodyCell(tbl, t.getFormattedResult() + " " + t.getUnit(),  bg, Element.ALIGN_CENTER);
            addBodyCell(tbl, t.getReferenceRange(p.getSex()),              bg, Element.ALIGN_CENTER);
            addStatusCell(tbl, status);
            addBodyCell(tbl, t.getTurnaroundLabel(),                       bg, Element.ALIGN_CENTER);
            addBodyCell(tbl, t.getExpectedReadyTime(cdt),                  bg, Element.ALIGN_CENTER);
            alt = !alt;
        }
        doc.add(tbl);
    }

    private static void addLivePaymentTable(Document doc, ClinicalLabPatient p)
            throws DocumentException {
        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(55);
        tbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tbl.setWidths(new float[]{60f, 40f});
        tbl.setSpacingBefore(4);
        tbl.setSpacingAfter(10);

        // Itemized tests
        boolean alt = false;
        for (ClinicalLabTest t : p.getCompletedTests()) {
            BaseColor bg = alt ? C_ROW_ALT : BaseColor.WHITE;
            addPayRow(tbl, t.getTestName(),
                      String.format("PhP %,.2f", t.getPrice()), bg, false);
            alt = !alt;
        }

        // PhilHealth subsidy line
        if ("PHILHEALTH".equalsIgnoreCase(p.getPaymentMethod())) {
            double subsidy = p.getSubtotal() - p.getTotalAmount();
            addPayRow(tbl, "PhilHealth Subsidy",
                      String.format("- PhP %,.2f", subsidy),
                      new BaseColor(230, 255, 230), false);
        }

        // Divider
        addSpanRow(tbl, "", new BaseColor(180, 195, 220), 1);

        addPayRow(tbl, "TOTAL DUE",
                  String.format("PhP %,.2f", p.getTotalAmount()),
                  C_LIGHT, true);
        addPayRow(tbl, "Payment Method",
                  p.getPaymentMethod(), BaseColor.WHITE, false);
        addPayRow(tbl, "Amount Paid",
                  String.format("PhP %,.2f", p.getAmountPaid()),
                  BaseColor.WHITE, false);
        addPayRow(tbl, "Change",
                  String.format("PhP %,.2f", p.getChange()),
                  BaseColor.WHITE, false);

        doc.add(tbl);
    }

    private static PdfPTable buildRawTestTable(String[] c, int testCol)
            throws DocumentException {
        String[] hdrs   = {"Test", "Result", "Reference Range",
                           "Status", "TAT", "Expected Ready"};
        float[]  widths = {24f, 14f, 20f, 16f, 10f, 16f};

        PdfPTable tbl = new PdfPTable(hdrs.length);
        tbl.setWidthPercentage(100);
        tbl.setWidths(widths);
        tbl.setSpacingBefore(4);
        tbl.setSpacingAfter(8);
        tbl.setHeaderRows(1);

        for (String h : hdrs) addColHeader(tbl, h);

        if (testCol >= 0 && testCol < c.length && !c[testCol].trim().isEmpty()) {
            boolean alt = false;
            for (String entry : c[testCol].split(";;")) {
                String[]  f      = entry.split("~", -1);
                boolean   nf     = f.length >= 8;
                BaseColor bg     = alt ? C_ROW_ALT : BaseColor.WHITE;
                String    status = f.length > 4 ? f[4] : "N/A";
                String    tat    = nf ? PatientRecord.formatTat(f[6]) : "N/A";
                String    ready  = nf ? f[7] : "N/A";
                String    result = (f.length > 1 ? f[1] : "") + " " + (f.length > 2 ? f[2] : "");
                addBodyCell(tbl, f.length > 0 ? f[0] : "N/A", bg, Element.ALIGN_LEFT);
                addBodyCell(tbl, result.trim(),                 bg, Element.ALIGN_CENTER);
                addBodyCell(tbl, f.length > 3 ? f[3] : "N/A", bg, Element.ALIGN_CENTER);
                addStatusCell(tbl, status);
                addBodyCell(tbl, tat,   bg, Element.ALIGN_CENTER);
                addBodyCell(tbl, ready, bg, Element.ALIGN_CENTER);
                alt = !alt;
            }
        }
        return tbl;
    }

    private static void addRawPaymentTable(Document doc, String[] c, int testCol)
            throws DocumentException {
        PdfPTable tbl = new PdfPTable(2);
        tbl.setWidthPercentage(55);
        tbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tbl.setWidths(new float[]{60f, 40f});
        tbl.setSpacingBefore(4);
        tbl.setSpacingAfter(10);

        if (testCol >= 0 && testCol < c.length && !c[testCol].trim().isEmpty()) {
            boolean alt = false;
            for (String entry : c[testCol].split(";;")) {
                String[] f      = entry.split("~", -1);
                String   tName  = f.length > 0 ? f[0] : "Unknown";
                String   tPrice = f.length > 5
                    ? String.format("PhP %s", f[5])
                    : "N/A";
                addPayRow(tbl, tName, tPrice, alt ? C_ROW_ALT : BaseColor.WHITE, false);
                alt = !alt;
            }
        }

        addSpanRow(tbl, "", new BaseColor(180, 195, 220), 1);
        addPayRow(tbl, "Payment Method", get(c, 6),
                  BaseColor.WHITE, false);
        addPayRow(tbl, "TOTAL DUE",
                  String.format("PhP %s", get(c, 7)), C_LIGHT, true);
        addPayRow(tbl, "Amount Paid",
                  String.format("PhP %s", get(c, 8)), BaseColor.WHITE, false);
        addPayRow(tbl, "Change",
                  String.format("PhP %s", get(c, 9)), BaseColor.WHITE, false);
        doc.add(tbl);
    }

    private static void addFooter(Document doc) throws DocumentException {
        doc.add(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY,
                Element.ALIGN_CENTER, -5));
        Paragraph disc = new Paragraph(
            "This result is for laboratory and diagnostic purposes only. "
          + "Please consult your physician/doctor for proper medical advice.", F_FOOTER);
        disc.setAlignment(Element.ALIGN_CENTER);
        disc.setSpacingBefore(6);
        doc.add(disc);

        PdfPTable sig = new PdfPTable(3);
        sig.setWidthPercentage(90);
        sig.setHorizontalAlignment(Element.ALIGN_CENTER);
        sig.setSpacingBefore(28);
        addSigCell(sig, "Medical Technologist");
        addSigCell(sig, "Laboratory Head");
        addSigCell(sig, "Authorized Signatory");
        doc.add(sig);
    }

    // ═════════════════════════════════════════════════════════════
    // CELL / ROW HELPERS
    // ═════════════════════════════════════════════════════════════

    private static void addSectionTitle(Document doc, String text)
            throws DocumentException {
        Paragraph p = new Paragraph(text, F_SECTION);
        p.setSpacingBefore(10);
        p.setSpacingAfter(3);
        doc.add(p);
        doc.add(new LineSeparator(0.5f, 100, C_GOLD, Element.ALIGN_CENTER, -3));
    }

    private static void addColHeader(PdfPTable tbl, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_COL_HDR));
        cell.setBackgroundColor(C_NAVY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tbl.addCell(cell);
    }

    private static void addInfoRow(PdfPTable tbl, String label, String value) {
        PdfPCell lc = new PdfPCell(new Phrase(label + ":", F_BOLD));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(4);
        tbl.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, F_BODY));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPadding(4);
        tbl.addCell(vc);
    }

    private static void addBodyCell(PdfPTable tbl, String text,
                                     BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_BODY));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        cell.setHorizontalAlignment(align);
        tbl.addCell(cell);
    }

    private static void addStatusCell(PdfPTable tbl, String status) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(statusBg(status));
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.addElement(new Phrase(status, statusFont(status)));
        tbl.addCell(cell);
    }

    private static void addPayRow(PdfPTable tbl, String label, String value,
                                   BaseColor bg, boolean bold) {
        Font f = bold ? F_TOTAL : F_BODY;
        PdfPCell lc = new PdfPCell(new Phrase(label, f));
        lc.setBackgroundColor(bg); lc.setPadding(5);
        tbl.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, f));
        vc.setBackgroundColor(bg); vc.setPadding(5);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tbl.addCell(vc);
    }

    private static void addSpanRow(PdfPTable tbl, String text,
                                    BaseColor bg, int pad) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setColspan(2);
        cell.setBackgroundColor(bg);
        cell.setPadding(pad);
        cell.setBorder(Rectangle.NO_BORDER);
        tbl.addCell(cell);
    }

    private static void addSigCell(PdfPTable tbl, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColorTop(C_NAVY);
        cell.setBorderWidthTop(0.5f);
        cell.setPadding(5);
        Paragraph p = new Paragraph(label, F_SMALL);
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        tbl.addCell(cell);
    }

    // ═════════════════════════════════════════════════════════════
    // UTILITIES
    // ═════════════════════════════════════════════════════════════

    private static BaseColor statusBg(String s) {
        if (s == null)             return BaseColor.WHITE;
        if (s.startsWith("HIGH")) return C_HIGH_BG;
        if (s.startsWith("LOW"))  return C_LOW_BG;
        return C_OK_BG;
    }

    private static Font statusFont(String s) {
        if (s == null)             return F_BODY;
        if (s.startsWith("HIGH")) return F_HIGH;
        if (s.startsWith("LOW"))  return F_LOW;
        return F_OK;
    }

    private static void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(null,
                    "PDF saved:\n" + file.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Saved but could not open automatically.\nLocation: "
                + file.getAbsolutePath(),
                "Export", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void ensureDir() throws Exception {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists() && !dir.mkdirs())
            throw new Exception("Cannot create export directory: "
                + dir.getAbsolutePath());
    }

    private static String buildPath(String name, String date) {
        return EXPORT_DIR + File.separator
            + sanitize(name) + "_" + sanitize(date) + ".pdf";
    }

    private static String sanitize(String s) {
        return (s == null ? "unknown" : s)
            .replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static String get(String[] arr, int i) {
        return (arr != null && i < arr.length && arr[i] != null)
               ? arr[i].trim() : "N/A";
    }
}

// this is one of the repo that i study :  https://github.com/tieniber/PDF-Exporter/blob/master/javasource/pdf_exporter/lib/PDFInjector.java
