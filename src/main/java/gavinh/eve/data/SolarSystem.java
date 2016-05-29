package gavinh.eve.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class SolarSystem implements Serializable {

    @Id
    private int id;
    
    @Column(length=255, nullable=false)
    private String name;
    
    @Column(length=255, nullable=false)
    private String href;
    
    @Column(nullable=false)
    private char zone;      // H(igh), L(ow), N(ull), I(sland)
    
    private float security;

    @ManyToOne(optional=false)
    private Region region;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSecurity() {
        return security;
    }

    public void setSecurity(float security) {
        this.security = security;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public char getZone() {
        return zone;
    }

    public void setZone(char zone) {
        this.zone = zone;
    }
    
}
