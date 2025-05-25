package at.ac.tuwien.sepr.groupphase.backend.service;

import java.io.OutputStream;

/**
 * Service for pdf export of ticket.
 *
 */
public interface PdfExportService {

    /**
     * Export of Ticket as Pdf.
     *
     * @param id the unique identifier of the ticket to export
     * @param responseBody outputStream of the http responseBody
     */
    void makeTicketPdf(Long id, OutputStream responseBody);

    /**
     * Export Invoice of an Order as Pdf.
     *
     * @param id the unique identifier of the order to export
     * @param responseBody outputStream of the http responseBody
     */
    void makeInvoicePdf(Long id, OutputStream responseBody);

    /**
     * Export the Cancellation of an Invoice as Pdf.
     *
     * @param id the unique identifier of the canceled order to export
     * @param responseBody outputStream of the http responseBody
     */
    void makeCancelInvoicePdf(Long id, OutputStream responseBody);

}
