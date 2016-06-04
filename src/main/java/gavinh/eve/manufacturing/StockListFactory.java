/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.manufacturing;

import com.esotericsoftware.yamlbeans.YamlReader;
import gavinh.eve.utils.Utils;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produce a shopping list from a blueprint.
 * 
 * For now this produces a hard coded shopping list that is more-or-less what
 * is needed to manufacture 3520 Covert Ops Cloaks
 * 
 * @author Gavin
 */
public class StockListFactory {

    private static final Logger log = LoggerFactory.getLogger(StockListFactory.class);
    
    public static StockList getStockList(ITEM_TYPE itemType, int quantity) {
        // Params should define the output and then the items should be 
        // calculated by recursivly examining blueprint
        StockList stockList = new StockList();
        stockList.add(16670, 1625602);
        stockList.add(20414, 1320);
        stockList.add(20419, 1320);
        stockList.add(2361, 9328);
        stockList.add(37, 1428152);
        stockList.add(40, 105578);
        stockList.add(36, 2415424);
        stockList.add(9842, 90948);
        stockList.add(11399, 83446);
        stockList.add(16681, 342646);
        stockList.add(38, 530906);
        stockList.add(16680, 1103719);
        stockList.add(33359, 191258);
        stockList.add(34205, 660);
        stockList.add(35, 19723792);
        stockList.add(11483, 7040);
        stockList.add(2349, 6226);
        stockList.add(16671, 1662748);
        stockList.add(9840, 90948);
        stockList.add(34, 50459728);
        stockList.add(39, 416042);
        return stockList;
    }
    
    // 12105 makes 11578
    // 11578 is Covert Ops Cloaking Device
    // 12105 is not in the ItemTypes table because it is not a market item
    
    /**
     * Find the itemId of the blueprint.
     * 
     * Load the yaml file.  Iterate over the blueprints and log any that produce
     * the specified itemType.  The only quirk is that a blueprint produces 
     * multiple products, so the path "activities/manufacturing/products/typeID"
     * has to be mapped to a list.
     * 
     * @param itemTypeId
     * @throws Exception 
     */
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
            }
        }
    }
    
//
// A random example of a yaml blueprint
//    
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
    
}
