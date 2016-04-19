package ru.bystrov.musicinstagram.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.bystrov.musicinstagram.entities.AttributeValue;
import ru.bystrov.musicinstagram.entities.Objects;
import ru.bystrov.musicinstagram.entities.dataobject.Attribute;

/**
 * REST Web Service
 *
 * @author Energetiks
 */
@Path("source")
public class SourceResource {

    @Context
    private UriInfo context;
    @PersistenceContext(unitName = "MusicInstagramPU")
    private EntityManager em;
    @Resource
    UserTransaction utx;
    
    final private int NAME_ID = 10;
    final private int DURATION_ID = 11;
    final private int GENRE_ID = 12;
    final private int LANGUAGE_ID = 13;
    final private int ALBUM_ID = 14;
    final private int YEAR_ID = 15;
    final private int MUSICAL_GROUP_NAME_ID = 16;
    final private int FILE_REF_ID = 17;
    final private int USER_REF_ID = 18;
    
    final private int SOURCE_TYPE_ID = 2;

    
    public SourceResource() {
    }
    
    @POST
    @Path("loadSource")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String loadNewSource(@Context HttpServletRequest req,
                             @FormParam("name") String name,
                             @FormParam("duration") int duration,
                             @FormParam("genre") String genre,
                             @FormParam("language") String language,
                             @FormParam("album_name") String albumName,
                             @FormParam("year") String year,
                             @FormParam("musical_group_name") String musicalGroupName,
                             @FormParam("file_ref") Objects fileRef,
                             @FormParam("user_ref") Objects userRef) {
        JSONObject result = new JSONObject();

        HashMap<Integer,Attribute> attrValue = new HashMap<>();
        attrValue.put(NAME_ID,new Attribute("String", name));
        attrValue.put(DURATION_ID,new Attribute("int", duration));
        attrValue.put(GENRE_ID,new Attribute("String", genre));
        attrValue.put(LANGUAGE_ID,new Attribute("String", language));
        attrValue.put(ALBUM_ID,new Attribute("String", albumName));
        attrValue.put(YEAR_ID,new Attribute("String", year));
        attrValue.put(MUSICAL_GROUP_NAME_ID,new Attribute("String", musicalGroupName));
        attrValue.put(FILE_REF_ID,new Attribute("Reference", fileRef));
        attrValue.put(USER_REF_ID,new Attribute("Reference", userRef));
        try {
            utx.begin();
            Objects newSource  = new Objects();
            
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newSource.setObjId(id+1);
            newSource.setName(name);
            newSource.setObjTypeId(SOURCE_TYPE_ID);
                
            em.persist(newSource);
            
            AttributeValue newAttrValue;
            id = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {   
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrId(id);
                
                newAttrValue.setObjId(newSource.getObjId());
                if (entry.getValue().getType().equals("String")) {
                    newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                    newAttrValue.setNumberValue(0);
                    newAttrValue.setReferenceValue(0);
                    
                }
                if (entry.getValue().getType().equals("int")) {
                    newAttrValue.setNumberValue((int)entry.getValue().getValue());
                    newAttrValue.setStringValue("");
                    newAttrValue.setReferenceValue(0);
                }
                if (entry.getValue().getType().equals("Reference")) {
                    newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                    newAttrValue.setNumberValue(0);
                    newAttrValue.setStringValue("");
                }
                em.persist(newAttrValue);
            }           
            
            utx.commit();
            result.put("result","ok");
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "DB Error:", ex.getLocalizedMessage()));
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
    @Path("deleteSource")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String deleteSourcebyId(@Context HttpServletRequest req,
                                 @QueryParam("objId") int objId) {
        JSONObject result = new JSONObject();
        if (objId == 0) {
            result.put("result", "failed");
            return result.toString();
        }
        try {    
            utx.begin();
            
            em.remove(em.find(Objects.class, objId));
            List<AttributeValue> attributes = em.createNativeQuery("select * from AttributeValue where objId = ?",AttributeValue.class)
                    .setParameter(1, objId).getResultList();
            attributes.stream().forEach((attr) -> {
                em.remove(attr);
            });
            utx.commit();
            result.put("result", "ok");
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
            try {
                Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex);
                utx.rollback();
                result.put("result", "failed - ROLLBACK");
                return result.toString();
            } catch (IllegalStateException | SecurityException | SystemException | JSONException ex1) {
                Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex1);
                result.put("result", "failed - ROLLBACK DENIED");
                return result.toString();
            }
        }
        
    }
    
    @GET
    @Path("getSources")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSources() {
        JSONArray array = new JSONArray();
        List<Integer> usersId = em.createNativeQuery("select o.objId from Objects o, ObjectTypes OT "
                                            + " where o.objTypeId = OT.otid and"
                                            + " OT.name = ?")
                                    .setParameter(1, "Source").getResultList();
        JSONObject obj;
        List<AttributeValue> attrs;
        for(int objId : usersId) {
            obj = new JSONObject();
            attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
            
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != 0) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != 0) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
    }
    
    @GET
    @Path("getSourceById")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSourceById(@Context HttpServletRequest req,
                                 @QueryParam("objId") int objId) {
        List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
        JSONObject obj = new JSONObject();
        for (AttributeValue attr : attrs){
            if (attr.getNumberValue() != 0) {
                obj.put(String.valueOf(attr.getAttrId()),attr.getNumberValue());
            } else
            if (!attr.getStringValue().equals("")) {
                obj.put(String.valueOf(attr.getAttrId()),attr.getStringValue());
            } else
            if (attr.getReferenceValue() != 0) {
                obj.put(String.valueOf(attr.getAttrId()),attr.getReferenceValue());
            }
        }
        return obj.toString();
    
    }
    
    @GET
    @Path("getSourcesByName")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSourcesByName(@Context HttpServletRequest req,
                                 @QueryParam("name") String name) {
        List<Integer> sourcesId = em.createNativeQuery("select attrV.objId "
                                                        + "from AttributeValue attrV "
                                                        + "where attrV.stringValue = ? and "
                                                              + "attrV.attrId = ? ")
            .setParameter(1, name).setParameter(2, NAME_ID).getResultList();
        return findSMTHbyParameter(sourcesId);
    }
    
    @GET
    @Path("getSourcesByMusicalGroup")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSourcebyMusicalGroup(@Context HttpServletRequest req,
                                        @QueryParam("musical_group_name") String musicalGroupName) {
        List<Integer> sourcesId = em.createNativeQuery("select attrV.objId "
                                                        + "from AttributeValue attrV "
                                                        + "where attrV.stringValue = ? and "
                                                              + "attrV.attrId = ?")
            .setParameter(1, musicalGroupName).setParameter(2, MUSICAL_GROUP_NAME_ID).getResultList();
        return findSMTHbyParameter(sourcesId);
    }
    
    @GET
    @Path("getSourcesByAlbum")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSourcebyAlbum(@Context HttpServletRequest req,
                                        @QueryParam("album_name") String albumName) {
        List<Integer> sourceId = em.createNativeQuery("select attrV.objId "
                                                        + "from AttributeValue attrV "
                                                        + "where attrV.stringValue = ? and "
                                                              + "attrV.attrId = ?")
            .setParameter(1, albumName).setParameter(2, ALBUM_ID).getResultList();
        return findSMTHbyParameter(sourceId);
    }
    
    @GET
    @Path("getSourcesByGenre")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getSourcebyGenre(@Context HttpServletRequest req,
                                        @QueryParam("genre") String genre) {
        List<Integer> sourcesID = em.createNativeQuery("select attrV.objId "
                                                        + "from AttributeValue attrV "
                                                        + "where attrV.stringValue = ? and "
                                                              + "attrV.attrId = ?")
            .setParameter(1, genre).setParameter(2, GENRE_ID).getResultList();
        return findSMTHbyParameter(sourcesID);
    }
    
        @GET
    @Path("getThreeRandomSourcesById")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String geThreeRandomSourcesById(@Context HttpServletRequest req,
                                 @QueryParam("objId") String objId) {
        JSONArray array = new JSONArray();
        List<Integer> sourcesId = em.createNativeQuery(" select attrV.objId "
                                                     + " from AttributeValue attrV, Objects o  "
                                                     + " where attrV.objId = o.objId  and "
                                                     + " o.objTypeId = ? and "
                                                     + " attrV.attrId = ? and "
                                                     + " attrV.referenceValue = ?")
                        .setParameter(1, SOURCE_TYPE_ID).setParameter(2,USER_REF_ID).setParameter(3, objId).getResultList();
        JSONObject obj;
        
        Integer[] arrayOfSourcesId = sourcesId.toArray(new Integer[sourcesId.size()]);
        for(int i = 0; i < 3; i++) {
            Integer sourceId  = arrayOfSourcesId[(int) (Math.random() * arrayOfSourcesId.length)];
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, sourceId).getResultList();
            
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != 0) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != 0) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
    }
    
    public String findSMTHbyParameter(List<Integer> sourcesId) {
        JSONArray array = new JSONArray();
        JSONObject obj;
        for(int objId : sourcesId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
            
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != 0) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != 0) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
    }
}
