package gavinh.eve;

import gavinh.eve.utils.Runner;
import gavinh.eve.data.Region;
import gavinh.eve.data.RegionRepository;
import gavinh.eve.service.MarketOrderService;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("market-orders")
public class ApiMarketOrders implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApiMarketOrders.class);
    private static final String[] orderTypes = new String[] { "buy", "sell" };
    private static final Integer[] itemTypeIds = new Integer[] {34, 35, 36, 37, 38, 39, 40, 11483, 9842, 9840, 11399, 2361, 2349, 16680, 16681, 16670, 16671, 33359, 11541, 11693, 11370, 11578, 20414, 20419, 34205, 40519, 40520, 29668, 34133 };

    @Autowired
    private MarketOrderService marketOrderService;

    @Autowired
    private RegionRepository regionRepository;
    
    @Override
    public void run(String... strings) throws Exception {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fetched = sdf.format(new Date());

        Runner runner = new Runner(20);
        Iterable<Region> regions = regionRepository.findAll();
        
        for(Integer itemTypeId : itemTypeIds) {
            for(Region region : regions) {
                runner.run(new FetchOrders(fetched, itemTypeId, region));
            }
            // Finish to prevent multile threads operating on the same region,
            // which could lead to deadlocks if they both discover the same
            // station.
            runner.finish();        
        }
    }


    /**
     * Fetch all the orders for the itemType/region
     */
    public class FetchOrders implements Runnable {
        
        private final String fetched;
        private final Integer itemTypeId;
        private final Region region;
        
        public FetchOrders(String fetched, Integer itemTypeId, Region region) {
            this.fetched = fetched;
            this.itemTypeId = itemTypeId;
            this.region = region;
        }
        
        @Override
        public void run() {
            for(String orderType : orderTypes) {
                int retry = 10;
                while(retry > 0) {
                    try {
                        marketOrderService.fetchOrders(fetched, itemTypeId, orderType, region);
                        retry = 0;
                    } catch (RuntimeException e) {
                        log.error(String.format("[%d] [%s]", retry--, e.getMessage()));
                    }
                    if (retry > 0) {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }
        }
    }
    
}
