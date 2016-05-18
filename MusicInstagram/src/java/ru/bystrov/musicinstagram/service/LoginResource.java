/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.bystrov.musicinstagram.service;

import java.util.List;
import javax.annotation.Resource;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import ru.bystrov.musicinstagram.cryptography.PasswordHash;

/**
 *
 * @author defesteban
 */

@Path("user")
@SessionScoped
public class LoginResource {
    
    @Context
    private UriInfo context;
    @PersistenceContext(unitName = "MusicInstagramPU")
    private EntityManager em;
    @Resource
    UserTransaction utx;
    
    final private int LOGIN_ID = 8;
    final private int PASSWORD_ID = 9;
    final private int SALT_ID = 39;
    
    @POST
    @Path("login")
    public String login(@Context HttpServletRequest req,
                        @FormParam("username") String username,
                        @FormParam("password") String password) {
        
        String login = username.toLowerCase();
        String result = checkUser(login, password);
        
        if (result.equals("Success")) {
            HttpSession session = req.getSession(true);
            session.setAttribute("name", login);
        }

        return "\"" + result + "\"";
    }
    
    @POST
    @Path("exit")
    public void exit(@Context HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.invalidate();
    }
    
    @GET
    @Path("getSession")
    public String getSession(@Context HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null)
            return "fail";
        return "success";
    }
    
    public Integer getUserIdByLogin(String login) {
        List elements = em.createNativeQuery("select objId from AttributeValue where attrId = ? and stringValue = ?")
                .setParameter(1, LOGIN_ID).setParameter(2, login).getResultList();
        if (elements.isEmpty())
            return null;
        return (Integer) elements.get(0);
    }
    
    public String getUserPasswordByID(Integer id) {
        
        return (String) em.createNativeQuery("SELECT stringValue FROM AttributeValue WHERE objId = ? and attrId = ?")
                .setParameter(1, id).setParameter(2, PASSWORD_ID).getResultList().get(0);
    }
    
    public String getUserSaltByID(Integer id) {
        
        return (String) em.createNativeQuery("SELECT stringValue FROM AttributeValue WHERE objId = ? and attrId = ?")
                .setParameter(1, id).setParameter(2, SALT_ID).getResultList().get(0);
    }
    
    public String checkUser(String login, String password) {
        
        if (login == null)
            return "System Error";
        
        Integer userId = getUserIdByLogin(login);
        if (userId == null) {
            return "Invalid login or password";
        }
        
        PasswordHash passwordHash = new PasswordHash();
        String salt = getUserSaltByID(userId);
        
        if (salt == null)
            return "Error in database";

        String saltPassword = passwordHash.getSecurePassword(password, salt);
        
        if (!saltPassword.equals(getUserPasswordByID(userId)))
            return "Invalid login or password";

        return "Success";
    }
    
}
