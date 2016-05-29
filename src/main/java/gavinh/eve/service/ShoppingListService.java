package gavinh.eve.service;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.data.Station;
import gavinh.eve.data.StationRepository;
import gavinh.eve.manufacturing.ShoppingList;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Gavin
 */
@Service
public class ShoppingListService {
    
    private static final Logger log = LoggerFactory.getLogger(ShoppingListService.class);
    
    @Autowired
    private MarketOrderRepository marketOrderRepository;
    
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ItemTypeRepository itemTypeRepository;

    private final String TODAY;
    private static final String ORDER_TYPE = "sell";
    
    public ShoppingListService() {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TODAY = sdf.format(new Date());
        
    }
    
    public void makePurchases(ShoppingList shoppingList, Integer stationId) {
        Station station = stationRepository.findOne(stationId);
        
        for(ShoppingList.Item item : shoppingList.items) {
            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeAndStation(TODAY, ORDER_TYPE, itemType, station);
            Collections.sort(marketOrders, new MarketOrderComparator(ORDER_TYPE));
            item.purchases = new ArrayList<>();
            item.purchases.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
        }
    }
    
    public void makePurchases(ShoppingList shoppingList, List<Integer> stationIds) {
        
        List<Station> stations = new ArrayList<>();
        for(Integer stationId : stationIds) {
            stations.add(stationRepository.findOne(stationId));
        }
        
        for(ShoppingList.Item item : shoppingList.items) {

            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = new ArrayList<>();
            for(Station station : stations) {
                marketOrders.addAll(marketOrderRepository.findByFetchedAndBuysellAndItemTypeAndStation(TODAY, ORDER_TYPE, itemType, station));
            }
            Collections.sort(marketOrders, new MarketOrderComparator(ORDER_TYPE));

            item.purchases = new ArrayList<>();
            item.purchases.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
        }            
    }
    
    public void makeHighsecPurchases(ShoppingList shoppingList) {
        for(ShoppingList.Item item : shoppingList.items) {
            
            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, ORDER_TYPE, itemType);
            Collections.sort(marketOrders, new MarketOrderComparator(ORDER_TYPE));
            
            item.purchases = new ArrayList<>();
            item.purchases.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
        }            
    }
        
    
    private Collection<ShoppingList.Purchase> processMarketOrders(ItemType itemType, List<MarketOrder> marketOrders, int quantity) {
        
        Map<Integer,ShoppingList.Purchase> result = new HashMap<>();        // StationId,Purchase
        
        int outstanding = quantity;
        for(MarketOrder marketOrder : marketOrders) {
            int q = Math.min(marketOrder.getQuantity(), outstanding);
            if (q > 0) {
                ShoppingList.Purchase purchase = result.get(marketOrder.getStation().getId());
                if (purchase == null) {
                    purchase = new ShoppingList.Purchase();
                    purchase.station = marketOrder.getStation();
                    purchase.itemType = itemType;
                    purchase.minPrice = marketOrder.getPrice();
                    result.put(marketOrder.getStation().getId(), purchase);
                }
                purchase.maxPrice = marketOrder.getPrice();
                purchase.totalQuantity += q;
                purchase.totalCost += (q * marketOrder.getPrice());
                outstanding -= q;
            }
        }
        for(ShoppingList.Purchase purchase : result.values()) {
            purchase.outOfStock = outstanding > 0;
        }
        return result.values();
    }
    
    static private class MarketOrderComparator implements Comparator<MarketOrder> {
    
        private final String orderType;
        
        public MarketOrderComparator(String orderType) {
            this.orderType = orderType;
        }
        
        @Override
        public int compare(MarketOrder o1, MarketOrder o2) {
            switch (orderType) {
                case "sell":
                    return Float.compare(o1.getPrice(), o2.getPrice());
                case "buy":
                    return Float.compare(o2.getPrice(), o1.getPrice());
                default:
                    throw new RuntimeException("Unrecognised ordertype - must be buy or sell");
            }
        }
    }
}
