package gavinh.eve.data;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MarketOrderRepository extends CrudRepository<MarketOrder, Long> {
    
    @Query("FROM MarketOrder AS mo WHERE mo.fetched = ?1 AND mo.buysell = ?2 AND mo.itemType = ?3 AND mo.station.solarSystem.zone = 'H'")
    List<MarketOrder> findByFetchedAndBuysellAndItemTypeInHighsec(String fetched, String buysell, ItemType itemType);

    @Query("FROM MarketOrder AS mo WHERE mo.fetched = ?1 AND mo.buysell = ?2 AND mo.itemType = ?3 AND mo.station.solarSystem.zone in ( 'L' , 'I' )")
    List<MarketOrder> findByFetchedAndBuysellAndItemTypeInLowsec(String TODAY, String BUY_ORDERS, ItemType itemType);
    
    List<MarketOrder> findByFetchedAndBuysellAndItemTypeAndStation(String fetched, String buysell, ItemType itemType, Station station);
    
    List<MarketOrder> findByFetchedAndBuysellAndItemType(String fetched, String buysell, ItemType itemType);
    
    List<MarketOrder> findByFetchedAndBuysellAndStation(String fetched, String buysell, Station station);
    
    void deleteByFetchedAndBuysellAndItemTypeAndRegion(String fetched, String buysell, ItemType itemType, Region region);

}
