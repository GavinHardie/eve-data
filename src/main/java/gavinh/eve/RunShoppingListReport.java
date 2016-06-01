/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import gavinh.eve.manufacturing.MarketHub;
import gavinh.eve.manufacturing.ShoppingList;
import gavinh.eve.manufacturing.ShoppingListFactory;
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
        //shoppingListService.makePurchases(shoppingList, MarketHub.Jita.getStationId());     // Buy everything at Jita
        shoppingListService.makePurchases(shoppingList, stationIds);                        // Shop for best price across 5 hubs
        //shoppingListService.makeHighsecPurchases(shoppingList);                             // Shop for best price across HS

        // Print the results
        log.info("ShoppingListReport\n" + shoppingList.toString());
        
        float totalCost = shoppingList.getTotalPrice();
        totalCost *= 1.03;          // 3% manufacturing slot cost
        totalCost *= 1.035;         // 3.5% tax when selling
        totalCost += 150000000;     // Fuel
        
        float costPer = totalCost / shoppingList.output.quantity;
        
        log.info(String.format("Total cost [%,.2f] cost per item [%,.2f]", totalCost, costPer));
    }
}
