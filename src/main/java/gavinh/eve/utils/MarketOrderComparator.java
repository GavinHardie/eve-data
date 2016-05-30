/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.utils;

import gavinh.eve.data.MarketOrder;
import java.util.Comparator;

/**
 *
 * @author Gavin
 */
public class MarketOrderComparator implements Comparator<MarketOrder> {

    private final String orderType;

    public MarketOrderComparator(String orderType) {
        this.orderType = orderType;
    }

    @Override
    public int compare(MarketOrder o1, MarketOrder o2) {
        switch (orderType) {
            case "sell":
                return Float.compare(o1.getPrice(), o2.getPrice());
            case "buy":
                return Float.compare(o2.getPrice(), o1.getPrice());
            default:
                throw new RuntimeException("Unrecognised ordertype - must be buy or sell");
        }
    }
}
