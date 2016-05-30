package gavinh.eve.utils;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.Station;
import java.util.Comparator;

public class Purchase {
    
    public Station station;
    public ItemType itemType;
    public float minPrice;
    public float maxPrice;
    public float totalCost;
    public int totalQuantity;
    public boolean outOfStock;

    public static class PurchaseComparator implements Comparator<Purchase> {
        
            private final String orderType;

            public PurchaseComparator(String orderType) {
                this.orderType = orderType;
            }

            @Override
            public int compare(Purchase o1, Purchase o2) {
                switch (orderType) {
                    case "sell":
                        return Float.compare(o1.minPrice, o2.minPrice);
                    case "buy":
                        return Float.compare(o2.minPrice, o1.minPrice);
                    default:
                        throw new RuntimeException("Unrecognised ordertype - must be buy or sell");

                }
            }
    }
    
}
