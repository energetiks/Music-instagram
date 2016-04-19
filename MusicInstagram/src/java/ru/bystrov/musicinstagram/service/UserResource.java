package ru.bystrov.musicinstagram.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    final private int USER_TYPE_ID = 1;

    public UserResource() {
    }
    
    @POST
    @Path("createUser")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String createUser(@Context HttpServletRequest req,
                             @FormParam("first_name") String firstName,
                             @FormParam("last_name") String lastName,
                             @FormParam("age") int age,
                             @FormParam("country") String country,
                             @FormParam("city") String city,
                             @FormParam("univercity") String univercity,
                             @FormParam("phone_number") String phoneNumber,
                             @FormParam("login") String login,
                             @FormParam("password") String password) {
        JSONObject result = new JSONObject();

        HashMap<Integer,Attribute> attrValue = new HashMap<>();
        attrValue.put(FIRST_NAME_ID,new Attribute("String", firstName));
        attrValue.put(LAST_NAME_ID,new Attribute("String", lastName));
        attrValue.put(AGE_ID,new Attribute("int", age));
        attrValue.put(COUNTRY_ID,new Attribute("String", country));
        attrValue.put(CITY_ID,new Attribute("String", city));
        attrValue.put(UNIVERCITY_ID,new Attribute("String", univercity));
        attrValue.put(PHONE_NUMBER_ID,new Attribute("String", phoneNumber));
        attrValue.put(LOGIN_ID,new Attribute("String", login));
        attrValue.put(PASSWORD_ID,new Attribute("String", password));
        try {
            utx.begin();
            
            Objects newUser  = new Objects();
            
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newUser.setObjId(id+1);
            newUser.setName(getName(attrValue));
            newUser.setObjTypeId(USER_TYPE_ID);
                
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
                        newAttrValue.setNumberValue(0);
                        newAttrValue.setReferenceValue(0);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int)entry.getValue().getValue());
                        newAttrValue.setStringValue("");
                        newAttrValue.setReferenceValue(0);
                        break;
                    case "Reference":
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
    @Path("getUserbyId")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getUserById(@Context HttpServletRequest req,
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
    @Path("getFriendsById")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getFriendsbyUserId(@Context HttpServletRequest req,
                                 @QueryParam("objId") String objId) {
        JSONArray array = new JSONArray();
        List<Integer> friendsId = em.createNativeQuery(" select attrV.referencevalue "
                                                     + " from AttributeValue attrV  "
                                                     + " where attrV.objId = ?  and "
                                                     + " attrV.attrId = ?")
                                    .setParameter(1, objId).setParameter(2,FRIEND_REF_ID).getResultList();
        JSONObject obj;
        for(int friendId : friendsId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, friendId).getResultList();
            
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
    @Path("getThreeRandomFriendsById")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String geThreeRandomFriendById(@Context HttpServletRequest req,
                                 @QueryParam("objId") String objId) {
        JSONArray array = new JSONArray();
        List<Integer> friendsId = em.createNativeQuery(" select attrV.referencevalue "
                                                     + " from AttributeValue attrV  "
                                                     + " where attrV.objId = ?  and "
                                                     + " attrV.attrId = ?")
                                    .setParameter(1, objId).setParameter(2,FRIEND_REF_ID).getResultList();
        JSONObject obj;
        
        Integer[] arrayOfFriendsId = friendsId.toArray(new Integer[friendsId.size()]);
        for(int i = 0; i < 3; i++) {
            Integer friendId  = arrayOfFriendsId[(int) (Math.random() * arrayOfFriendsId.length)];
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, friendId).getResultList();
            
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
    
    
    private String getName(HashMap<Integer,Attribute> attributes) {
        return attributes.get(FIRST_NAME_ID).getValue().toString() + " " 
                 + attributes.get(LAST_NAME_ID).getValue().toString();
    }
    
}
