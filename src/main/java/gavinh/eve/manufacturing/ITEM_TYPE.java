package gavinh.eve.manufacturing;

public enum ITEM_TYPE {
    
    CovertOpsCloak(11578), PLEX(29668), TrainingCertificate(34133), SkillpointExtractor(40519), SkillpointInjector(40520);
    
    private final int itemTypeId;
    
    ITEM_TYPE(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }
    
    public int getItemTypeId() {
        return itemTypeId;
    }
}
