package gavinh.eve.data;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MarketOrderRepository extends CrudRepository<MarketOrder, Long> {
    
    @Query("FROM MarketOrder AS mo WHERE mo.fetched = ?1 AND mo.buysell = ?2 AND mo.itemType = ?3 AND mo.station.solarSystem.security >= 0.5")
    List<MarketOrder> findByFetchedAndBuysellAndItemTypeInHighsec(String fetched, String buysell, ItemType itemType);
    
    List<MarketOrder> findByFetchedAndBuysellAndItemTypeAndStation(String fetched, String buysell, ItemType itemType, Station station);
    
    List<MarketOrder> findByFetchedAndBuysellAndItemType(String fetched, String buysell, ItemType itemType);
    
    void deleteByFetchedAndBuysellAndItemTypeAndRegion(String fetched, String buysell, ItemType itemType, Region region);
}
