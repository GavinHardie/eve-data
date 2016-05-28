/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.manufacturing;

import gavinh.eve.data.ItemType;
import gavinh.eve.data.Station;

/**
 *
 * @author Gavin
 */
public class Purchase {
    
    public Station station;
    public ItemType itemType;
    public float minPrice;
    public float maxPrice;
    public float totalCost;
    public int totalQuantity;
    public boolean outOfStock;
    
}
