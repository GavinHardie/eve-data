/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.manufacturing;

/**
 *
 * @author Gavin
 */
public class ShoppingListFactory {

    private static final ShoppingList COVOP_CLOAKS;

    static {
        COVOP_CLOAKS = new ShoppingList();
        COVOP_CLOAKS.output = new ShoppingList.Item(11578, 3520);
        COVOP_CLOAKS.items.add(new ShoppingList.Item(16670, 1625602));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(20414, 1320));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(20419, 1320));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(2361, 9328));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(37, 1428152));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(40, 105578));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(36, 2415424));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(9842, 90948));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(11399, 83446));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(16681, 342646));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(38, 530906));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(16680, 1103719));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(33359, 191258));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(34205, 660));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(35, 19723792));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(11483, 7040));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(2349, 6226));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(16671, 1662748));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(9840, 90948));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(34, 50459728));
        COVOP_CLOAKS.items.add(new ShoppingList.Item(39, 416042));
    }

    public static ShoppingList getShoppingList() {
        return COVOP_CLOAKS;
    }
}
