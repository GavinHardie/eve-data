package gavinh.eve.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ItemType implements Serializable {
    
    @Id
    private int id;
    
    @Column(length=255, nullable=false)
    private String name;

    @Column(length=255, nullable=false)
    private String href;
    
    private float volume;
    
    @ManyToOne(optional=false)
    private MarketGroup marketGroup;
    
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

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public MarketGroup getMarketGroup() {
        return marketGroup;
    }

    public void setMarketGroup(MarketGroup marketGroup) {
        this.marketGroup = marketGroup;
    }
    
}
