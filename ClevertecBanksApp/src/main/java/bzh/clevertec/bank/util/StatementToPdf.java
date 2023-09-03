package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.BankStatement;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Класс формирующий выписку в формате pdf
 */
public class StatementToPdf {

    private String fileNameBase = "statement";
    private final PdfWriter pdfWriter;

    public StatementToPdf(String basePath) throws IOException {
        Path path = Path.of(basePath, "WEB-INF", "check");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        String fileName = fileNameBase + ".pdf";
        path = Path.of(basePath, "WEB-INF", "check", fileName);
        File fullFileName = path.toFile();
        pdfWriter = new PdfWriter(fullFileName);
    }

    public StatementToPdf(OutputStream os) {
        pdfWriter = new PdfWriter(os);
    }

    /**
     * Создается pdf-представление выписки и направляется либо в файл либо в поток вывода - исходя из того,
     * с каким параметром был вызван конструктор при создании объекта
     * @param statement - объект содержащий информацию по выпеске
     * @throws IOException
     */
    public void createStatementPdf(BankStatement statement) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(pdfWriter);
        Document doc = new Document(pdfDoc);

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("arial.ttf");
        PdfFont font = PdfFontFactory.createFont(is.readAllBytes(), PdfEncodings.IDENTITY_H);
        doc.setFont(font);

        Paragraph paragraph;
        paragraph = new Paragraph("Statement");
        paragraph.setFontSize(14).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER);
        doc.add(paragraph);
        createMainPartOfDocument(doc, statement);
        Table table2 = new Table(UnitValue.createPercentArray(new float[]{25, 45, 30}));
        table2.useAllAvailableWidth()
                .setBorderTop(new SolidBorder(1)).setBorderBottom(new SolidBorder(1))
                .setMarginTop(0);
        table2.addCell(new Cell().add(getSimpleCell("Date", TextAlignment.LEFT)));
        table2.addCell(new Cell().add(getSimpleCell("Description", TextAlignment.CENTER)));
        table2.addCell(new Cell().add(getSimpleCell("Amount", TextAlignment.LEFT)));
        for (ExtendTransactionData it : statement.getItems()) {
            table2.addCell(new Cell().add(getSimpleCell(it.getCarryOutAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    TextAlignment.LEFT)));
            if (it.getTransactionType() == OperationType.ADDING || it.getTransactionType() == OperationType.WITHDRAW) {
                table2.addCell(new Cell().add(getSimpleCell(it.getTransactionType(), TextAlignment.LEFT)));
            } else if (statement.getAccountId() == it.getAccountIdTo()) {
                table2.addCell(new Cell().add(getSimpleCell("Getting money from " + it.getNameFrom(), TextAlignment.LEFT)));
            } else {
                table2.addCell(new Cell().add(getSimpleCell("Transfer money to " + it.getNameFrom(), TextAlignment.LEFT)));
            }
            table2.addCell(new Cell().add(getSimpleCell(String.format("%.2f", it.getSum() / 100.0), TextAlignment.LEFT)));
        }
        doc.add(table2);
        doc.close();
    }

    /**
     * Создается pdf-представление выписки с оборотами по счету и направляется либо в файл либо в поток вывода - исходя из того,
     * с каким параметром был вызван конструктор при создании объекта
     * @param statement - объект содержащий информацию по выписке с оборотами по счету
     * @throws IOException
     */
    public void createTurnoverStatementPdf(BankStatement statement) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(pdfWriter);
        Document doc = new Document(pdfDoc);

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("arial.ttf");
        PdfFont font = PdfFontFactory.createFont(is.readAllBytes(), PdfEncodings.IDENTITY_H);
        doc.setFont(font);

        Paragraph paragraph;
        paragraph = new Paragraph("Money statement");
        paragraph.setFontSize(14).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER);
        doc.add(paragraph);
        createMainPartOfDocument(doc, statement);
        Table table2 = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        table2.useAllAvailableWidth()
                .setBorderTop(new SolidBorder(1)).setBorderBottom(new SolidBorder(1))
                .setMarginTop(0);
        table2.addCell(new Cell().add(getSimpleCell("Getting", TextAlignment.CENTER)));
        table2.addCell(new Cell().add(getSimpleCell("Withdrawal", TextAlignment.CENTER)));
        List<Long> turnover = statement.getTurnover();
        table2.addCell(new Cell().add(getSimpleCell(String.format("%.2f", turnover.get(1)/100.0), TextAlignment.CENTER)));
        table2.addCell(new Cell().add(getSimpleCell(String.format("%.2f", turnover.get(0)/100.0), TextAlignment.CENTER)));

        doc.add(table2);
        doc.close();
    }


    /**
     * Создать одиночную ячейку таблицы без внешней рамки
     * @param text      - содержимое ячейки
     * @param alignment - выравнивание содержимого ячейки
     * @return - сформированная ячейка
     */
    private Cell getSimpleCell(Object text, TextAlignment alignment) {
        Paragraph p = new Paragraph(text.toString());
        p.setFontSize(12).setBorder(Border.NO_BORDER);
        return new Cell(1, 1)
                .setTextAlignment(alignment)
                .add(p)
                .setBorder(Border.NO_BORDER);
    }


     // Создание основной части выписки
    private void createMainPartOfDocument(Document doc, BankStatement statement){
        Paragraph paragraph = new Paragraph(statement.getBankName());
        paragraph.setFontSize(14).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER);
        doc.add(paragraph);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        table.useAllAvailableWidth()
                .setBorderTop(new SolidBorder(1)).setBorderBottom(new SolidBorder(1))
                .setMarginTop(20);
        table.addCell(new Cell().add(getSimpleCell("Client", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(statement.getOwner(), TextAlignment.LEFT)));

        table.addCell(new Cell().add(getSimpleCell("Account", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(statement.getAccountNumber(), TextAlignment.LEFT)));

        table.addCell(new Cell().add(getSimpleCell("Currency", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(statement.getCurrency(), TextAlignment.LEFT)));

        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatterDataTime = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

        table.addCell(new Cell().add(getSimpleCell("Open Date", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(statement.getCreateAt().format(formatterData), TextAlignment.LEFT)));

        table.addCell(new Cell().add(getSimpleCell("Period", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(statement.getBeginPeriod().format(formatterData) + " - "
                + statement.getEndPeriod().format(formatterData), TextAlignment.LEFT)));

        table.addCell(new Cell().add(getSimpleCell("Create at", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(statement.getDate().format(formatterDataTime), TextAlignment.LEFT)));

        table.addCell(new Cell().add(getSimpleCell("Balance", TextAlignment.LEFT)));
        table.addCell(new Cell().add(getSimpleCell(String.format("%.2f", statement.getBalance() / 100.0), TextAlignment.LEFT)));
        doc.add(table);
    }
}
