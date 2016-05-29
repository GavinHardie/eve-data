package gavinh.eve.data;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SolarSystemRepository extends CrudRepository<SolarSystem, Integer> {
    
    public SolarSystem findByName(String name);
    public List<SolarSystem> findByZone(char zone);
}
