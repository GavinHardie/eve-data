package gavinh.eve.utils;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.Station;
import java.util.Comparator;

public class Transaction {
    
    public Station station;
    public ItemType itemType;
    public float minPrice;
    public float maxPrice;
    public float totalPrice;
    public int totalQuantity;
    public boolean outstanding;

    public static class TransactionComparator implements Comparator<Transaction> {
        
        private final String orderType;

        public TransactionComparator(String orderType) {
            this.orderType = orderType;
        }

        @Override
        public int compare(Transaction o1, Transaction o2) {
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
