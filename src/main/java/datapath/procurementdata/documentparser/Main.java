package datapath.procurementdata.documentparser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = Files.newInputStream(Path.of("/home/pylypchuk/Desktop/ДКТ послуги СГ DOCX.docx"));


//        HWPFDocument doc = new HWPFDocument(inputStream);
//        SummaryInformation summaryInformation = doc.getSummaryInformation();
//        System.out.println(summaryInformation);

//        InputStream inputStream = Files.newInputStream(Path.of("/home/pylypchuk/Desktop/ДКТ послуги СГ DOCX.docx"));
//        XWPFDocument xdoc = new XWPFDocument(inputStream);
//        PackageProperties props = xdoc.getPackage().getPackageProperties();
//        System.out.println(props);
    }
}
