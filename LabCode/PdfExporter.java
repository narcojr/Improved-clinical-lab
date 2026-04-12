import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PdfExporter - Pure Java PDF writer. NO external libraries needed.
 * Compile: javac *.java
 * Run:     java ClinicalLabGUI
 */
public class PdfExporter {

    private static final String RESULTS_DIR = "results";

    // A4 page in PDF points (72pt = 1 inch)
    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;
    private static final int MARGIN_LEFT = 50;
    private static final int MARGIN_TOP  = 50;

    // ================================================================
    // PUBLIC API
    // ================================================================

    public static void exportAndOpen(ClinicalLabPatient p) throws Exception {
        prepareDir();
        String path = makeFilePath(p.getName(), p.getCollectionDate().toString());
        byte[] pdf  = buildPdfFromPatient(p);
        saveFile(path, pdf);
        openFile(new File(path));
    }

    public static void exportFromHistory(int index) throws Exception {
        List<String[]> all = PatientRecord.loadAll();
        if (index < 0 || index >= all.size())
            throw new Exception("No record found at row " + index + ".");
        prepareDir();
        String[] row  = all.get(index);
        String   path = makeFilePath(
            PatientRecord.get(row, 0) + "_history",
            PatientRecord.get(row, 4));
        byte[] pdf = buildPdfFromRow(row);
        saveFile(path, pdf);
        openFile(new File(path));
    }

    // ================================================================
    // BUILD PDF FROM LIVE PATIENT
    // ================================================================

    private static byte[] buildPdfFromPatient(ClinicalLabPatient p) throws Exception {
        SimplePdf pdf = new SimplePdf();

        pdf.addBold("NUCOMP DIAGNOSTIC CORPORATION", 14);
        pdf.addText("Clinical Chemistry Laboratory System", 10);
        pdf.addText("Date: " + LocalDate.now(), 9);
        pdf.addLine();

        pdf.addBold("PATIENT INFORMATION", 11);
        pdf.addText("Name         : " + p.getName(), 9);
        pdf.addText("Age          : " + p.getAge(), 9);
        pdf.addText("Sex          : " + p.getSex(), 9);
        pdf.addText("Last Meal    : " + p.getTimeLastMeal(), 9);
        pdf.addText("Date         : " + p.getCollectionDate(), 9);
        pdf.addText("Time         : " + p.getCollectionTime(), 9);
        pdf.addLine();

        pdf.addBold("PAYMENT INFORMATION", 11);
        pdf.addText("Payment Method : " + p.getPaymentMethod(), 9);
        if (p.getPaymentReference() != null && !p.getPaymentReference().isEmpty()) {
            pdf.addText("Reference No.  : " + p.getPaymentReference(), 9);
        }
        pdf.addText("Total Due      : PhP " + fmt(p.getTotalAmount()), 9);
        pdf.addText("Amount Paid    : PhP " + fmt(p.getAmountPaid()), 9);
        pdf.addText("Change         : PhP " + fmt(p.getChange()), 9);
        pdf.addLine();

        pdf.addBold("LABORATORY TEST RESULTS", 11);
        pdf.addBlank();
        pdf.addBold(
            col("TEST NAME", 26) + col("RESULT", 14) +
            col("REFERENCE", 20) + col("TAT", 8) +
            col("READY BY", 18) + "INTERPRETATION", 8);
        pdf.addDash();

        for (ClinicalLabTest t : p.getCompletedTests()) {
            pdf.addText(
                col(t.getTestName(), 26) +
                col(t.getFormattedResult() + " " + t.getUnit(), 14) +
                col(t.getReferenceRange(p.getSex()), 20) +
                col(t.getTurnaroundLabel(), 8) +
                col(t.getExpectedReadyTime(p.getCollectionDateTime()), 18) +
                t.getInterpretation(p.getSex()), 8);
            pdf.addText("  Price: PhP " + fmt(t.getPrice()), 8);
            pdf.addBlank();
        }

        pdf.addDash();
        pdf.addBold("ITEMIZED CHARGES", 10);
        for (ClinicalLabTest t : p.getCompletedTests()) {
            pdf.addText("  " + col(t.getTestName(), 32) + "PhP " + fmt(t.getPrice()), 9);
        }
        if ("PHILHEALTH".equalsIgnoreCase(p.getPaymentMethod())) {
            double sub = p.getSubtotal() - p.getTotalAmount();
            if (sub > 0)
                pdf.addText("  " + col("PhilHealth Subsidy", 32) + "-PhP " + fmt(sub), 9);
        }
        pdf.addDash();
        pdf.addBold("  " + col("TOTAL DUE", 32) + "PhP " + fmt(p.getTotalAmount()), 10);
        pdf.addLine();

        pdf.addBlank();
        pdf.addBlank();
        pdf.addText(
            col("_______________________", 28) +
            col("_______________________", 28) +
            "_______________________", 9);
        pdf.addText(
            col("Medical Technologist", 28) +
            col("Laboratory Head", 28) +
            "Authorized Signatory", 9);
        pdf.addLine();
        pdf.addText("This result is for laboratory and diagnostic purposes only.", 8);
        pdf.addText("Please consult your physician for proper medical advice.", 8);

        return pdf.generate();
    }

