package gavinh.eve;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.data.Station;
import gavinh.eve.data.StationRepository;
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
@Profile("station-trade-opportunity")
public class StationTradeReport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StationTradeReport.class);
    private static final String sourceStationName = "Jita IV - Moon 4 - Caldari Navy Assembly Plant";
    private static final String destinationStationName = "Amarr VIII (Oris) - Emperor Family Academy";
            
//            "Masanuh V - Moon 8 - Ishukone Corporation Factory";
    @Autowired
    MarketOrderRepository marketOrderRepository;
    
    @Autowired
    StationRepository stationRepository;
    
    @Override
    public void run(String... strings) throws Exception {
        Station sourceStation = stationRepository.findByName(sourceStationName);
        Station destinatioStation = stationRepository.findByName(destinationStationName);
        
        report(sourceStation, destinatioStation);
        
        log.info("--------------- reverse route ----------------");
        report (destinatioStation, sourceStation);
    }
    
    public void report(Station source, Station destination) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fetched = sdf.format(new Date());
        
        List<MarketOrder> sellOrders = marketOrderRepository.findByFetchedAndBuysellAndStation(fetched, "sell", source);
        List<MarketOrder> buyOrders = marketOrderRepository.findByFetchedAndBuysellAndStation(fetched, "buy", destination);
        
        Collections.sort(sellOrders, new SellOrderComparator());
        Collections.sort(buyOrders, new BuyOrderComparator());
        
        List<Opportunity> opportunities = new ArrayList<>();
        
        while(!sellOrders.isEmpty()) {
            MarketOrder sellOrder = sellOrders.get(0);
            
            MarketOrder buyOrder = null;
            for(MarketOrder order : buyOrders) {
                if (order.getItemType().getId() == sellOrder.getItemType().getId()) {
                    buyOrder = order;
                    break;
                }
            }
            
            if (buyOrder == null) {
                sellOrders.remove(sellOrder);
                continue;
            }
                
            int quantity = Math.min(sellOrder.getQuantity(), buyOrder.getQuantity());
            float perItemProfit = (buyOrder.getPrice() * 0.99f) - sellOrder.getPrice();
            if (perItemProfit < 0.0f) {
                sellOrders.remove(sellOrder);
                continue;
            }
            
            float perM3profit = perItemProfit / sellOrder.getItemType().getVolume();
            Opportunity opportunity = new Opportunity();
            opportunity.itemType = sellOrder.getItemType();
            opportunity.quantity = quantity;
            opportunity.volume = quantity * opportunity.itemType.getVolume();
            opportunity.perM3profit = perM3profit;
            opportunity.profit = quantity * perItemProfit;
            opportunities.add(opportunity);
            
            if (sellOrder.getQuantity() == opportunity.quantity) {
                sellOrders.remove(sellOrder);
            } else {
                sellOrder.setQuantity(sellOrder.getQuantity() - opportunity.quantity);
            }
            
            if (buyOrder.getQuantity() == opportunity.quantity) {
                buyOrders.remove(buyOrder);
            } else {
                buyOrder.setQuantity(buyOrder.getQuantity() - opportunity.quantity);
            }
        }

        Collections.sort(opportunities, new OpportunityComparator());
        for(Opportunity opportunity : opportunities) {
            log.info(opportunity.toString());
        }
    }
    
    static private class Opportunity {
        private ItemType itemType;
        private int quantity;
        private float volume;
        private float perM3profit;
        private float profit;
        
        @Override
        public String toString() {
            return String.format("[%d] %s takes up %,.02f m3 profit of %,.2f which is %,.2f per m3", quantity, itemType.getName(), volume, profit, perM3profit);
        }
    }
    
    static private class OpportunityComparator implements Comparator<Opportunity> {

        @Override
        public int compare(Opportunity o1, Opportunity o2) {
            return Float.compare(o2.perM3profit, o1.perM3profit);
        }
        
    }
    
    static private class SellOrderComparator implements Comparator<MarketOrder> {

        @Override
        public int compare(MarketOrder o1, MarketOrder o2) {
            int itemTypeCompare = Integer.compare(o1.getItemType().getId(), o2.getItemType().getId());
            if (itemTypeCompare == 0) {
                return Float.compare(o1.getPrice(), o2.getPrice());
            } else {
                return itemTypeCompare;
            }
        }
    }
    
    static private class BuyOrderComparator implements Comparator<MarketOrder> {
        
        @Override
        public int compare(MarketOrder o1, MarketOrder o2) {
            int itemTypeCompare = Integer.compare(o1.getItemType().getId(), o2.getItemType().getId());
            if (itemTypeCompare == 0) {
                return Float.compare(o2.getPrice(), o1.getPrice());
            } else {
                return itemTypeCompare;
            }
        }
    }
}
