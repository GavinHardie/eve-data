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
        run-shoppinglist-report     produce shopping list report on todays market orders
        run-stockbreakdown-report   breakdown universe supply of todays orders into price bands
        run-direct-sell-report      list the best buy orders for direct sell
        */
        
//        application.setAdditionalProfiles("load");
//        application.setAdditionalProfiles("market-orders");
//        application.setAdditionalProfiles("run-shoppinglist-report");
        application.setAdditionalProfiles("run-direct-sell-report");
//        application.setAdditionalProfiles("run-stockbreakdown-report");
        application.run(args);
    }

}
