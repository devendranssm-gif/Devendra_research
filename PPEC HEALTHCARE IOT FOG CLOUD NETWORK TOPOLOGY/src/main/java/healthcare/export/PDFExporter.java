package healthcare.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import healthcare.metrics.MetricsCollector.ScenarioResult;
import healthcare.utils.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * PDFExporter.java
 * Generates a formatted PDF research report using iText 5.
 */
public class PDFExporter {

    private static final Logger LOG = Logger.getLogger(PDFExporter.class.getName());

    private static final Font TITLE_FONT   = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   BaseColor.DARK_GRAY);
    private static final Font SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   BaseColor.WHITE);
    private static final Font CELL_FONT    = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, BaseColor.BLACK);

    public PDFExporter() {
        try { Files.createDirectories(Paths.get(Constants.REPORT_DIR)); }
        catch (IOException e) { LOG.warning("Could not create report dir: " + e.getMessage()); }
    }

    public void exportReport(List<ScenarioResult> results) {
        String path = Constants.REPORT_DIR + "PPEC_Simulation_Report.pdf";
        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            addTitle(doc);
            addConfigSection(doc);
            addResultsTable(doc, results);
            addFooter(doc);

            doc.close();
            System.out.println("[PDF] Report written: " + path);
        } catch (Exception e) {
            LOG.warning("[PDFExporter] Failed: " + e.getMessage());
        }
    }

    private void addTitle(Document doc) throws DocumentException {
        doc.add(new Paragraph("\n"));
        Paragraph title = new Paragraph(
            "PPEC Healthcare Fog Simulation Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph(
            "Privacy-Preserving and Energy-Efficient Computation Framework\n" +
            "for Secure IoT-Enabled Smart Healthcare using Fog Computing",
            new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, BaseColor.GRAY));
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);

        doc.add(new Paragraph("Generated: " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY)));
        doc.add(Chunk.NEWLINE);
    }

    private void addConfigSection(Document doc) throws DocumentException {
        doc.add(new Paragraph("Simulation Configuration", SECTION_FONT));
        doc.add(new Paragraph(
            "  Fog Nodes       : " + Constants.NUM_FOG_NODES + "\n" +
            "  Coverage Radius : " + Constants.COVERAGE_RADIUS + " m\n" +
            "  Simulation Area : " + Constants.SIMULATION_AREA_WIDTH + " × " + Constants.SIMULATION_AREA_HEIGHT + " m\n" +
            "  Simulation Time : " + Constants.SIMULATION_TIME + " s\n" +
            "  Cloud MIPS      : " + Constants.CLOUD_MIPS + "\n" +
            "  Fog MIPS        : " + Constants.FOG_MIPS + "\n" +
            "  ECC Curve       : " + Constants.ECC_CURVE + "\n" +
            "  Encryption      : " + Constants.ECC_ALGORITHM + "\n",
            new Font(Font.FontFamily.COURIER, 9, Font.NORMAL)));
        doc.add(Chunk.NEWLINE);
    }

    private void addResultsTable(Document doc, List<ScenarioResult> results) throws DocumentException {
        doc.add(new Paragraph("Performance Results", SECTION_FONT));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2f, 2f, 2f, 2f, 2f});

        addHeaderCell(table, "IoT Devices");
        addHeaderCell(table, "Latency (ms)");
        addHeaderCell(table, "Energy (J)");
        addHeaderCell(table, "Cost ($)");
        addHeaderCell(table, "Network (MB)");
        addHeaderCell(table, "Exec Time (ms)");

        boolean alt = false;
        for (ScenarioResult r : results) {
            BaseColor bg = alt ? new BaseColor(245, 245, 245) : BaseColor.WHITE;
            addDataCell(table, String.valueOf(r.iotCount),                         bg);
            addDataCell(table, String.format("%.2f",  r.avgLatencyMs),            bg);
            addDataCell(table, String.format("%.4f",  r.energyJoules),            bg);
            addDataCell(table, String.format("%.6f",  r.executionCostUSD),        bg);
            addDataCell(table, String.format("%.2f",  r.networkUsageMB),          bg);
            addDataCell(table, String.format("%.2f",  r.executionTimeMs),         bg);
            alt = !alt;
        }
        doc.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new BaseColor(50, 50, 150));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
            "PPEC Framework — ECC + DILP | iFogSim2 Simulation",
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)));
    }
}
