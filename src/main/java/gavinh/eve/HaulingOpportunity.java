/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.manufacturing.ITEM_TYPE;
import gavinh.eve.utils.MarketOrderComparator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

@SpringBootApplication
@Profile("hauling-opportunity")
public class HaulingOpportunity implements CommandLineRunner {

    private static final boolean CONSIDER_LOWSEC = false;
    private static final String BUY_ORDERS = "buy";
    private static final String SELL_ORDERS = "sell";
    private static final int MAX_LOAD_VALUE = 1000000000;       // 1b
    private static final int FREIGHTER_CAPACITY = 300000;       // 300k m3
    private static final int BLOCKADE_CAPACITY = 10000;         // 10k m3
    
    private String TODAY;
    
    private static final Logger log = LoggerFactory.getLogger(HaulingOpportunity.class);
    
    @Autowired
    MarketOrderRepository marketOrderRepository;
    
    @Autowired
    ItemTypeRepository itemTypeRepository;
    
    @Override
    public void run(String... strings) throws Exception {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TODAY = sdf.format(new Date());

        List<Opportunity> opportunities = new ArrayList<>();

        Set<Integer> itemTypeIdToProcess = new HashSet<>();
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.covopRelatedGoods));
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.advancedComponents));
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.tradeGoods));
        itemTypeIdToProcess.addAll(Arrays.asList(ITEM_TYPE.datacores));
        for(int itemTypeId : itemTypeIdToProcess) {
            Opportunity opportunity = consider(itemTypeId, CONSIDER_LOWSEC);
            if (opportunity != null)
                opportunities.add(opportunity);
        }
        
        Collections.sort(opportunities, new OpportunityComparator());
        
        for(Opportunity opportunity : opportunities) {
            log.info(opportunity.toString());
        }
    }
    
    private Opportunity consider(int itemTypeId, boolean includeLow) {
        
        ItemType itemType = itemTypeRepository.findOne(itemTypeId);
        
        // Get the buy orders, optionally including the lowsec buy orders
        List<MarketOrder> buyOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, BUY_ORDERS, itemType);
        if (includeLow) 
            buyOrders.addAll(marketOrderRepository.findByFetchedAndBuysellAndItemTypeInLowsec(TODAY, BUY_ORDERS, itemType));
        Collections.sort(buyOrders, new MarketOrderComparator(BUY_ORDERS));

        // Get the sell orders, optionaly including the lowsec sell orders
        List<MarketOrder> sellOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, SELL_ORDERS, itemType);
        if (includeLow)
            sellOrders.addAll(marketOrderRepository.findByFetchedAndBuysellAndItemTypeInLowsec(TODAY, SELL_ORDERS, itemType));
        Collections.sort(sellOrders, new MarketOrderComparator(SELL_ORDERS));
        
        // If either buy or sell orders is empty, quit.  No trade possible.
        if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
            return null;
        }

        Opportunity bestOpportunity = null;     // The best trading opportunity found so far
        
        for(MarketOrder buyOrder : buyOrders) {
            for(MarketOrder sellOrder : sellOrders) {

                // Workout the trading limit
                int maxCapacity = includeLow ? BLOCKADE_CAPACITY : FREIGHTER_CAPACITY;
                int valueQuantityLimit = (int)(MAX_LOAD_VALUE / buyOrder.getPrice());
                int volumeQuantityLimit = (int)(maxCapacity/ itemType.getVolume());
                int quantityLimit;
                LIMITED_BY quantityLimitReason;
                if (valueQuantityLimit < volumeQuantityLimit) {
                    quantityLimit = valueQuantityLimit;
                    quantityLimitReason = LIMITED_BY.VALUE;
                } else {
                    quantityLimit = volumeQuantityLimit;
                    quantityLimitReason = LIMITED_BY.VOLUME;
                }
                
                // Work out the details of this trade
                int origQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                float profitPerItem = buyOrder.getPrice() - (sellOrder.getPrice() * 1.01f);    // Sales tax included
                int quantity = origQuantity;
                LIMITED_BY limitedBy = LIMITED_BY.STOCK;
                if (quantity > quantityLimit) {
                    quantity = quantityLimit;
                    limitedBy = quantityLimitReason;
                }

                float profit = profitPerItem * quantity;
                
                // Update bestOpportunity if better
                if (profit > 0) {
                    if (bestOpportunity == null) {
                        bestOpportunity = new Opportunity();
                        bestOpportunity.buy = buyOrder;
                        bestOpportunity.sell = sellOrder;
                        bestOpportunity.quantity = quantity;
                        bestOpportunity.volume = quantity * itemType.getVolume();
                        bestOpportunity.profit = profit;
                        bestOpportunity.limitedBy = limitedBy;
                    } else if (profit > bestOpportunity.profit) {
                        bestOpportunity.buy = buyOrder;
                        bestOpportunity.sell = sellOrder;
                        bestOpportunity.quantity = quantity;
                        bestOpportunity.volume = quantity * itemType.getVolume();
                        bestOpportunity.profit = profit;
                        bestOpportunity.limitedBy = limitedBy;
                    }
                }
            }
        }

        return bestOpportunity;
    }

    private enum LIMITED_BY { STOCK, VALUE, VOLUME };

    private class Opportunity {
        
        private MarketOrder buy;
        private MarketOrder sell;
        private int quantity;
        private float profit;
        private LIMITED_BY limitedBy;
        private float volume;
        
        @Override
        public String toString() {
            return String.format("%,.2f can be made shipping %d %s from %s/%s to %s/%s volume is %,.0f limited by %s",
                    profit, 
                    quantity, 
                    buy.getItemType().getName(), 
                    sell.getStation().getName(), 
                    sell.getStation().getSolarSystem().getName(),
                    buy.getStation().getName(), 
                    buy.getStation().getSolarSystem().getName(),
                    volume,
                    limitedBy.toString().toLowerCase());
          }
    }
    
    private class OpportunityComparator implements Comparator<Opportunity> {

        @Override
        public int compare(Opportunity o1, Opportunity o2) {
            return Float.compare(o2.profit, o1.profit);
        }
        
    }
}
