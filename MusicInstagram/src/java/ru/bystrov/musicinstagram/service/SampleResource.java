package ru.bystrov.musicinstagram.service;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
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
import javax.persistence.NonUniqueResultException;
import javax.ws.rs.Consumes;
import org.glassfish.jersey.media.multipart.FormDataParam;
import ru.bystrov.musicinstagram.entities.dataobject.CountOfLikeObject;
import static ru.bystrov.musicinstagram.service.SourceResource.GetHash;

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
    
    
    final private int  DIRECTORY_TYPE_ID = 4;
    final private int DIR_USER_REF_ID = 26;
    final private int DIR_NAME_ID = 25;
    
    final private int PATH_FILE_ID = 27;
    final private int TYPE_FILE_ID = 28;
    
    final private int USERID_ID = 35;
    final private int SAMPLEID_ID = 36;
    final private int ISLIKE_ID = 37;
    final private int ISDISLIKE_ID = 38;
    
    
    
    
    
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
            
            Object o = em.createNativeQuery("select MAX(objId) from Objects").getSingleResult();
            if (o == null) o = 0;
            Integer id = Integer.valueOf(o.toString());
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
    
    private void writeToFile(
            InputStream uploadedInputStream,
            String uploadedFileLocation
    ) throws FileNotFoundException, IOException {
        OutputStream out;
        int read;
        byte[] bytes = new byte[1024];
        out = new FileOutputStream(new File(uploadedFileLocation));
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
    
    @POST
    @Path("uploadSample")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String upload(
            @FormDataParam("file") InputStream file,
            @FormDataParam("name") String name,
            @FormDataParam("duration") int duration,
            @FormDataParam("source") int source_objid,
            @FormDataParam("directory") String directory,
            @FormDataParam("login") String login) {
        try {
            JSONObject result = new JSONObject();
            
            Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                        + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
            java.sql.Timestamp time = (java.sql.Timestamp) em.createNativeQuery("select CURRENT_TIMESTAMP from Objects").getResultList().get(0);
            
            HashMap<Integer,Attribute> attrValue = new HashMap<>();
            attrValue.put(NAME_ID,new Attribute("string", name));
            attrValue.put(DURATION_ID,new Attribute("int", duration));            
            attrValue.put(SOURCE_REF_ID,new Attribute("reference", source_objid));
            attrValue.put(USER_REF_ID,new Attribute("reference", objId));
            attrValue.put(TIME_ID,new Attribute("date", time.toString()));
            attrValue.put(COUNT_OF_LIKE_ID, new Attribute("int",0));
            attrValue.put(COUNT_OF_DISLIKE_ID, new Attribute("int",0));
            attrValue.put(DIRECTORY_REF_ID, new Attribute("string",directory));
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Fake code simulating the copy
            // You can generally do better with nio if you need...
            // And please, unlike me, do something about the Exceptions :D
            byte[] buffer = new byte[1024];
            int len;
            while ((len = file.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            utx.begin();
            // Open new InputStreams using the recorded bytes
            // Can be repeated as many times as you wish
            InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
            
            Objects newSample  = new Objects();
            Objects newSampleFile = new Objects();
            
            Object o = em.createNativeQuery("select MAX(objId) from Objects").getSingleResult();
            if (o == null) o = 0;
            Integer id = Integer.valueOf(o.toString());
            newSample.setObjId(id+1);
            newSample.setName("Sample " + name);
            newSample.setObjTypeId(SAMPLE_TYPE_ID);
            
            newSampleFile.setObjId(id+2);
            newSampleFile.setName("Sample File " + name);
            newSampleFile.setObjTypeId(FILE_TYPE_ID);
                
            attrValue.put(FILE_REF_ID, new Attribute("reference", id+2));
            
            em.persist(newSample);
            em.persist(newSampleFile);
            
            try{
               Integer dirId = (Integer) em.createNativeQuery("select a1.objId from AttributeValue a1, AttributeValue a2 "
                                                        + "where a1.stringValue = ? and "
                                                        + "      a1.attrId = ? and "
                                                        + "      a1.objId = a2.objId and "
                                                        + " a2.referenceValue = ? and "
                                                        + " a2.attrId = ?")
                                            .setParameter(1, directory).setParameter(2, DIR_NAME_ID)
                                            .setParameter(3, objId).setParameter(4, DIR_USER_REF_ID).getSingleResult();
            } catch(NoResultException ex) {
                Objects newDirectory = new Objects();
                newDirectory.setObjId(id+3);
                newDirectory.setName("Directory " + name);
                newDirectory.setObjTypeId(DIRECTORY_TYPE_ID);
                em.persist(newDirectory);
                Integer dirId = id+3;
                HashMap<Integer,Attribute> attrValueForDir = new HashMap<>();
                attrValueForDir.put(DIR_NAME_ID,new Attribute("string", name));
                attrValueForDir.put(DIR_USER_REF_ID,new Attribute("reference", objId));
                MainResource resource = new MainResource();
                resource.addAttributes(attrValueForDir, newDirectory, em);
            }
        
            attrValue.put(DIRECTORY_REF_ID, new Attribute("reference", id+3));
            
            MainResource resource = new MainResource();
            resource.addAttributes(attrValue, newSample, em);
           
            attrValue = new HashMap<>();
            String hashName = GetHash(is1);
            
            String pathToSource = "C:\\Users\\Test\\Desktop\\Programms on java\\MusicInstagram\\Music-instagram\\MusicInstagram\\web\\upload\\".concat(hashName);
            attrValue.put(PATH_FILE_ID,new Attribute("string", hashName));
            resource = new MainResource();
            resource.addAttributes(attrValue, newSampleFile, em);
            
            utx.commit();
            writeToFile(is2, pathToSource);
            return "Written";
        } catch (IOException | NotSupportedException | 
                NoSuchAlgorithmException | SystemException | RollbackException | 
                HeuristicMixedException | HeuristicRollbackException | 
                SecurityException | IllegalStateException ex) {
            return "Error Written to server disk";
        }
    }
    
    @GET
    @Path("getSamplesByLoginInTimeOrder")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSamplesByLoginInTimeOrder(@QueryParam("login") String login) {
    
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
        SortedMap<Timestamp,Integer> mapOfSamplesId = new TreeMap<>();
        
        for(Integer sampleId : samplesId) {
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, sampleId).getResultList();
            Timestamp time = (Timestamp) em.createNativeQuery("select dateValue from AttributeValue "
                                                            + "where attrId = ? and objId = ? ")
                    .setParameter(1, TIME_ID).setParameter(2,sampleId).getResultList().get(0);
            mapOfLastSamples.put(time, attrs);
            mapOfSamplesId.put(time,sampleId);
        }
        for(Map.Entry<Timestamp,List<AttributeValue>> entry : mapOfLastSamples.entrySet()) {
            obj = new JSONObject();
            List<AttributeValue> attrs = entry.getValue();
            obj.put("SampleId",mapOfSamplesId.get(entry.getKey()));
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
            obj.put("SampleId", entry.getKey().getObjId());
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
//            if (samplesId.isEmpty()) {
//                return result.toString();
//            }
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
            obj.put("nameOfDirectory", nameOfDirectory);    
            for(Map.Entry<CountOfLikeObject,List<AttributeValue>> entry : mapOfLastSamples.entrySet()) {
                obj = new JSONObject();
                
                List<AttributeValue> attrs = entry.getValue();
                obj.put("SampleId", entry.getKey().getObjId());
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
    @Path("doLikeOrDisLike")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String doLikeOrDisLike(@Context HttpServletRequest req,
                             @QueryParam("sampleId") int sampleId,
                             @QueryParam("login") String login,
                             @QueryParam("operation") String operation) {
        JSONObject result = new JSONObject();
        
        try {
            utx.begin();
            Attribute[] attrValues = preparedOfDoLike(login, sampleId);
            Integer userId = (Integer) attrValues[0].getValue(); 
            Boolean isLike = (Boolean) attrValues[1].getValue();
            Boolean isDisLike = (Boolean) attrValues[2].getValue();
            AttributeValue countOfLikes = (AttributeValue) attrValues[3].getValue();
            AttributeValue countOfDisLikes = (AttributeValue) attrValues[4].getValue();
            AttributeValue isLikeId = (AttributeValue) attrValues[5].getValue();
            AttributeValue isDisLikeId = (AttributeValue) attrValues[6].getValue();
            
            if ("like".equals(operation)) {
            
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
            }
            
            if ("dislike".equals(operation)) {
                if (isDisLike == true) {
                    countOfDisLikes.setNumberValue(countOfDisLikes.getNumberValue() - 1);
                    em.merge(countOfDisLikes);
                    isDisLikeId.setBooleanValue(false);
                    em.merge(isDisLikeId);

                } else {
                    if (isLike == false) {
                        countOfDisLikes.setNumberValue(countOfDisLikes.getNumberValue() + 1);
                        em.merge(countOfDisLikes);
                        isDisLikeId.setBooleanValue(true);
                        em.merge(isDisLikeId);
                    } else {
                        countOfLikes.setNumberValue(countOfLikes.getNumberValue() - 1);
                        countOfDisLikes.setNumberValue(countOfDisLikes.getNumberValue() + 1);
                        em.merge(countOfDisLikes);
                        em.merge(countOfLikes);
                        isDisLikeId.setBooleanValue(true);
                        isLikeId.setBooleanValue(false);
                        em.merge(isDisLikeId);
                        em.merge(isLikeId);
                    }
                }
                
            }
            utx.commit();
            
            result.put("newCountOflike", countOfLikes.getNumberValue());
            result.put("newCountOfDislike", countOfDisLikes.getNumberValue());
            return result.toString();
        } catch (NoResultException ex) {
            try {
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
                String sampleName = (String) em.createNativeQuery(
                        "SELECT stringValue " +
                                "FROM ATTRIBUTEVALUE " +
                                "where OBJID = ? and " +
                                "      ATTRID = (select attrs.attrId " +
                                "                from ATTRIBUTES attrs, ATTRIBUTEOBJECTTYPES attrObj " +
                                "                where attrs.name = 'name' and " +
                                "                      attrObj.OTID = (select OTID from OBJECTTYPES where name = 'Sample' ) and " +
                                "                      attrs.ATTRID = attrObj.ATTRID )")
                        .setParameter(1, sampleId).getResultList().get(0);
                
                Objects newLikeObject = new Objects();
                
                Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());

                newLikeObject.setObjId(id+1);
                newLikeObject.setName("LikesAndDisLikes by " + userName + " of " + sampleName);
                newLikeObject.setObjTypeId(LIKES_TYPE_ID);
                
                em.persist(newLikeObject);
                Integer userId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                        + "where stringValue = ? and attrId = ? ")
                        .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
                
                HashMap<Integer,Attribute> attrValue = new HashMap<>();
                String nameofBoolean = new String ();
                String nameCountOfFirstOperation = new String ();
                String nameCountOfSecondOperation = new String ();
                attrValue.put(USERID_ID,new Attribute("reference", userId));
                attrValue.put(SAMPLEID_ID,new Attribute("reference", sampleId));
                if ("like".equals(operation)) {
                    attrValue.put(ISLIKE_ID,new Attribute("boolean", true));
                    attrValue.put(ISDISLIKE_ID,new Attribute("boolean", false));
                    nameofBoolean = "isLike";
                    nameCountOfFirstOperation = "count_of_like";
                    nameCountOfSecondOperation = "count_of_dislike";
                } else
                if ("dislike".equals(operation)) {
                    attrValue.put(ISLIKE_ID,new Attribute("boolean", false));
                    attrValue.put(ISDISLIKE_ID,new Attribute("boolean", true));
                    nameofBoolean = "isDisLike";
                    nameCountOfFirstOperation = "count_of_dislike";
                    nameCountOfSecondOperation = "count_of_like";
                }
                
                MainResource resource = new MainResource();
                resource.addAttributes(attrValue, newLikeObject, em);
                
                
                Integer attrValueId = (Integer) em.createNativeQuery(
                        "select attrValueId from AttributeValue " +
                                "where objId = ? and " +
                                "      attrId = (select attrId from attributes where name = ?)")
                        .setParameter(1,newLikeObject.getObjId()).setParameter(2, nameofBoolean).getSingleResult();
                AttributeValue isObjectLikeId = em.find(AttributeValue.class, attrValueId);
                
                attrValueId = (Integer) em.createNativeQuery(
                        "select attrValueId from AttributeValue "
                                + "where objId = ? and "
                                + "attrId = (select attrId from attributes where name = ?)")
                        .setParameter(1, sampleId).setParameter(2, nameCountOfFirstOperation).getSingleResult();
                AttributeValue countOfFirstOperation = em.find(AttributeValue.class, attrValueId);
                
                attrValueId = (Integer) em.createNativeQuery(
                        "select attrValueId from AttributeValue "
                                + "where objId = ? and "
                                + "attrId = (select attrId from attributes where name = ?)")
                        .setParameter(1, sampleId).setParameter(2, nameCountOfSecondOperation).getSingleResult();
                AttributeValue countOfSecondOperation = em.find(AttributeValue.class, attrValueId);
        
                countOfFirstOperation.setNumberValue(countOfFirstOperation.getNumberValue() + 1);
                em.merge(countOfFirstOperation);
                isObjectLikeId.setBooleanValue(true);
                em.merge(isObjectLikeId);
                utx.commit();
                
                if (null != operation) 
                    switch (operation) {
                    case "like":
                        result.put("newCountOflike", countOfFirstOperation.getNumberValue());
                        result.put("newCountOfDislike", countOfSecondOperation.getNumberValue());
                        result.put("result", "Success");
                        break;
                    case "dislike":
                        result.put("newCountOflike", countOfSecondOperation.getNumberValue());
                        result.put("newCountOfDislike", countOfFirstOperation.getNumberValue());
                        result.put("result", "Success");
                        break;
                    default:
                        result.put("result", "Error operation");
                        break;
                }
                return result.toString();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex1) {
                Logger.getLogger(SampleResource.class.getName()).log(Level.SEVERE, null, ex1);
                result.put("result", "create LikeObject - error");
                return result.toString();
            }
            
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
    
    private Attribute[] preparedOfDoLike(String login,
                                      Integer sampleId) {
        Attribute[] attrValues = new Attribute[7];
        Integer userId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                        + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        attrValues[0] = new Attribute();
        attrValues[0].setType("int");
        attrValues[0].setValue(userId);
        
        Integer objId = (Integer) em.createNativeQuery(
                "    select objId " +
                "    from ATTRIBUTEVALUE " +
                "    where attrId = (select attrId from ATTRIBUTES where name = 'userId') and " +
                "          referenceValue = ? " +
                " INTERSECT " +
                "    select objId " +
                "    from ATTRIBUTEVALUE " +
                "    where attrId = (select attrId from ATTRIBUTES where name = 'sampleId') and " +
                "          referenceValue = ?")
                .setParameter(1, userId).setParameter(2, sampleId).getSingleResult();
        
        
        Boolean isLike = (Boolean) em.createNativeQuery(
            "select booleanValue " +
            "from ATTRIBUTEVALUE " +
            "where objId = ? and " +
            "      attrId = (select attrId from ATTRIBUTES where name = 'isLike')")
                .setParameter(1,objId).getSingleResult();
        attrValues[1] = new Attribute();
        attrValues[1].setType("boolean");
        attrValues[1].setValue(isLike);

        Boolean isDisLike = (Boolean) em.createNativeQuery(
            "select booleanValue " +
            "from ATTRIBUTEVALUE " +
            "where objId = ? and " +
            "      attrId = (select attrId from ATTRIBUTES where name = 'isDisLike')")
                .setParameter(1,objId).getSingleResult();
        attrValues[2] = new Attribute();
        attrValues[2].setType("boolean");
        attrValues[2].setValue(isDisLike);


        Integer attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue "
              + "where objId = ? and "
                    + "attrId = (select attrId from attributes where name = 'count_of_like')")
                .setParameter(1, sampleId).getSingleResult();
        AttributeValue countOfLikes = em.find(AttributeValue.class, attrValueId);
        attrValues[3] = new Attribute();
        attrValues[3].setType("int");
        attrValues[3].setValue(countOfLikes);

        attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue "
              + "where objId = ? and "
                    + "attrId = (select attrId from attributes where name = 'count_of_dislike') ")
                .setParameter(1, sampleId).getSingleResult();
        AttributeValue countOfDisLikes = em.find(AttributeValue.class, attrValueId);
        attrValues[4] = new Attribute();
        attrValues[4].setType("int");
        attrValues[4].setValue(countOfDisLikes);


        attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue " +
                "where objId = ? and " +
                "      attrId = (select attrId from attributes where name = 'isLike')")
                .setParameter(1,objId).getSingleResult();
        AttributeValue isLikeId = em.find(AttributeValue.class, attrValueId);
        attrValues[5] = new Attribute();
        attrValues[5].setType("object");
        attrValues[5].setValue(isLikeId);

        attrValueId = (Integer) em.createNativeQuery(
                "select attrValueId from AttributeValue " +
                "where objId = ? and " +
               "attrId = (select attrId from attributes where name = 'isDisLike')")
                .setParameter(1,objId).getSingleResult();
        AttributeValue isDisLikeId = em.find(AttributeValue.class, attrValueId);
        attrValues[6] = new Attribute();
        attrValues[6].setType("object");
        attrValues[6].setValue(isDisLikeId);
            
        return attrValues;
    }
    
    
    @GET
    @Path("getPathById")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPathById(@QueryParam("id") String sourceId) {
        try {
            Object opath = em.createNativeQuery(
                    "select file.stringvalue "
                    + "from attributevalue file, attributevalue source "
                    + "where source.objid = ? and source.referencevalue = file.objid "
                    + "and source.attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot "
                    + "where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'file_ref' and ot.name = 'Sample')"
                    + "and file.attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot "
                    + "where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'path' and ot.name = 'File')"
            )
                    .setParameter(1, sourceId).getSingleResult();
            return (new Gson()).toJson(opath);
        } catch (NoResultException | NonUniqueResultException e) {
            return (new Gson()).toJson("File not found");
        }
    }
}