    // ================================================================
    // BUILD PDF FROM RAW CSV ROW (History export)
    // ================================================================

    private static byte[] buildPdfFromRow(String[] c) throws Exception {
        int     tc        = PatientRecord.testCol(c);
        boolean hasPay    = c.length >= 11;
        boolean hasPayRef = c.length >= 12;

        SimplePdf pdf = new SimplePdf();

        pdf.addBold("NUCOMP DIAGNOSTIC CORPORATION", 14);
        pdf.addText("Clinical Chemistry Laboratory System", 10);
        pdf.addText("Date: " + LocalDate.now(), 9);
        pdf.addLine();

        pdf.addBold("PATIENT INFORMATION", 11);
        pdf.addText("Name         : " + PatientRecord.get(c, 0), 9);
        pdf.addText("Age          : " + PatientRecord.get(c, 1), 9);
        pdf.addText("Sex          : " + PatientRecord.get(c, 2), 9);
        pdf.addText("Last Meal    : " + PatientRecord.get(c, 3), 9);
        pdf.addText("Date         : " + PatientRecord.get(c, 4), 9);
        pdf.addText("Time         : " + PatientRecord.get(c, 5), 9);

        if (hasPay) {
            pdf.addLine();
            pdf.addBold("PAYMENT INFORMATION", 11);
            pdf.addText("Payment Method : " + PatientRecord.get(c, 6), 9);
            if (hasPayRef) {
                String ref = PatientRecord.get(c, 11);
                if (!ref.isEmpty())
                    pdf.addText("Reference No.  : " + ref, 9);
            }
            pdf.addText("Total Due      : PhP " + PatientRecord.get(c, 7), 9);
            pdf.addText("Amount Paid    : PhP " + PatientRecord.get(c, 8), 9);
            pdf.addText("Change         : PhP " + PatientRecord.get(c, 9), 9);
        }

        pdf.addLine();
        pdf.addBold("LABORATORY TEST RESULTS", 11);
        pdf.addBlank();
        pdf.addBold(
            col("TEST NAME", 26) + col("RESULT", 14) +
            col("REFERENCE", 20) + col("TAT", 8) +
            col("READY BY", 18) + "INTERPRETATION", 8);
        pdf.addDash();

        if (tc >= 0 && tc < c.length && !c[tc].trim().isEmpty()) {
            for (String entry : c[tc].split(";;")) {
                String[] f    = entry.split("~", -1);
                boolean  has8 = f.length >= 8;
                String result = (f.length > 1 ? f[1] : "") + " " + (f.length > 2 ? f[2] : "");
                String ref    = f.length > 3 ? f[3] : "";
                String interp = f.length > 4 ? f[4] : "";
                String price  = f.length > 5 ? f[5] : "";
                String tat    = has8 ? PatientRecord.formatTat(f[6]) : "";
                String ready  = has8 ? f[7] : "";
                pdf.addText(
                    col(f.length > 0 ? f[0] : "", 26) +
                    col(result.trim(), 14) +
                    col(ref, 20) +
                    col(tat, 8) +
                    col(ready, 18) +
                    interp, 8);
                if (!price.isEmpty())
                    pdf.addText("  Price: PhP " + price, 8);
                pdf.addBlank();
            }
        }

        pdf.addDash();
        pdf.addBlank();
        pdf.addBlank();
        pdf.addText(
            col("_______________________", 28) +
            col("_______________________", 28) +
            "_______________________", 9);
        pdf.addText(
            col("Medical Technologist", 28) +
            col("Laboratory Head", 28) +
            "Authorized Signatory", 9);
        pdf.addLine();
        pdf.addText("This result is for laboratory and diagnostic purposes only.", 8);
        pdf.addText("Please consult your physician for proper medical advice.", 8);

        return pdf.generate();
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private static String fmt(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }

    /** Pad or truncate string to exactly n characters */
    private static String col(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }

    private static void prepareDir() throws Exception {
        File d = new File(RESULTS_DIR);
        if (!d.exists() && !d.mkdirs())
            throw new Exception("Cannot create folder: " + d.getAbsolutePath());
    }

    private static String makeFilePath(String name, String date) {
        String n = (name == null ? "record" : name).replaceAll("[^a-zA-Z0-9_-]", "_");
        String d = (date == null ? "nodate" : date).replaceAll("[^a-zA-Z0-9_-]", "_");
        return RESULTS_DIR + File.separator + n + "_" + d + ".pdf";
    }

    private static void saveFile(String path, byte[] data) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data);
            fos.flush();
        } finally {
            if (fos != null) try { fos.close(); } catch (IOException ignored) {}
        }
    }

    private static void openFile(File f) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(f);
            } else {
                JOptionPane.showMessageDialog(null,
                    "PDF saved to:\n" + f.getAbsolutePath(),
                    "Exported", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                "PDF saved. Open it from:\n" + f.getAbsolutePath(),
                "Saved", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ================================================================
    // SIMPLE PDF ENGINE - no external dependencies
    // ================================================================

    static class SimplePdf {

        // Entry types
        private static final int TYPE_TEXT = 0;
        private static final int TYPE_LINE = 1;
        private static final int TYPE_BLANK = 2;
        private static final int TYPE_DASH = 3;

        private final List<Object[]> entries = new ArrayList<>();

        /** Add a normal text line */
        void addText(String text, float size) {
            entries.add(new Object[]{clean(text), size, false, TYPE_TEXT});
        }

        /** Add a bold text line */
        void addBold(String text, float size) {
            entries.add(new Object[]{clean(text), size, true, TYPE_TEXT});
        }

        /** Add a horizontal separator line */
        void addLine() {
            entries.add(new Object[]{"", 4f, false, TYPE_LINE});
        }

        /** Add a blank spacing line */
        void addBlank() {
            entries.add(new Object[]{"", 5f, false, TYPE_BLANK});
        }

        /** Add a dashed divider line */
        void addDash() {
            entries.add(new Object[]{"", 4f, false, TYPE_DASH});
        }

        /**
         * Remove characters unsafe inside PDF string literals.
         * Only allow printable Latin-1 (32-255), escape parens and backslash.
         */
        private static String clean(String s) {
            if (s == null) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if      (ch == '(')            sb.append("\\(");
                else if (ch == ')')            sb.append("\\)");
                else if (ch == '\\')           sb.append("\\\\");
                else if (ch == '\r' || ch == '\n') sb.append(' ');
                else if (ch >= 32 && ch < 256) sb.append(ch);
                else                           sb.append(' ');
            }
            return sb.toString();
        }

        /**
         * Render all entries into a complete, valid PDF 1.4 byte array.
         *
         * PDF coordinate origin = bottom-left corner.
         * We begin near the top (y = PAGE_H - MARGIN_TOP) and decrease y per line.
         *
         * Object map:
         *   1 0 obj  Catalog
         *   2 0 obj  Pages
         *   3 0 obj  Page  (media box, fonts, content ref)
         *   4 0 obj  Content stream
         *   5 0 obj  Font /F1 = Helvetica
         *   6 0 obj  Font /F2 = Helvetica-Bold
         */
        byte[] generate() throws Exception {

            StringBuilder cs = new StringBuilder();
            float y = PAGE_H - MARGIN_TOP;

            for (Object[] e : entries) {
                String  text = (String)  e[0];
                float   size = (Float)   e[1];
                boolean bold = (Boolean) e[2];
                int     type = (Integer) e[3];

                if (type == TYPE_LINE) {
                    // Thin horizontal rule
                    y -= 4;
                    cs.append(String.format(Locale.US,
                        "0.4 w 0.5 0.5 0.5 RG %d %.2f m %d %.2f l S\n",
                        MARGIN_LEFT, y, PAGE_W - MARGIN_LEFT, y));
                    y -= 5;

                } else if (type == TYPE_BLANK) {
                    y -= size;

                } else if (type == TYPE_DASH) {
                    // Grey dashed text divider
                    y -= 3;
                    String dashes =
                        "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - " +
                        "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -";
                    cs.append(String.format(Locale.US,
                        "BT /F1 7 Tf 0.7 0.7 0.7 rg %d %.2f Td (%s) Tj 0 0 0 rg ET\n",
                        MARGIN_LEFT, y, dashes));
                    y -= 9;

                } else {
                    // Normal text line
                    y -= (size + 3);

                    // If we hit the bottom margin, insert a simple page break marker
                    if (y < 55) {
                        y = PAGE_H - MARGIN_TOP - size - 3;
                        // Draw a thin line to mark the logical break
                        cs.append(String.format(Locale.US,
                            "0.4 w 0.8 0.8 0.8 RG %d %.2f m %d %.2f l S\n",
                            MARGIN_LEFT, y + size + 6, PAGE_W - MARGIN_LEFT, y + size + 6));
                    }

                    String font = bold ? "/F2" : "/F1";
                    cs.append(String.format(Locale.US,
                        "BT %s %.1f Tf %d %.2f Td (%s) Tj ET\n",
                        font, size, MARGIN_LEFT, y, text));
                }
            }

            // Convert content stream to bytes
            byte[] csBytes = cs.toString().getBytes("ISO-8859-1");

            // ---- Build PDF objects ----

            String obj1 = "1 0 obj\n<</Type /Catalog /Pages 2 0 R>>\nendobj\n";

            String obj2 = "2 0 obj\n<</Type /Pages /Kids [3 0 R] /Count 1>>\nendobj\n";

            String obj3 =
                "3 0 obj\n" +
                "<</Type /Page\n" +
                "  /Parent 2 0 R\n" +
                "  /MediaBox [0 0 " + PAGE_W + " " + PAGE_H + "]\n" +
                "  /Contents 4 0 R\n" +
                "  /Resources <</Font <</F1 5 0 R /F2 6 0 R>>>>\n" +
                ">>\n" +
                "endobj\n";

            String obj4head = "4 0 obj\n<</Length " + csBytes.length + ">>\nstream\n";
            String obj4tail = "\nendstream\nendobj\n";

            String obj5 =
                "5 0 obj\n" +
                "<</Type /Font /Subtype /Type1\n" +
                "  /BaseFont /Helvetica\n" +
                "  /Encoding /WinAnsiEncoding>>\n" +
                "endobj\n";

            String obj6 =
                "6 0 obj\n" +
                "<</Type /Font /Subtype /Type1\n" +
                "  /BaseFont /Helvetica-Bold\n" +
                "  /Encoding /WinAnsiEncoding>>\n" +
                "endobj\n";

            // ---- Assemble with byte-offset tracking ----

            List<byte[]> parts   = new ArrayList<>();
            int[]        offsets = new int[7]; // offsets[1..6]

            byte[] hdr = "%PDF-1.4\n".getBytes("ISO-8859-1");
            parts.add(hdr);
            int pos = hdr.length;

            byte[] b1 = obj1.getBytes("ISO-8859-1");
            offsets[1] = pos; parts.add(b1); pos += b1.length;

            byte[] b2 = obj2.getBytes("ISO-8859-1");
            offsets[2] = pos; parts.add(b2); pos += b2.length;

            byte[] b3 = obj3.getBytes("ISO-8859-1");
            offsets[3] = pos; parts.add(b3); pos += b3.length;

            byte[] b4h = obj4head.getBytes("ISO-8859-1");
            offsets[4] = pos;
            parts.add(b4h);   pos += b4h.length;
            parts.add(csBytes); pos += csBytes.length;
            byte[] b4t = obj4tail.getBytes("ISO-8859-1");
            parts.add(b4t);   pos += b4t.length;

            byte[] b5 = obj5.getBytes("ISO-8859-1");
            offsets[5] = pos; parts.add(b5); pos += b5.length;

            byte[] b6 = obj6.getBytes("ISO-8859-1");
            offsets[6] = pos; parts.add(b6); pos += b6.length;

            // ---- Cross-reference table ----

            int xrefPos = pos;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n0 7\n");
            xref.append("0000000000 65535 f \n");
            for (int i = 1; i <= 6; i++)
                xref.append(String.format("%010d 00000 n \n", offsets[i]));
            xref.append("trailer\n<</Size 7 /Root 1 0 R>>\n");
            xref.append("startxref\n").append(xrefPos).append("\n%%EOF\n");
            parts.add(xref.toString().getBytes("ISO-8859-1"));

            // ---- Merge all parts ----
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (byte[] part : parts) out.write(part);
            return out.toByteArray();
        }
    }
}