/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import com.esotericsoftware.yamlbeans.YamlReader;
import gavinh.eve.manufacturing.MARKET_HUB;
import gavinh.eve.manufacturing.ShoppingListFactory;
import gavinh.eve.report.SpreadsheetReport;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("blueprint-test")
public class BlueprintReader implements CommandLineRunner {

//    {activities=
//            {manufacturing={
//                    skills=[
//                        {level=1, typeID=3380}, 
//                        {level=1, typeID=22242}
//                    ], 
//                    materials=[
//                        {quantity=1, typeID=40354}, 
//                        {quantity=8, typeID=41267}, 
//                        {quantity=12, typeID=41308}, 
//                        {quantity=41, typeID=41268}, 
//                        {quantity=44, typeID=41266}, 
//                        {quantity=47, typeID=41309}, 
//                        {quantity=50, typeID=41307}
//                    ], 
//                    time=36000, 
//                    products=[
//                        {quantity=1, typeID=41459}
//                    ]
//            }
//        }, maxProductionLimit=5, blueprintTypeID=41603}    
    
    
    private static final Logger log = LoggerFactory.getLogger(BlueprintReader.class);
    
    @Autowired
    SpreadsheetReport spreadsheetReport;
    
    @Override
    public void run(String... strings) throws Exception {
        
//        YamlReader reader = new YamlReader(new FileReader("blueprints.yaml"));
//        Map blueprints = (Map) reader.read();
//        Map<String,Object> blueprint = (Map<String,Object>) blueprints.get(12105);

//        List materials = Utils.mapPathToList(Object.class, blueprint, "activities", "manufacturing", "materials");
        List<Integer> stationIds = new ArrayList<>();
        for(MARKET_HUB hub : MARKET_HUB.values()) {
            stationIds.add(hub.getStationId());
        }
        spreadsheetReport.printPurchases(stationIds, ShoppingListFactory.getShoppingList());
    }
    
    
    // 12105 makes 11578
    private void findBlueprintFor(Integer itemTypeId) throws Exception {
        
        YamlReader reader = new YamlReader(new FileReader("blueprints.yaml"));
        Map blueprints = (Map) reader.read();
        for(Object key : blueprints.keySet()) {
            Map blueprint = (Map) blueprints.get(key);
            // This path maps to a list because blueprints can produce multiple products
            List<Integer> products = Utils.mapPathToList(Integer.class, blueprint, "activities", "manufacturing", "products", "typeID");    
            if (products != null) {
                for(Integer productId : products) {
                    if (productId == 11578) {
                        log.info(String.format("Blueprint [%s] makes 11578", key.toString()));
                    }
                }
                log.info(products.toString());
            }
        }
    }
}
