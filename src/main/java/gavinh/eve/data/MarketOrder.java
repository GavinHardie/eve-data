package gavinh.eve.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class MarketOrder implements Serializable {
    
    @Id
    private long id;
    
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
    
    @Override
    public String toString() {
        return String.format("%d %s %s %s %d %f", id, fetched, buysell, itemType.getName(), quantity, price);
    }
}
