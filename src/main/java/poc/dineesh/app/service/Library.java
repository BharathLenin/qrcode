package poc.dineesh.app.service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.xmlgraphics.image.loader.impl.imageio.ImageIOUtil;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.RendererException;
import org.ghost4j.renderer.SimpleRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.imageio.ImageIO;

import ai.api.AIConfiguration;
import ai.api.AIConfiguration.SupportedLanguages;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
@CrossOrigin
@RestController
@RequestMapping(value = "/qr")
public class Library {
	
	private static Integer BASE_FONT_SIZE = 24;
	private static String printURL = "http://stuxsh01.stXXXX.homedepot.com:12020/OutputAPI/print/v2";
    
    @CrossOrigin
    @RequestMapping(value = "/getlabel", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8",produces = "application/json; charset=UTF-8")
    
    public String createlabel(@RequestBody Request request) throws DocumentException, IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	Document document = null;
		document = new Document(PageSize.LETTER.rotate(), 30.0f, 30.0f, 30.0f,
				30.0f);
		PdfWriter writer;

		BaseFont baseFont = BaseFont.createFont("HWYGNRRW.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
		Font whiteFont = new Font(baseFont, (BASE_FONT_SIZE*42)/10, Font.NORMAL, BaseColor.WHITE);

		writer = PdfWriter.getInstance(document, outputStream);
		PdfPTable headerTable = new PdfPTable(3);
		headerTable.setWidthPercentage(100);
		document.open();

		float[] columnWidths = { 1f, 3f, 1f };
		headerTable.setWidths(columnWidths);
		headerTable.setSpacingBefore(30);
		headerTable.addCell(frameDepartmentInformation(
				request.getDepartmentNumber(), writer, whiteFont));
		headerTable.addCell(frameQrInfo(request.getQrinfo(), baseFont));
		headerTable.addCell(frameSkuName(request.getSkuName(), baseFont));

		document.add(headerTable);
		document.add(frameQuantiyInformation(request.getPrice(), baseFont));
		document.add(frameSkuInformation(request.getSkuNumber(), baseFont));

		if (document != null && document.isOpen())
			document.close();
		
		writer.close();
		
		
		FileOutputStream fos = null;
		try {
		    fos = new FileOutputStream(new File("dineesh.pdf")); 

		    // Put data in your baos

		    outputStream.writeTo(fos);
		} catch(IOException ioe) {
		    // Handle exception here
		    ioe.printStackTrace();
		} finally {
		    fos.close();
		}
		PDDocument document1 = PDDocument.load(new File("dineesh.pdf"));
		PDFRenderer pdfRenderer = new PDFRenderer(document1);
		for (int page = 0; page < document1.getNumberOfPages(); ++page)
		{ 
		    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

		    // suffix in filename will be used as the file format
		    org.apache.pdfbox.tools.imageio.ImageIOUtil.writeImage(bim, "dineesh" + "-" + (page+1) + ".bmp", 300);
		    ImageIO.write(bim, "bmp", outputStream);
		}
		document1.close();
		HttpResponse POSTresponse = null;	

		try {
					HttpClient httpclient = HttpClientBuilder.create().build(); 
					HttpPost httpPost = new HttpPost(printURL.replace("XXXX", request.getStoreNumber()));
					ByteArrayBody uploadFilePart = new ByteArrayBody(outputStream.toByteArray(),"autoPalletTagPDF.pdf");
					StringBody ux_queue_name = new StringBody(request.getPrintQueueName());
					MultipartEntity reqEntity = new MultipartEntity();
					reqEntity.addPart("ux_queue_name", ux_queue_name);
					reqEntity.addPart("file_1", uploadFilePart);
					httpPost.setEntity(reqEntity);
					POSTresponse = httpclient.execute(httpPost);
					HttpEntity resEntity = POSTresponse.getEntity();
					EntityUtils.consume(resEntity);
					} catch (Exception e) {
						return "pdf not printed";
					}

		return "pdf printed";


        
        
    
}
    
    
    public PdfPCell frameDepartmentInformation(String departmentNumber,
			PdfWriter writer, Font whiteFont) throws BadElementException {

		PdfPCell cell;
		if (!departmentNumber.equals("")) {

			PdfContentByte canvas = writer.getDirectContent();
			PdfTemplate template = canvas.createTemplate(160f, 160f);

			template.setColorFill(BaseColor.BLACK);
			template.circle(80f, 80f, 80);
			template.fillStroke();
			Image img = Image.getInstance(template);
			ColumnText.showTextAligned(template, Element.ALIGN_CENTER,
					new Phrase(departmentNumber, whiteFont), 80, 45, 0);
			cell = new PdfPCell(img);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

		} else {
			cell = new PdfPCell();
			cell.setBorder(Rectangle.NO_BORDER);
		}
		return cell;

	}
    
    public PdfPCell frameQrInfo(String qrinfo, BaseFont baseFont) throws BadElementException {
    	BarcodeQRCode barcodeQRCode = new BarcodeQRCode(qrinfo, 1000, 1000, null);
    	Image codeQrImage = barcodeQRCode.getImage();
    	codeQrImage.scaleAbsolute(100, 100);
		PdfPCell cell = new PdfPCell(codeQrImage);
		cell.setColspan(1);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingLeft(40);

		return cell;

	}
    
    public PdfPCell frameSkuName(String skuname, BaseFont baseFont) {
		int textHeightInGlyphSpaceForDate = baseFont.getAscent(skuname)
				- baseFont.getDescent(skuname);
		float fontSizeForDate = 190f * 172f / textHeightInGlyphSpaceForDate;
		Font blackFontForLDAP = new Font(baseFont, fontSizeForDate, Font.NORMAL,
				BaseColor.BLACK);
		PdfPCell cell = new PdfPCell(new Paragraph(skuname, blackFontForLDAP));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		return cell;
	}
    
    public PdfPTable frameQuantiyInformation(String quantity, BaseFont baseFont) {
		Font blackFontForRow2 = new Font(baseFont, (BASE_FONT_SIZE*145)/24, Font.NORMAL,
				BaseColor.BLACK);

		PdfPTable quantityTable = new PdfPTable(new float[] { 25, 35, 40 });
		quantityTable.setWidthPercentage(100);
		quantityTable.setSpacingBefore(30);

		PdfPCell emptyCell = new PdfPCell();
		emptyCell.setBorder(Rectangle.NO_BORDER);
		PdfPCell quantityNameCell = new PdfPCell(new Paragraph("AMT",
				blackFontForRow2));
		quantityNameCell.setBorder(Rectangle.NO_BORDER);
		quantityNameCell.setPaddingTop(-8);
		PdfPCell quantityValueCell = new PdfPCell(new Paragraph((quantity.equals("0") ? "" : quantity+"$"),
				blackFontForRow2));
		quantityValueCell.setBorder(Rectangle.BOX);
		quantityValueCell.setBorderWidth(5);
		quantityValueCell.setPaddingBottom(25);
		quantityValueCell.setBorderColor(BaseColor.BLACK);
		quantityValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

		quantityValueCell.setPaddingRight(30);
		quantityValueCell.setFixedHeight(172f);
		quantityValueCell.setPaddingTop(-8);

		quantityTable.addCell(emptyCell);
		quantityTable.addCell(quantityNameCell);
		quantityTable.addCell(quantityValueCell);

		return quantityTable;

	}

	public PdfPTable frameSkuInformation(String skuNumber, BaseFont baseFont) {
		PdfPTable skuTable = new PdfPTable(1);
		skuTable.setWidthPercentage(120);
		skuTable.setSpacingBefore(18);
		Font blackFontForSku = new Font(baseFont, (BASE_FONT_SIZE*160)/24, Font.NORMAL,
				BaseColor.BLACK);
		PdfPCell skuCell = new PdfPCell(new Paragraph(skuNumber,
				blackFontForSku));
		skuCell.setBorder(Rectangle.NO_BORDER);
		skuCell.setFixedHeight(172f);
		skuCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		skuTable.addCell(skuCell);
		return skuTable;

	}

}   


