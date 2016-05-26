package gavinh.eve;

import com.jayway.jsonpath.DocumentContext;
import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketApiCall;
import gavinh.eve.data.MarketApiCallKey;
import gavinh.eve.data.MarketApiCallRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.data.Region;
import gavinh.eve.data.RegionRepository;
import gavinh.eve.data.SolarSystem;
import gavinh.eve.data.SolarSystemRepository;
import gavinh.eve.data.Station;
import gavinh.eve.data.StationRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transactional;
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
    private static final Long[] itemTypeIds = new Long[] {34L, 35L, 36L, 37L, 38L, 39L, 40L, 11483L, 9842L, 9840L, 11399L, 2361L, 2349L, 16680L, 16681L, 16670L, 16671L, 33359L, 11541L, 11693L, 11370L, 11578L };
    private static final Date nowLessCache = new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000));
    private static String TODAY;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;
    
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private SolarSystemRepository solarSystemRepository;
    
    @Autowired
    private MarketApiCallRepository marketApiCallRepository;

    @Autowired
    private MarketOrderRepository marketOrderRepository;
    
    @Override
    public void run(String... strings) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TODAY = sdf.format(new Date());
        
        Iterable<Region> regions = regionRepository.findAll();
        for(Long itemTypeId : itemTypeIds) {
            for(String orderType : orderTypes) {
                for(Region region : regions) {
                    int retry = 10;
                    while(retry > 0) {
                        try {
                            fetchOrders(itemTypeId, orderType, region);
                            retry = 0;
                        } catch (RuntimeException e) {
                            log.error(String.format("[%d] [%s]", retry--, e.getMessage()));
                            Thread.sleep(15000);
                        }
                    }
                }
            }
        }
    }
    
    @Transactional
    private void fetchOrders(Long itemTypeId, String orderType, Region region) {

        
        ItemType itemType = itemTypeRepository.findOne(itemTypeId);
        
        log.info(String.format("Fetching %s orders for %s in %s", orderType, itemType.getName(), region.getName()));
        
        MarketApiCallKey marketApiCallKey = new MarketApiCallKey();
        marketApiCallKey.setBuysell(orderType);
        marketApiCallKey.setItemType(itemType);
        marketApiCallKey.setRegion(region);
        MarketApiCall marketApiCall = marketApiCallRepository.findOne(marketApiCallKey);
        if (marketApiCall != null && marketApiCall.getFetchDatetime().after(nowLessCache))
            return;

        marketOrderRepository.deleteByFetchedAndBuysellAndItemTypeAndRegion(TODAY, orderType, itemType, region);
        
        String marketorders_url = String.format("https://crest-tq.eveonline.com/market/%d/orders/%s/?type=http://crest-tq.eveonline.com/types/%d/", region.getId(), orderType, itemType.getId());
        DocumentContext marketorders_context = Rest.get(marketorders_url);
        while(marketorders_context != null) {

            // Process the locations
            List<Map<String,Object>> location_maps = marketorders_context.read("$.items[*].location");
            Set<Integer> validStationIds = new HashSet<>();
            for(Map<String,Object> location_map : location_maps) {
                Integer stationId = Rest.mapPath(Integer.class, location_map, "id");
                if (validStationIds.contains(stationId))
                    continue;
                Station station = stationRepository.findOne(stationId);
                if (station == null) {
                    DocumentContext location_context = Rest.decodeAndGet(Rest.mapPath(String.class, location_map, "href"));
                    Integer solarSystemId = location_context.read("$.solarSystem.id", Integer.class);
                    SolarSystem solarSystem = solarSystemRepository.findOne(solarSystemId);
                    if (solarSystem == null) {
                        solarSystem = new SolarSystem();
                        solarSystem.setId(solarSystemId);
                        solarSystem.setName(location_context.read("$.solarSystem.name", String.class));
                        solarSystem.setSecurity(location_context.read("$.solarSystem.securityStatus", Float.class));
                        solarSystem.setRegion(region);
                        solarSystemRepository.save(solarSystem);
                    }
                    station = new Station();
                    station.setId(stationId);
                    station.setName(location_context.read("$.station.name", String.class));
                    station.setSolarSystem(solarSystem);
                    stationRepository.save(station);
                    log.info(String.format("Added station [%s] in solar system [%s]", station.getName(), solarSystem.getName()));
                }
                validStationIds.add(stationId);
            }
            
            List<Map<String,Object>> marketorder_maps = marketorders_context.read("$.items[*]");
            for(Map<String,Object> marketorder_map : marketorder_maps) {
                Station station = stationRepository.findOne(Rest.mapPath(Integer.class, marketorder_map, "location", "id"));

                MarketOrder marketOrder = new MarketOrder();
                marketOrder.setId((Long) marketorder_map.get("id"));
                marketOrder.setQuantity((Integer) marketorder_map.get("volume"));
                marketOrder.setPrice(Rest.mapPath(Float.class, marketorder_map, "price"));
                marketOrder.setBuysell(orderType);
                marketOrder.setFetched(TODAY);
                marketOrder.setItemType(itemType);
                marketOrder.setRegion(region);
                marketOrder.setStation(station);
                marketOrderRepository.save(marketOrder);
            }
            
            int numMarketOrders = marketorder_maps.size();
            if (numMarketOrders > 0) {
                log.info(String.format("Added [%d] market orders", numMarketOrders));
            }
            marketorders_context = Rest.decodeAndGet(marketorders_context.read("$.next.href", String.class));
        }
        
        // Update marketApiCall
        if (marketApiCall == null) {
            marketApiCall = new MarketApiCall();
            marketApiCall.setKey(marketApiCallKey);
        }
        marketApiCall.setFetchDatetime(new Date());
        marketApiCallRepository.save(marketApiCall);
        
    }
    
}
