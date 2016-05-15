package ru.bystrov.musicinstagram.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
@Path("user")
@javax.enterprise.context.RequestScoped
public class UserResource {
    @Context
    private UriInfo context;
    @PersistenceContext(unitName = "MusicInstagramPU")
    private EntityManager em;
    @Resource
    UserTransaction utx;
    
    
    
    final private int FIRST_NAME_ID = 1;
    final private int LAST_NAME_ID = 2;
    final private int AGE_ID = 3;
    final private int COUNTRY_ID = 4;
    final private int CITY_ID = 5;
    final private int UNIVERCITY_ID = 6;
    final private int PHONE_NUMBER_ID = 7;
    final private int LOGIN_ID = 8;
    final private int PASSWORD_ID = 9;
    final private int FRIEND_REF_ID = 29;
    final private int PHOTO_REF_ID = 30;
    
    
    final private int PATH_FILE_ID = 27;
    final private int TYPE_FILE_ID = 28;
    
    final private int USER_TYPE_ID = 1;
    final private int FILE_TYPE_ID = 5;

    public UserResource() {
    }
    
    @POST
    @Path("createUserFull")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String createUserFull(@Context HttpServletRequest req,
                             @FormParam("first_name") String firstName,
                             @FormParam("last_name") String lastName,
                             @FormParam("age") int age,
                             @FormParam("city") String city,
                             @FormParam("country") String country,
                             @FormParam("univercity") String univercity,
                             @FormParam("phone_number") String phoneNumber,
                             @FormParam("photo_ref") String photoRef,
                             @FormParam("login") String login,
                             @FormParam("password") String password) {
        JSONObject result = new JSONObject();

        
        try {
            
            utx.begin();
            HashMap<Integer,Attribute> attrValue = new HashMap<>();
            attrValue.put(FIRST_NAME_ID,new Attribute("string", firstName));
            attrValue.put(LAST_NAME_ID,new Attribute("string", lastName));
            attrValue.put(AGE_ID,new Attribute("int", age));
            attrValue.put(CITY_ID,new Attribute("string", city));
            attrValue.put(COUNTRY_ID,new Attribute("string", country));
            attrValue.put(UNIVERCITY_ID,new Attribute("string", univercity));
            attrValue.put(PHONE_NUMBER_ID,new Attribute("string", phoneNumber));
            attrValue.put(LOGIN_ID,new Attribute("string", login));
            attrValue.put(PASSWORD_ID,new Attribute("string", password));
            
            Objects newUser  = new Objects();
            Objects newPhotoFile = new Objects();
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newUser.setObjId(id+1);
            newUser.setName("User " + getName(attrValue));
            newUser.setObjTypeId(USER_TYPE_ID);
            
            newPhotoFile.setObjId(id+2);
            newPhotoFile.setName("Photo File " + getName(attrValue));
            newPhotoFile.setObjTypeId(FILE_TYPE_ID);
            
            attrValue.put(PHOTO_REF_ID,new Attribute("reference", id+2));
                
            em.persist(newUser);
            em.persist(newPhotoFile);
            AttributeValue newAttrValue;
            id = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newUser.getObjId());
                switch (entry.getValue().getType()) {
                    case "String":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(0);
                        newAttrValue.setReferenceValue(0);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int)entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(0);
                        break;
                    case "reference":
                        newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setNumberValue(0);
                        break;
                    default:
                        break;
                }
                em.persist(newAttrValue);
            }           
            
            
            //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            attrValue.put(PATH_FILE_ID,new Attribute("String", photoRef));
            attrValue.put(TYPE_FILE_ID,new Attribute("String", "jpg"));
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
            newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newPhotoFile.getObjId());
                switch (entry.getValue().getType()) {
                    case "string":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(0);
                        newAttrValue.setReferenceValue(0);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int)entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(0);
                        break;
                    case "reference":
                        newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setNumberValue(0);
                        break;
                    default:
                        break;
                }
                em.persist(newAttrValue);
            }
            
            
            result.put("result","ok");
            utx.commit();
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
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
    
    @POST
    @Path("createUser")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String createUser(@Context HttpServletRequest req,
                             @FormParam("login") String login,
                             @FormParam("password") String password) {
        JSONObject result = new JSONObject();

        HashMap<Integer,Attribute> attrValue = new HashMap<>();
        attrValue.put(FIRST_NAME_ID,new Attribute("String", "User"));
        attrValue.put(LAST_NAME_ID,new Attribute("String", ""));
        attrValue.put(AGE_ID,new Attribute("int", 0));
        attrValue.put(COUNTRY_ID,new Attribute("String", ""));
        attrValue.put(CITY_ID,new Attribute("String", ""));
        attrValue.put(UNIVERCITY_ID,new Attribute("String", ""));
        attrValue.put(PHONE_NUMBER_ID,new Attribute("String", ""));
        attrValue.put(LOGIN_ID,new Attribute("String", login));
        attrValue.put(PASSWORD_ID,new Attribute("String", password));
        try {
            utx.begin();
            
            Objects newUser  = new Objects();
            Objects newPhotoFile = new Objects();
            
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newUser.setObjId(id+1);
            newUser.setName("User " + getName(attrValue));
            newUser.setObjTypeId(USER_TYPE_ID);
            
            newPhotoFile.setObjId(id+2);
            newPhotoFile.setName("Photo File " + getName(attrValue));
            newPhotoFile.setObjTypeId(FILE_TYPE_ID);
            
            attrValue.put(PHOTO_REF_ID,new Attribute("reference", id+2));
                
            em.persist(newUser);
            em.persist(newPhotoFile);
                
            em.persist(newUser);
            AttributeValue newAttrValue;
            id = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newUser.getObjId());
                switch (entry.getValue().getType()) {
                    case "String":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setReferenceValue(-1);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int)entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(-1);
                        break;
                    case "Reference":
                        newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setNumberValue(-1);
                        break;
                    default:
                        break;
                }
                em.persist(newAttrValue);
            }
            
            
            //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            attrValue.put(PATH_FILE_ID,new Attribute("String", "images/userPhoto/user.jpg"));
            attrValue.put(TYPE_FILE_ID,new Attribute("String", "jpg"));
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
            newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newPhotoFile.getObjId());
                switch (entry.getValue().getType()) {
                    case "String":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setReferenceValue(-1);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int)entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(-1);
                        break;
                    case "reference":
                        newAttrValue.setReferenceValue((int) entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setNumberValue(-1);
                        break;
                    default:
                        break;
                }
                em.persist(newAttrValue);
            }
            
            
            result.put("result","ok");
            utx.commit();
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
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
    @Path("deleteUser")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String deleteUserById(@Context HttpServletRequest req,
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
    @Path("getUserByLogin")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getUserByLogin(@Context HttpServletRequest req,
                                 @QueryParam("login") String login) {
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
        List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
        
        JSONObject obj = new JSONObject();
        for (AttributeValue attr : attrs){
            if (attr.getNumberValue() != -1) {
                obj.put(String.valueOf("User" + attr.getAttrId()),attr.getNumberValue());
            } else
            if (!attr.getStringValue().equals("")) {
                obj.put(String.valueOf("User" + attr.getAttrId()),attr.getStringValue());
            } else
            if (attr.getReferenceValue() != -1) {
                obj.put(String.valueOf("User" + attr.getAttrId()),attr.getReferenceValue());
            }
        }
        return obj.toString();
    }
    
    @GET
    @Path("getUsers")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getUsers(@Context HttpServletRequest req) {
        JSONArray array = new JSONArray();
        List<Integer> usersId = em.createNativeQuery("select o.objId from Objects o, ObjectTypes OT "
                                            + " where o.objTypeId = OT.otid and"
                                            + " OT.name = ?")
                                    .setParameter(1, "User").getResultList();
        JSONObject obj;
        
        for(Integer objId : usersId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
            
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != -1) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    obj.put(String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
    }
    
    @GET
    @Path("getFriendsByLogin")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String geFriendsByLogin(@Context HttpServletRequest req,
                                 @QueryParam("login") String login) {
        JSONArray array = new JSONArray();
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
        
        
        List<Integer> friendsId = em.createNativeQuery(" select attrV.referencevalue "
                                                     + " from AttributeValue attrV  "
                                                     + " where attrV.objId = ?  and "
                                                     + " attrV.attrId = ?")
                                    .setParameter(1, objId).setParameter(2,FRIEND_REF_ID).getResultList();
        
        JSONObject obj = new JSONObject();
        obj.put("countOfFriend",friendsId.size());
        array.put(obj);
        
        Integer[] arrayOfFriendsId = friendsId.toArray(new Integer[friendsId.size()]);
        Set<Integer> intset = new TreeSet<Integer>();
        Random random = new Random(Calendar.getInstance().getTimeInMillis());
        while (intset.size() < friendsId.size()){
            intset.add(random.nextInt(arrayOfFriendsId.length));
        }
        Integer[] intarrayRandom = intset.toArray(new Integer[friendsId.size()]);
        
        if (friendsId.isEmpty()) {
            return array.toString(); 
        } 
        for(int i = 0; i < friendsId.size(); i++) {
            Integer friendId  = arrayOfFriendsId[intarrayRandom[i]];
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, friendId).getResultList();
            
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != -1) {
                    obj.put(String.valueOf("Friend" + attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf("Friend" + attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    if (attr.getAttrId() == PHOTO_REF_ID) {
                        String photoRef = (String) em.createNativeQuery("select attrV.stringValue "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ? and attrV.attrId = ? ")
                                .setParameter(1, attr.getReferenceValue()).setParameter(2, PATH_FILE_ID).getResultList().get(0);
                        obj.put(String.valueOf("Friend" + attr.getAttrId()),photoRef);
                    } else {
                        obj.put(String.valueOf("Friend" + attr.getAttrId()),attr.getReferenceValue());
                    }
                }
            }
            array.put(obj);
        }
        return array.toString();
    }
    
    @GET
    @Path("getLogin")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getLogin(@Context HttpServletRequest req) {
        try {    
            utx.begin();
            
            JSONObject result = new JSONObject();
            result.put("nickName", "energetiks");
            return result.toString();
        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException | JSONException ex) {
            try {
                Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex);
                utx.rollback();
                return "Exception!!!";
            } catch (IllegalStateException | SecurityException | SystemException | JSONException ex1) {
                Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex1);
                return "Exception!!!";
            }
        }
    }
    
    @GET
    @Path("getPhotoByLogin")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getPhotoByLogin(@Context HttpServletRequest req,
                           @QueryParam("login") String login) {
        try {    
            utx.begin();
             String photo = (String) em.createNativeQuery(
                     "select photo.stringValue "
                   + "from AttributeValue object, "
                        + "AttributeValue file, "
                        + "AttributeValue photo "
                   + "where object.stringValue = ? and object.attrId = ? and  "
                         + "file.objId = object.objId and file.attrId = ? and "
                         + "photo.objId = file.referenceValue and photo.attrId = ? ")
                     .setParameter(1, login)
                     .setParameter(2, LOGIN_ID)
                     .setParameter(3, PHOTO_REF_ID)
                     .setParameter(4, PATH_FILE_ID).getResultList().get(0);
            utx.commit();
            JSONObject result = new JSONObject();
            result.put("photo", photo);
            return result.toString();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
            try {
                utx.rollback();
                return "Exception!!!";
            } catch (IllegalStateException | SecurityException | SystemException | JSONException ex1) {
                return "Exception!!!";
            }
        }
    }
    
    private String getName(HashMap<Integer,Attribute> attributes) {
        return attributes.get(FIRST_NAME_ID).getValue().toString() + " " 
                 + attributes.get(LAST_NAME_ID).getValue().toString();
    }
    
}
