package gavinh.eve.manufacturing;

public enum ITEM_TYPE {
  
    CovertOpsCloak(11578), PLEX(29668), TrainingCertificate(34133), SkillpointExtractor(40519), SkillpointInjector(40520);

    public static final Integer[] covopRelatedGoods = new Integer[] {34, 35, 36, 37, 38, 39, 40, 11483, 9842, 9840, 11399, 2361, 2349, 16680, 16681, 16670, 16671, 33359, 11541, 11693, 11370, 11578, 20414, 20419, 34205, 40519, 40520, 29668, 34133 };
    public static final Integer[] tradeGoods = new Integer[] { 43, 42, 45, 11069, 9844, 3673, 3715, 3777, 3717, 9852, 3647, 3699, 9850, 12995, 11585, 11944, 11855, 12478, 14358, 12865, 12994, 16712, 16713, 15410, 16714, 17143, 15316, 17423, 17424 };
    public static final Integer[] advancedComponents = new Integer[] { 11535, 11534, 11536, 11552, 11553, 11554, 11537, 11555, 11694, 11538, 11556, 11539, 11557, 11541, 11690, 11540, 11558, 11543, 11691, 11545, 11689, 11547, 11692, 11542, 11549, 11693, 11530, 11544, 11688, 11531, 11548, 11532, 11551, 11533, 11550, 11695 };
    public static final Integer[] datacores = new Integer[] { 11496, 20116, 20114, 20115, 20171, 20172, 20415, 20417, 20416, 20418, 20419, 20420, 20421, 20423, 20424, 20410, 20425, 20411, 20412, 20413, 20414, 25887  };

    private final int itemTypeId;
    
    ITEM_TYPE(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }
    
    public int getItemTypeId() {
        return itemTypeId;
    }
}
