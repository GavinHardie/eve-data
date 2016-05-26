package gavinh.eve.data;

import org.springframework.data.repository.CrudRepository;

public interface MarketOrderRepository extends CrudRepository<MarketOrder, Long> {
    
    void deleteByFetchedAndBuysellAndItemTypeAndRegion(String fetched, String buysell, ItemType itemType, Region region);
}
