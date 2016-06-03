/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import gavinh.eve.data.MarketOrder;
import gavinh.eve.manufacturing.ITEM_TYPE;
import gavinh.eve.service.DirectSellService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("run-direct-sell-report")
public class RunDirectSellReport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RunDirectSellReport.class);
    
    @Autowired
    private DirectSellService directSellService;
    
    @Override
    public void run(String... strings) throws Exception {
        List<MarketOrder> marketOrders = directSellService.getBuyOrders(ITEM_TYPE.CovertOpsCloak, 3520);
        for(MarketOrder marketOrder : marketOrders) {
            log.info(String.format("%s will buy [%d] at [%,.2f]", marketOrder.getStation().getSolarSystem().getName(), marketOrder.getQuantity(), marketOrder.getPrice()));
        }
    }
    
}
