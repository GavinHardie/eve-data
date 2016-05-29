/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Stargate implements Serializable {
    
    @Id
    private int id;
    
    @ManyToOne(optional=false)
    private SolarSystem solarSystem;

    @ManyToOne(optional=false)
    private SolarSystem remoteSolarSystem;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SolarSystem getSolarSystem() {
        return solarSystem;
    }

    public void setSolarSystem(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    public SolarSystem getRemoteSolarSystem() {
        return remoteSolarSystem;
    }

    public void setRemoteSolarSystem(SolarSystem remoteSolarSystem) {
        this.remoteSolarSystem = remoteSolarSystem;
    }
}
