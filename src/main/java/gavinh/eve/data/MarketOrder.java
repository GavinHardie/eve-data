package gavinh.eve.data;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@IdClass(MarketOrderKey.class)
@Table(indexes = {  @Index(columnList="fetched,buysell,itemType_id,region_id", unique=false), 
                    @Index(columnList="region_id", unique=false), 
                    @Index(columnList="itemType_id", unique=false),
                    @Index(columnList="station_id", unique=false)})
public class MarketOrder implements Serializable {

    @Id
    private long id;
    
    @Id
    @Column(length=10, nullable=false)
    private String fetched;
    
    @Column(length=4, nullable=false)
    private String buysell;
    
    private int quantity;
    
    private float price;
    
    @ManyToOne(optional=false)
    private Region region;
    
    @ManyToOne(optional=false)
    private ItemType itemType;
    
    @ManyToOne(optional=false)
    private Station station;

    public String getBuysell() {
        return buysell;
    }

    public void setBuysell(String buysell) {
        this.buysell = buysell;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFetched() {
        return fetched;
    }

    public void setFetched(String fetched) {
        this.fetched = fetched;
    }
    
    @Override
    public String toString() {
        return String.format("Id:%d Fetched:%s OrderType:%s Name:%s Quantity:%d Price:%f", getId(), getFetched(), buysell, itemType.getName(), quantity, price);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 37 * hash + Objects.hashCode(this.fetched);
        return hash;
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
        final MarketOrder other = (MarketOrder) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.fetched, other.fetched)) {
            return false;
        }
        return true;
    }


    
    
}
