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
