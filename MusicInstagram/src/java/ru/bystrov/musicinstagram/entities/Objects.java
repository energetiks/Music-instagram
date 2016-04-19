/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Test
 */
@Entity
@Table(name="Objects")
@XmlRootElement
public class Objects {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int objId;
    @Column
    private String name;
    @Column
    private int objTypeId;

    public Objects() {
    }
    
    public int getObjId() {
        return objId;
    }

    public void setObjId(int objId) {
        this.objId = objId;
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
        final Objects other = (Objects) obj;
        if (!java.util.Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!java.util.Objects.equals(this.objId, other.objId)) {
            return false;
        }
        if (!java.util.Objects.equals(this.objTypeId, other.objTypeId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Objects[ id=" + objId + " ]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getObjTypeId() {
        return objTypeId;
    }

    public void setObjTypeId(int objTypeId) {
        this.objTypeId = objTypeId;
    }
    
}
