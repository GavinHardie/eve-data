package gavinh.eve.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Temporal;

@Entity
public class MarketApiCall implements Serializable {

    @EmbeddedId
    MarketApiCallKey key;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(nullable = false)
    Date fetchDatetime;

    public MarketApiCallKey getKey() {
        return key;
    }

    public void setKey(MarketApiCallKey key) {
        this.key = key;
    }

    public Date getFetchDatetime() {
        return fetchDatetime;
    }

    public void setFetchDatetime(Date fetchDatetime) {
        this.fetchDatetime = fetchDatetime;
    }
    
    
}
