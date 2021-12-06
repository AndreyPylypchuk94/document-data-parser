package datapath.procurementdata.documentparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DocumentParserApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DocumentParserApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }
}
