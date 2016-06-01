package gavinh.eve;

import gavinh.eve.service.MarketOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("market-orders")
public class ApiMarketOrders implements CommandLineRunner {

    private static final Long[] itemTypeIds = new Long[] {34L, 35L, 36L, 37L, 38L, 39L, 40L, 11483L, 9842L, 9840L, 11399L, 2361L, 2349L, 16680L, 16681L, 16670L, 16671L, 33359L, 11541L, 11693L, 11370L, 11578L, 20414L, 20419L, 34205L, 40519L, 40520L, 29668L, 34133L };

    @Autowired
    private MarketOrderService marketOrderService;
    
    
    @Override
    public void run(String... strings) throws Exception {

        for(Long itemTypeId : itemTypeIds) {
            marketOrderService.fetchOrders(itemTypeId);
        }
    }
    
    
}
