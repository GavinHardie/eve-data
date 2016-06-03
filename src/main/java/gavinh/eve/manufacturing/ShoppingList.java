package gavinh.eve.manufacturing;

import gavinh.eve.utils.Purchase;
import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

    public Item output;
    public List<Item> items = new ArrayList<>();
    
    static public class Item {
        
        public final int itemTypeId;
        public final int quantity;
        public List<Purchase> purchases;
        
        Item(int itemTypeId, int quantity) {
            this.itemTypeId = itemTypeId;
            this.quantity = quantity;
        }
    }
    
 
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        float totalCost = 0.0f;
        for(ShoppingList.Item item : items) {
            boolean first = true;
            for(Purchase purchase : item.purchases) {
                if (first) {
                    result.append(String.format("%,d %s [%d]\n", item.quantity, purchase.itemType.getName(), purchase.itemType.getId()));
                    first = false;
                }
                totalCost += purchase.totalCost;
                result.append(String.format("\t%,d at %s costing %,.2f (from %,.2f to %,.2f)\n", 
                        purchase.totalQuantity, purchase.station.getSolarSystem().getName(), purchase.totalCost, purchase.minPrice, purchase.maxPrice));
            }
        }
        result.append(String.format("Total cost %,.2f", totalCost));
        return result.toString();
    }
    
    public float getTotalCost() {
        float result = 0.0f;
        for(Item item : items) {
            for(Purchase purchase : item.purchases) {
                result += purchase.totalCost;
            }
        }
        return result;
    }
    
}
