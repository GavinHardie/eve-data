package gavinh.eve.service;

import com.jayway.jsonpath.DocumentContext;
import gavinh.eve.Utils;
import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketApiCall;
import gavinh.eve.data.MarketApiCallKey;
import gavinh.eve.data.MarketApiCallRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.data.Region;
import gavinh.eve.data.SolarSystem;
import gavinh.eve.data.SolarSystemRepository;
import gavinh.eve.data.Station;
import gavinh.eve.data.StationRepository;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketOrderService {

    private static final Logger log = LoggerFactory.getLogger(MarketOrderService.class);
    
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
    
    public void fetchOrders(int CACHE_HOURS, String TODAY, Long itemTypeId, String orderType, Region region) {

        Date nowLessCache = new Date(System.currentTimeMillis() - (CACHE_HOURS * 60 * 60 * 1000));
        
        ItemType itemType = itemTypeRepository.findOne(itemTypeId);

        log.info(String.format("Fetching %s orders for %s in %s", orderType, itemType.getName(), region.getName()));

        MarketApiCallKey marketApiCallKey = new MarketApiCallKey();
        marketApiCallKey.setBuysell(orderType);
        marketApiCallKey.setItemType(itemType);
        marketApiCallKey.setRegion(region);
        MarketApiCall marketApiCall = marketApiCallRepository.findOne(marketApiCallKey);
        if (marketApiCall != null && marketApiCall.getFetchDatetime().after(nowLessCache)) {
            return;
        }

        // Delete any records in the DB from requests made earlier today
        marketOrderRepository.deleteByFetchedAndBuysellAndItemTypeAndRegion(TODAY, orderType, itemType, region);

        String marketorders_url = String.format("https://crest-tq.eveonline.com/market/%d/orders/%s/?type=http://crest-tq.eveonline.com/types/%d/", region.getId(), orderType, itemType.getId());
        DocumentContext marketorders_context = Utils.get(marketorders_url);
        while (marketorders_context != null) {

            // Process the locations.  Fetch station & solarsystem information if not in DB
            List<Map<String, Object>> location_maps = marketorders_context.read("$.items[*].location");
            Set<Integer> validStationIds = new HashSet<>();
            for (Map<String, Object> location_map : location_maps) {
                Integer stationId = Utils.mapPath(Integer.class, location_map, "id");
                if (validStationIds.contains(stationId)) {
                    continue;
                }
                Station station = stationRepository.findOne(stationId);
                if (station == null) {
                    DocumentContext location_context = Utils.decodeAndGet(Utils.mapPath(String.class, location_map, "href"));
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

            // Process the marketorders
            List<Map<String, Object>> marketorder_maps = marketorders_context.read("$.items[*]");
            for (Map<String, Object> marketorder_map : marketorder_maps) {
                Station station = stationRepository.findOne(Utils.mapPath(Integer.class, marketorder_map, "location", "id"));

                MarketOrder marketOrder = new MarketOrder();
                marketOrder.setId(Utils.mapPath(Long.class, marketorder_map, "id"));
                marketOrder.setQuantity(Utils.mapPath(Integer.class, marketorder_map, "volume"));
                marketOrder.setPrice(Utils.mapPath(Float.class, marketorder_map, "price"));
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
            marketorders_context = Utils.decodeAndGet(marketorders_context.read("$.next.href", String.class));
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