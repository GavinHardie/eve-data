package gavinh.eve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) {
        SpringApplication application = new SpringApplication(Main.class);
        application.setAdditionalProfiles("market-orders");  // load market-orders 
        application.run(args);
    }

}
