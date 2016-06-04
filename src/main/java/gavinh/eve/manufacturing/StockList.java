package gavinh.eve.manufacturing;

import gavinh.eve.utils.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockList {

    public final Map<Integer,Item> items = new HashMap<>();
    
    public void add(Integer itemTypeId, int quantity) {
        if (items.containsKey(itemTypeId)) {
            Item item = items.get(itemTypeId);
            item.quantity += quantity;
        } else {
            Item item = new Item(itemTypeId, quantity);
            items.put(itemTypeId, item);
        }
    }
    
    static public class Item {
        
        public final int itemTypeId;
        public int quantity;
        public List<Transaction> transactions;
        
        private Item(int itemTypeId, int quantity) {
            this.itemTypeId = itemTypeId;
            this.quantity = quantity;
        }
    }
    
 
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        float totalPrice = 0.0f;
        for(StockList.Item item : items.values()) {
            boolean first = true;
            for(Transaction purchase : item.transactions) {
                if (first) {
                    result.append(String.format("%,d %s [%d]\n", item.quantity, purchase.itemType.getName(), purchase.itemType.getId()));
                    first = false;
                }
                totalPrice += purchase.totalPrice;
                result.append(String.format("\t%,d at %s price %,.2f (from %,.2f to %,.2f)\n", 
                        purchase.totalQuantity, purchase.station.getSolarSystem().getName(), purchase.totalPrice, purchase.minPrice, purchase.maxPrice));
            }
        }
        result.append(String.format("Total price %,.2f", totalPrice));
        return result.toString();
    }
    
    public float getTotalPrice() {
        float result = 0.0f;
        for(Item item : items.values()) {
            for(Transaction purchase : item.transactions) {
                result += purchase.totalPrice;
            }
        }
        return result;
    }
    
}
