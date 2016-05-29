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
        shoppingListService.makePurchases(shoppingList, stationIds);
        //shoppingListService.makeHighsecPurchases(shoppingList);         // TODO: Deal with HS islands

        // Print the results
        log.info("ShoppingListReport\n" + shoppingList.toString());
    }
}