package gavinh.eve.service;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.data.Station;
import gavinh.eve.data.StationRepository;
import gavinh.eve.manufacturing.ShoppingList;
import gavinh.eve.utils.MarketOrderComparator;
import gavinh.eve.utils.Purchase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    public ShoppingList makePurchases(ShoppingList shoppingList, Integer stationId) {
        Station station = stationRepository.findOne(stationId);
        
        for(ShoppingList.Item item : shoppingList.items) {
            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeAndStation(TODAY, ORDER_TYPE, itemType, station);
            
            Collections.sort(marketOrders, new MarketOrderComparator(ORDER_TYPE));
            
            item.purchases = new ArrayList<>();
            item.purchases.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
            Collections.sort(item.purchases, new Purchase.PurchaseComparator(ORDER_TYPE));
        }
        return shoppingList;
    }
    
    public ShoppingList makePurchases(ShoppingList shoppingList, List<Integer> stationIds) {
        
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
            Collections.sort(item.purchases, new Purchase.PurchaseComparator(ORDER_TYPE));
        }
        return shoppingList;
    }
    
    public ShoppingList makeHighsecPurchases(ShoppingList shoppingList) {
        for(ShoppingList.Item item : shoppingList.items) {
            
            ItemType itemType = itemTypeRepository.findOne(item.itemTypeId);
            List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, ORDER_TYPE, itemType);
            Collections.sort(marketOrders, new MarketOrderComparator(ORDER_TYPE));
            
            item.purchases = new ArrayList<>();
            item.purchases.addAll(processMarketOrders(itemType, marketOrders, item.quantity));
            Collections.sort(item.purchases, new Purchase.PurchaseComparator(ORDER_TYPE));
        }
        return shoppingList;
    }
        
    
    private Collection<Purchase> processMarketOrders(ItemType itemType, List<MarketOrder> marketOrders, int quantity) {
        
        Map<Integer,Purchase> result = new HashMap<>();        // StationId,Purchase
        
        int outstanding = quantity;
        for(MarketOrder marketOrder : marketOrders) {
            int q = Math.min(marketOrder.getQuantity(), outstanding);
            if (q > 0) {
                Purchase purchase = result.get(marketOrder.getStation().getId());
                if (purchase == null) {
                    purchase = new Purchase();
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
        for(Purchase purchase : result.values()) {
            purchase.outOfStock = outstanding > 0;
        }
        return result.values();
    }
    
}
