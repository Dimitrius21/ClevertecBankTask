package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.BankStatement;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class StatementToPdfTest {

    @Test
    void createStatementPdf() throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StatementToPdf pdfCreator = new StatementToPdf(os);

        BankStatement statement = getStatement();
        List<ExtendTransactionData> extTransactions = new ArrayList<>(1);
        extTransactions.add(new ExtendTransactionData(1, 500, "BYN", 0, 0, "", 1, 1500,
                "Jon", LocalDateTime.of(2023, 10, 22, 14, 15), OperationType.ADDING));
        statement.setItems(extTransactions);

        pdfCreator.createStatementPdf(statement);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PdfReader reader = new PdfReader(is);

        PdfDocument pdfDoc = new PdfDocument(reader);
        String pageContent = "";
        for (int page = 1; page <= pdfDoc.getNumberOfPages(); page++) {
            ITextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
            pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(page), strategy);
        }
        pdfDoc.close();
        reader.close();

        List<String> contains = Arrays.asList("Statement", statement.getBankName(), "Client",
                "Account", "Currency", "Open Date", "Period", "Create at", "Balance", "Date", "Description", "Amount");
        Assertions.assertThat(pageContent).contains(contains);
    }


    @Test
    void createTurnoverStatementPdf() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StatementToPdf pdfCreator = new StatementToPdf(os);

        BankStatement statement = getStatement();
        List<Long> turnovers = List.of(5000L, 4000L);
        statement.setTurnover(turnovers);

        pdfCreator.createTurnoverStatementPdf(statement);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PdfReader reader = new PdfReader(is);

        PdfDocument pdfDoc = new PdfDocument(reader);
        String pageContent = "";
        for (int page = 1; page <= pdfDoc.getNumberOfPages(); page++) {
            ITextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
            pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(page), strategy);
        }
        pdfDoc.close();
        reader.close();

        List<String> contains = Arrays.asList("Money statement", statement.getBankName(), "Client",
                "Account", "Currency", "Open Date", "Period", "Create at", "Balance", "Getting", "Withdrawal");
        Assertions.assertThat(pageContent).contains(contains);
    }

    private BankStatement getStatement() {
        BankStatement statement = new BankStatement();
        statement.setDate(LocalDateTime.now());
        statement.setAccountId(1);
        statement.setAccountNumber("BY11CLBK181901001");
        statement.setBankName("Clever-Bank");
        statement.setCurrency("BYN");
        statement.setCreateAt(LocalDateTime.of(2023, 10, 1, 15, 20));
        LocalDateTime from = LocalDateTime.of(2023, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2023, 10, 31, 23, 59);
        statement.setBeginPeriod(from);
        statement.setEndPeriod(to);
        statement.setOwner("Jon");
        List<ExtendTransactionData> extTransactions = new ArrayList<>(2);
        statement.setBalance(1500);
        return statement;
    }
}