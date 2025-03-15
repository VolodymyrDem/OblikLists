package com.work.oblikpodorojlist.managers;
import com.work.oblikpodorojlist.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class DocumentsManager {
    private static DocumentsManager instance;

    private DocumentsManager(){}

    public static DocumentsManager getInstance() {
        if(instance == null){
            instance = new DocumentsManager();
        }
        return instance;
    }

    private String[] folders = {
            "01_Cars",
            "02_Workers",
            "03_Orders",
            "04_ReportsOrd",
            "05_TravelDocs",
            "06_OrdersReg",
            "07_FuelReg",
            "08_TravelDocsReg"
    };

    public String[] getFolders() {
        return folders;
    }

    public void setFolders(String[] folders) {
        this.folders = folders;
    }

    public void createFolders(String code){
        String folderPath = getDocsFolderPath() + "DocFiles\\"+code;
        File backupFolder = new File(folderPath);

        if (!backupFolder.exists()) {
            if (backupFolder.mkdirs()) {
                System.out.println("Папка для файлів створена: " + folderPath);
            } else {
                System.out.println("Не вдалося створити папку для бекапів.");
                return;
            }
        }

        for (String folder : folders) {
            String path = folderPath + "\\"+folder;
            File file = new File(path);
            if (!file.exists()) {
                if (file.mkdirs()) {
                    System.out.println("Папка для файлів створена: " + folderPath);
                } else {
                    System.out.println("Не вдалося створити папку для бекапів.");
                    return;
                }
            }
        }
    }

    public String getDocsFolderPath() {

        // Якщо програма працює в середовищі розробки (наприклад, як .jar), беремо папку ресурсів
        URL resource = DBManager.class.getClassLoader().getResource("config");
        if (resource != null) {
            // Якщо ресурс існує, це ймовірно, коли програма запущена з .jar
            // Шлях до папки ресурсів (відносний)
            String resourcePath = resource.getPath();
            return new File(resourcePath).getParent() + File.separator;
        } else {
            // Якщо програма працює в .exe середовищі, використовуємо директорію виконуваного файлу
            String currentDir = System.getProperty("user.dir");
            return currentDir + File.separator;
        }
    }

    private static void setParagraphSpacing(XWPFParagraph paragraph) {
        CTP ctp = paragraph.getCTP();
        CTPPr ctpPr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();
        CTSpacing spacing = ctpPr.isSetSpacing() ? ctpPr.getSpacing() : ctpPr.addNewSpacing();

        spacing.setBefore(BigInteger.valueOf(0)); // 5pt before each paragraph
        spacing.setAfter(BigInteger.valueOf(0));  // 5pt after each paragraph
    }

    private String getNameSurname(String fullname) {
        String[] words = fullname.split(" ");
        String newString = "";
        if (words.length >= 2) {
            newString = words[0] + " " + words[1];
        }
        return newString;
    }

    private String getNAmeInitials(String fullname) {
        String[] words = fullname.split(" ");
        String newString = "";
        if (words.length >= 2) {
            newString = words[0] + " " + words[1].charAt(0) + "." + words[2].charAt(0) + ".";
        }
        return newString;
    }

    // Method to create and save a Word document
    public void createOrderDocument(DBManager dbManager, _Order order) {
        try {
            _Company _company = dbManager.getCompanyInfo();

            _Worker _worker = dbManager.getWorker(order.getIdWorker());
            LocalDate departureDate = order.getStartDate();
            LocalDate arrivalDate = order.getEndDate();
            LocalDate currentDate = order.getOrderDate();
            String number = order.getOrderNumber();
            String path = order.getRoute();
            String money = String.valueOf(order.getMoney());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            long days = ChronoUnit.DAYS.between(departureDate, arrivalDate);

            days++;

            String DAYS = new String(" днів");
            if(days%10 == 1 && days != 11)
                DAYS = " день";
            else if((days%10 == 2 || days%10 == 3 || days%10 == 4 ) && days != 12 && days != 13 && days != 14)
                DAYS = " дні";

            String formattedDate = currentDate.format(formatter);

            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[2] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            fileChooser.setDialogTitle("Save Word Document");
            String filename = "Наказ №"+order.getOrderNumber();
            fileChooser.setSelectedFile(new File(filename+".docx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .docx extension
                if (!fileToSave.getName().endsWith(".docx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".docx");
                }

                // Create a new Word document using Apache POI
                XWPFDocument document = new XWPFDocument();

                XWPFParagraph TOV = document.createParagraph();
                TOV.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun TOVrun = TOV.createRun();
                TOVrun.setFontFamily("Bookman Old Style");
                TOVrun.setBold(true);
                TOVrun.setFontSize(16);
                TOVrun.setText(_company.getTypeFull() + "\n");

                setParagraphSpacing(TOV);


                XWPFParagraph name = document.createParagraph();
                name.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun nameRun = name.createRun();
                nameRun.setFontFamily("Bookman Old Style");
                nameRun.setBold(true);
                nameRun.setFontSize(24);
                nameRun.setText("«"+_company.getName()+"»");

                CTP ctp = name.getCTP();
                CTPPr ctpPr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();
                CTBorder border = ctpPr.isSetPBdr() ? ctpPr.getPBdr().addNewBottom() : ctpPr.addNewPBdr().addNewBottom();

                border.setVal(STBorder.SINGLE);
                border.setSz(BigInteger.valueOf(12)); // Thickness (1/8 pt units, 24 = 3pt)
                border.setColor("000000");

                setParagraphSpacing(name);


                XWPFParagraph address = document.createParagraph();
                XWPFRun addressRun = address.createRun();
                addressRun.setFontFamily("Bookman Old Style");
                addressRun.setBold(true);
                addressRun.setItalic(true);
                address.setAlignment(ParagraphAlignment.CENTER);
                addressRun.setFontSize(12);
                addressRun.setText(_company.getAddress());

                setParagraphSpacing(address);


                XWPFParagraph code = document.createParagraph();
                XWPFRun codeRun = code.createRun();
                codeRun.setFontFamily("Bookman Old Style");
                codeRun.setBold(true);
                codeRun.setItalic(true);
                code.setAlignment(ParagraphAlignment.CENTER);
                codeRun.setFontSize(12);
                codeRun.setText("Код ЄДРПОУ " + String.valueOf(_company.getCode()));

                setParagraphSpacing(code);

                XWPFParagraph separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph orderTitle = document.createParagraph();
                orderTitle.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun orderTitleRun = orderTitle.createRun();
                orderTitleRun.setFontFamily("Times New Roman");
                orderTitleRun.setText("НАКАЗ");
                orderTitleRun.setBold(true);
                orderTitleRun.setFontSize(14);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                setParagraphSpacing(orderTitle);

                XWPFParagraph date = document.createParagraph();
                date.setAlignment(ParagraphAlignment.LEFT);

                // Set tab stops
                CTPPr ppr = date.getCTP().getPPr();
                if (ppr == null) {
                    ppr = date.getCTP().addNewPPr();
                }
                CTTabs tabs = ppr.addNewTabs();
                CTTabStop tabStop = tabs.addNewTab();
                tabStop.setVal(STTabJc.RIGHT); // Right alignment
                tabStop.setPos(BigInteger.valueOf(9000)); // Adjust position as needed

                XWPFRun dateRun = date.createRun();
                dateRun.setFontFamily("Times New Roman");
                dateRun.setFontSize(12);

                dateRun.setText(formattedDate); // Add the date
                dateRun.addTab();  // Move cursor to the tab stop
                dateRun.setText("№ " + number); // Add the number at the right

                setParagraphSpacing(date);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph text = document.createParagraph();
                text.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun textRun = text.createRun();
                textRun.setFontFamily("Times New Roman");
                textRun.setText("Про направлення у відрядження");
                textRun.setBold(true);
                textRun.setItalic(true);
                textRun.setFontSize(12);

                setParagraphSpacing(text);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph text1 = document.createParagraph();
                text1.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun text1Run = text1.createRun();
                text1Run.setFontFamily("Times New Roman");
                text1Run.setText("НАКАЗУЮ");
                text1Run.setBold(true);
                text1Run.setFontSize(14);

                setParagraphSpacing(text1);

                XWPFParagraph body1 = document.createParagraph();
                body1.setAlignment(ParagraphAlignment.BOTH);
                XWPFRun body1Run = body1.createRun();
                body1Run.setFontFamily("Times New Roman");
                body1Run.setText(
                        "1. Відрядити "+ _worker.getNameR() +
                        ", "+dbManager.getWorkerPosition(false, _worker.getId())+", у відрядження до міст: "+
                                path +
                        " на " + days + DAYS +
                        " з "+ departureDate.format(formatter) +
                        " по "+ arrivalDate.format(formatter) +
                        " з метою  "+order.getGoal() +".");
                body1Run.setFontSize(12);

                setParagraphSpacing(body1);

                XWPFParagraph body2 = document.createParagraph();
                body2.setAlignment(ParagraphAlignment.BOTH);
                XWPFRun body2Run = body2.createRun();
                body2Run.setFontFamily("Times New Roman");
                body2Run.setText(
                        "2. Бухгалтерії провести нарахування та виплату грошових коштів на відрядження у розмірі " +
                                money+" грн." +
                                " на добу.");
                body2Run.setFontSize(12);

                setParagraphSpacing(body2);

                XWPFParagraph body3 = document.createParagraph();
                body3.setAlignment(ParagraphAlignment.BOTH);
                XWPFRun body3Run = body3.createRun();
                body3Run.setFontFamily("Times New Roman");
                body3Run.setText(
                        "3. Після повернення з відрядження " +
                                getNAmeInitials(_worker.getNameR()) +
                                " звітувати щодо виконання поставленого завдання та суми використаних коштів у порядку та строки, передбачені законодавством. ");
                body3Run.setFontSize(12);

                setParagraphSpacing(body3);

                XWPFParagraph body4 = document.createParagraph();
                body4.setAlignment(ParagraphAlignment.BOTH);
                XWPFRun body4Run = body4.createRun();
                body4Run.setFontFamily("Times New Roman");
                body4Run.setText(
                        "4. Контроль за виконанням наказу залишаю за собою.");
                body4Run.setFontSize(12);

                setParagraphSpacing(body4);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph director = document.createParagraph();
                director.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun directorRun = director.createRun();
                directorRun.setFontFamily("Times New Roman");
                directorRun.setText((Objects.equals(order.getHead(), _company.getCeo()))?"Директор":"В.о. директора");
                directorRun.setBold(true);
                directorRun.setFontSize(12);

                setParagraphSpacing(director);

                // Create the first paragraph
                XWPFParagraph TOV1 = document.createParagraph();
                TOV1.setAlignment(ParagraphAlignment.LEFT);  // Left-align the first part
                ppr = TOV1.getCTP().getPPr();
                if (ppr == null) {
                    ppr = TOV1.getCTP().addNewPPr();
                }
                tabs = ppr.addNewTabs();

                // Add tab stops for right alignment
                CTTabStop leftTab = tabs.addNewTab();
                leftTab.setVal(STTabJc.LEFT);
                leftTab.setPos(BigInteger.valueOf(3600)); // Adjust this position as needed

                CTTabStop rightTab = tabs.addNewTab();
                rightTab.setVal(STTabJc.RIGHT);
                rightTab.setPos(BigInteger.valueOf(9000)); // Right-aligned tab stop

                XWPFRun TOV1Run = TOV1.createRun();
                TOV1Run.setFontFamily("Times New Roman");
                TOV1Run.setFontSize(12);
                TOV1Run.setBold(true);
                TOV1Run.setText(_company.getTypeShort() + "«"+_company.getName()+"»");
                TOV1Run.addTab();  // Move to the next tab position (right)
                TOV1Run.setText("_________________");
                TOV1Run.addTab();  // Move to the next tab position (right)
                TOV1Run.setText(getNameSurname(order.getHead()));


                setParagraphSpacing(TOV1);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph readBy = document.createParagraph();
                readBy.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun readByRun = readBy.createRun();
                readByRun.setFontFamily("Times New Roman");
                readByRun.setText("З наказом ознайомлені:");
                readByRun.setBold(true);
                readByRun.setFontSize(12);

                setParagraphSpacing(readBy);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                // Create another paragraph for the next field (like the accountant)
                XWPFParagraph accountant = document.createParagraph();
                accountant.setAlignment(ParagraphAlignment.LEFT);  // Left-align the first part
                ppr = accountant.getCTP().getPPr();
                if (ppr == null) {
                    ppr = accountant.getCTP().addNewPPr();
                }
                tabs = ppr.addNewTabs();

                leftTab = tabs.addNewTab();
                leftTab.setVal(STTabJc.LEFT);
                leftTab.setPos(BigInteger.valueOf(3600)); // Left-aligned position

                rightTab = tabs.addNewTab();
                rightTab.setVal(STTabJc.RIGHT);
                rightTab.setPos(BigInteger.valueOf(9000)); // Right-aligned tab stop

                XWPFRun accountantRun = accountant.createRun();
                accountantRun.setFontFamily("Times New Roman");
                accountantRun.setFontSize(12);
                accountantRun.setText("Головний бухгалтер");
                accountantRun.addTab();  // Move to the next tab position (right)
                accountantRun.setText("_________________");
                accountantRun.addTab();  // Move to the next tab position (right)
                accountantRun.setText(getNameSurname(_company.getAccountant()));

                setParagraphSpacing(accountant);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();
                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                // Repeat for other fields (e.g., worker)
                XWPFParagraph worker = document.createParagraph();
                worker.setAlignment(ParagraphAlignment.LEFT);  // Left-align the first part
                ppr = worker.getCTP().getPPr();
                if (ppr == null) {
                    ppr = worker.getCTP().addNewPPr();
                }
                tabs = ppr.addNewTabs();

                leftTab = tabs.addNewTab();
                leftTab.setVal(STTabJc.LEFT);
                leftTab.setPos(BigInteger.valueOf(3600)); // Left-aligned position

                rightTab = tabs.addNewTab();
                rightTab.setVal(STTabJc.RIGHT);
                rightTab.setPos(BigInteger.valueOf(9000)); // Right-aligned tab stop

                XWPFRun workerRun = worker.createRun();
                workerRun.setFontFamily("Times New Roman");
                workerRun.setFontSize(12);
                workerRun.setText(dbManager.getWorkerPosition(true, _worker.getId()));
                workerRun.addTab();  // Move to the next tab position (right)
                workerRun.setText("_________________");
                workerRun.addTab();  // Move to the next tab position (right)
                workerRun.setText(getNameSurname(_worker.getNameN()));

                setParagraphSpacing(worker);

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();


                try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                    document.write(out);
                    JOptionPane.showMessageDialog(null, "Document saved successfully!");

                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(fileToSave);
                    } else {
                        JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving document: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void applyBordersToMergedRegion(Sheet sheet, CellRangeAddress range, CellStyle style) {
        for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
            Row currentRow = sheet.getRow(row);
            if (currentRow == null) {
                currentRow = sheet.createRow(row);
            }
            for (int col = range.getFirstColumn(); col <= range.getLastColumn(); col++) {
                Cell currentCell = currentRow.getCell(col);
                if (currentCell == null) {
                    currentCell = currentRow.createCell(col);
                }
                currentCell.setCellStyle(style);
            }
        }
    }

    public void createList(DBManager dbManager, _List  list) {
        try {
            _Company _company = _Company.getInstance();
            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[4] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            String filename = "Подорожній лист №"+String.valueOf(list.getNumber());
            fileChooser.setSelectedFile(new File(filename+".xlsx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .xlsx extension
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Create a new Excel workbook and populate it with sample data
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {

                    _Car car = dbManager.getCar(list.getIdCar());
                    _Worker worker = dbManager.getWorker(list.getIdWorker());

                    Sheet sheet = workbook.createSheet(dbManager.getCarNumber(list.getIdCar()));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                    XSSFFont font = workbook.createFont();
                    font.setBold(true);

                    XSSFFont font14 = workbook.createFont();
                    font14.setBold(true);
                    font14.setFontHeight(14);

                    XSSFFont font12 = workbook.createFont();
                    font12.setBold(true);
                    font12.setFontHeight(12);

                    sheet.setColumnWidth(0, (int)(8.09 * 256 * 1.08));
                    sheet.setColumnWidth(1, (int)(8.09 * 256 * 1.08));
                    sheet.setColumnWidth(2, (int)(8.09 * 256 * 1.08));
                    sheet.setColumnWidth(3, (int)(10.91 * 256 * 1.08));
                    sheet.setColumnWidth(4, (int)(14.36 * 256 * 1.08));
                    sheet.setColumnWidth(5, (int)(12.18 * 256 * 1.08));
                    sheet.setColumnWidth(6, (int)(11.82 * 256 * 1.08));
                    sheet.setColumnWidth(7, (int)(4.09 * 256 * 1.08));
                    sheet.setColumnWidth(8, (int)(6.91 * 256 * 1.08));
                    sheet.setColumnWidth(9, (int)(7.82 * 256 * 1.08));
                    sheet.setColumnWidth(10, (int)(7.82 * 256 * 1.08));
                    sheet.setColumnWidth(11, (int)(4.82 * 256 * 1.08));

                    CellStyle overStyle = workbook.createCellStyle();
                    overStyle.setWrapText(false);
                    overStyle.setFont(font14);

                    CellStyle borderTopStyle = workbook.createCellStyle();
                    borderTopStyle.setBorderTop(BorderStyle.THICK);

                    CellStyle borderTopThinStyle = workbook.createCellStyle();
                    borderTopThinStyle.setBorderTop(BorderStyle.THIN);

                    CellStyle borderTopLeftStyle = workbook.createCellStyle();
                    borderTopLeftStyle.setBorderTop(BorderStyle.THICK);
                    borderTopLeftStyle.setBorderLeft(BorderStyle.THICK);

                    CellStyle borderTopLeftStyleC = workbook.createCellStyle();
                    borderTopLeftStyleC.setBorderTop(BorderStyle.THICK);
                    borderTopLeftStyleC.setBorderLeft(BorderStyle.THICK);
                    borderTopLeftStyleC.setAlignment(HorizontalAlignment.CENTER);
                    borderTopLeftStyleC.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderTopLeftStyleB = workbook.createCellStyle();
                    borderTopLeftStyleB.setBorderTop(BorderStyle.THICK);
                    borderTopLeftStyleB.setBorderLeft(BorderStyle.THICK);
                    borderTopLeftStyleB.setFont(font);

                    CellStyle borderTopLeftStyleBC = workbook.createCellStyle();
                    borderTopLeftStyleBC.setBorderTop(BorderStyle.THICK);
                    borderTopLeftStyleBC.setBorderLeft(BorderStyle.THICK);
                    borderTopLeftStyleBC.setFont(font);
                    borderTopLeftStyleBC.setAlignment(HorizontalAlignment.CENTER);
                    borderTopLeftStyleBC.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderTopLeftStyleBCS = workbook.createCellStyle();
                    borderTopLeftStyleBCS.setBorderTop(BorderStyle.THICK);
                    borderTopLeftStyleBCS.setBorderLeft(BorderStyle.THICK);
                    borderTopLeftStyleBCS.setFont(font);
                    borderTopLeftStyleBCS.setAlignment(HorizontalAlignment.CENTER);
                    borderTopLeftStyleBCS.setVerticalAlignment(VerticalAlignment.CENTER);
                    borderTopLeftStyleBCS.setWrapText(true);

                    CellStyle borderTopLeftStyleCenter = workbook.createCellStyle();
                    borderTopLeftStyleCenter.setBorderTop(BorderStyle.THICK);
                    borderTopLeftStyleCenter.setBorderLeft(BorderStyle.THICK);
                    borderTopLeftStyleCenter.setAlignment(HorizontalAlignment.CENTER);
                    borderTopLeftStyleCenter.setVerticalAlignment(VerticalAlignment.CENTER);
                    borderTopLeftStyleCenter.setWrapText(true);


                    CellStyle borderTopRightStyle = workbook.createCellStyle();
                    borderTopRightStyle.setBorderTop(BorderStyle.THICK);
                    borderTopRightStyle.setBorderRight(BorderStyle.THICK);

                    CellStyle borderTopRightStyleC = workbook.createCellStyle();
                    borderTopRightStyleC.setBorderTop(BorderStyle.THICK);
                    borderTopRightStyleC.setBorderRight(BorderStyle.THICK);
                    borderTopRightStyleC.setAlignment(HorizontalAlignment.CENTER);
                    borderTopRightStyleC.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderLeftStyle = workbook.createCellStyle();
                    borderLeftStyle.setBorderLeft(BorderStyle.THICK);

                    CellStyle borderTopThinRightThickStyle = workbook.createCellStyle();
                    borderTopThinRightThickStyle.setBorderTop(BorderStyle.THIN);
                    borderTopThinRightThickStyle.setBorderRight(BorderStyle.THICK);

                    CellStyle borderTopThinLeftThickStyle = workbook.createCellStyle();
                    borderTopThinLeftThickStyle.setBorderTop(BorderStyle.THIN);
                    borderTopThinLeftThickStyle.setBorderLeft(BorderStyle.THICK);

                    CellStyle borderTopThinLeftThickStyleB = workbook.createCellStyle();
                    borderTopThinLeftThickStyleB.setBorderTop(BorderStyle.THIN);
                    borderTopThinLeftThickStyleB.setBorderLeft(BorderStyle.THICK);
                    borderTopThinLeftThickStyleB.setFont(font);

                    CellStyle borderTopThinRightThickBottomThickStyle = workbook.createCellStyle();
                    borderTopThinRightThickBottomThickStyle.setBorderTop(BorderStyle.THIN);
                    borderTopThinRightThickBottomThickStyle.setBorderRight(BorderStyle.THICK);
                    borderTopThinRightThickBottomThickStyle.setBorderBottom(BorderStyle.THICK);

                    CellStyle borderTopThinRightThickBottomThickStyleC = workbook.createCellStyle();
                    borderTopThinRightThickBottomThickStyleC.setBorderTop(BorderStyle.THIN);
                    borderTopThinRightThickBottomThickStyleC.setBorderRight(BorderStyle.THICK);
                    borderTopThinRightThickBottomThickStyleC.setBorderBottom(BorderStyle.THICK);
                    borderTopThinRightThickBottomThickStyleC.setAlignment(HorizontalAlignment.CENTER);
                    borderTopThinRightThickBottomThickStyleC.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderTopThinLeftThickBottomThickStyle = workbook.createCellStyle();
                    borderTopThinLeftThickBottomThickStyle.setBorderTop(BorderStyle.THIN);
                    borderTopThinLeftThickBottomThickStyle.setBorderLeft(BorderStyle.THICK);
                    borderTopThinLeftThickBottomThickStyle.setBorderBottom(BorderStyle.THICK);

                    CellStyle borderBottomThinStyle = workbook.createCellStyle();
                    borderBottomThinStyle.setBorderBottom(BorderStyle.THIN);

                    CellStyle borderTopThinLeftThickBottomThickStyleC = workbook.createCellStyle();
                    borderTopThinLeftThickBottomThickStyleC.setBorderTop(BorderStyle.THIN);
                    borderTopThinLeftThickBottomThickStyleC.setBorderLeft(BorderStyle.THICK);
                    borderTopThinLeftThickBottomThickStyleC.setBorderBottom(BorderStyle.THICK);
                    borderTopThinLeftThickBottomThickStyleC.setAlignment(HorizontalAlignment.CENTER);
                    borderTopThinLeftThickBottomThickStyleC.setVerticalAlignment(VerticalAlignment.CENTER);

                    CellStyle borderTopThinLeftThickBottomThickStyleB = workbook.createCellStyle();
                    borderTopThinLeftThickBottomThickStyleB.setBorderTop(BorderStyle.THIN);
                    borderTopThinLeftThickBottomThickStyleB.setBorderLeft(BorderStyle.THICK);
                    borderTopThinLeftThickBottomThickStyleB.setBorderBottom(BorderStyle.THICK);
                    borderTopThinLeftThickBottomThickStyleB.setFont(font);

                    CellStyle borderTopThinBottomThickStyle = workbook.createCellStyle();
                    borderTopThinBottomThickStyle.setBorderTop(BorderStyle.THIN);
                    borderTopThinBottomThickStyle.setBorderBottom(BorderStyle.THICK);

                    CellStyle allBordersStyle = workbook.createCellStyle();
                    allBordersStyle.setBorderTop(BorderStyle.THICK);
                    allBordersStyle.setBorderBottom(BorderStyle.THICK);
                    allBordersStyle.setBorderRight(BorderStyle.THICK);
                    allBordersStyle.setBorderLeft(BorderStyle.THICK);
                    allBordersStyle.setAlignment(HorizontalAlignment.CENTER);
                    allBordersStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    allBordersStyle.setFont(font);

                    CellStyle allBordersStyleN = workbook.createCellStyle();
                    allBordersStyleN.setBorderTop(BorderStyle.THICK);
                    allBordersStyleN.setBorderBottom(BorderStyle.THICK);
                    allBordersStyleN.setBorderRight(BorderStyle.THICK);
                    allBordersStyleN.setBorderLeft(BorderStyle.THICK);
                    allBordersStyleN.setAlignment(HorizontalAlignment.CENTER);
                    allBordersStyleN.setVerticalAlignment(VerticalAlignment.CENTER);




                    CellStyle centeredStyle = workbook.createCellStyle();
                    centeredStyle.setAlignment(HorizontalAlignment.CENTER);
                    centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    centeredStyle.setWrapText(true);
                    centeredStyle.setFont(font);


                    CellStyle centeredStyle14 = workbook.createCellStyle();
                    centeredStyle14.setAlignment(HorizontalAlignment.CENTER);
                    centeredStyle14.setVerticalAlignment(VerticalAlignment.CENTER);
                    centeredStyle14.setWrapText(true);
                    centeredStyle14.setFont(font14);

                    CellStyle centeredStyle12 = workbook.createCellStyle();
                    centeredStyle12.setAlignment(HorizontalAlignment.CENTER);
                    centeredStyle12.setVerticalAlignment(VerticalAlignment.CENTER);
                    centeredStyle12.setWrapText(true);
                    centeredStyle12.setFont(font12);




                    CellStyle leftedStyle = workbook.createCellStyle();
                    leftedStyle.setAlignment(HorizontalAlignment.LEFT);
                    leftedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    leftedStyle.setWrapText(false);
                    leftedStyle.setFont(font);

                    Row currentRow = sheet.createRow(0);
                    Cell currentCell  = currentRow.createCell(0);
                    currentCell.setCellStyle(overStyle);
                    currentCell.setCellValue(dbManager.getCompanyInfo().getTypeShort() + "\"" + dbManager.getCompanyInfo().getName()+"\"");


                    currentRow = sheet.createRow(2);

                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(centeredStyle14);
                    currentCell.setCellValue("Подорожній лист службового автомобіля №" + list.getNumber());

                    sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 11)); // 5th and 6th columns (0-indexed)

                    currentRow = sheet.createRow(3);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(centeredStyle12);
                    currentCell.setCellValue(list.getStartDate().format(formatter));

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellStyle(centeredStyle12);
                    currentCell.setCellValue("по");

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(centeredStyle12);
                    currentCell.setCellValue(list.getEndDate().format(formatter));


                    int rowIndex = 5;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopLeftStyleB);
                    currentCell.setCellValue("Автомобіль");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopLeftStyle);
                    currentCell.setCellValue(car.getModel());
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopRightStyle);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickStyleB);
                    currentCell.setCellValue("Державний номер");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopThinStyle);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue(car.getNumber());
                    currentCell.setCellStyle(borderTopThinLeftThickStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopThinStyle);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopThinRightThickStyle);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickStyleB);
                    currentCell.setCellValue("Водій");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopThinStyle);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue(worker.getNameN());
                    currentCell.setCellStyle(borderTopThinLeftThickStyle);

                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopThinStyle);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopThinRightThickStyle);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickStyleB);
                    currentCell.setCellValue("Номер посвідчення водія");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopThinStyle);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopThinLeftThickStyle);
                    currentCell.setCellValue(worker.getDrivingLicense());
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopThinStyle);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopThinRightThickStyle);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickStyleB);
                    currentCell.setCellValue("Тип двигуна");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopThinStyle);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopThinLeftThickStyle);
                    currentCell.setCellValue(car.getFuelType());
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopThinStyle);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopThinRightThickStyle);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickStyleB);
                    currentCell.setCellValue("Об'єм двигуна, л");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopThinStyle);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopThinLeftThickStyle);
                    currentCell.setCellValue(String.valueOf(car.getEngineVolume()));
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopThinStyle);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopThinRightThickStyle);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyleB);
                    currentCell.setCellValue("Норма витрат палива, л");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 3), borderTopThinBottomThickStyle);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyle);
                    currentCell.setCellValue(String.valueOf(new BigDecimal(car.getFuelUsage()).setScale(2, RoundingMode.DOWN)));
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 10), borderTopThinBottomThickStyle);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopThinRightThickBottomThickStyle);



                    rowIndex+=2;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(centeredStyle);
                    currentCell.setCellValue("Маршрут(завдання):");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 11));

                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopLeftStyleBC);
                    currentCell.setCellValue("Адреса, пункт призначення");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 5), borderTopStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 5));

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(borderTopLeftStyleBC);
                    currentCell.setCellValue("Підстава");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 7, 10), borderTopStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 6, 11));
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopRightStyle);

                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopLeftStyleCenter);
                    currentCell.setCellValue(list.getRoute());
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 5), borderTopStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex+1, rowIndex+2, 0, 0), borderLeftStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+3, 0, 5));

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(borderTopLeftStyleCenter);
                    currentCell.setCellValue(list.getGoal());
                    currentCell = currentRow.createCell(7);
                    currentCell.setCellStyle(borderTopLeftStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 8, 10), borderTopStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex+1, rowIndex+2, 6, 6), borderLeftStyle);

                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex+3, 12, 12), borderLeftStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+3, 6, 11));
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopRightStyle);

                    currentRow = sheet.createRow(rowIndex+3);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyle);

                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex+3, rowIndex+3, 1, 5), borderTopThinBottomThickStyle);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex+3, rowIndex+3, 7, 11), borderTopThinBottomThickStyle);
                    currentCell = currentRow.createCell(12);
                    currentCell.setCellStyle(borderLeftStyle);

                    rowIndex++;

                    rowIndex+=4;
                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(centeredStyle);
                    currentCell.setCellValue("Транспортна робота автомобіля");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 11));

                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Операція");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 0, 1), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Момент часу");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 2, 3), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 2, 3));

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Показники одометра, км");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 4, 7), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 7));

                    currentCell = currentRow.createCell(8);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Пройдена відстань, км");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 8, 11), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 8, 11));


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopLeftStyleB);
                    currentCell.setCellValue("Виїзд");
                    currentCell = currentRow.createCell(1);
                    currentCell.setCellStyle(borderTopRightStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellStyle(borderTopLeftStyleC);
                    currentCell.setCellValue(list.getStartDate().format(formatter2));
                    currentCell = currentRow.createCell(3);
                    currentCell.setCellStyle(borderTopRightStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 2, 3));

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopLeftStyleC);
                    currentCell.setCellValue(String.valueOf(list.getStartMileage()));
                    currentCell = currentRow.createCell(7);
                    currentCell.setCellStyle(borderTopRightStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 7));

                    currentCell = currentRow.createCell(8);
                    currentCell.setCellValue((list.isDone()?String.valueOf(list.getEndMileage()-list.getStartMileage()): " "));
                    currentCell.setCellStyle(borderTopRightStyleC);
                    currentCell = currentRow.createCell(11);
                    currentCell.setCellStyle(borderTopRightStyleC);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1, 8, 11));

                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyleB);
                    currentCell.setCellValue("Повернення");
                    currentCell = currentRow.createCell(1);
                    currentCell.setCellStyle(borderTopThinRightThickBottomThickStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyleC);
                    currentCell.setCellValue(list.getEndDate().format(formatter2));
                    currentCell = currentRow.createCell(3);
                    currentCell.setCellStyle(borderTopThinRightThickBottomThickStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 2, 3));

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyleC);
                    currentCell.setCellValue(list.isDone()?String.valueOf(list.getEndMileage()): " ");
                    currentCell = currentRow.createCell(7);
                    currentCell.setCellStyle(borderTopThinRightThickBottomThickStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 7), borderTopThinBottomThickStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 4, 7));

                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 8, 11), allBordersStyleN);

                    rowIndex+=2;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(centeredStyle);
                    currentCell.setCellValue("Рух пального, л");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 11));

                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);

                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Операція");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 0, 1), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1, 0, 1));

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Залишок у баку");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 2, 3), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1, 2, 3));

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Заправка на АЗС");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 4, 5), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1, 4, 5));

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Витрата");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 6, 11), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 6, 11));

                    rowIndex++;
                    currentRow = sheet.createRow(rowIndex);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 0, 1), allBordersStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 2, 3), allBordersStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 4, 5), allBordersStyle);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 6, 11), allBordersStyle);
                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Факт");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 6, 8), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 6, 8));

                    currentCell = currentRow.createCell(9);
                    currentCell.setCellStyle(allBordersStyle);
                    currentCell.setCellValue("Норма");
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 9, 11), allBordersStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 9, 11));

                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopLeftStyleB);
                    currentCell.setCellValue("Виїзд");
                    currentCell = currentRow.createCell(1);
                    currentCell.setCellStyle(borderTopRightStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellStyle(borderTopLeftStyleC);
                    currentCell.setCellValue(String.valueOf(list.getStartFuel()));
                    currentCell = currentRow.createCell(3);
                    currentCell.setCellStyle(borderTopRightStyleC);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 2, 3));

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellStyle(allBordersStyleN);
                    currentCell.setCellValue(list.isDone()?String.valueOf(list.getRefuel()):" ");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1, 4, 5));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 5, 5), allBordersStyleN);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellStyle(allBordersStyleN);
                    currentCell.setCellValue(list.isDone() ?
                            String.format("%.1f", list.getStartFuel() + list.getRefuel() - list.getEndFuel())
                            : " ");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1,6 , 8));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 7, 8), allBordersStyleN);

                    currentCell = currentRow.createCell(9);
                    currentCell.setCellStyle(allBordersStyleN);
                    currentCell.setCellValue(list.isDone()?String.valueOf(new BigDecimal((list.getEndMileage() - list.getStartMileage())*car.getFuelUsage()/100).setScale(2, RoundingMode.DOWN)):" ");
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1,9 , 11));
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 8, 11), allBordersStyleN);


                    rowIndex++;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyleB);
                    currentCell.setCellValue("Повернення");
                    currentCell = currentRow.createCell(1);
                    currentCell.setCellStyle(borderTopThinRightThickBottomThickStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellStyle(borderTopThinRightThickBottomThickStyleC);
                    currentCell.setCellValue(list.isDone()?String.valueOf(list.getEndFuel()):" ");
                    currentCell = currentRow.createCell(3);
                    currentCell.setCellStyle(borderTopThinLeftThickBottomThickStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 2, 3));

                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 9, 11), allBordersStyleN);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 6, 8), allBordersStyleN);
                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 4, 5), allBordersStyleN);


                    rowIndex+=4;
                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Водій");

                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 1, 4), borderBottomThinStyle);


                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue("Бухгалтер");

                    applyBordersToMergedRegion(sheet, new CellRangeAddress(rowIndex, rowIndex, 7, 11), borderBottomThinStyle);


                    // Write the workbook to the selected file
                    try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                        workbook.write(out);
                        JOptionPane.showMessageDialog(null, "Excel file saved successfully!");

                        // Open the file automatically if supported
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void createRegisterOrders(DBManager dbManager, List<_Order> orders, String WorkerName, LocalDate startDate, LocalDate endDate) {
        try {
            _Company _company = _Company.getInstance();
            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[5] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            DateTimeFormatter formatterFile = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String filename = "Реєстр наказів " + startDate.format(formatterFile) + " - " + endDate.format(formatterFile);
            fileChooser.setSelectedFile(new File(filename+".xlsx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .xlsx extension
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Create a new Excel workbook and populate it with sample data
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    Font BoldFont = workbook.createFont();
                    BoldFont.setBold(true);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


                    CellStyle CompanyStyle = workbook.createCellStyle();
                    CompanyStyle.setWrapText(false);
                    CompanyStyle.setFont(BoldFont);

                    CellStyle overlapStyle = workbook.createCellStyle();
                    overlapStyle.setWrapText(false);

                    CellStyle CenterStyle = workbook.createCellStyle();
                    CenterStyle.setAlignment(HorizontalAlignment.CENTER);

                    CellStyle AllBorders = workbook.createCellStyle();
                    AllBorders.setAlignment(HorizontalAlignment.CENTER);
                    AllBorders.setBorderLeft(BorderStyle.THICK);
                    AllBorders.setBorderRight(BorderStyle.THICK);
                    AllBorders.setBorderBottom(BorderStyle.THICK);
                    AllBorders.setBorderTop(BorderStyle.THICK);

                    CellStyle rightInside = workbook.createCellStyle();
                    rightInside.setBorderLeft(BorderStyle.THIN);
                    rightInside.setBorderRight(BorderStyle.THICK);
                    rightInside.setBorderBottom(BorderStyle.THIN);
                    rightInside.setBorderTop(BorderStyle.THIN);

                    CellStyle leftInside = workbook.createCellStyle();
                    leftInside.setBorderLeft(BorderStyle.THICK);
                    leftInside.setBorderRight(BorderStyle.THIN);
                    leftInside.setBorderBottom(BorderStyle.THIN);
                    leftInside.setBorderTop(BorderStyle.THIN);

                    CellStyle Inside = workbook.createCellStyle();
                    Inside.setBorderLeft(BorderStyle.THIN);
                    Inside.setBorderRight(BorderStyle.THIN);
                    Inside.setBorderBottom(BorderStyle.THIN);
                    Inside.setBorderTop(BorderStyle.THIN);

                    CellStyle Top = workbook.createCellStyle();
                    Top.setBorderTop(BorderStyle.THICK);

                    CreationHelper createHelper = workbook.getCreationHelper();

                    CellStyle dateCellInsideStyle = workbook.createCellStyle();
                    dateCellInsideStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
                    dateCellInsideStyle.setBorderLeft(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderRight(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderBottom(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderTop(BorderStyle.THIN);


                    Sheet sheet = workbook.createSheet("Реєстр наказів на відрядження");



                    Row currentRow = sheet.createRow(4);
                    Cell currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Реєстр наказів на відрядження");
                    currentCell.setCellStyle(CenterStyle);
                    sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 6));

                    int rowIndex = 5;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("№ п.п.");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(1);
                    currentCell.setCellValue("№ наказу");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellValue("Дата наказу");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(3);
                    currentCell.setCellValue("ПІБ працівник");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue("Дата початку");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellValue("Дата кінця");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue("Маршрут");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(7);
                    currentCell.setCellValue("Мета");
                    currentCell.setCellStyle(AllBorders);

                    rowIndex++;

                    int j = 1;

                    for(_Order order : orders) {
                        currentRow = sheet.createRow(rowIndex);
                        currentCell = currentRow.createCell(0);
                        currentCell.setCellValue(j);
                        currentCell.setCellStyle(leftInside);

                        currentCell = currentRow.createCell(1);
                        currentCell.setCellValue(order.getOrderNumber());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(2);
                        currentCell.setCellValue(order.getOrderDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(3);
                        currentCell.setCellValue(dbManager.getWorkerName(true, order.getIdWorker()));
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(4);
                        currentCell.setCellValue(order.getStartDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(5);
                        currentCell.setCellValue(order.getEndDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(6);
                        currentCell.setCellValue(order.getRoute());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(7);
                        currentCell.setCellValue(order.getGoal());
                        currentCell.setCellStyle(rightInside);
                        rowIndex++;
                        j++;
                    }

                    currentRow = sheet.createRow(rowIndex);

                    for(int i = 0; i <= 7; i++) {
                        currentCell = currentRow.createCell(i);
                        currentCell.setCellStyle(Top);
                    }

                    for (int i = 0; i <= 7; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    currentRow = sheet.createRow(0);
                    currentCell  = currentRow.createCell(0);
                    currentCell.setCellValue(dbManager.getCompanyInfo().getTypeShort() + "\"" + dbManager.getCompanyInfo().getName()+"\"");
                    currentCell.setCellStyle(CompanyStyle);

                    currentRow = sheet.createRow(2);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue((WorkerName == null)?"Усі працівники":("Працівник: " + WorkerName));

                    currentCell.setCellStyle(overlapStyle);

                    currentRow = sheet.createRow(3);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Період: " + startDate.format(formatter) + " - " + endDate.format(formatter));
                    currentCell.setCellStyle(overlapStyle);


                    // Write the workbook to the selected file
                    try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                        workbook.write(out);
                        JOptionPane.showMessageDialog(null, "Excel file saved successfully!");

                        // Open the file automatically if supported
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void createReportDocument(DBManager dbManager, _Report report) {
        try {
            _Company _company = dbManager.getCompanyInfo();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            String orderNumber =  dbManager.getOrderNumber(report.getOrderId());
            String worker =  dbManager.getOrderWorkerName(report.getOrderId());
            String position =  dbManager.getWorkerPosition(true, dbManager.getOrderIdWorker(report.getOrderId()));
            String goal = dbManager.getOrderGoal(report.getOrderId());
            String comments = report.getComments();
            String date = report.getDate().format(formatter);
            String orderDate = dbManager.getOrderDate(report.getOrderId()).format(formatter);
            String head = dbManager.getOrderHead(report.getOrderId());

            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Word Document");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[3] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }


            String filename = "Звіт про виконання наказу №"+orderNumber;
            fileChooser.setSelectedFile(new File(filename+".docx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .docx extension
                if (!fileToSave.getName().endsWith(".docx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".docx");
                }

                // Create a new Word document using Apache POI
                XWPFDocument document = new XWPFDocument();

                XWPFParagraph orderTitle = document.createParagraph();
                orderTitle.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun orderTitleRun = orderTitle.createRun();
                orderTitleRun.setFontFamily("Times New Roman");
                orderTitleRun.setText("Звіт про виконання завдання");
                orderTitleRun.setBold(true);
                orderTitleRun.setFontSize(14);

                setParagraphSpacing(orderTitle);

                XWPFParagraph order = document.createParagraph();
                order.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun orderRun = order.createRun();
                orderRun.setFontFamily("Times New Roman");
                orderRun.setText("наказ № " + orderNumber + " від " + orderDate);
                orderRun.setFontSize(12);

                setParagraphSpacing(order);

                XWPFParagraph separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph workerP = document.createParagraph();
                workerP.setAlignment(ParagraphAlignment.LEFT);

                XWPFRun boldRun = workerP.createRun();
                boldRun.setFontFamily("Times New Roman");
                boldRun.setText("П.І.Б.: ");
                boldRun.setBold(true);
                boldRun.setFontSize(14);

                XWPFRun normalRun = workerP.createRun();
                normalRun.setFontFamily("Times New Roman");
                normalRun.setText(worker);
                normalRun.setFontSize(14);

                setParagraphSpacing(workerP);

                XWPFParagraph positionP = document.createParagraph();
                positionP.setAlignment(ParagraphAlignment.LEFT);

                XWPFRun Position = positionP.createRun();
                Position.setFontFamily("Times New Roman");
                Position.setText("Посада: ");
                Position.setBold(true);
                Position.setFontSize(14);

                XWPFRun positionNorm = positionP.createRun();
                positionNorm.setFontFamily("Times New Roman");
                positionNorm.setText(position);
                positionNorm.setFontSize(14);




                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph textP = document.createParagraph();
                textP.setAlignment(ParagraphAlignment.LEFT);

                XWPFRun textField = textP.createRun();
                textField.setFontFamily("Times New Roman");
                textField.setText("Мета: ");
                textField.setBold(true);
                textField.setFontSize(14);

                textField = textP.createRun();
                textField.setFontFamily("Times New Roman");
                textField.setText(goal);
                textField.setFontSize(14);


                if(comments != null && !comments.equals("")) {
                    XWPFParagraph textA = document.createParagraph();
                    textA.setAlignment(ParagraphAlignment.LEFT);

                    XWPFRun textAF = textA.createRun();
                    textAF.setFontFamily("Times New Roman");
                    textAF.setText("Додаткова інформація: ");
                    textAF.setBold(true);
                    textAF.setFontSize(14);

                    textAF = textA.createRun();
                    textAF.setFontFamily("Times New Roman");
                    textAF.setText(comments);
                    textAF.setFontSize(14);
                    setParagraphSpacing(textP);

                }

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph dateP = document.createParagraph();
                dateP.setAlignment(ParagraphAlignment.LEFT);

                // Set tab stops
                CTPPr ppr = dateP.getCTP().getPPr();
                if (ppr == null) {
                    ppr = dateP.getCTP().addNewPPr();
                }
                CTTabs tabs = ppr.addNewTabs();
                CTTabStop tabStop = tabs.addNewTab();
                tabStop.setVal(STTabJc.RIGHT); // Right alignment
                tabStop.setPos(BigInteger.valueOf(9000)); // Adjust position as needed

                XWPFRun dateRun = dateP.createRun();
                dateRun.setFontFamily("Times New Roman");
                dateRun.setFontSize(12);

                dateRun.setText(date); // Add the date
                dateRun.addTab();  // Move cursor to the tab stop
                dateRun.setText("Підпис виконавця   _________________"); // Add the number at the right

                separator = document.createParagraph();
                setParagraphSpacing(separator);
                separator.createRun();

                XWPFParagraph textPA = document.createParagraph();
                textPA.setAlignment(ParagraphAlignment.LEFT);

                XWPFRun textFieldr = textPA.createRun();
                textFieldr.setFontFamily("Times New Roman");
                textFieldr.setText("Доцільність відрядження підтверджую");
                textFieldr.setFontSize(14);

                // Create the first paragraph
                XWPFParagraph TOV1 = document.createParagraph();
                TOV1.setAlignment(ParagraphAlignment.LEFT);  // Left-align the first part
                ppr = TOV1.getCTP().getPPr();
                if (ppr == null) {
                    ppr = TOV1.getCTP().addNewPPr();
                }
                tabs = ppr.addNewTabs();

                // Add tab stops for right alignment
                CTTabStop leftTab = tabs.addNewTab();
                leftTab.setVal(STTabJc.LEFT);
                leftTab.setPos(BigInteger.valueOf(3600)); // Adjust this position as needed

                CTTabStop rightTab = tabs.addNewTab();
                rightTab.setVal(STTabJc.RIGHT);
                rightTab.setPos(BigInteger.valueOf(9000)); // Right-aligned tab stop

                XWPFRun TOV1Run = TOV1.createRun();
                TOV1Run.setFontFamily("Times New Roman");
                TOV1Run.setFontSize(14);
                TOV1Run.setText((Objects.equals(head, _company.getCeo()))?"Директор":"В.о. директора");
                TOV1Run.addTab();  // Move to the next tab position (right)
                TOV1Run.setText("_________________");
                TOV1Run.addTab();  // Move to the next tab position (right)
                TOV1Run.setText(head);


                try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                    document.write(out);
                    JOptionPane.showMessageDialog(null, "Document saved successfully!");

                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(fileToSave);
                    } else {
                        JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving document: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void createRegisterFuel(DBManager dbManager, List<String> numbers, List<FuelUsage> usages, LocalDate startDate, LocalDate endDate, Period period) {
        try {
            _Company _company = _Company.getInstance();
            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[6] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            DateTimeFormatter formatterFile = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String filename = "Реєстр використання палива " + startDate.format(formatterFile) + " - " + endDate.format(formatterFile);

            fileChooser.setSelectedFile(new File(filename + ".xlsx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .xlsx extension
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Create a new Excel workbook and populate it with sample data
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    Font BoldFont = workbook.createFont();
                    BoldFont.setBold(true);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


                    CellStyle CompanyStyle = workbook.createCellStyle();
                    CompanyStyle.setWrapText(false);
                    CompanyStyle.setFont(BoldFont);

                    CellStyle overlapStyle = workbook.createCellStyle();
                    overlapStyle.setWrapText(false);

                    CellStyle CenterStyle = workbook.createCellStyle();
                    CenterStyle.setAlignment(HorizontalAlignment.CENTER);

                    CellStyle AllBorders = workbook.createCellStyle();
                    AllBorders.setAlignment(HorizontalAlignment.CENTER);
                    AllBorders.setBorderLeft(BorderStyle.THICK);
                    AllBorders.setBorderRight(BorderStyle.THICK);
                    AllBorders.setBorderBottom(BorderStyle.THICK);
                    AllBorders.setBorderTop(BorderStyle.THICK);

                    CellStyle rightInside = workbook.createCellStyle();
                    rightInside.setBorderLeft(BorderStyle.THIN);
                    rightInside.setBorderRight(BorderStyle.THICK);
                    rightInside.setBorderBottom(BorderStyle.THIN);
                    rightInside.setBorderTop(BorderStyle.THIN);

                    CellStyle rightInsideBot = workbook.createCellStyle();
                    rightInsideBot.setBorderLeft(BorderStyle.THIN);
                    rightInsideBot.setBorderRight(BorderStyle.THICK);
                    rightInsideBot.setBorderBottom(BorderStyle.THIN);
                    rightInsideBot.setBorderTop(BorderStyle.THICK);

                    CellStyle leftInside = workbook.createCellStyle();
                    leftInside.setBorderLeft(BorderStyle.THICK);
                    leftInside.setBorderRight(BorderStyle.THIN);
                    leftInside.setBorderBottom(BorderStyle.THIN);
                    leftInside.setBorderTop(BorderStyle.THIN);

                    CellStyle Inside = workbook.createCellStyle();
                    Inside.setBorderLeft(BorderStyle.THIN);
                    Inside.setBorderRight(BorderStyle.THIN);
                    Inside.setBorderBottom(BorderStyle.THIN);
                    Inside.setBorderTop(BorderStyle.THIN);

                    CellStyle InsideBot = workbook.createCellStyle();
                    InsideBot.setBorderLeft(BorderStyle.THIN);
                    InsideBot.setBorderRight(BorderStyle.THIN);
                    InsideBot.setBorderBottom(BorderStyle.THIN);
                    InsideBot.setBorderTop(BorderStyle.THICK);

                    CellStyle Top = workbook.createCellStyle();
                    Top.setBorderTop(BorderStyle.THICK);

                    CreationHelper createHelper = workbook.getCreationHelper();

                    CellStyle dateCellInsideStyle = workbook.createCellStyle();
                    dateCellInsideStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
                    dateCellInsideStyle.setBorderLeft(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderRight(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderBottom(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderTop(BorderStyle.THIN);


                    Sheet sheet = workbook.createSheet("Реєстр використання палива");



                    Row currentRow = sheet.createRow(5);
                    Cell currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Реєстр використання палива");
                    currentCell.setCellStyle(CenterStyle);
                    sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 8));

                    int rowIndex = 6;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("№ п.п.");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(1);
                    currentCell.setCellValue("Дата початку");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellValue("Дата кінця");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(3);
                    currentCell.setCellValue("Номер авто");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue("Пробіг за період");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellValue("Фактичне використання палива");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue("Норма використання палива");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(7);
                    currentCell.setCellValue("Перевикористання палива");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(8);
                    currentCell.setCellValue("Економія палива");
                    currentCell.setCellStyle(AllBorders);

                    rowIndex++;

                    int j = 1;
                    int allMilaege = 0;
                    double allFactUsage = 0;
                    double allNormUsage = 0;
                    double allOverUse = 0;
                    double allUnderUse = 0;


                    for(FuelUsage usage : usages) {
                        currentRow = sheet.createRow(rowIndex);
                        currentCell = currentRow.createCell(0);
                        currentCell.setCellValue(j);
                        currentCell.setCellStyle(leftInside);

                        currentCell = currentRow.createCell(1);
                        currentCell.setCellValue(usage.getStartDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(2);
                        currentCell.setCellValue(usage.getEndDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(3);
                        currentCell.setCellValue(usage.getCarNumber());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(4);
                        currentCell.setCellValue(usage.getMileage());
                        currentCell.setCellStyle(Inside);
                        allMilaege+=usage.getMileage();

                        currentCell = currentRow.createCell(5);
                        currentCell.setCellValue(String.valueOf(new BigDecimal(usage.getFuelFact()).setScale(2, RoundingMode.DOWN)));
                        currentCell.setCellStyle(Inside);
                        allFactUsage+=usage.getFuelFact();

                        currentCell = currentRow.createCell(6);
                        currentCell.setCellValue(usage.getFuelNorm());
                        currentCell.setCellStyle(Inside);
                        allNormUsage+=usage.getFuelNorm();

                        currentCell = currentRow.createCell(7);
                        currentCell.setCellValue((usage.getOverUse() == null)?"": String.valueOf(new BigDecimal(usage.getOverUse()).setScale(2, RoundingMode.DOWN)));
                        currentCell.setCellStyle(Inside);
                        allOverUse+=(usage.getOverUse() == null)?0:usage.getOverUse();

                        currentCell = currentRow.createCell(8);
                        currentCell.setCellValue((usage.getUnderUse() == null)? "":String.valueOf(new BigDecimal(usage.getUnderUse()).setScale(2, RoundingMode.DOWN)));
                        currentCell.setCellStyle(rightInside);
                        allUnderUse+=(usage.getUnderUse() == null)?0:usage.getUnderUse();

                        rowIndex++;
                        j++;
                    }

                    currentRow = sheet.createRow(rowIndex);

                    for(int i = 0; i <= 8; i++) {
                        currentCell = currentRow.createCell(i);
                        currentCell.setCellStyle(Top);
                    }

                    for (int i = 0; i <= 8; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    currentCell = currentRow.createCell(0);
                    currentCell.setCellStyle(AllBorders);
                    currentCell.setCellValue("Всього:");

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue(allMilaege);
                    currentCell.setCellStyle(InsideBot);

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellValue(allFactUsage);
                    currentCell.setCellStyle(InsideBot);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue(allNormUsage);
                    currentCell.setCellStyle(InsideBot);

                    currentCell = currentRow.createCell(7);
                    currentCell.setCellValue(allOverUse);
                    currentCell.setCellStyle(InsideBot);

                    currentCell = currentRow.createCell(8);
                    currentCell.setCellValue(allUnderUse);
                    currentCell.setCellStyle(rightInsideBot);


                    rowIndex++;
                    currentRow = sheet.createRow(rowIndex);

                    for(int i = 0; i <= 8; i++) {
                        currentCell = currentRow.createCell(i);
                        currentCell.setCellStyle(Top);
                    }


                    currentRow = sheet.createRow(0);
                    currentCell  = currentRow.createCell(0);
                    currentCell.setCellValue(dbManager.getCompanyInfo().getTypeShort() + "\"" + dbManager.getCompanyInfo().getName()+"\"");
                    currentCell.setCellStyle(CompanyStyle);

                    currentRow = sheet.createRow(2);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Автомобілі: "+String.join(", ", numbers));

                    currentCell.setCellStyle(overlapStyle);

                    currentRow = sheet.createRow(3);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Період: " + startDate.format(formatter) + " - " + endDate.format(formatter));
                    currentCell.setCellStyle(overlapStyle);
                    String str = "По днях";

                    if(period.equals(Period.ofWeeks(1))){
                        str = "По тижнях";
                    }
                    if(period.equals(Period.ofMonths(1))){
                        str = "По місяцях";
                    }
                    if(period.equals(Period.ofMonths(3))){
                        str = "По кварталах";
                    }
                    if(period.equals(Period.ofYears(1))){
                        str = "По роках";
                    }


                    currentRow = sheet.createRow(4);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Групування: " + str);
                    currentCell.setCellStyle(overlapStyle);


                    // Write the workbook to the selected file
                    try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                        workbook.write(out);
                        JOptionPane.showMessageDialog(null, "Excel file saved successfully!");

                        // Open the file automatically if supported
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void createRegisterLists(DBManager dbManager, List<String> numbers, List<_List> lists, LocalDate startDate, LocalDate endDate) {
        try {
            _Company _company = _Company.getInstance();
            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[7] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            DateTimeFormatter formatterFile = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String filename = "Реєстр подорожніх листів " + startDate.format(formatterFile) + " - " + endDate.format(formatterFile);
            fileChooser.setSelectedFile(new File(filename+".xlsx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .xlsx extension
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Create a new Excel workbook and populate it with sample data
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    Font BoldFont = workbook.createFont();
                    BoldFont.setBold(true);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


                    CellStyle CompanyStyle = workbook.createCellStyle();
                    CompanyStyle.setWrapText(false);
                    CompanyStyle.setFont(BoldFont);

                    CellStyle overlapStyle = workbook.createCellStyle();
                    overlapStyle.setWrapText(false);

                    CellStyle CenterStyle = workbook.createCellStyle();
                    CenterStyle.setAlignment(HorizontalAlignment.CENTER);

                    CellStyle AllBorders = workbook.createCellStyle();
                    AllBorders.setAlignment(HorizontalAlignment.CENTER);
                    AllBorders.setBorderLeft(BorderStyle.THICK);
                    AllBorders.setBorderRight(BorderStyle.THICK);
                    AllBorders.setBorderBottom(BorderStyle.THICK);
                    AllBorders.setBorderTop(BorderStyle.THICK);

                    CellStyle rightInside = workbook.createCellStyle();
                    rightInside.setBorderLeft(BorderStyle.THIN);
                    rightInside.setBorderRight(BorderStyle.THICK);
                    rightInside.setBorderBottom(BorderStyle.THIN);
                    rightInside.setBorderTop(BorderStyle.THIN);

                    CellStyle leftInside = workbook.createCellStyle();
                    leftInside.setBorderLeft(BorderStyle.THICK);
                    leftInside.setBorderRight(BorderStyle.THIN);
                    leftInside.setBorderBottom(BorderStyle.THIN);
                    leftInside.setBorderTop(BorderStyle.THIN);

                    CellStyle Inside = workbook.createCellStyle();
                    Inside.setBorderLeft(BorderStyle.THIN);
                    Inside.setBorderRight(BorderStyle.THIN);
                    Inside.setBorderBottom(BorderStyle.THIN);
                    Inside.setBorderTop(BorderStyle.THIN);

                    CellStyle Top = workbook.createCellStyle();
                    Top.setBorderTop(BorderStyle.THICK);

                    CreationHelper createHelper = workbook.getCreationHelper();

                    CellStyle dateCellInsideStyle = workbook.createCellStyle();
                    dateCellInsideStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
                    dateCellInsideStyle.setBorderLeft(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderRight(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderBottom(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderTop(BorderStyle.THIN);


                    Sheet sheet = workbook.createSheet("Реєстр подорожніх листів");



                    Row currentRow = sheet.createRow(3);
                    Cell currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Реєстр подорожніх листів");
                    currentCell.setCellStyle(CenterStyle);
                    sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 9));

                    int rowIndex = 4;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("№ п.п.");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(1);
                    currentCell.setCellValue("№ листа");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellValue("ПІБ працівник");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(3);
                    currentCell.setCellValue("Виїзд: дата");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue("Поверення: дата");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellValue("Номер авто");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue("№ наказу");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(7);
                    currentCell.setCellValue("Маршрут");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(8);
                    currentCell.setCellValue("Мета");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(9);
                    currentCell.setCellValue("Виїзд: пробіг");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(10);
                    currentCell.setCellValue("Виїзд: паливо");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(11);
                    currentCell.setCellValue("Повернення: пробіг");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(12);
                    currentCell.setCellValue("Повернення: паливо");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(13);
                    currentCell.setCellValue("Заправка");
                    currentCell.setCellStyle(AllBorders);

                    rowIndex++;

                    int j = 1;

                    for(_List list : lists) {
                        currentRow = sheet.createRow(rowIndex);
                        currentCell = currentRow.createCell(0);
                        currentCell.setCellValue(j);
                        currentCell.setCellStyle(leftInside);

                        currentCell = currentRow.createCell(1);
                        currentCell.setCellValue(list.getNumber());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(2);
                        currentCell.setCellValue(dbManager.getWorkerName(true, list.getIdWorker()));
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(3);
                        currentCell.setCellValue(list.getStartDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(4);
                        currentCell.setCellValue(list.getEndDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(5);
                        currentCell.setCellValue(dbManager.getCarNumber(list.getIdCar()));
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(6);
                        if(list.getIdOrder() == -1) {
                            currentCell.setCellValue("По місту");
                        } else {
                            currentCell.setCellValue(dbManager.getOrderNumber(list.getIdOrder()));
                        }
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(7);
                        currentCell.setCellValue(list.getRoute());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(8);
                        currentCell.setCellValue(list.getGoal());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(9);
                        currentCell.setCellValue(list.getStartMileage());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(10);
                        currentCell.setCellValue(list.getStartFuel());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(11);
                        currentCell.setCellValue(list.getEndMileage());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(12);
                        currentCell.setCellValue(list.getEndFuel());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(13);
                        currentCell.setCellValue(list.getRefuel());
                        currentCell.setCellStyle(rightInside);
                        rowIndex++;
                        j++;
                    }

                    currentRow = sheet.createRow(rowIndex);

                    for(int i = 0; i <= 13; i++) {
                        currentCell = currentRow.createCell(i);
                        currentCell.setCellStyle(Top);
                    }

                    for (int i = 0; i <= 13; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    currentRow = sheet.createRow(0);
                    currentCell  = currentRow.createCell(0);
                    currentCell.setCellValue(dbManager.getCompanyInfo().getTypeShort() + "\"" + dbManager.getCompanyInfo().getName()+"\"");
                    currentCell.setCellStyle(CompanyStyle);

                    currentRow = sheet.createRow(2);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Період: " + startDate.format(formatter) + " - " + endDate.format(formatter));
                    currentCell.setCellStyle(overlapStyle);

                    // Write the workbook to the selected file
                    try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                        workbook.write(out);
                        JOptionPane.showMessageDialog(null, "Excel file saved successfully!");

                        // Open the file automatically if supported
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void createCarsHandbook(DBManager dbManager, List<_Car> cars) {
        try {
            _Company _company = _Company.getInstance();
            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[0] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            DateTimeFormatter formatterFile = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String filename = "Довідник автомобілів " + LocalDate.now().format(formatterFile);
            fileChooser.setSelectedFile(new File(filename+".xlsx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .xlsx extension
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Create a new Excel workbook and populate it with sample data
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    Font BoldFont = workbook.createFont();
                    BoldFont.setBold(true);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


                    CellStyle CompanyStyle = workbook.createCellStyle();
                    CompanyStyle.setWrapText(false);
                    CompanyStyle.setFont(BoldFont);

                    CellStyle overlapStyle = workbook.createCellStyle();
                    overlapStyle.setWrapText(false);

                    CellStyle CenterStyle = workbook.createCellStyle();
                    CenterStyle.setAlignment(HorizontalAlignment.CENTER);

                    CellStyle AllBorders = workbook.createCellStyle();
                    AllBorders.setAlignment(HorizontalAlignment.CENTER);
                    AllBorders.setBorderLeft(BorderStyle.THICK);
                    AllBorders.setBorderRight(BorderStyle.THICK);
                    AllBorders.setBorderBottom(BorderStyle.THICK);
                    AllBorders.setBorderTop(BorderStyle.THICK);

                    CellStyle rightInside = workbook.createCellStyle();
                    rightInside.setBorderLeft(BorderStyle.THIN);
                    rightInside.setBorderRight(BorderStyle.THICK);
                    rightInside.setBorderBottom(BorderStyle.THIN);
                    rightInside.setBorderTop(BorderStyle.THIN);

                    CellStyle leftInside = workbook.createCellStyle();
                    leftInside.setBorderLeft(BorderStyle.THICK);
                    leftInside.setBorderRight(BorderStyle.THIN);
                    leftInside.setBorderBottom(BorderStyle.THIN);
                    leftInside.setBorderTop(BorderStyle.THIN);

                    CellStyle Inside = workbook.createCellStyle();
                    Inside.setBorderLeft(BorderStyle.THIN);
                    Inside.setBorderRight(BorderStyle.THIN);
                    Inside.setBorderBottom(BorderStyle.THIN);
                    Inside.setBorderTop(BorderStyle.THIN);

                    CellStyle Top = workbook.createCellStyle();
                    Top.setBorderTop(BorderStyle.THICK);

                    CreationHelper createHelper = workbook.getCreationHelper();

                    CellStyle dateCellInsideStyle = workbook.createCellStyle();
                    dateCellInsideStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
                    dateCellInsideStyle.setBorderLeft(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderRight(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderBottom(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderTop(BorderStyle.THIN);


                    Sheet sheet = workbook.createSheet("Довідник автомобілів");



                    Row currentRow = sheet.createRow(2);
                    Cell currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Довідник автомобілів");
                    currentCell.setCellStyle(CenterStyle);
                    sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 9));

                    int rowIndex = 3;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("№ п.п.");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(1);
                    currentCell.setCellValue("Номер авто");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellValue("Модель");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(3);
                    currentCell.setCellValue("Тип палива");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue("Використання палива(л/100км)");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellValue("Об'єм двигуна");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue("Дата початку експлуатації");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(7);
                    currentCell.setCellValue("Номер наказу початку експлуатації");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(8);
                    currentCell.setCellValue("Дата закінчення експлуатації");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(9);
                    currentCell.setCellValue("Номер наказу закінчення експлуатації");
                    currentCell.setCellStyle(AllBorders);

                    rowIndex++;

                    int j = 1;

                    for(_Car car : cars) {
                        currentRow = sheet.createRow(rowIndex);
                        currentCell = currentRow.createCell(0);
                        currentCell.setCellValue(j);
                        currentCell.setCellStyle(leftInside);

                        currentCell = currentRow.createCell(1);
                        currentCell.setCellValue(car.getNumber());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(2);
                        currentCell.setCellValue(car.getModel());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(3);
                        currentCell.setCellValue(car.getFuelType());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(4);
                        currentCell.setCellValue(car.getFuelUsage());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(5);
                        currentCell.setCellValue(car.getEngineVolume());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(6);
                        currentCell.setCellValue(car.getStartDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(7);
                        currentCell.setCellValue(car.getStartOrderNumber());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(8);
                        currentCell.setCellValue(car.getEndDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(9);
                        currentCell.setCellValue(car.getEndOrderNumber());
                        currentCell.setCellStyle(rightInside);
                        rowIndex++;
                        j++;
                    }

                    currentRow = sheet.createRow(rowIndex);

                    for(int i = 0; i <= 9; i++) {
                        currentCell = currentRow.createCell(i);
                        currentCell.setCellStyle(Top);
                    }

                    for (int i = 0; i <= 9; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    currentRow = sheet.createRow(0);
                    currentCell  = currentRow.createCell(0);
                    currentCell.setCellValue(dbManager.getCompanyInfo().getTypeShort() + "\"" + dbManager.getCompanyInfo().getName()+"\"");
                    currentCell.setCellStyle(CompanyStyle);

                    // Write the workbook to the selected file
                    try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                        workbook.write(out);
                        JOptionPane.showMessageDialog(null, "Excel file saved successfully!");

                        // Open the file automatically if supported
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void createWorkersHandbook(DBManager dbManager, List<_Worker> workers) {
        try {
            _Company _company = _Company.getInstance();
            // Set the system's native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Open the system's default file save dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");

            File defaultDirectory = new File(getDocsFolderPath() + "DocFiles\\"+ dbManager.getCompany() + "\\" + folders[1] + "\\");
            
            if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
                
                fileChooser.setCurrentDirectory(defaultDirectory);
            }

            DateTimeFormatter formatterFile = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String filename = "Довідник працівників " + LocalDate.now().format(formatterFile);
            fileChooser.setSelectedFile(new File(filename+".xlsx"));  // Default file name

            // Show save dialog and capture the user's selection
            int userSelection = fileChooser.showSaveDialog(null);

            // Check if the user approved the file selection
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure the file has a .xlsx extension
                if (!fileToSave.getName().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                }

                // Create a new Excel workbook and populate it with sample data
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    Font BoldFont = workbook.createFont();
                    BoldFont.setBold(true);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


                    CellStyle CompanyStyle = workbook.createCellStyle();
                    CompanyStyle.setWrapText(false);
                    CompanyStyle.setFont(BoldFont);

                    CellStyle overlapStyle = workbook.createCellStyle();
                    overlapStyle.setWrapText(false);

                    CellStyle CenterStyle = workbook.createCellStyle();
                    CenterStyle.setAlignment(HorizontalAlignment.CENTER);

                    CellStyle AllBorders = workbook.createCellStyle();
                    AllBorders.setAlignment(HorizontalAlignment.CENTER);
                    AllBorders.setBorderLeft(BorderStyle.THICK);
                    AllBorders.setBorderRight(BorderStyle.THICK);
                    AllBorders.setBorderBottom(BorderStyle.THICK);
                    AllBorders.setBorderTop(BorderStyle.THICK);

                    CellStyle rightInside = workbook.createCellStyle();
                    rightInside.setBorderLeft(BorderStyle.THIN);
                    rightInside.setBorderRight(BorderStyle.THICK);
                    rightInside.setBorderBottom(BorderStyle.THIN);
                    rightInside.setBorderTop(BorderStyle.THIN);

                    CellStyle leftInside = workbook.createCellStyle();
                    leftInside.setBorderLeft(BorderStyle.THICK);
                    leftInside.setBorderRight(BorderStyle.THIN);
                    leftInside.setBorderBottom(BorderStyle.THIN);
                    leftInside.setBorderTop(BorderStyle.THIN);

                    CellStyle Inside = workbook.createCellStyle();
                    Inside.setBorderLeft(BorderStyle.THIN);
                    Inside.setBorderRight(BorderStyle.THIN);
                    Inside.setBorderBottom(BorderStyle.THIN);
                    Inside.setBorderTop(BorderStyle.THIN);

                    CellStyle Top = workbook.createCellStyle();
                    Top.setBorderTop(BorderStyle.THICK);

                    CreationHelper createHelper = workbook.getCreationHelper();

                    CellStyle dateCellInsideStyle = workbook.createCellStyle();
                    dateCellInsideStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
                    dateCellInsideStyle.setBorderLeft(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderRight(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderBottom(BorderStyle.THIN);
                    dateCellInsideStyle.setBorderTop(BorderStyle.THIN);


                    Sheet sheet = workbook.createSheet("Довідник працівників");



                    Row currentRow = sheet.createRow(2);
                    Cell currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("Довідник працівників");
                    currentCell.setCellStyle(CenterStyle);
                    sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));

                    int rowIndex = 3;

                    currentRow = sheet.createRow(rowIndex);
                    currentCell = currentRow.createCell(0);
                    currentCell.setCellValue("№ п.п.");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(1);
                    currentCell.setCellValue("ПІБ");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(2);
                    currentCell.setCellValue("Посада");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(3);
                    currentCell.setCellValue("№ водійського посвідчення");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(4);
                    currentCell.setCellValue("Дата працевлаштування");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(5);
                    currentCell.setCellValue("Номер наказу працевлаштування");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(6);
                    currentCell.setCellValue("Дата звільнення");
                    currentCell.setCellStyle(AllBorders);

                    currentCell = currentRow.createCell(7);
                    currentCell.setCellValue("Номер наказу звільнення");
                    currentCell.setCellStyle(AllBorders);
                    rowIndex++;

                    int j = 1;

                    for(_Worker worker : workers) {
                        currentRow = sheet.createRow(rowIndex);
                        currentCell = currentRow.createCell(0);
                        currentCell.setCellValue(j);
                        currentCell.setCellStyle(leftInside);

                        currentCell = currentRow.createCell(1);
                        currentCell.setCellValue(worker.getNameN());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(2);
                        currentCell.setCellValue(worker.getPositionN());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(3);
                        currentCell.setCellValue(worker.getDrivingLicense());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(4);
                        currentCell.setCellValue(worker.getStartDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(5);
                        currentCell.setCellValue(worker.getStartOrderNumber());
                        currentCell.setCellStyle(Inside);

                        currentCell = currentRow.createCell(6);
                        currentCell.setCellValue(worker.getEndDate());
                        currentCell.setCellStyle(dateCellInsideStyle);

                        currentCell = currentRow.createCell(7);
                        currentCell.setCellValue(worker.getEndOrderNumber());
                        currentCell.setCellStyle(rightInside);

                        rowIndex++;
                        j++;
                    }

                    currentRow = sheet.createRow(rowIndex);

                    for(int i = 0; i <= 7; i++) {
                        currentCell = currentRow.createCell(i);
                        currentCell.setCellStyle(Top);
                    }

                    for (int i = 0; i <= 7; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    currentRow = sheet.createRow(0);
                    currentCell  = currentRow.createCell(0);
                    currentCell.setCellValue(dbManager.getCompanyInfo().getTypeShort() + "\"" + dbManager.getCompanyInfo().getName()+"\"");
                    currentCell.setCellStyle(CompanyStyle);

                    // Write the workbook to the selected file
                    try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                        workbook.write(out);
                        JOptionPane.showMessageDialog(null, "Excel file saved successfully!");

                        // Open the file automatically if supported
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(null, "Desktop is not supported. Cannot open the file automatically.");
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + e.getMessage());
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
