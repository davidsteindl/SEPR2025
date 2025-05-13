package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PdfExportDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/pdf-export")
public class PdfExportEndpoint {

    @PostMapping
    @Secured("ROLE_USER")
    @Operation(summary = "Generate a PDF (Ticket, Invoice, Storno)", security = @SecurityRequirement(name = "apiKey"))
    public void exportPdf(@RequestBody PdfExportDto dto, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + dto.getType() + ".pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        String title = switch (dto.getType()) {
            case "ticket" -> "Ticket für:";
            case "invoice" -> "Rechnung für:";
            case "storno" -> "Stornorechnung für:";
            default -> "Dokument für:";
        };

        document.add(new Paragraph(title));
        document.add(new Paragraph(dto.getFirstName() + " " + dto.getLastName()));
        document.add(new Paragraph("Event: " + dto.getEventName()));
        document.add(new Paragraph("Datum: " + dto.getEventDate()));
        document.add(new Paragraph("Ort: " + dto.getLocation()));
        document.add(new Paragraph("Preis: " + dto.getPrice() + " EUR"));

        document.close();
    }
}
