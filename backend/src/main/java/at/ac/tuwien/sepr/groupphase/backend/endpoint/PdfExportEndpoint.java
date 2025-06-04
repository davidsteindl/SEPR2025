package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pdf-export")
public class PdfExportEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PdfExportService pdfExportService;

    @Autowired
    public PdfExportEndpoint(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/tickets/{ticketId}")
    @Secured("ROLE_USER")
    @Operation(summary = "Generate a Ticket PDF.", security = @SecurityRequirement(name = "apiKey"))
    public void exportTicketPdf(@PathVariable("ticketId") Long ticketId, HttpServletResponse response) throws
        IOException, ValidationException {

        LOGGER.info("GET /tickets/{ticketId}");
        var pdf = new ByteArrayOutputStream();
        pdfExportService.makeTicketPdf(ticketId, pdf, Optional.empty());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Ticket" + ".pdf");
        pdf.writeTo(response.getOutputStream());

    }

    @GetMapping(value = "/tickets/{ticketId}/{randomTicketCode}")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "View a Ticket PDF in the browser.", security = @SecurityRequirement(name = "apiKey"))
    public void viewTicketPdf(@PathVariable("ticketId") Long ticketId, HttpServletResponse response,
                                                               @PathVariable("randomTicketCode") String randomTicketCode) throws IOException, ValidationException {
        LOGGER.info("GET /tickets/{ticketId}/{randomTicketCode}");

        var pdf = new ByteArrayOutputStream();
        pdfExportService.makeTicketPdf(ticketId, pdf, Optional.of(randomTicketCode));
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Ticket" + ticketId + ".pdf");
        pdf.writeTo(response.getOutputStream());


    }

    @GetMapping("/invoice/{orderId}")
    @Secured("ROLE_USER")
    @Operation(summary = "Generate an Invoice Pdf.", security = @SecurityRequirement(name = "apiKey"))
    public void exportInvoicePdf(@PathVariable("orderId") Long orderId, HttpServletResponse response) throws IOException {

        LOGGER.info("GET /invoice/{orderId}");
        var pdf = new ByteArrayOutputStream();
        pdfExportService.makeInvoicePdf(orderId, pdf);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Invoice" + orderId + ".pdf");
        pdf.writeTo(response.getOutputStream());


    }

    @GetMapping("/cancelinvoice/{orderId}")
    @Secured("ROLE_USER")
    @Operation(summary = "Generate a CancellationInvoice Pdf.", security = @SecurityRequirement(name = "apiKey"))
    public void exportCancelInvoicePdf(@PathVariable("orderId") Long orderId, HttpServletResponse response) throws IOException, ValidationException {

        LOGGER.info("GET /cancelinvoice/{orderId}");
        var pdf = new ByteArrayOutputStream();
        pdfExportService.makeCancelInvoicePdf(orderId, pdf);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=CancellationInvoice" + orderId + ".pdf");
        pdf.writeTo(response.getOutputStream());


    }

}
