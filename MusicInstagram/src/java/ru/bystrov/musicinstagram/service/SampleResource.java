package ru.bystrov.musicinstagram.service;

import java.sql.ResultSet;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
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
    final private int LIKES_TYPE_ID = 6;
    
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
            
            MainResource resource = new MainResource();
            resource.addAttributes(attrValue, newSample, em);
            
             //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            attrValue.put(PATH_FILE_ID,new Attribute("String", file_ref));
            attrValue.put(TYPE_FILE_ID,new Attribute("String", "mp3"));
            
            resource.addAttributes(attrValue, newSample, em);
            
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
                             @QueryParam("sampleId") int sampleId,
                             @QueryParam("login") String login) throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException, NotSupportedException {
        JSONObject result = new JSONObject();
        
        try {
            utx.begin();
            Object[] attrValues = preparedOfDoLike(login, sampleId);
            Integer userId = (Integer) attrValues[0]; 
            Boolean isLike = (Boolean) attrValues[1];
            Boolean isDisLike = (Boolean) attrValues[2];
            AttributeValue countOfLikes = (AttributeValue) attrValues[3];
            AttributeValue countOfDisLikes = (AttributeValue) attrValues[4];
            AttributeValue isLikeId = (AttributeValue) attrValues[5];
            AttributeValue isDisLikeId = (AttributeValue) attrValues[6];
            
            if (isLike == true) {
                countOfLikes.setNumberValue(countOfLikes.getNumberValue() - 1);
                em.merge(countOfLikes);
                isLikeId.setBooleanValue(false);
                em.merge(isLikeId);
                
            } else {
                if (isDisLike == false) {
                    countOfLikes.setNumberValue(countOfLikes.getNumberValue() + 1);
                    em.merge(countOfLikes);
                    isLikeId.setBooleanValue(true);
                    em.merge(isLikeId);
                } else {
                    countOfDisLikes.setNumberValue(countOfDisLikes.getNumberValue() - 1);
                    countOfLikes.setNumberValue(countOfLikes.getNumberValue() + 1);
                    em.merge(countOfLikes);
                    em.merge(countOfDisLikes);
                    isLikeId.setBooleanValue(true);
                    isDisLikeId.setBooleanValue(false);
                    em.merge(isLikeId);
                    em.merge(isDisLikeId);
                }
            }
            
            
            
            utx.commit();
            result.put("result", "ok");
            return result.toString();
        } catch (NoResultException ex) {
                utx.begin();
                String userName = (String) em.createNativeQuery(
                    "SELECT a1.stringValue||' '||a2.stringValue  " +
                    "FROM ATTRIBUTEVALUE a1, ATTRIBUTEVALUE a2, ATTRIBUTEVALUE login " +
                    "where a1.OBJID = login.objId and " +
                    "      a1.ATTRID = (select attrId from ATTRIBUTES where name = 'first_name') and " +
                    "      a2.ATTRID = (select attrId from ATTRIBUTES where name = 'last_name') and " +
                    "      a1.OBJID = a2.OBJID and " +
                    "      login.ATTRID = (select attrId from ATTRIBUTES where name = 'login') and " +
                    "      login.stringValue = ? ")
                        .setParameter(1, login).getSingleResult();
//                String sampleName = (String) em.createNamedQuery(
//                        "SELECT stringValue " +
//                        "FROM ATTRIBUTEVALUE " +
//                        "where OBJID = 26 and " +
//                        "      ATTRID = (select attrs.attrId " +
//                        "                from ATTRIBUTES attrs, ATTRIBUTEOBJECTTYPES attrObj " +
//                        "                where attrs.name = 'name' and " +
//                        "                      attrObj.OTID = (select OTID from OBJECTTYPES where name = 'Sample' ) and " +
//                        "                      attrs.ATTRID = attrObj.ATTRID )")
//                        .setParameter(1, sampleId).getResultList().get(0);
                
                Objects newLikeObject = new Objects();
                
                Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());

                newLikeObject.setObjId(id+1);
                newLikeObject.setName("LikesAndDisLikes by " + userName + " of " + sampleId);
                newLikeObject.setObjTypeId(LIKES_TYPE_ID);
                
                em.persist(newLikeObject);
                
                HashMap<Integer,Attribute> attrValue = new HashMap<>();
                attrValue.put(35,new Attribute("reference", 2));
                attrValue.put(36,new Attribute("reference", 26));
                attrValue.put(37,new Attribute("boolean", true));
                attrValue.put(38,new Attribute("boolean", false));
                
                
                MainResource resource = new MainResource();
                resource.addAttributes(attrValue, newLikeObject, em);
                utx.commit();
                result.put("result", "failed - ROLLBACK");
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
    
    private Object[] preparedOfDoLike(String login,
                                      Integer sampleId) {
        Object[] attrValues = new AttributeValue[6];
        Integer userId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                        + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
         attrValues[0] = userId;
        
        Integer objId = (Integer) em.createNamedQuery(
                "select objId " +
                "from ATTRIBUTEVALUE " +
                "where attrId = (select attrId " +
                "                from ATTRIBUTES " +
                "                where name = 'userId') and " +
                "                      referenceValue = ?" +
                "              INTERSECT " +
                "               select objId " +
                "               from ATTRIBUTEVALUE " +
                "               where attrId = (select attrId from ATTRIBUTES where name = 'sampleId') and " +
                "                     referenceValue = ?")
                .setParameter(1, userId).setParameter(2, sampleId).getSingleResult();
        
        
        Boolean isLike = (Boolean) em.createNativeQuery(
            "select booleanValue " +
            "from ATTRIBUTEVALUE " +
            "where objId = ? and " +
            "      attrId = (select attrId from ATTRIBUTES where name = 'isLike')")
                .setParameter(1,objId).getSingleResult();
        attrValues[1] = isLike;

        Boolean isDisLike = (Boolean) em.createNativeQuery(
            "select booleanValue " +
            "from ATTRIBUTEVALUE " +
            "where objId = ? and " +
            "      attrId = (select attrId from ATTRIBUTES where name = 'isDisLike')")
                .setParameter(1,objId).getSingleResult();
        attrValues[2] = isDisLike;


        Integer attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue "
              + "where objId = ? and "
                    + "attrId = (select attrId from attributes where name = 'count_of_like')")
                .setParameter(1, sampleId).getSingleResult();
        AttributeValue countOfLikes = em.find(AttributeValue.class, attrValueId);
        attrValues[3] = countOfLikes;

        attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue "
              + "where objId = ? and "
                    + "attrId = (select attrId from attributes where name = 'count_of_dislike') ")
                .setParameter(1, sampleId).getSingleResult();
        AttributeValue countOfDisLikes = em.find(AttributeValue.class, attrValueId);
        attrValues[4] = countOfDisLikes;


        attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue " +
                "where objId = ? and " +
                "      attrId = (select attrId from attributes where name = 'isLike')")
                .setParameter(1,objId).getSingleResult();
        AttributeValue isLikeId = em.find(AttributeValue.class, attrValueId);
        attrValues[5] = isLikeId;

        attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue " +
                "where objId = ? and " +
               "attrId = (select attrId from attributes where name = 'isDisLike')")
                .setParameter(1,objId).getSingleResult();
        AttributeValue isDisLikeId = em.find(AttributeValue.class, attrValueId);
        attrValues[6] = isDisLikeId;
            
        return attrValues;
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
