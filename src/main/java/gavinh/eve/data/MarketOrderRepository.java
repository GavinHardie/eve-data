package gavinh.eve.data;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface MarketOrderRepository extends CrudRepository<MarketOrder, Long> {
    
    List<MarketOrder> findByFetchedAndBuysellAndItemTypeAndStation(String fetched, String buysell, ItemType itemType, Station station);
    List<MarketOrder> findByFetchedAndBuysellAndItemType(String fetched, String buysell, ItemType itemType);
    void deleteByFetchedAndBuysellAndItemTypeAndRegion(String fetched, String buysell, ItemType itemType, Region region);
}
