package gavinh.eve.data;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface MarketGroupRepository  extends CrudRepository<MarketGroup, Integer> {
    
    List<MarketGroup> findByParentMarketGroup(MarketGroup marketGroup);
    
}
