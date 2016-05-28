/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import com.esotericsoftware.yamlbeans.YamlReader;
import gavinh.eve.manufacturing.MarketHub;
import gavinh.eve.manufacturing.ShoppingListFactory;
import gavinh.eve.service.SpreadsheetReport;
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
@Profile("run-report")
public class RunReport implements CommandLineRunner {

   
    
    private static final Logger log = LoggerFactory.getLogger(RunReport.class);
    
    @Autowired
    SpreadsheetReport spreadsheetReport;
    
    @Override
    public void run(String... strings) throws Exception {
        
        List<Integer> stationIds = new ArrayList<>();
        for(MarketHub hub : MarketHub.values()) {
            stationIds.add(hub.getStationId());
        }
        
        spreadsheetReport.runReport(stationIds, ShoppingListFactory.getShoppingList());
    }
    
    
}
