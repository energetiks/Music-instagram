/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.service;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;
import ru.bystrov.musicinstagram.entities.AttributeValue;
import ru.bystrov.musicinstagram.entities.Objects;
import ru.bystrov.musicinstagram.entities.dataobject.Attribute;

@Path("main")
@javax.enterprise.context.RequestScoped
public class MainResource {
    
    public void addAttributes(HashMap<Integer,Attribute> attrValue, Objects newObject, EntityManager em) {
        AttributeValue newAttrValue;
        Integer id = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
        
        for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {   
            newAttrValue = new AttributeValue();
            id = id + 1;
            newAttrValue.setAttrValueId(id);
            newAttrValue.setAttrId(entry.getKey());
            newAttrValue.setObjId(newObject.getObjId());
            switch (entry.getValue().getType()) {
                case "string":
                    newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                    newAttrValue.setNumberValue(-1);
                    newAttrValue.setReferenceValue(-1);
                    newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                    newAttrValue.setBooleanValue(false);
                    break;
                case "int":
                    newAttrValue.setNumberValue((int)entry.getValue().getValue());
                    newAttrValue.setStringValue("");
                    newAttrValue.setReferenceValue(-1);
                    newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                    newAttrValue.setBooleanValue(false);
                    break;
                case "reference":
                    newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                    newAttrValue.setNumberValue(-1);
                    newAttrValue.setStringValue("");
                    newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                    newAttrValue.setBooleanValue(false);
                    break;
                case "date":
                    newAttrValue.setDateValue((String) entry.getValue().getValue());
                    newAttrValue.setNumberValue(-1);
                    newAttrValue.setStringValue("");
                    newAttrValue.setReferenceValue(-1);
                    newAttrValue.setBooleanValue(false);
                    break;
                case "boolean":
                    newAttrValue.setBooleanValue((Boolean) entry.getValue().getValue());
                    newAttrValue.setNumberValue(-1);
                    newAttrValue.setStringValue("");
                    newAttrValue.setReferenceValue(-1);
                    newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                    break;    
                default:
                    break;
            }
            em.persist(newAttrValue);
        }
    }
    
}
