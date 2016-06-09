package gavinh.eve.data;

import org.springframework.data.repository.CrudRepository;

public interface StationRepository extends CrudRepository<Station, Integer> {

    Station findByName(String name);
    
}
