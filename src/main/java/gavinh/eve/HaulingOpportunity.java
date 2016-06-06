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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("hauling-opportunity")
public class HaulingOpportunity implements CommandLineRunner {

    private static final String BUY_ORDERS = "buy";
    private static final String SELL_ORDERS = "sell";
    private static final int MAX_LOAD_VALUE = 1000000000;       // 1b
    private static final int MAX_CAPACITY = 300000 ;            // 300k m3
    
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
        for(int itemTypeId : ITEM_TYPE.advancedComponents) {
            Opportunity opportunity = consider(itemTypeId);
            if (opportunity != null)
                opportunities.add(opportunity);
        }
        
        Collections.sort(opportunities, new OpportunityComparator());
        
        for(Opportunity opportunity : opportunities) {
            log.info(opportunity.toString());
        }
    }
    
    private Opportunity consider(int itemTypeId) {
        
        ItemType itemType = itemTypeRepository.findOne(itemTypeId);
        
        List<MarketOrder> buyOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, BUY_ORDERS, itemType);
        Collections.sort(buyOrders, new MarketOrderComparator(BUY_ORDERS));

        List<MarketOrder> sellOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(TODAY, SELL_ORDERS, itemType);
        Collections.sort(sellOrders, new MarketOrderComparator(SELL_ORDERS));
        
        if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
            return null;
        }

        Opportunity bestOpportunity = null;
        
        for(MarketOrder buyOrder : buyOrders) {
            for(MarketOrder sellOrder : sellOrders) {

                int valueQuantityLimit = (int)(MAX_LOAD_VALUE / buyOrder.getPrice());
                int volumeQuantityLimit = (int)(MAX_CAPACITY / itemType.getVolume());
                int quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                float markup = buyOrder.getPrice() - sellOrder.getPrice();
                float profit = markup * quantity;
                LIMITED_BY limitedBy = LIMITED_BY.stock;
                if (quantity > volumeQuantityLimit) {
                    quantity = volumeQuantityLimit;
                    limitedBy = LIMITED_BY.volume;
                }
                if (quantity > valueQuantityLimit) {
                    quantity = valueQuantityLimit;
                    limitedBy = LIMITED_BY.value;
                }
                
                if (profit > 0) {
                    if (bestOpportunity == null) {
                        bestOpportunity = new Opportunity();
                        bestOpportunity.buy = buyOrder;
                        bestOpportunity.sell = sellOrder;
                        bestOpportunity.quantity = quantity;
                        bestOpportunity.profit = profit;
                        bestOpportunity.limitedBy = limitedBy;
                    } else if (profit > bestOpportunity.profit) {
                        bestOpportunity.buy = buyOrder;
                        bestOpportunity.sell = sellOrder;
                        bestOpportunity.quantity = quantity;
                        bestOpportunity.profit = profit;
                        bestOpportunity.limitedBy = limitedBy;
                    }
                }
            }
        }
        

        return bestOpportunity;
        
//        if (bestOpportunity != null) {
//            log.info();
//        }        
    }

    private enum LIMITED_BY { stock, value, volume };

    private class Opportunity {
        
        MarketOrder buy;
        MarketOrder sell;
        int quantity;
        float profit;
        LIMITED_BY limitedBy;
        
        public String toString() {
            return String.format("%,.2f can be made shipping %d %s from %s to %s limited by %s",
                    profit, 
                    quantity, 
                    buy.getItemType().getName(), 
                    sell.getStation().getSolarSystem().getName(),
                    buy.getStation().getSolarSystem().getName(),
                    limitedBy.toString());
          }
    }
    
    private class OpportunityComparator implements Comparator<Opportunity> {

        @Override
        public int compare(Opportunity o1, Opportunity o2) {
            return Float.compare(o2.profit, o1.profit);
        }
        
    }
}
