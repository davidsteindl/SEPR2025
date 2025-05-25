package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthorizationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfExportService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Autowired
    public PdfExportServiceImpl(
        TicketRepository ticketRepository,
        OrderRepository orderRepository,
        UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void makeTicketPdf(Long id, OutputStream responseBody) {

        System.out.println(id);
        var ticket = ticketRepository.findById(id).orElseThrow(NotFoundException::new);
        var idloggedin = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        if (!ticket.getOrder().getUserId().equals(idloggedin)) {
            throw new AuthorizationException("You are not authorized to export this ticket.");
        }

        PdfWriter writer = new PdfWriter(responseBody);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Ticket"));
        document.add(new Paragraph("Show: " + ticket.getShow().getName()));
        document.add(new Paragraph("Event: " + ticket.getShow().getEvent().getName()));
        document.add(new Paragraph("Location: " + ticket.getShow().getEvent().getLocation().getName()));
        document.add(new Paragraph("Location: " + ticket.getShow().getEvent().getLocation().getCity()));
        document.add(new Paragraph("Location: " + ticket.getShow().getEvent().getLocation().getStreet()));
        document.add(new Paragraph("Location: " + ticket.getShow().getEvent().getLocation().getPostalCode()));
        document.add(new Paragraph("Date: " + ticket.getShow().getDate()));
        document.add(new Paragraph("Room: " + ticket.getSector().getRoom().getName()));
        document.add(new Paragraph("Sector: " + ticket.getSector().getId()));
        if (ticket.getSeat() != null) {
            document.add(new Paragraph("Seat: " + ticket.getSeat().getRowNumber() + ticket.getSeat().getColumnNumber()));
        }

        document.add(new Paragraph("Price: " + ticket.getSector().getPrice() + " EUR"));
        document.add(new Paragraph("Ticket Id: " + ticket.getId()));

        // Generate QR Code
        String qrContent = "http://localhost:4200/ticket/" + ticket.getId();
        BufferedImage qrImage = generateQrCodeImage(qrContent);

        // Convert BufferedImage to iText Image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(qrImage, "PNG", baos);
        } catch (IOException e) {
            throw new RuntimeException("Error while generating QR code", e);
        }
        ImageData imageData = ImageDataFactory.create(baos.toByteArray());
        Image qrCode = new Image(imageData);

        // Add QR Code to the PDF
        document.add(qrCode);




        document.close();
    }

    private BufferedImage generateQrCodeImage(String content) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException("Error while generating QR Code", e);
        }
    }



    @Override
    @Transactional
    public void makeInvoicePdf(Long id, OutputStream responseBody) {

        System.out.println(id);
        var order = orderRepository.findById(id).orElseThrow(NotFoundException::new);
        final var user = userRepository.findById(order.getUserId()).orElseThrow(NotFoundException::new);
        var idloggedin = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        if (!order.getUserId().equals(idloggedin)) {
            throw new AuthorizationException("You are not authorized to export this invoice.");
        }

        PdfWriter writer = new PdfWriter(responseBody);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);


        var ticketLine = new Paragraph("""
            TicketLine
            Verkauf von Tickets für Kino, Theater, Opern, Konzerte und mehr
            Karlsplatz 13, 1040 Wien
            Tel.: 0043 1 523543210, Mail: shop@ticketline.at
            www.tickeltine.at""");
        ticketLine.setTextAlignment(TextAlignment.RIGHT);
        document.add(ticketLine);


        var adresspronom = switch(user.getSex()) {
            case Sex.FEMALE -> "Mrs./Frau";
            case Sex.MALE -> "Mr./Herr";
            default -> "";
        };

        document.add(new Paragraph(adresspronom));

        document.add(new Paragraph(user.getFirstName() + " " + user.getLastName()));

        if (user.getStreet() != null && user.getHousenumber() != null && user.getPostalCode() != null
            && user.getCity() != null && user.getCountry() != null) {
            document.add(new Paragraph(user.getStreet() + " " + user.getHousenumber()));
            document.add(new Paragraph(user.getPostalCode() + " " + user.getCity()));
            document.add(new Paragraph(user.getCountry()));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        var invoiceDate = new Paragraph(dateFormat.format(new Date()));
        invoiceDate.setTextAlignment(TextAlignment.RIGHT);
        document.add(invoiceDate);

        var invoiceNumber = new Paragraph("Invoice Number / Rechnung Nr." + order.getId());
        invoiceNumber.setTextAlignment(TextAlignment.CENTER);
        invoiceNumber.setBold();
        document.add(invoiceNumber);

        // Creating a table
        Table table = new Table(6);

        // Adding cells to the table
        table.addCell(new Cell().add(new Paragraph("Datum")));
        table.addCell(new Cell().add(new Paragraph("Bezeichnung")));
        table.addCell(new Cell().add(new Paragraph("Menge")));
        table.addCell(new Cell().add(new Paragraph("Platzwahl")));
        table.addCell(new Cell().add(new Paragraph("Ust.")));
        table.addCell(new Cell().add(new Paragraph("Gesamt")));

        var sum = 0;
        for (var ticket : order.getTickets()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            table.addCell(new Cell().add(new Paragraph(ticket.getShow().getDate().format(formatter))));
            table.addCell(new Cell().add(new Paragraph(ticket.getShow().getEvent().getName())));
            table.addCell(new Cell().add(new Paragraph("1")));
            var seatchoice = ticket.getSector().getRoom().getName() + ","
                + ticket.getSector().getId();
            if (ticket.getSeat() != null) {
                seatchoice = seatchoice + "," + ticket.getSeat().getRowNumber() + ","
                    + ticket.getSeat().getColumnNumber();
            }
            table.addCell(new Cell().add(new Paragraph(seatchoice)));

            var bruttoPrice = ticket.getSector().getPrice();
            sum = sum + bruttoPrice;
            var ust = bruttoPrice - (bruttoPrice / (1 + 0.13));

            table.addCell(new Cell().add(new Paragraph(String.format("%.02f", ust))));
            table.addCell(new Cell().add(new Paragraph(ticket.getSector().getPrice() + " EUR")));
        }

        // Adding Table to document
        document.add(table);


        var ust = sum - (sum / (1 + 0.13));

        document.add(new Paragraph("Summe " + sum + " EUR"));
        document.add(new Paragraph("Betrag enthält wie folgt: " + String.format("%.02f", ust)));
        document.add(new Paragraph("USt 13% (ermäßigter Steuersatz für Konzerte und Opernkarten etc)"));
        document.add(new Paragraph("""
            Bitte um Bezahlung unter Angabe der Nummer der Honorarnote
            auf das Konto der TicketLine Gmbh IBAN BIC binnen 7 Tagen.

            Wir wünschen Ihnen einen interessanten und angenehmen Veranstaltungsbesuch!

            Freundliche Grüße,
            Das TicketLine Team"""));
        document.add(new Paragraph("UID: ATU1234567"));


        document.close();
    }

    @Override
    @Transactional
    public void makeCancelInvoicePdf(Long id, OutputStream responseBody) {

        System.out.println(id);
        var order = orderRepository.findById(id).orElseThrow(NotFoundException::new);
        var idloggedin = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        if (!order.getUserId().equals(idloggedin)) {
            throw new AuthorizationException("You are not authorized to export this cancellation invoice.");
        }

        PdfWriter writer = new PdfWriter(responseBody);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Cancellation of Invoice"));

        document.close();
    }


}
