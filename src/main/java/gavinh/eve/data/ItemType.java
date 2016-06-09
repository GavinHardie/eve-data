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

    public String toString() {
        return String.format("%d %s", id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItemType other = (ItemType) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.id;
        return hash;
    }
}

