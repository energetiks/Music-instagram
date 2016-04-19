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

@Entity
@Table(name="AttributeObjectTypes")
public class AttributeObjectTypes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int attrObjTypeId;
    @Column
    private int attrId;
    @Column
    private int otId;

    public int getAttrObjTypeId() {
        return attrObjTypeId;
    }

    public void setAttrObjTypeId(int attrObjTypeId) {
        this.attrObjTypeId = attrObjTypeId;
    }
    
    @Override
    public String toString() {
        return "entities.AttributeObjectTypes[ id=" + attrObjTypeId + " ]";
    }

    public int getAttrId() {
        return attrId;
    }

    public void setAttrId(int attrId) {
        this.attrId = attrId;
    }

    public int getOtId() {
        return otId;
    }

    public void setOtId(int otId) {
        this.otId = otId;
    }
    
}
