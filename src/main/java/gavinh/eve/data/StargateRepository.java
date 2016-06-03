package gavinh.eve.data;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface StargateRepository  extends CrudRepository<Stargate, Integer> {

    List<Stargate> findBySolarSystem(SolarSystem solarSystem);
    
    @Query("FROM Stargate AS sg WHERE sg.solarSystem.zone = 'H' AND sg.remoteSolarSystem.zone = ' '")
    List<Stargate> findWhereHighsec();
}
