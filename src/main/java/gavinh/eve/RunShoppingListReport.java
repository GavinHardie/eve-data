/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import gavinh.eve.manufacturing.MarketHub;
import gavinh.eve.manufacturing.Purchase;
import gavinh.eve.manufacturing.ShoppingList;
import gavinh.eve.manufacturing.ShoppingListFactory;
import gavinh.eve.service.PurchasesService;
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
    PurchasesService purchasesService;
    
    @Override
    public void run(String... strings) throws Exception {
        
        // Get a list of stations
        List<Integer> stationIds = new ArrayList<>();
        for(MarketHub hub : MarketHub.values()) {
            stationIds.add(hub.getStationId());
        }
        
        // Get a shopping list
        ShoppingList shoppingList = ShoppingListFactory.getShoppingList();
        
        // Generate purchases
        purchasesService.makePurchases(stationIds, shoppingList);

        float totalCost = 0.0f;
        for(ShoppingList.Item item : shoppingList.items) {
            boolean first = true;
            for(Purchase purchase : item.purchases) {
                if (first) {
                    log.info(String.format("%,d %s [%d]", item.quantity, purchase.itemType.getName(), purchase.itemType.getId()));
                    first = false;
                }
                totalCost += purchase.totalCost;
                log.info(String.format("\t%,d at %s costing %,.2f (from %,.2f to %,.2f)", 
                        purchase.totalQuantity, purchase.station.getSolarSystem().getName(), purchase.totalCost, purchase.minPrice, purchase.maxPrice));
            }
        }
        log.info("---");
        log.info(String.format("Total cost %,.2f", totalCost));
        
    }
    
    
}
