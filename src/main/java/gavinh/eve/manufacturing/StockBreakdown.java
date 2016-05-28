/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.manufacturing;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.Station;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Gavin
 */
public class StockBreakdown {
    
    public static final List<Integer> marketHubStationIds;
    
    static {
        marketHubStationIds = new ArrayList<>();
        for(MarketHub hub : MarketHub.values()) {
            marketHubStationIds.add(hub.getStationId());
        }
    }
    
    public List<StockBand> stockBands = new ArrayList<>();
    public Map<StockZone,Integer> totals = new HashMap<>();
    
    public StockBreakdown(ItemType itemType, float minPrice, float bandWidth, int numBands) {
        
        stockBands.add(new StockBand(0.0f, minPrice));
        float floor = minPrice;
        for(int i = 0; i < numBands; ++i) {
            stockBands.add(new StockBand(floor, floor + bandWidth));
            floor = floor + bandWidth;
        }
        stockBands.add(new StockBand(floor, Float.MAX_VALUE));
    }

    public void addStock(Station station, float price, int quantity) {
        StockBand selectedBand = null;
        for(StockBand stockBand : stockBands) {
            if (stockBand.minPrice < price && stockBand.maxPrice >= price) {
                selectedBand = stockBand;
                break;
            }
        }
        if (selectedBand == null)
            throw new RuntimeException(String.format("Couldnt find a band to cover %f", price));
        if (station.getId() == MarketHub.Jita.getStationId()) {
            add(selectedBand.quantity, StockZone.Jita, quantity);
            add(totals, StockZone.Jita, quantity);
        } else if (marketHubStationIds.contains(station.getId())) {
            add(selectedBand.quantity, StockZone.Hubs, quantity);
            add(totals, StockZone.Hubs, quantity);
        } else {
            if (station.getSolarSystem().getSecurity() < 0.1f) {
                add(selectedBand.quantity, StockZone.Nullsec, quantity);
                add(totals, StockZone.Nullsec, quantity);
            } else if (station.getSolarSystem().getSecurity() < 0.5f) {
                add(selectedBand.quantity, StockZone.Lowsec, quantity);
                add(totals, StockZone.Lowsec, quantity);
            } else {
                add(selectedBand.quantity, StockZone.Highsec, quantity);
                add(totals, StockZone.Highsec, quantity);
            }
        }
    }
    
    private void add(Map<StockZone,Integer> map, StockZone zone, int quantity) {
        Integer currentQuantity = map.get(zone);
        if (currentQuantity == null) currentQuantity = 0;
        map.put(zone, currentQuantity + quantity);
    }
    
    public static enum StockZone { Jita, Hubs, Highsec, Lowsec, Nullsec };
    
    public static class StockBand {
        
        public final float minPrice;
        public final float maxPrice;
        public Map<StockZone,Integer> quantity = new HashMap<>();
        
        public StockBand(float minPrice, float maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
        
        public String getDesc() {
            if (maxPrice == Float.MAX_VALUE)
                return String.format("%,.2f - Infinity", minPrice);
            else
                return String.format("%,.2f - %,.2f", minPrice, maxPrice);
        }
    }
}
