package gavinh.eve;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class Main {

    private static final Logger log = LoggerFactory.getLogger(JpaTestApplication.class);

    public static void main(String args[]) {
        SpringApplication application = new SpringApplication(Main.class);
        application.setAdditionalProfiles("market-orders");  // load market-orders jpa-test rest-test
        application.run(args);
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix="datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }    
    
}
