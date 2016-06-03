/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.service;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketOrder;
import gavinh.eve.data.MarketOrderRepository;
import gavinh.eve.manufacturing.ITEM_TYPE;
import gavinh.eve.utils.MarketOrderComparator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gavin
 */
@Service
public class DirectSellService {

    @Autowired
    private MarketOrderRepository marketOrderRepository;
    
    @Autowired
    private ItemTypeRepository itemTypeRepository;
    
    public List<MarketOrder> getAllBuyOrders(ITEM_TYPE item_type) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fetched = sdf.format(new Date());
        
        ItemType itemType = itemTypeRepository.findOne(item_type.getItemTypeId());
        List<MarketOrder> marketOrders = marketOrderRepository.findByFetchedAndBuysellAndItemTypeInHighsec(fetched, "buy", itemType);
        
        Collections.sort(marketOrders, new MarketOrderComparator("buy"));
        
        return marketOrders;
    }

    public List<MarketOrder> getBuyOrders(ITEM_TYPE item_type, int quantity) {
        List<MarketOrder> marketOrders = getAllBuyOrders(item_type);
        List<MarketOrder> result = new ArrayList<>();
        for(MarketOrder marketOrder : marketOrders) {
            result.add(marketOrder);
            quantity -= marketOrder.getQuantity();
            if (quantity <= 0)
                break;
        }
        return result;
    }
}
