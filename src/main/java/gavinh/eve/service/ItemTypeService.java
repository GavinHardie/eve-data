/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.service;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.ItemTypeRepository;
import gavinh.eve.data.MarketGroup;
import gavinh.eve.data.MarketGroupRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemTypeService {

    @Autowired
    MarketGroupRepository marketGroupRepository;
    
    @Autowired
    ItemTypeRepository itemTypeRepository;
    
    public List<ItemType> deepScanMarketGroup(int marketgroup_id) {
        
        
        Set<MarketGroup> marketGroups = new HashSet<>();
        marketGroups.add(marketGroupRepository.findOne(marketgroup_id));
        
        boolean finished = false;
        while(!finished) {
            Set<MarketGroup> newGroups = new HashSet<>();
            for(MarketGroup marketGroup : marketGroups) {
                newGroups.addAll(marketGroupRepository.findByParentMarketGroup(marketGroup));
            }
            finished = true;
            for(MarketGroup marketGroup : newGroups) {
                if (!marketGroups.contains(marketGroup)) {
                    marketGroups.add(marketGroup);
                    finished = false;
                }
            }
        }
        
        List<ItemType> result = new ArrayList<>();
        for(MarketGroup marketGroup : marketGroups) {
            result.addAll(itemTypeRepository.findByMarketGroup(marketGroup));
        }
        return result;
    }
}
