package gavinh.eve;

import gavinh.eve.data.Person;
import gavinh.eve.data.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("jpa-test")
public class JpaTestApplication implements CommandLineRunner {

    @Autowired
    PersonRepository repository;
    
    private static final Logger log = LoggerFactory.getLogger(JpaTestApplication.class);

    @Override
    public void run(String... strings) throws Exception {
        // save a couple of customers
        repository.save(new Person("Jack", "Bauer"));
        repository.save(new Person("Chloe", "O'Brian"));
        repository.save(new Person("Kim", "Bauer"));
        repository.save(new Person("David", "Palmer"));
        repository.save(new Person("Michelle", "Dessler"));

        // fetch all customers
        log.info("Customers found with findAll():");
        log.info("-------------------------------");
        for (Person person : repository.findAll()) {
            log.info(person.toString());
        }
        log.info("");

        // fetch an individual customer by ID
        Person person = repository.findOne(1L);
        log.info("Person found with findOne(1L):");
        log.info("--------------------------------");
        log.info(person.toString());
        log.info("");

        // fetch customers by last name
        log.info("Person found with findByLastName('Bauer'):");
        log.info("--------------------------------------------");
        for (Person bauer : repository.findByLastName("Bauer")) {
            log.info(bauer.toString());
        }
        log.info("");
    }

}
