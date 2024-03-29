package com.maveric.hr360.service.implementation;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormCreator;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.maveric.hr360.entity.Employee;
import com.maveric.hr360.entity.Questions;
import com.maveric.hr360.repository.EmployeeRepository;
import com.maveric.hr360.repository.QuestionsRepository;
import com.maveric.hr360.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.maveric.hr360.utils.Constants.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class PDFReportService {
    
    private final QuestionsRepository questionsRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    @Value("${templates.path}")
    private String templatePath;

    @Value("${output.path}")
    private String outputPath;
    @Value("${pagenumber.added.filepath}")
    private String pageNumberAddedFilePath;

    public File createReport(Map<String,byte[]> chartDetails,Map<String,List<String>> messageDetails,String employeeId,
                             String grade) throws Exception {
        log.info("Generating pdf for employee {} started.",employeeId);
        String outputPdfPath = outputPath+employeeId+".pdf";
        // Create a PdfWriter object for the output PDF
        PdfWriter writer = new PdfWriter(outputPdfPath);
        PdfDocument outputPdfDoc = new PdfDocument(writer);
        Document document = new Document(outputPdfDoc);
        getFirstPage(outputPdfDoc);
        getMavericDetailsPage(outputPdfDoc);
        document.add(new AreaBreak());
        getEmployeeDetailsPage(outputPdfDoc,document,employeeId);
        getHowToUsePage(outputPdfDoc,document);
        getAnalyzeReportPage(outputPdfDoc,document);
        addOverallPage(outputPdfDoc,document,chartDetails.get(OVERALL));
        addSectionWiseDetails(chartDetails, messageDetails, outputPdfDoc, document, grade);
        addTrainingDetailsPage(outputPdfDoc,document,messageDetails,"D",grade);
        geRadarChartPage(outputPdfDoc,document,chartDetails.get("Radar"));
        getLastPage(outputPdfDoc);

        outputPdfDoc.close();
        log.info("Generating pdf for employee {} completed.",employeeId);
        return  new File(outputPdfPath);
    }

    private void addSectionWiseDetails(Map<String, byte[]> chartDetails, Map<String, List<String>> messageDetails,
                                       PdfDocument outputPdfDoc, Document document, String grade) throws IOException {
        addIndividualCategoryDetails(chartDetails, messageDetails, outputPdfDoc,"L","L0",
                document,LEADERSHIP_SKILLS, grade);
        getNoteForSelfPage(outputPdfDoc, document);
        addIndividualCategoryDetails(chartDetails, messageDetails, outputPdfDoc,"C","C0",
                document,CLIENT_MANAGEMENT, grade);
        addStrengthAndFocus(outputPdfDoc, document, messageDetails,"C");
        getNoteForSelfPage(outputPdfDoc, document);
        addIndividualCategoryDetails(chartDetails, messageDetails, outputPdfDoc,"T","T0",
                document,TEAM_MANAGEMENT, grade);
        addStrengthAndFocus(outputPdfDoc, document, messageDetails,"T");
        getNoteForSelfPage(outputPdfDoc, document);
        addIndividualCategoryDetails(chartDetails, messageDetails, outputPdfDoc,"D","D0",
                document,DELIVERY_MANAGEMENT, grade);
        addStrengthAndFocus(outputPdfDoc, document, messageDetails,"D");
        getNoteForSelfPage(outputPdfDoc,document);
    }

    private void addSectionDetails(byte[] overallImageData,byte[] imageData, PdfDocument outputPdfDoc, Document document,
                                          String questionId,String questionText,String sectionHeader) throws IOException {

           addTemplateWithFooter(outputPdfDoc);
           document.add(new AreaBreak());
           document.add(new Paragraph(sectionHeader)
                   .setBold()
                   .setFontColor(FONT_COLOR)
                   .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                   .setMarginTop(30f)
                   .setFont(getPdfFont())
                   .setFontSize(16));
           document.add(new Paragraph(""));

           document.add(new Paragraph(SECTION_DETAILS.replace("sectionHeader",sectionHeader))
                .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                .setFontColor(FONT_COLOR).setFontSize(12))
                   .setFont(getPdfFont());
           document.add(new Paragraph(""));
           Image img = new Image(ImageDataFactory.create(overallImageData)).setMarginLeft(MARGIN_LEFT_FONT_SIZE);
           img.setWidth(IMAGE_WIDTH);
           img.setHeight(IMAGE_HEIGHT);
           document.add(img);
           document.add(getEmptyParagraph());
           addChartToPdf(document, imageData,questionId,questionText);

        // Close all PdfDocument objects
    }

    private void addChartToPdf(Document document, byte[] imageData,String questionId,String questionText) {

            Image img = new Image(ImageDataFactory.create(imageData)).setMarginLeft(25f);
            img.setWidth(IMAGE_WIDTH);
            img.setHeight(IMAGE_HEIGHT);
            addBorder(document);
            float[] columnWidth = {125f,410f};
            Table table = new Table(columnWidth);
            // question id text
            table.addCell(new Cell().add(new Paragraph(questionId))
                    .setFontColor(FONT_COLOR)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24));
            // question text
            table.addCell(new Cell().add(new Paragraph(questionText))
                    .setBorderBottom(new SolidBorder(1))
                    .setBorderTop(new SolidBorder(1))
                    .setBorderLeft(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setFontSize(11));
            document.add(table);
            document.add(getEmptyParagraph());
            document.add(img);

    }

    private static void addBorder(Document document) {
        Paragraph paragraph = getEmptyParagraph();
        paragraph.setBorderTop(new SolidBorder(FONT_COLOR,1f)); // Sets a solid bottom border with width 1 point
        document.add(paragraph);
    }

    private static Paragraph getEmptyParagraph() {
        return new Paragraph("\n");
    }

    public void getFirstPage(PdfDocument outputPdfDoc) throws IOException {
        String pageToCopyPath = templatePath+"FirstPage.pdf"; // Path to the PDF containing the page to copy
        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageToCopyPath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
    }


    private void addTemplateWithFooter(PdfDocument outputPdfDoc) throws IOException {
        String pageToCopyPath = templatePath+"Template.pdf"; // Path to the PDF containing the page to copy
        addPageNumber(outputPdfDoc, pageToCopyPath);

        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageNumberAddedFilePath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
        deleteFile(pageNumberAddedFilePath);
    }

    private void addPageNumber(PdfDocument outputPdfDoc, String pageToCopyPath) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(pageToCopyPath), new PdfWriter(pageNumberAddedFilePath));
        PdfAcroForm form = PdfFormCreator.getAcroForm(pdfDoc, true);
        form.setGenerateAppearance(true);
        form.getField("pageNo").setValue(String.valueOf(outputPdfDoc.getNumberOfPages() +1));
        form.flattenFields();
        pdfDoc.close();
    }

    public void getMavericDetailsPage(PdfDocument outputPdfDoc) throws IOException {
        String pageToCopyPath = templatePath+"MavericDetails.pdf"; // Path to the PDF containing the page to copy
        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageToCopyPath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
    }

    public void getHowToUsePage(PdfDocument outputPdfDoc,Document document) throws IOException {
        String pageToCopyPath = templatePath+"HowToUse.pdf"; // Path to the PDF containing the page to copy
        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageToCopyPath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
        document.add(new AreaBreak());
    }

    public void getAnalyzeReportPage(PdfDocument outputPdfDoc,Document document) throws IOException {
        String pageToCopyPath = templatePath+"AnalyzeReport.pdf"; // Path to the PDF containing the page to copy
        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageToCopyPath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
        document.add(new AreaBreak());
    }

    public void getNoteForSelfPage(PdfDocument outputPdfDoc,Document document) throws IOException {
        String pageToCopyPath = templatePath+"NoteForSelf.pdf"; // Path to the PDF containing the page to copy
        addPageNumber(outputPdfDoc, pageToCopyPath);

        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageNumberAddedFilePath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
        deleteFile(pageNumberAddedFilePath);
        document.add(new AreaBreak(outputPdfDoc.getDefaultPageSize()));
    }

    public void getLastPage(PdfDocument outputPdfDoc) throws IOException {
        String pageToCopyPath = templatePath+"LastPage.pdf"; // Path to the PDF containing the page to copy
        addPageNumber(outputPdfDoc, pageToCopyPath);

        // Open the PDF containing the page to copy
        PdfReader pageToCopyReader = new PdfReader(pageNumberAddedFilePath);
        PdfDocument pageToCopyDoc = new PdfDocument(pageToCopyReader);
        // Copy the first page from the 'pageToCopyDoc' to the 'existingPdfDoc'
        pageToCopyDoc.copyPagesTo(1, 1, outputPdfDoc);
        pageToCopyDoc.close();
        deleteFile(pageNumberAddedFilePath);
    }

    public void getEmployeeDetailsPage(PdfDocument outputPdfDoc,Document document,String employeeId) throws Exception {
        addTemplateWithFooter(outputPdfDoc);
        document.add(new AreaBreak());
        Employee emp = getEmployee(employeeId);
        String fullName = EmployeeDetailsServiceImpl.getDecryptedValue(emp.getEmployeeFullName());
        float[] columnWidth = {150f,445f};
        Table table = new Table(columnWidth);
        Cell imageCell = new Cell().setBorder(Border.NO_BORDER);
        imageCell.add(new Image(ImageDataFactory.create(employeeService.getImageByEmployeeId(employeeId)))
                .setHeight(EMPLOYEE_IMAGE_DIMENSION).setWidth(EMPLOYEE_IMAGE_DIMENSION));
        Paragraph textBelowImage = new Paragraph(getEmployeeDetails(emp)).setBold();
        textBelowImage.setFontColor(FONT_COLOR); // Set font color
        textBelowImage.setFontSize(10f).setFont(getPdfFont()); // Set font size
        textBelowImage.setTextAlignment(TextAlignment.CENTER);

        // Add the paragraph to the cell
        imageCell.add(textBelowImage.setMarginTop(10f));
        table.setHeight(700f).setMarginTop(30f).setMarginLeft(10f);
        table.addCell(imageCell.setPaddingLeft(25f));
        Cell userDetailsCell =new Cell().setBorder(Border.NO_BORDER);
        userDetailsCell.add(new Paragraph(fullName).setFontColor(FONT_COLOR)
                .setFontSize(28f)).setTextAlignment(TextAlignment.RIGHT);
        userDetailsCell.add(new Paragraph("Leadership Development")
                .setMarginTop(20f).setBold()).setFontSize(16f).setFont(getPdfFont());
        userDetailsCell.add(new Paragraph("360 Degree Insights Summary").setFontSize(HEADER_FONT_SIZE)).setFont(getPdfFont());
        userDetailsCell.add(new Paragraph("\n\n"));
        userDetailsCell.add(new Paragraph("Dear "+fullName.split(" ")[0]+",").setFontSize(HEADER_FONT_SIZE)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBold()
                        .setMarginLeft(20f)
                        .setFont(getPdfFont())
                        .setFontColor(FONT_COLOR));
        userDetailsCell.add(new Paragraph(SUMMARY).setFontSize(12)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginLeft(20f)
                .setMarginTop(10f)
                .setFontColor(FONT_COLOR)).setFont(getPdfFont());
        userDetailsCell.add(new Image(ImageDataFactory.create(templatePath+MAVERIC_LOGO)).setWidth(100f).setMarginLeft(MARGIN_LEFT_FONT_SIZE));
        table.addCell(userDetailsCell);
        document.add(table.setFontColor(FONT_COLOR));
    }

    public void addIndividualCategoryDetails(Map<String,byte[]> chartDetails,Map<String,List<String>> messageDetails,
                                             PdfDocument outputPdfDoc, String questionIdPrefix,String overallSectionId,
                                             Document document,String sectionHeader,String grade) throws IOException {
        List<Questions> questions = questionsRepository.
                findByQuestionIdStartingWithAndLevelAndQuestionType(questionIdPrefix, grade,"Score");
        questions.sort((Comparator.comparingInt(Questions::getSequence)));
        for(int i =0;i< questions.size();i++){
            if(i == 0){
                addSectionDetails(chartDetails.get(overallSectionId),
                        chartDetails.get(questions.get(i).getQuestionId())
                        ,outputPdfDoc,document,questions.get(i).getQuestionId()
                        ,questions.get(i).getQuestion(),sectionHeader);
            }else {
                addTemplateWithFooter(outputPdfDoc);
                document.add(new AreaBreak());
                document.add(getEmptyParagraph());
                addChartToPdf(document,chartDetails.get(questions.get(i).getQuestionId()),
                        questions.get(i).getQuestionId(),questions.get(i).getQuestion());
                i++;
                if(i == questions.size()){
                    addLeadershipTraining(questionIdPrefix, document,messageDetails);
                    continue;
                }
                addChartToPdf(document,chartDetails.get(questions.get(i).getQuestionId()),
                        questions.get(i).getQuestionId(),questions.get(i).getQuestion());
                if(i+1 == questions.size()){
                    addLeadershipTraining(questionIdPrefix, document,messageDetails);
                }
            }
        }
    }

    private void addLeadershipTraining(String questionIdPrefix, Document document,Map<String,List<String>> messageDetails) {
        if(questionIdPrefix.equalsIgnoreCase("L")){
            document.add(new Paragraph(LEADERSHIP_HEADER)
                    .setFontSize(HEADER_FONT_SIZE)
                    .setFontColor(FONT_COLOR)
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setFont(getPdfFont())
                    .setBold());
            document.add(new Paragraph(messageDetails.get(questionIdPrefix+"_Training").get(0))
                    .setFontSize(12)
                    .setFontColor(FONT_COLOR)
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setFont(getPdfFont())
                    .setMarginTop(10f));
        }
    }

    private Employee getEmployee(String employeeId){
        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        if(employee != null){
            return employee;
        }else {
            throw new RuntimeException("Employee Details missing in employee table ");
        }
    }

    private String getEmployeeDetails(Employee employee){
        return employee.getAccount()+"\n"+
                EmployeeDetailsServiceImpl.getDecryptedValue(employee.getMavericMail())+"\n"+
                EmployeeDetailsServiceImpl.getDecryptedValue(employee.getBusinessPhoneNumber());
    }

    private void deleteFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }

    private void addStrengthAndFocus(PdfDocument pdfDocument,Document document,
                                     Map<String,List<String>> messageDetails,String questionIdPrefix) throws IOException {
        addTemplateWithFooter(pdfDocument);
        document.add(new AreaBreak());
        document.add(new Paragraph());
        addBorder(document);
        document.add(new Paragraph("Strengths")
                .setFontSize(HEADER_FONT_SIZE)
                .setFontColor(FONT_COLOR)
                .setBold()
                .setFont(getPdfFont())
                .setMarginLeft(MARGIN_LEFT_FONT_SIZE));
        com.itextpdf.layout.element.List strengthList = new com.itextpdf.layout.element.List();
        strengthList.setMarginTop(20f).setMarginLeft(MARGIN_LEFT_FONT_SIZE);
        List<String> bulletPoints = messageDetails.get(questionIdPrefix+"_Strength");
        for (String bulletPoint : bulletPoints) {
            ListItem item = new ListItem(bulletPoint);
            item.setFontColor(new DeviceRgb(0, 0, 88))
                    .setFontSize(12)
                    .setFont(getPdfFont());
            strengthList.add(item);
        }

        document.add(strengthList);
        com.itextpdf.layout.element.List focusList = new com.itextpdf.layout.element.List();
        focusList.setMarginTop(20f).setMarginLeft(MARGIN_LEFT_FONT_SIZE);
        document.add(new Paragraph("Focus")
                .setFontSize(HEADER_FONT_SIZE)
                .setFontColor(FONT_COLOR)
                .setBold()
                .setFont(getPdfFont())
                .setMarginLeft(MARGIN_LEFT_FONT_SIZE));
        bulletPoints = messageDetails.get(questionIdPrefix+"_Focus");
        for (String bulletPoint : bulletPoints) {
            ListItem item = new ListItem(bulletPoint);
            item.setFontColor(new DeviceRgb(0, 0, 88))
                    .setFontSize(12)
                    .setFont(getPdfFont());
            focusList.add(item);
        }
        document.add(focusList);
    }

    private void addOverallPage(PdfDocument pdfDocument,Document document,byte[] imageData) throws IOException {
        addTemplateWithFooter(pdfDocument);
        document.add(new AreaBreak());
        addBorder(document);
        document.add(new Paragraph("Overall Ratings")
                .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                .setFontColor(FONT_COLOR)
                .setFontSize(16));
        document.add(new Paragraph(OVERALL_SECTION)
                .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                .setFontColor(FONT_COLOR)
                .setMarginTop(10f)
                .setFontSize(12));
        Image img = new Image(ImageDataFactory.create(imageData)).setMarginLeft(MARGIN_LEFT_FONT_SIZE);
        img.setWidth(IMAGE_WIDTH);
        img.setHeight(IMAGE_HEIGHT);
        document.add(img);
    }

    public void geRadarChartPage(PdfDocument outputPdfDoc,Document document,byte[] imageData) throws IOException {
        addTemplateWithFooter(outputPdfDoc);
        document.add(new AreaBreak());
        Image img = new Image(ImageDataFactory.create(imageData));
        img.setWidth(RADAR_CHART_WIDTH);
        img.setHeight(RADAR_CHART_HEIGHT);
        document.add(getEmptyParagraph());
        addBorder(document);
        document.add(new Paragraph(RADAR_CHART_HEADER)
                .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                .setFontColor(FONT_COLOR)
                .setFontSize(HEADER_FONT_SIZE));
        document.add(getEmptyParagraph());
        document.add(img);
    }

    private String getOverallPageDetails(){
        return """
                LS -> Leadership Skill
                TM -> Team Management
                DM -> Delivery Management
                CM -> Customer Management""";
    }

    private PdfFont getPdfFont(){
        PdfFont font ;
        try {
            font = PdfFontFactory.createFont(templatePath+"arial.ttf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return font;
    }

    private void addTrainingDetailsPage(PdfDocument pdfDocument,Document document,Map<String,
            List<String>> messageDetails,String questionIdPrefix,String grade) throws IOException {
        if (grade.equalsIgnoreCase("Manager")) {
            addTemplateWithFooter(pdfDocument);
            document.add(new AreaBreak());
            addBorder(document);
            document.add(new Paragraph(DOMAIN_TRAINING_HEADER)
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setBold()
                    .setMarginTop(10)
                    .setFontColor(FONT_COLOR)
                    .setFontSize(HEADER_FONT_SIZE));
            document.add(new Paragraph(messageDetails.get(questionIdPrefix+"_DomainTraining").get(0))
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setMarginTop(10)
                    .setFontColor(FONT_COLOR)
                    .setFontSize(HEADER_FONT_SIZE));
            document.add(new Paragraph(PROJECT_MANAGEMENT_HEADER)
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setMarginTop(10)
                    .setBold()
                    .setFontColor(FONT_COLOR)
                    .setFontSize(HEADER_FONT_SIZE));
            document.add(new Paragraph(messageDetails.get(questionIdPrefix+"_DeliveryManagementTraining").get(0))
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setMarginTop(10)
                    .setFontColor(FONT_COLOR)
                    .setFontSize(HEADER_FONT_SIZE));
            document.add(new Paragraph(TECHNICAL_TRAINING_HEADER)
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setMarginTop(10)
                    .setBold()
                    .setFontColor(FONT_COLOR)
                    .setFontSize(HEADER_FONT_SIZE));
            document.add(new Paragraph(messageDetails.get(questionIdPrefix+"_TechnicalTraining").get(0))
                    .setMarginLeft(MARGIN_LEFT_FONT_SIZE)
                    .setMarginTop(10)
                    .setFontColor(FONT_COLOR)
                    .setFontSize(HEADER_FONT_SIZE));
        }
    }

}
