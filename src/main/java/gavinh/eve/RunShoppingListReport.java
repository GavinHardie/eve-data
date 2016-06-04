/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import gavinh.eve.data.MarketOrder;
import gavinh.eve.manufacturing.ITEM_TYPE;
import gavinh.eve.manufacturing.MARKET_HUB;
import gavinh.eve.manufacturing.StockList;
import gavinh.eve.manufacturing.StockListFactory;
import gavinh.eve.service.DirectSellService;
import gavinh.eve.service.StockListService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("run-shoppinglist-report")
public class RunShoppingListReport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunShoppingListReport.class);
    
    @Autowired
    StockListService stockListService;
    
    @Autowired
    DirectSellService directSellService;
    
    @Override
    public void run(String... strings) throws Exception {
        
        // Get a list of stations
        List<Integer> stationIds = new ArrayList<>();
        for(MARKET_HUB hub : MARKET_HUB.values()) {
            stationIds.add(hub.getStationId());
        }

        ITEM_TYPE itemType = ITEM_TYPE.CovertOpsCloak;
        int quantity = 3520;
        
        // Generate purchases
        StockList materialsStockList = StockListFactory.getStockList(itemType, quantity);
        
        stockListService.makePurchases(materialsStockList, MARKET_HUB.Jita.getStationId());
        log.info(String.format("Unit cost [%,.2f], total cost [%,.2f] shopping at Jita", getPrice(materialsStockList) / quantity, getPrice(materialsStockList)));
        
        stockListService.makePurchases(materialsStockList, stationIds);
        log.info(String.format("Unit cost [%,.2f], total cost [%,.2f] shopping at Hubs", getPrice(materialsStockList) / quantity, getPrice(materialsStockList)));
        
        stockListService.makeHighsecPurchases(materialsStockList);
        log.info(String.format("Unit cost [%,.2f], total cost [%,.2f] shopping in High", getPrice(materialsStockList) / quantity, getPrice(materialsStockList)));

        StockList cloakStockList = new StockList();
        cloakStockList.add(itemType.getItemTypeId(), quantity);
        stockListService.makeSales(cloakStockList, stationIds);
        log.info(String.format("Avg unit sell [%,.2f], total sell [%,.2f] at Hubs", getPrice(cloakStockList) / quantity, getPrice(cloakStockList)));
        
        log.info(cloakStockList.toString());
//        List<MarketOrder> marketOrders = directSellService.getBuyOrders(ITEM_TYPE.CovertOpsCloak, 3520);
//        log.info(String.format("Buy order for [%d] [%s] in high:", quantity, itemType.toString()));
//        for(MarketOrder marketOrder : marketOrders) {
//            log.info(String.format("[%s] buys [%d] at [%,.2f] ", marketOrder.getStation().getSolarSystem().getName(),
//                                                              marketOrder.getQuantity(),
//                                                              marketOrder.getPrice()));
//        }
    }
    
    public float getPrice(StockList shoppingList) {
        float totalCost = shoppingList.getTotalPrice();
        totalCost *= 1.03;          // 3% manufacturing slot cost
        totalCost *= 1.035;         // 3.5% tax when selling
        totalCost += 150000000.0;     // Fuel
        
        return totalCost;
    }
}
