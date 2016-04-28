/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.bystrov.musicinstagram.entities.AttributeValue;
import ru.bystrov.musicinstagram.entities.Objects;
import ru.bystrov.musicinstagram.entities.dataobject.Attribute;

@Path("directory")
public class DirectoryService {
    @Context
    private UriInfo context;
    @PersistenceContext(unitName = "MusicInstagramPU")
    private EntityManager em;
    @Resource
    UserTransaction utx;
    
    final private int LOGIN_ID = 8;
    
    final private int NAME_ID = 25;
    final private int USER_REF_ID = 26;
    final private int CREATED_TIME_ID = 35;
    final private int DIRECTORY_TYPE_ID = 4;
    
    @GET
    @Path("getDirectoriesByLogin")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getDirectoriesByLogin(@Context HttpServletRequest req,
                                                @QueryParam("login") String login) {
    
        JSONObject result = new JSONObject();
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
        JSONArray array = new JSONArray();
        List<Integer> directoriesId = em.createNativeQuery("select objId from AttributeValue"
                                            + " where  referenceValue = ? and"
                                            + "  attrId = ?")
                                    .setParameter(1, objId).setParameter(2, USER_REF_ID).getResultList();
        
        result.put("countOfDirectories", directoriesId.size());
        array.put(result);
        JSONObject obj;
        if (directoriesId.isEmpty()) {
            return array.toString();
        }
        SortedMap<Timestamp,List<AttributeValue>> mapOfLastSamples = new TreeMap<>();
        
        for(Integer directoryId : directoriesId) {
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, directoryId).getResultList();
            Timestamp time = (Timestamp) em.createNativeQuery("select dateValue from AttributeValue "
                                                            + "where attrId = ? and objId = ? ")
                    .setParameter(1, CREATED_TIME_ID).setParameter(2,directoryId).getResultList().get(0);
            mapOfLastSamples.put(time, attrs);
        }
        for(Map.Entry<Timestamp,List<AttributeValue>> entry : mapOfLastSamples.entrySet()) {
            obj = new JSONObject();
            List<AttributeValue> attrs = entry.getValue();
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != -1) {
                    obj.put("Directory" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put("Directory" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    obj.put("Directory" + String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                } else
                if (!"1970-01-01 00:00:00.000".equals(attr.getDateValue())) {
                    obj.put("Directory" + String.valueOf(attr.getAttrId()),attr.getDateValue());
                }
            }
            array.put(obj);
        }
        return array.toString(); 
    }
    
    @POST
    @Path("createNewDirectory")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String createNewDirectory( @Context HttpServletRequest req,
                             @FormParam("name") String name,
                             @FormParam("login") String login) throws RollbackException, HeuristicMixedException, HeuristicRollbackException {
        JSONObject result = new JSONObject();
        
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
        java.sql.Timestamp time = (java.sql.Timestamp) em.createNativeQuery("select CURRENT_TIMESTAMP from Objects").getResultList().get(0);
        
        HashMap<Integer,Attribute> attrValue = new HashMap<>();
        attrValue.put(NAME_ID,new Attribute("String", name));
        attrValue.put(USER_REF_ID,new Attribute("int", objId));
        attrValue.put(CREATED_TIME_ID,new Attribute("date",time.toString()));
        try {
            utx.begin();
            Objects newDirectory  = new Objects();
            
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newDirectory.setObjId(id+1);
            newDirectory.setName(name + "Directory");
            newDirectory.setObjTypeId(DIRECTORY_TYPE_ID);
            
            em.persist(newDirectory);
            AttributeValue newAttrValue;
            id = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {   
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newDirectory.getObjId());
                switch (entry.getValue().getType()) {
                    case "String":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setReferenceValue(-1);
                        newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int)entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(-1);
                        newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                        break;
                    case "reference":
                        newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setStringValue("");
                        newAttrValue.setDateValue("1970-01-01 00:00:00.000");
                        break;
                    case "date":
                        newAttrValue.setDateValue((String) entry.getValue().getValue());
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(-1);
                        break;
                    default:
                        break;
                }
                em.persist(newAttrValue);
            }
            utx.commit();
            result.put("result","ok");
            return result.toString(); 
        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException | JSONException ex) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ex.printStackTrace(System.err);
            try {
                utx.rollback();
                result.put("result","failed");
                return result.toString();
            } catch (IllegalStateException | SecurityException | SystemException | JSONException exc) { 
                exc.printStackTrace(System.err);
                result.put("result","failed");
                return result.toString();
            }
        }
        

    }
//    
//    public String renameDirectory() {
//        
//    }
//    
    
    
}
