package gavinh.eve.manufacturing;

/**
 *
 * @author Gavin
 */
public enum MARKET_HUB {
    
    Jita(60003760), Amarr(60008494), Dodixie(60011866), Rens(60004588), Hek(60005686);
    
    private final int stationId;
    
    MARKET_HUB(int stationId) {
        this.stationId = stationId;
    }
    
    public int getStationId() {
        return stationId;
    }
}
