/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.entities;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table; 

/**
 *
 * @author Test
 */
@Entity
@Table(name="AttributeValue")
public class AttributeValue implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int attrValueId;
    @Column
    private int objId;
    @Column
    private int attrId;
    @Column
    private String stringValue;
    @Column
    private int numberValue;
    @Column
    private int referenceValue;
    @Column
    private String dateValue;
    @Column
    private Boolean booleanValue;

    public int getAttrValueId() {
        return attrValueId;
    }

    public void setAttrValueId(int attrValueId) {
        this.attrValueId = attrValueId;
    }

    @Override
    public String toString() {
        return "entities.AttributeValue[ id=" + attrValueId + " ]";
    }

    public int getObjId() {
        return objId;
    }

    public void setObjId(int objId) {
        this.objId = objId;
    }

    public int getAttrId() {
        return attrId;
    }

    public void setAttrId(int attrId) {
        this.attrId = attrId;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(int numberValue) {
        this.numberValue = numberValue;
    }

    public int getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(int ref) {
        this.referenceValue = ref;
    }

    public String getDateValue() {
        return dateValue;
    }

    public void setDateValue(String dateValue) {
        this.dateValue = dateValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
    
}
