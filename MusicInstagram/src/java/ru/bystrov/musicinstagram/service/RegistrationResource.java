/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.service;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.faces.bean.SessionScoped;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.json.JSONException;
import ru.bystrov.musicinstagram.cryptography.PasswordHash;
import ru.bystrov.musicinstagram.entities.AttributeValue;
import ru.bystrov.musicinstagram.entities.Objects;
import ru.bystrov.musicinstagram.entities.dataobject.Attribute;

/**
 *
 * @author defesteban
 */
@Path("registration")
@SessionScoped
public class RegistrationResource {

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
    final private int SALT_ID = 39;
    
    
    final private int PATH_FILE_ID = 27;
    final private int TYPE_FILE_ID = 28;
    
    final private int USER_TYPE_ID = 1;
    final private int FILE_TYPE_ID = 5;

    public RegistrationResource() {
    }

    @POST
    @Path("registration")
    public String registration(@Context HttpServletRequest req,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("confirmed_password") String confirmed_password) {

        String result = checkUser(username.toLowerCase(), password, confirmed_password);

        return "\"" + result + "\"";
    }

    public Integer getUserIdByLogin(String login) {
        List elements = em.createNativeQuery("select objId from AttributeValue where attrId = ? and stringValue = ?")
                .setParameter(1, LOGIN_ID).setParameter(2, login).getResultList();
        if (elements.isEmpty()) {
            return null;
        }
        return (Integer) elements.get(0);
    }

    private String getName(HashMap<Integer, Attribute> attributes) {
        return attributes.get(FIRST_NAME_ID).getValue().toString() + " "
                + attributes.get(LAST_NAME_ID).getValue().toString();
    }

    public Boolean createUser(String login, String password) {

        PasswordHash passwordHash = new PasswordHash();
        String salt;
        String securePassword;

        HashMap<Integer, Attribute> attrValue = new HashMap<>();
        attrValue.put(FIRST_NAME_ID, new Attribute("string", "User"));
        attrValue.put(LAST_NAME_ID, new Attribute("string", ""));
        attrValue.put(AGE_ID, new Attribute("int", 0));
        attrValue.put(COUNTRY_ID, new Attribute("string", ""));
        attrValue.put(CITY_ID, new Attribute("string", ""));
        attrValue.put(UNIVERCITY_ID, new Attribute("string", ""));
        attrValue.put(PHONE_NUMBER_ID, new Attribute("string", ""));
        attrValue.put(LOGIN_ID, new Attribute("string", login));
        try {

            salt = passwordHash.getSalt();
            securePassword = passwordHash.getSecurePassword(password, salt);

            attrValue.put(PASSWORD_ID, new Attribute("string", securePassword));
            attrValue.put(SALT_ID, new Attribute("string", salt));

            utx.begin();

            Objects newUser = new Objects();
            Objects newPhotoFile = new Objects();

            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newUser.setObjId(id + 1);
            newUser.setName("User " + getName(attrValue));
            newUser.setObjTypeId(USER_TYPE_ID);

            newPhotoFile.setObjId(id + 2);
            newPhotoFile.setName("Photo File " + getName(attrValue));
            newPhotoFile.setObjTypeId(FILE_TYPE_ID);

            attrValue.put(PHOTO_REF_ID, new Attribute("reference", id + 2));

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
                    case "string":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setReferenceValue(-1);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int) entry.getValue().getValue());
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

            //CREATE A FILE OBJECT!!!
            attrValue = new HashMap<>();
            attrValue.put(PATH_FILE_ID, new Attribute("string", "images/userPhoto/user.jpg"));
            attrValue.put(TYPE_FILE_ID, new Attribute("string", "jpg"));
            for (Map.Entry<Integer, Attribute> entry : attrValue.entrySet()) {
                newAttrValue = new AttributeValue();
                id = id + 1;
                newAttrValue.setAttrValueId(id);
                newAttrValue.setAttrId(entry.getKey());
                newAttrValue.setObjId(newPhotoFile.getObjId());
                switch (entry.getValue().getType()) {
                    case "string":
                        newAttrValue.setStringValue(String.valueOf(entry.getValue().getValue()));
                        newAttrValue.setNumberValue(-1);
                        newAttrValue.setReferenceValue(-1);
                        break;
                    case "int":
                        newAttrValue.setNumberValue((int) entry.getValue().getValue());
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

            utx.commit();
            return true;
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | JSONException | NoSuchAlgorithmException ex) {
            ex.printStackTrace(System.err);
            try {
                utx.rollback();
                return false;
            } catch (IllegalStateException | SecurityException | SystemException | JSONException exc) {
                exc.printStackTrace(System.err);
                return false;
            }
        }
    }

    private String checkUser(String login, String password, String confirmedPassword) {

        if (getUserIdByLogin(login) != null) {
            return "This login is already exist!";
        }
        if (login == null || password == null) {
            return "System Error! Password or login is null!";
        }
        if (login.length() < 2 || login.length() > 20) {
            return "Login should contain more than 1 characters and less than 20!";
        }
        if (password.length() < 5 || password.length() > 20) {
            return "Password should contain more than 4 characters and less than 20!";
        }
        if (!password.equals(confirmedPassword)) {
            return "Passwords are not equal!";
        }
        if (!createUser(login, password)) {
            return "System error! Can't create user!";
        }

        return "Login: " + login + " successfully registered!";
    }

}
