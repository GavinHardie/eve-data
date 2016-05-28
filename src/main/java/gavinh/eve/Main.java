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
        
        /*
        load                        populate an empty DB
        market-orders               update todays market orders in the DB
        run-shoppinglist-report     produce shopping list report
        run-stocklevel-report       breakdown universe supply into price bands
        */
        
//        application.setAdditionalProfiles("market-orders");
        application.setAdditionalProfiles("run-stocklevel-report");
        application.run(args);
    }

}
