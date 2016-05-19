package ru.bystrov.musicinstagram.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.servlet.http.HttpSession;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
    @Path("addDataAboutUser")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String addDataAboutUser(
                             @FormDataParam("firstName") String firstName,
                             @FormDataParam("lastName") String lastName,
                             @FormDataParam("age") int age,
                             @FormDataParam("country") String country,
                             @FormDataParam("city") String city,
                             @FormDataParam("university") String university,
                             @FormDataParam("phoneNumber") String phoneNumber,
                             @FormDataParam("photo") InputStream is,
                             @FormDataParam("login") String login) {
        JSONObject result = new JSONObject();

        
        try {
            
            utx.begin();
            HashMap<Integer,Attribute> attrValue = new HashMap<>();
            attrValue.put(FIRST_NAME_ID,new Attribute("string", firstName));
            attrValue.put(LAST_NAME_ID,new Attribute("string", lastName));
            attrValue.put(AGE_ID,new Attribute("int", age));
            attrValue.put(COUNTRY_ID,new Attribute("string", country));
            attrValue.put(CITY_ID,new Attribute("string", city));
            attrValue.put(UNIVERCITY_ID,new Attribute("string", university));
            attrValue.put(PHONE_NUMBER_ID,new Attribute("string", phoneNumber));
            
            Integer userId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                    + "where stringValue = ? and "
                    + "attrId = (select attrId from ATTRIBUTES where name = 'login')")
                    .setParameter(1, login).getResultList().get(0);
            
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
            
                Integer attrValueId = (Integer) em.createNativeQuery(
                        "SELECT attrValueID " +
                        "FROM ATTRIBUTEVALUE " +
                        "where objId = ? and " +
                        "      attrId = ?")
                        .setParameter(1, userId).setParameter(2, entry.getKey()).getSingleResult(); 
                AttributeValue attribute = em.find(AttributeValue.class, attrValueId);
                switch (entry.getValue().getType()) {
                    case "string":
                        attribute.setStringValue((String) entry.getValue().getValue());
                        break;
                    case "int":
                        attribute.setNumberValue((int) entry.getValue().getValue());
                        break;
                    case "reference":
                        attribute.setReferenceValue((int) entry.getValue().getValue());
                        break;
                    case "date":
                        attribute.setDateValue((String) entry.getValue().getValue());
                        break;
                    case "boolean":
                        attribute.setBooleanValue((Boolean) entry.getValue().getValue());
                        break;    
                    default:
                        break;
                }
                em.merge(attribute);
            }
            
            
            
            //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            String pathToPhoto = "C:\\Users\\Test\\Desktop\\Programms on java\\MusicInstagram\\Music-instagram\\MusicInstagram\\web\\images\\userPhoto\\" + login + ".jpg";

            attrValue.put(PATH_FILE_ID,new Attribute("String", "images/userPhoto/" + login + ".jpg"));
            attrValue.put(TYPE_FILE_ID,new Attribute("String", "jpg"));
            
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
                Integer attrValueId = (Integer) em.createNativeQuery(
                        "SELECT attrValueID " +
                        "FROM ATTRIBUTEVALUE " +
                        "where objId = (select referenceValue " +
                        "               from ATTRIBUTEVALUE " +
                        "               where objId= ? and " +
                        "                     attrId = (select attrId " + 
                        "                               from Attributes " + 
                        "                               where name='photo_ref'))  and " +
                        "      attrId = ?")
                        .setParameter(1, userId).setParameter(2, entry.getKey()).getSingleResult(); 
                AttributeValue attribute = em.find(AttributeValue.class, attrValueId);
                attribute.setStringValue((String) entry.getValue().getValue());
                em.merge(attribute);
            }
            writeToFile(is, pathToPhoto);
            result.put("result","ok");
            utx.commit();
            return result.toString();
        } catch (IOException| NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException ex) {
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
                
            
            MainResource resource = new MainResource();
            resource.addAttributes(attrValue, newUser, em);
            
            
            //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            attrValue.put(PATH_FILE_ID,new Attribute("String", "images/userPhoto/user.jpg"));
            attrValue.put(TYPE_FILE_ID,new Attribute("String", "jpg"));
            
            resource = new MainResource();
            resource.addAttributes(attrValue, newPhotoFile, em);
            
            
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
    @Path("deleteFriend")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String deleteFriend(@Context HttpServletRequest req,
                                 @FormParam("myLogin") String myLogin,
                                 @FormParam("login") String login) {
        JSONObject result = new JSONObject();
        try {    
            utx.begin();
            Integer myObjId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, myLogin).setParameter(2, LOGIN_ID).getSingleResult();
            Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getSingleResult();
            Integer attrValueId = (Integer) em.createNativeQuery("select attrValueId from AttributeValue "
                                                     + "where objId = ? and "
                                                     + "      attrId = (select attrId from Attributes where name='friend_ref') and "
                                                     + "      referenceValue = ?")
                                                     .setParameter(1, myObjId)
                                                     .setParameter(2, objId).getSingleResult();
            
            em.remove(em.find(AttributeValue.class, attrValueId));
            
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
        List<Long> usersId =  em.createNativeQuery(
                                              " select o.objId from Objects o, ObjectTypes OT "
                                            + " where o.objTypeId = OT.otid and"
                                            + " OT.name = ?")
                                    .setParameter(1, "User").getResultList();
        JSONObject obj;
        
        for(Long objId : usersId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
            
            for (AttributeValue attr : attrs){
                
                if (attr.getNumberValue() != -1) {
                    obj.put("User" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put("User" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    obj.put("User" +String.valueOf(attr.getAttrId()),attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
    }
    
    @GET
    @Path("getUsersByName")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getUsersByName(@QueryParam("login") String login,
                                 @QueryParam("name") String name) {
        JSONArray array = new JSONArray();
        List<Integer> usersId =  em.createNativeQuery(
                                    "select atV1.objId " +
                                    "from AttributeValue atV1, " +
                                    "     AttributeValue atV2, " +
                                    "     AttributeValue atV3 " +
                                    "where atV2.ATTRID = (select attrId " +
                                    "                     from Attributes " +
                                    "                     where name='last_name') and " +
                                    "      atV1.attrId = (select attrId " +
                                    "                     from Attributes " +
                                    "                     where name='first_name') and " +
                                    "      atV3.attrId = (select attrId " +
                                    "                     from Attributes " +
                                    "                     where name='login') and " +
                                    "      atV1.OBJID = atV2.OBJID and " +
                                    "      atV1.OBJID = atV3.OBJID and " +
                                    "      atV1.STRINGVALUE||' '||atV2.STRINGVALUE like ? and " +
                                    "      atV3.STRINGVALUE != ?")
                                    .setParameter(1, "%"+name+"%")
                                    .setParameter(2,login).getResultList();
        JSONObject obj;
        JSONObject result = new JSONObject();
        result.put("countOfUsers",usersId.size());
        for(Integer objId : usersId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
            obj.put("UserId",objId);
            for (AttributeValue attr : attrs){
                if (attr.getNumberValue() != -1) {
                    obj.put("User" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put("User" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    if (attr.getAttrId() == PHOTO_REF_ID) {
                        String photoRef = (String) em.createNativeQuery("select attrV.stringValue "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ? and attrV.attrId = ? ")
                                .setParameter(1, attr.getReferenceValue()).setParameter(2, PATH_FILE_ID).getResultList().get(0);
                        obj.put(String.valueOf("User" + attr.getAttrId()),photoRef);
                    } else {
                        obj.put(String.valueOf("User" + attr.getAttrId()),attr.getReferenceValue());
                    }
                }
            }
            array.put(obj);
        }
        result.put("Users", array);
        return result.toString();
    }
    @GET
    @Path("getUsersByField")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String getUsersByField(@QueryParam("login") String login,
                                 @QueryParam("search") String searchField,
                                 @QueryParam("field") String field) {
        
        JSONArray array = new JSONArray();
        List<Integer> usersId =  em.createNativeQuery(
                                    "select atV1.objId " +
                                    "from AttributeValue atV1, " +
                                    "     AttributeValue atV3 " +
                                    "where atV1.attrId = (select attrId " +
                                    "                     from Attributes " +
                                    "                     where name=?) and " +
                                    "      atV3.attrId = (select attrId " +
                                    "                     from Attributes " +
                                    "                     where name='login') and " +
                                    "      atV1.OBJID = atV3.OBJID and " +
                                    "      atV1.STRINGVALUE like ? and " +
                                    "      atV3.STRINGVALUE != ?")
                                    .setParameter(1, field)
                                    .setParameter(2, "%"+searchField+"%")
                                    .setParameter(3,login).getResultList();
        JSONObject obj;
        JSONObject result = new JSONObject();
        result.put("countOfUsers",usersId.size());
        for(Integer objId : usersId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                                .setParameter(1, objId).getResultList();
            
            for (AttributeValue attr : attrs){
                
                if (attr.getNumberValue() != -1) {
                    obj.put("User" + String.valueOf(attr.getAttrId()),attr.getNumberValue());
                } else
                if (!attr.getStringValue().equals("")) {
                    obj.put("User" + String.valueOf(attr.getAttrId()),attr.getStringValue());
                } else
                if (attr.getReferenceValue() != -1) {
                    if (attr.getAttrId() == PHOTO_REF_ID) {
                        String photoRef = (String) em.createNativeQuery("select attrV.stringValue "
                                                            + "from AttributeValue attrV "
                                                            + "where attrV.objId = ? and attrV.attrId = ? ")
                                .setParameter(1, attr.getReferenceValue()).setParameter(2, PATH_FILE_ID).getResultList().get(0);
                        obj.put(String.valueOf("User" + attr.getAttrId()),photoRef);
                    } else {
                        obj.put(String.valueOf("User" + attr.getAttrId()),attr.getReferenceValue());
                    }
                }
            }
            array.put(obj);
        }
        result.put("Users", array);
        return result.toString();
    }
    
    @GET
    @Path("getFriendsByLogin")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String geFriendsByLogin(@Context HttpServletRequest req,
                                 @QueryParam("login") String login) {
        JSONArray array = new JSONArray();
        Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                                                     + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getSingleResult();
        
        
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
            HttpSession session = req.getSession();
            result.put("nickName", session.getAttribute("name"));
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
            
            JSONObject result = new JSONObject();
            result.put("photo", photo);
            return result.toString();
        } catch (SecurityException | IllegalStateException | JSONException ex) {
            return "Exception!!!";
        }
    }
    
    @POST
    @Path("addFriend")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=UTF-8")
    public String addFriend(@FormParam("login") String login,
                            @FormParam("friendId") Integer friendId) {
        JSONObject result = new JSONObject();
        try {
            utx.begin();
            Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                    + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getSingleResult();
            
            AttributeValue attrV = new AttributeValue();
            
            Integer attrValueId = Integer.valueOf(em.createNativeQuery("select MAX(attrValueId) from AttributeValue").getResultList().get(0).toString());
            attrV.setAttrValueId(attrValueId+1);
            attrV.setObjId(objId);
            attrV.setAttrId(FRIEND_REF_ID);
            attrV.setNumberValue(-1);
            attrV.setStringValue("");
            attrV.setDateValue("1970-01-01 00:00:00.000");
            attrV.setBooleanValue(false);
            attrV.setReferenceValue(friendId);
            
            em.persist(attrV);
            utx.commit();
            
            result.put("result","success");
            return result.toString();
        } catch (NotSupportedException| RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            try {
                Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex);
                utx.rollback();
                return result.toString();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex1);
                return result.toString();
            }
        }
        
    }
    
    private String getName(HashMap<Integer,Attribute> attributes) {
        return attributes.get(FIRST_NAME_ID).getValue().toString() + " " 
                 + attributes.get(LAST_NAME_ID).getValue().toString();
    }
    
}
