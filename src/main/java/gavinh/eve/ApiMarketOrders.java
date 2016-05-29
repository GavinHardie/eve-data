package gavinh.eve;

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

    private static final int CACHE_HOURS = 2;
    private static final int NUM_THREADS = 20;
    
    public static final String[] orderTypes = new String[] { "buy", "sell" };
    private static final Logger log = LoggerFactory.getLogger(ApiMarketOrders.class);
    private static final Long[] itemTypeIds = new Long[] {34L, 35L, 36L, 37L, 38L, 39L, 40L, 11483L, 9842L, 9840L, 11399L, 2361L, 2349L, 16680L, 16681L, 16670L, 16671L, 33359L, 11541L, 11693L, 11370L, 11578L, 20414L, 20419L, 34205L };
    private static String TODAY;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private MarketOrderService marketOrderService;
    
    
    @Override
    public void run(String... strings) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TODAY = sdf.format(new Date());
        
        Runner runner = new Runner(NUM_THREADS);
        
        Iterable<Region> regions = regionRepository.findAll();
        for(Long itemTypeId : itemTypeIds) {
                // Having regions as inner loop and waiting for runner.isFinished
                // prevents mlutiple threads ever working on the same region.
                // Multiple threads on same region give PK constrains as they
                // add stations.
            for(Region region : regions) {
                runner.run(new FetchOrders(itemTypeId, region));
            }
            while(!runner.isFinished()) {
                Thread.sleep(1000);
            }
        }
    }
    
    public class FetchOrders implements Runnable {
        private final Long itemTypeId;
        private final Region region;
        
        public FetchOrders(Long itemTypeId, Region region) {
            this.itemTypeId = itemTypeId;
            this.region = region;
        }
        
        @Override
        public void run() {
            for(String orderType : orderTypes) {
                int retry = 10;
                while(retry > 0) {
                    try {
                        marketOrderService.fetchOrders(CACHE_HOURS, TODAY, itemTypeId, orderType, region);
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
