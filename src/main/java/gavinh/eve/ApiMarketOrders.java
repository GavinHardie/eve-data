package gavinh.eve;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.utils.Runner;
import gavinh.eve.data.Region;
import gavinh.eve.data.RegionRepository;
import gavinh.eve.manufacturing.ITEM_TYPE;
import gavinh.eve.service.ItemTypeService;
import gavinh.eve.service.MarketOrderService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Profile("market-orders")
public class ApiMarketOrders implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApiMarketOrders.class);
    private static final String[] orderTypes = new String[] { "buy", "sell" };

    @Autowired
    Environment environment;
    
    @Autowired
    private MarketOrderService marketOrderService;

    @Autowired
    private ItemTypeService itemTypeService;
    
    @Autowired
    private ItemTypeRepository itemTypeRepository;
    
    @Autowired
    private RegionRepository regionRepository;
    
    @Override
    public void run(String... strings) throws Exception {
        
        boolean fast = environment.getProperty("fast") != null;
        if (fast)
            log.info("Running with --fast set");
        
        int numThreads = 100;
        String paramValue = environment.getProperty("threads", "100");
        try {
            numThreads = Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            log.error(String.format("Invalid parameter --threads=n expected.  [%s] is not a valid number.  Defaulting to 100", paramValue));
        }
        log.info(String.format("Running with %d threads", numThreads));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fetched = sdf.format(new Date());

        Runner runner = new Runner(numThreads);
        Iterable<Region> regions = regionRepository.findAll();
        
        Set<Integer> itemTypeIdToProcess = new HashSet<>();
        
        for(ItemType itemType : itemTypeService.deepScanMarketGroup(9)) {
            itemTypeIdToProcess.add(itemType.getId());
        }

        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.covopRelatedGoods));
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.advancedComponents));
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.tradeGoods));
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.datacores));
        
        List<Integer> sortedList = new ArrayList<>(itemTypeIdToProcess);
        sortedList.sort(Integer::compare);
        
        int total = sortedList.size();
        int count = 0;
        for(Integer itemTypeId : sortedList) {
            ItemType itemType = itemTypeRepository.findOne(itemTypeId);
            log.info(String.format("[%d / %d] %s ", ++count, total, itemType.getName()));
            for(Region region : regions) {
                runner.run(new FetchOrders(fetched, itemType, region));
            }
            // Finish to prevent multile threads operating on the same region,
            // which could lead to deadlocks if they both discover the same
            // station.
            if (!fast) {        // --fast on commandline skips the syn at the end of each itemType
                runner.finish();
            }
        }
    }


    /**
     * Fetch all the orders for the itemType/region
     */
    public class FetchOrders implements Runnable {
        
        private final String fetched;
        private final ItemType itemType;
        private final Region region;
        
        public FetchOrders(String fetched, ItemType itemType, Region region) {
            this.fetched = fetched;
            this.itemType = itemType;
            this.region = region;
        }
        
        @Override
        public void run() {
            for(String orderType : orderTypes) {
                int retry = 10;
                while(retry > 0) {
                    try {
                        marketOrderService.fetchOrders(fetched, itemType, orderType, region);
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
