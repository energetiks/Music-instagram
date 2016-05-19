package ru.bystrov.musicinstagram.service;

import com.google.gson.Gson;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo; 

@Path("file")
public class FileResource {
    
    @Context
    private UriInfo context;
    @PersistenceContext(unitName = "MusicInstagramPU")
    private EntityManager em;
    @Resource
    UserTransaction utx;
    
    @GET
    @Path("getPathById")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPathById(@QueryParam("id") String fileId) {
        try {
            Object opath = em.createNativeQuery(
                    "select stringValue " +
                    "from attributevalue " +
                    "where attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot " +
                    "where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'path' and ot.name = 'File')" +
                    "and ? = objid")
                    .setParameter(1, fileId).getSingleResult();
            return (new Gson()).toJson(opath);
        } catch (NoResultException | NonUniqueResultException e) {
            return (new Gson()).toJson("File not found");
        }
    }
    
    
}
