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

    /**
     * Setup.
     * 
     * 1. Put the DB credentials in resources\application.properties
     * 2. Run the "load" profile to load regions and itemTypes
     * 3. Run the "market-orders" profile to load market orders.  Currently hard
     *      coded to run for about 20 itemTypes.
     * 
     * @param args 
     */
    public static void main(String args[]) {
        SpringApplication application = new SpringApplication(Main.class);
        application.setAdditionalProfiles("blueprint-test");  // load market-orders blueprint-test
        application.run(args);
    }

}
