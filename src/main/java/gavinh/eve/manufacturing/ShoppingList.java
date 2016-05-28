/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.manufacturing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gavin
 */
public class ShoppingList {

    public Item output;
    public List<Item> items = new ArrayList<>();
    
    static public class Item {
        
        public final long itemTypeId;
        public final int quantity;
        public List<Purchase> purchases;
        
        Item(int itemTypeId, int quantity) {
            this.itemTypeId = itemTypeId;
            this.quantity = quantity;
        }
    }
}
