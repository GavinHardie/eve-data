package gavinh.eve.data;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ItemTypeRepository extends CrudRepository<ItemType, Integer> {
    
    List<ItemType> findByMarketGroup(MarketGroup marketGroup);
    
}
