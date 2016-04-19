package ru.bystrov.musicinstagram.entities.dataobject;

import java.util.Objects;
import java.util.Set;

public class DataObject {
    private Set<Attribute> attributes;
    private Long objId;
    private Long otId;

    public DataObject() {
    }

    public DataObject(Set<Attribute> attributes, Long objId, Long otId) {
        this.attributes = attributes;
        this.objId = objId;
        this.otId = otId;
    }

    public Long getOtId() {
        return otId;
    }

    public void setOtId(Long otId) {
        this.otId = otId;
    }

    public Set<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    public Long getObjId() {
        return objId;
    }

    public void setObjId(Long objId) {
        this.objId = objId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.attributes);
        hash = 97 * hash + Objects.hashCode(this.objId);
        hash = 97 * hash + Objects.hashCode(this.otId);
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
        final DataObject other = (DataObject) obj;
        if (!Objects.equals(this.attributes, other.attributes)) {
            return false;
        }
        if (!Objects.equals(this.objId, other.objId)) {
            return false;
        }
        if (!Objects.equals(this.otId, other.otId)) {
            return false;
        }
        return true;
    }
    
    
    
}
