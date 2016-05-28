/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.service;

import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.manufacturing.StockBreakdown;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gavin
 */
@Service
public class StockBreakdownService {

    @Autowired
    private MarketOrderRepository marketOrderRepository;
    
    public void makeBands(StockBreakdown stockBreakdown, String fetched) {
        List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemType(fetched, "sell", stockBreakdown.itemType);
        for(MarketOrder marketOrder : marketOrders) {
            stockBreakdown.addStock(marketOrder.getStation(), marketOrder.getPrice(), marketOrder.getQuantity());
        }
    }
}
