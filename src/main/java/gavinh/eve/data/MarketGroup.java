/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class MarketGroup implements Serializable {
    @Id
    private int id;
    
    @Column(length=255, nullable=false)
    private String name;
    
    @Column(length=255, nullable=false)
    private String href;
    
    @ManyToOne(optional=true)
    private MarketGroup parentMarketGroup;

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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public MarketGroup getParentMarketGroup() {
        return parentMarketGroup;
    }

    public void setParentMarketGroup(MarketGroup parentMarketGroup) {
        this.parentMarketGroup = parentMarketGroup;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.id;
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
        final MarketGroup other = (MarketGroup) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    
}
