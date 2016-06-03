package gavinh.eve.manufacturing;

public enum ITEM_TYPE {
    
    CovertOpsCloak(11578);
    
    private final int itemTypeId;
    
    ITEM_TYPE(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }
    
    public int getItemTypeId() {
        return itemTypeId;
    }
}