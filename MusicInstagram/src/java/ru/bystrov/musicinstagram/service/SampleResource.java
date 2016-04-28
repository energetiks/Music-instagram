package ru.bystrov.musicinstagram.service;

import java.util.HashMap;
import java.util.Map;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.json.JSONException;
import org.json.JSONObject;
import ru.bystrov.musicinstagram.entities.AttributeValue;
import ru.bystrov.musicinstagram.entities.Objects;
import ru.bystrov.musicinstagram.entities.dataobject.Attribute;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import org.json.JSONArray;
import java.sql.Timestamp;
import java.util.SortedMap;
import java.util.TreeMap;
import ru.bystrov.musicinstagram.entities.dataobject.CountOfLikeObject;

@Path("sample")
@javax.enterprise.context.RequestScoped
public class SampleResource {
    
    @Context
    private UriInfo context;
    @PersistenceContext(unitName = "MusicInstagramPU")
    private EntityManager em;
    @Resource
    UserTransaction utx;
    
    final private int NAME_ID = 19;
    final private int DURATION_ID = 20;
    final private int FILTER_ID = 21;
    final private int SOURCE_REF_ID = 22;
    final private int FILE_REF_ID = 23;
    final private int USER_REF_ID = 24;
    final private int COUNT_OF_LIKE_ID = 31;
    final private int COUNT_OF_DISLIKE_ID = 32;
    final private int TIME_ID = 33;
    final private int DIRECTORY_REF_ID = 34;
    
    final private int SAMPLE_TYPE_ID = 3;
    final private int FILE_TYPE_ID = 5;
    
    final private int DIR_USER_REF_ID = 26;
    final private int DIR_NAME_ID = 25;
    
    final private int PATH_FILE_ID = 27;
    final private int TYPE_FILE_ID = 28;
    
    
    
    final private int LOGIN_ID = 8;
    
