/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import gavinh.eve.data.MarketOrder;
import gavinh.eve.manufacturing.ITEM_TYPE;
import gavinh.eve.manufacturing.MARKET_HUB;
import gavinh.eve.manufacturing.ShoppingList;
import gavinh.eve.manufacturing.ShoppingListFactory;
import gavinh.eve.service.DirectSellService;
import gavinh.eve.service.ShoppingListService;
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
    ShoppingListService shoppingListService;
    
    @Autowired
    DirectSellService directSellService;
    
    @Override
    public void run(String... strings) throws Exception {
        
        // Get a list of stations
        List<Integer> stationIds = new ArrayList<>();
        for(MARKET_HUB hub : MARKET_HUB.values()) {
            stationIds.add(hub.getStationId());
        }

        ITEM_TYPE item_type = ITEM_TYPE.CovertOpsCloak;
        int quantity = 3520;
        
        // Generate purchases
        ShoppingList shoppingAtJita = shoppingListService.makePurchases(ShoppingListFactory.getShoppingList(item_type, quantity), MARKET_HUB.Jita.getStationId());
        ShoppingList shoppingAtHubs = shoppingListService.makePurchases(ShoppingListFactory.getShoppingList(item_type, quantity), stationIds);
        ShoppingList shoppingInHigh = shoppingListService.makeHighsecPurchases(ShoppingListFactory.getShoppingList(item_type, quantity));

        // Print the results
        // log.info("ShoppingListReport\n" + shoppingList.toString());
        
        log.info(String.format("Unit cost shopping at Jita [%,.2f]", getUnitCost(shoppingAtJita)));
        log.info(String.format("Unit cost shopping at Hubs [%,.2f]", getUnitCost(shoppingAtHubs)));
        log.info(String.format("Unit cost shopping in High [%,.2f]", getUnitCost(shoppingInHigh)));
        
        List<MarketOrder> marketOrders = directSellService.getBuyOrders(ITEM_TYPE.CovertOpsCloak, 3520);
        log.info(String.format("Buy order for [%d] [%s] in high:", quantity, item_type.toString()));
        for(MarketOrder marketOrder : marketOrders) {
            log.info(String.format("[%s] buys [%d] at [%,.2f] ", marketOrder.getStation().getSolarSystem().getName(),
                                                              marketOrder.getQuantity(),
                                                              marketOrder.getPrice()));
        }
    }
    
    public float getUnitCost(ShoppingList shoppingList) {
        float totalCost = shoppingList.getTotalPrice();
        totalCost *= 1.03;          // 3% manufacturing slot cost
        totalCost *= 1.035;         // 3.5% tax when selling
        totalCost += 150000000;     // Fuel
        
        float unitCost = totalCost / shoppingList.output.quantity;
        return unitCost;
    }
}
