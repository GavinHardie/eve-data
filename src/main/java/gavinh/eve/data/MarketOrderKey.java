package gavinh.eve.data;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class MarketOrderKey implements Serializable {
    
    private long id;
    
    private String fetched;

    public MarketOrderKey() {
        
    }
    
    public MarketOrderKey(long id, String fetched) {
        this.id = id;
        this.fetched = fetched;
    }
    
    public String getFetched() {
        return fetched;
    }

    public void setFetched(String fetched) {
        this.fetched = fetched;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 43 * hash + Objects.hashCode(this.fetched);
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
        final MarketOrderKey other = (MarketOrderKey) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.fetched, other.fetched)) {
            return false;
        }
        return true;
    }

    
}