    @POST
    @Path("createSample")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String createSample(@Context HttpServletRequest req,
                             @FormParam("name") String name,
                             @FormParam("duration") int duration,
                             @FormParam("filter1") String filter1,
                             @FormParam("filter2") String filter2,
                             @FormParam("source_ref") int source_ref,
                             @FormParam("file_ref") String file_ref,
                             @FormParam("directory_ref") int directory_ref,
                             @FormParam("login") String login) {
        JSONObject result = new JSONObject();
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
        java.sql.Timestamp time = (java.sql.Timestamp) em.createNativeQuery("select CURRENT_TIMESTAMP from Objects").getResultList().get(0);
        
        HashMap<Integer,Attribute> attrValue = new HashMap<>();
        attrValue.put(NAME_ID,new Attribute("String", name));
        attrValue.put(DURATION_ID,new Attribute("int", duration));
        attrValue.put(FILTER_ID,new Attribute("String", filter1));
        attrValue.put(FILTER_ID,new Attribute("String", filter2));
        attrValue.put(SOURCE_REF_ID,new Attribute("reference", source_ref));
        attrValue.put(USER_REF_ID,new Attribute("reference", objId));
        attrValue.put(TIME_ID,new Attribute("date", time.toString()));
        attrValue.put(COUNT_OF_LIKE_ID, new Attribute("int",0));
        attrValue.put(COUNT_OF_DISLIKE_ID, new Attribute("int",0));
        attrValue.put(DIRECTORY_REF_ID, new Attribute("int",directory_ref));
        try {
            utx.begin();
            Objects newSample  = new Objects();
            Objects newSampleFile = new Objects();
            
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newSample.setObjId(id+1);
            newSample.setName("Sample " + name);
            newSample.setObjTypeId(SAMPLE_TYPE_ID);
            
            newSampleFile.setObjId(id+2);
            newSampleFile.setName("Sample File " + name);
            newSampleFile.setObjTypeId(FILE_TYPE_ID);
                
            attrValue.put(FILE_REF_ID,new Attribute("reference", id+2));
            
            em.persist(newSample);
            em.persist(newSampleFile);
            AttributeValue newAttrValue;
            id = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {   
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newSample.getObjId());
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
            
             //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            attrValue.put(PATH_FILE_ID,new Attribute("String", file_ref));
            attrValue.put(TYPE_FILE_ID,new Attribute("String", "mp3"));
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newSampleFile.getObjId());
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
                        newAttrValue.setStringValue("");
                        newAttrValue.setNumberValue(-1);
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
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
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
    
    @GET
    @Path("getSamplesByLoginInTimeOrder")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSamplesByLoginInTimeOrder(@Context HttpServletRequest req,
                                                @QueryParam("login") String login) {
    
        JSONObject result = new JSONObject();
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
        JSONArray array = new JSONArray();
        List<Integer> samplesId = em.createNativeQuery("select objId from AttributeValue"
                                            + " where  referenceValue = ? and"
                                            + "  attrId = ?")
                                    .setParameter(1, objId).setParameter(2, USER_REF_ID).getResultList();
        
        result.put("countOfSamples", samplesId.size());
        array.put(result);
        JSONObject obj;
        if (samplesId.isEmpty()) {
            return array.toString();
        }
        SortedMap<Timestamp,List<AttributeValue>> mapOfLastSamples = new TreeMap<>();
        
        for(Integer sampleId : samplesId) {
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, sampleId).getResultList();
            Timestamp time = (Timestamp) em.createNativeQuery("select dateValue from AttributeValue "
                                                            + "where attrId = ? and objId = ? ")
                    .setParameter(1, TIME_ID).setParameter(2,sampleId).getResultList().get(0);
            mapOfLastSamples.put(time, attrs);
        }
        for(Map.Entry<Timestamp,List<AttributeValue>> entry : mapOfLastSamples.entrySet()) {
            obj = new JSONObject();
            List<AttributeValue> attrs = entry.getValue();
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != -1) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                } else
                if (!"1970-01-01 00:00:00.000".equals(attr.getDateValue())) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getDateValue());
                }
            }
            array.put(obj);
        }
        return array.toString(); 
    }
    
    @GET
    @Path("getSamplesInLikeOrder")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSamplesInLikeOrder(@Context HttpServletRequest req) {
        JSONArray array = new JSONArray();
        List<Long> samplesId = em.createNativeQuery("select o.objId from Objects o, ObjectTypes OT "
                                                    + " where o.objTypeId = OT.otid and"
                                                    + " OT.name = ?")
                                    .setParameter(1, "Sample").getResultList();
        JSONObject obj = new JSONObject();
        obj.put("countOfSamples", samplesId.size());
        array.put(obj);
        if (samplesId.isEmpty()) {
            return array.toString();
        }
        SortedMap<CountOfLikeObject,List<AttributeValue>> mapOfLastSamples = new TreeMap<>();
        for(Long sampleId : samplesId) {
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, sampleId).getResultList();
            Integer countOfLikes = (Integer) em.createNativeQuery("select numberValue from AttributeValue "
                                                            + "where attrId = ? and objId = ? ")
                    .setParameter(1, COUNT_OF_LIKE_ID).setParameter(2,sampleId).getResultList().get(0);
            Integer countOfDisLikes = (Integer) em.createNativeQuery("select numberValue from AttributeValue "
                                                            + "where attrId = ? and objId = ? ")
                    .setParameter(1, COUNT_OF_DISLIKE_ID).setParameter(2,sampleId).getResultList().get(0);
            CountOfLikeObject countOfLikeObject = new CountOfLikeObject(countOfLikes,countOfDisLikes,sampleId);
            mapOfLastSamples.put(countOfLikeObject, attrs);
        }   
        for(Map.Entry<CountOfLikeObject,List<AttributeValue>> entry : mapOfLastSamples.entrySet()) {
            obj = new JSONObject();
            List<AttributeValue> attrs = entry.getValue();
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != -1) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                } else
                if (!"1970-01-01 00:00:00.000".equals(attr.getDateValue())) {
                    obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getDateValue());
                }
            }
            array.put(obj);
        }
        return array.toString(); 
    }
    
    @GET
    @Path("getSamplesFromDirectory")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSamplesFromDirectory(@Context HttpServletRequest req,
                                          @QueryParam("login") String login) {
        JSONObject result = new JSONObject();
        JSONArray mainArray = new JSONArray();
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
         List<Integer> directoriesId = em.createNativeQuery("select objId from AttributeValue"
                                            + " where  referenceValue = ? and"
                                            + "  attrId = ?")
                                    .setParameter(1, objId).setParameter(2, DIR_USER_REF_ID).getResultList();
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("countOfDirectories", directoriesId.size());
        int count = 1;
        for(Integer directoryId : directoriesId) {
            obj.put("Directory" + count,directoryId);
            count = count + 1;
        }
        array.put(obj);
        result.put("DIRECTORIES",array);
        array = new JSONArray();
        if (directoriesId.isEmpty()) {
                return result.toString();
        }
        count = 1;
        for(Integer directoryId : directoriesId) {
            String nameOfDirectory = (String) em.createNativeQuery(" SELECT stringValue " +
                                                                   " FROM ATTRIBUTEVALUE " +
                                                                   " WHERE objId = ? and " +
                                                                   "       attrId = ? ")
                    .setParameter(1, directoryId).setParameter(2, DIR_NAME_ID).getResultList().get(0);
            List<Integer> samplesId = em.createNativeQuery("SELECT sample.objId " +
                                                           "FROM ATTRIBUTEVALUE dir, " +
                                                           "     ATTRIBUTEVALUE sample " +
                                                           "WHERE dir.OBJID = ? and " +
                                                           "      dir.ATTRID = ? and " +
                                                           "      dir.REFERENCEVALUE = ? and " +
                                                           "      dir.OBJID = sample.REFERENCEVALUE and " +
                                                           "      sample.ATTRID = ? ")
                    .setParameter(1, directoryId).setParameter(2, DIR_USER_REF_ID)
                    .setParameter(3, objId).setParameter(4, DIRECTORY_REF_ID).getResultList();
            if (samplesId.isEmpty()) {
                return result.toString();
            }
            SortedMap<CountOfLikeObject,List<AttributeValue>> mapOfLastSamples = new TreeMap<>();
            for(Integer sampleId : samplesId) {
                List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                    .setParameter(1, sampleId).getResultList();
                Integer countOfLikes = (Integer) em.createNativeQuery("select numberValue from AttributeValue "
                                                                + "where attrId = ? and objId = ? ")
                        .setParameter(1, COUNT_OF_LIKE_ID).setParameter(2,sampleId).getResultList().get(0);
                Integer countOfDisLikes = (Integer) em.createNativeQuery("select numberValue from AttributeValue "
                                                                + "where attrId = ? and objId = ? ")
                        .setParameter(1, COUNT_OF_DISLIKE_ID).setParameter(2,sampleId).getResultList().get(0);
                CountOfLikeObject countOfLikeObject = new CountOfLikeObject(countOfLikes,countOfDisLikes,Long.valueOf(sampleId));
                mapOfLastSamples.put(countOfLikeObject, attrs);
            }
            obj = new JSONObject();
            obj.put("countOfSamples", samplesId.size());
            array.put(obj);
                
            for(Map.Entry<CountOfLikeObject,List<AttributeValue>> entry : mapOfLastSamples.entrySet()) {
                obj = new JSONObject();
                obj.put("nameOfDirectory", nameOfDirectory);
                List<AttributeValue> attrs = entry.getValue();
                for (AttributeValue attr : attrs){
                    if (attr.getNumberValue() != -1) {
                        obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                    } else
                    if (!attr.getStringValue().equals("")) {
                        obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                    } else
                    if (attr.getReferenceValue() != -1) {
                        obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                    } else
                    if (!"1970-01-01 00:00:00.000".equals(attr.getDateValue())) {
                        obj.put("Sample" + String.valueOf(attr.getAttrId()),attr.getDateValue());
                    }
                }
                array.put(obj);
            }
            mainArray.put(array);
            array = new JSONArray();
        }
        result.put("Directory",mainArray);
        return result.toString(); 
    }
    
    
    @GET
    @Path("doLike")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String doLike(@Context HttpServletRequest req,
                             @QueryParam("objId") int objId) {
        JSONObject result = new JSONObject();
        if (objId == 0) {
            result.put("result", "failed");
            return result.toString();
        }
        try {    
            utx.begin();
            Integer attrValueId = (Integer) em.createNativeQuery("select attrValueId from AttributeValue where objId = ? and attrId = ?")
                    .setParameter(1, objId).setParameter(2, COUNT_OF_LIKE_ID).getResultList().get(0);
            AttributeValue attrV = em.find(AttributeValue.class, attrValueId);
            attrV.setNumberValue(attrV.getNumberValue() + 1);
            em.merge(attrV);
            utx.commit();
            result.put("result", "ok");
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
            try {
                utx.rollback();
                result.put("result", "failed - ROLLBACK");
                return result.toString();
            } catch (IllegalStateException | SecurityException | SystemException | JSONException ex1) {
                result.put("result", "failed - ROLLBACK DENIED");
                return result.toString();
            }
        } 
    }
    
    @GET
    @Path("doDislike")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String doDislike(@Context HttpServletRequest req,
                             @QueryParam("objId") int objId) {
        JSONObject result = new JSONObject();
        if (objId == 0) {
            result.put("result", "failed");
            return result.toString();
        }
        try {    
            utx.begin();
            Integer attrValueId = (Integer) em.createNativeQuery("select attrValueId from AttributeValue where objId = ? and attrId = ?")
                    .setParameter(1, objId).setParameter(2, COUNT_OF_DISLIKE_ID).getResultList().get(0);
            AttributeValue attrV = em.find(AttributeValue.class, attrValueId);
            attrV.setNumberValue(attrV.getNumberValue() + 1);
            em.merge(attrV);
            utx.commit();
            result.put("result", "ok");
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
            try {
                utx.rollback();
                result.put("result", "failed - ROLLBACK");
                return result.toString();
            } catch (IllegalStateException | SecurityException | SystemException | JSONException ex1) {
                result.put("result", "failed - ROLLBACK DENIED");
                return result.toString();
            }
        } 
    }
}
