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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    final private int FILE_TYPE_ID = 5;

    final private int LOGIN_ID = 8;
    
    final private int PATH_FILE_ID = 27;
    final private int TYPE_FILE_ID = 28;
    public SourceResource() {
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String upload(
            @FormDataParam("newSource") InputStream uploadedInputStream,
            @FormDataParam("fileName") String fileName,
            @FormDataParam("duration") Integer duration,
            @FormDataParam("genre") String genre,
            @FormDataParam("language") String language,
            @FormDataParam("albumName") String albumName,
            @FormDataParam("year") Integer year,
            @FormDataParam("musicalGroupName") String musicalGroupName,
            @FormDataParam("login") String login
    ) throws FileNotFoundException, IOException {
        try {
            JSONObject result = new JSONObject();
            Integer objId = (Integer) em.createNativeQuery("select objId from AttributeValue "
                    + "where stringValue = ? and attrId = ? ")
                    .setParameter(1, login).setParameter(2, LOGIN_ID).getResultList().get(0);
            
            java.sql.Timestamp time = (java.sql.Timestamp) em.createNativeQuery("select CURRENT_TIMESTAMP from Objects").getResultList().get(0);
            
            HashMap<Integer,Attribute> attrValue = new HashMap<>();
            attrValue.put(NAME_ID,new Attribute("string", fileName));
            //attrValue.put(DURATION_ID,new Attribute("int", duration));
            attrValue.put(GENRE_ID,new Attribute("string", genre));
            attrValue.put(LANGUAGE_ID,new Attribute("string", language));
            attrValue.put(ALBUM_ID,new Attribute("string", albumName));
            attrValue.put(YEAR_ID,new Attribute("string", year));
            attrValue.put(MUSICAL_GROUP_NAME_ID,new Attribute("string", musicalGroupName));
            attrValue.put(USER_REF_ID,new Attribute("reference", objId));
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Fake code simulating the copy
            // You can generally do better with nio if you need...
            // And please, unlike me, do something about the Exceptions :D
            byte[] buffer = new byte[1024];
            int len;
            while ((len = uploadedInputStream.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            // Open new InputStreams using the recorded bytes
            // Can be repeated as many times as you wish
            InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
            
            
            
            utx.begin();
            Objects newSource  = new Objects();
            Objects newSourceFile  = new Objects();
            
            
            Integer id = Integer.valueOf(em.createNativeQuery("select MAX(objId) from Objects").getResultList().get(0).toString());
            newSource.setObjId(id+1);
            newSource.setName("Source" + fileName);
            newSource.setObjTypeId(SOURCE_TYPE_ID);
            
            newSourceFile.setObjId(id+2);
            newSourceFile.setName("Source File " + fileName);
            newSourceFile.setObjTypeId(FILE_TYPE_ID);
            
            attrValue.put(FILE_REF_ID,new Attribute("reference", id+2));
            
            em.persist(newSource);
            em.persist(newSourceFile);
            MainResource resource = new MainResource();
            resource.addAttributes(attrValue, newSource, em);
            
           
            attrValue = new HashMap<>();
            String filename = GetHash(is1);
            
            String pathToSource = "C:\\Users\\Test\\Desktop\\Programms on java\\MusicInstagram\\Music-instagram\\MusicInstagram\\web\\upload\\".concat(filename);
            attrValue.put(PATH_FILE_ID,new Attribute("string", filename));
            resource = new MainResource();
            resource.addAttributes(attrValue, newSourceFile,em);
            
            utx.commit();
            writeToFile(is2, pathToSource);
            return "Written to server disk";
        } catch (NoSuchAlgorithmException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            return "Error Written to server disk";
        }
    }
    
    static String GetHash(InputStream is) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        
        
        byte[] dataBytes = new byte[1024];
 
        int nread = 0; 
        while ((nread = is.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        };
        byte[] mdbytes = md.digest();
 
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        
        return sb.toString();
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

    @GET
    @Path("deleteSource")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
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
            List<AttributeValue> attributes = em.createNativeQuery("select * from AttributeValue where objId = ?", AttributeValue.class)
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getSources() {
        JSONArray array = new JSONArray();
        List<Integer> usersId = em.createNativeQuery("select o.objId from Objects o, ObjectTypes OT "
                + " where o.objTypeId = OT.otid and"
                + " OT.name = ?")
                .setParameter(1, "Source").getResultList();
        JSONObject obj;
        List<AttributeValue> attrs;
        for (int objId : usersId) {
            obj = new JSONObject();
            attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                    .setParameter(1, objId).getResultList();

            for (AttributeValue attr : attrs) {
                if (attr.getNumberValue() != -1) {
                    obj.put(String.valueOf(attr.getAttrId()), attr.getNumberValue());
                } else if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf(attr.getAttrId()), attr.getStringValue());
                } else if (attr.getReferenceValue() != -1) {
                    obj.put(String.valueOf(attr.getAttrId()), attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
    }

    @GET
    @Path("getSourceById")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getSourceById(@Context HttpServletRequest req,
            @QueryParam("objId") int objId) {
        List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                .setParameter(1, objId).getResultList();
        JSONObject obj = new JSONObject();
        for (AttributeValue attr : attrs) {
            if (attr.getNumberValue() != -1) {
                obj.put(String.valueOf(attr.getAttrId()), attr.getNumberValue());
            } else if (!attr.getStringValue().equals("")) {
                obj.put(String.valueOf(attr.getAttrId()), attr.getStringValue());
            } else if (attr.getReferenceValue() != -1) {
                obj.put(String.valueOf(attr.getAttrId()), attr.getReferenceValue());
            }
        }
        return obj.toString();

    }

    @GET
    @Path("getSourcesByName")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getSourcebyGenre(@Context HttpServletRequest req,
            @QueryParam("genre") String genre) {
        List<Integer> sourcesID = em.createNativeQuery("select attrV.objId "
                + "from AttributeValue attrV "
                + "where attrV.stringValue = ? and "
                + "attrV.attrId = ?")
                .setParameter(1, genre).setParameter(2, GENRE_ID).getResultList();
        return findSMTHbyParameter(sourcesID);
    }

    public String findSMTHbyParameter(List<Integer> sourcesId) {
        JSONArray array = new JSONArray();
        JSONObject obj;
        for (int objId : sourcesId) {
            obj = new JSONObject();
            List<AttributeValue> attrs = em.createNativeQuery("select * from AttributeValue attrV where attrV.objId = ?", AttributeValue.class)
                    .setParameter(1, objId).getResultList();

            for (AttributeValue attr : attrs) {
                if (attr.getNumberValue() != -1) {
                    obj.put(String.valueOf(attr.getAttrId()), attr.getNumberValue());
                } else if (!attr.getStringValue().equals("")) {
                    obj.put(String.valueOf(attr.getAttrId()), attr.getStringValue());
                } else if (attr.getReferenceValue() != -1) {
                    obj.put(String.valueOf(attr.getAttrId()), attr.getReferenceValue());
                }
            }
            array.put(obj);
        }
        return array.toString();
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
                    + "where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'file_ref' and ot.name = 'Source')"
                    + "and file.attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot "
                    + "where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'path' and ot.name = 'File')"
            )
                    .setParameter(1, sourceId).getSingleResult();
            return (new Gson()).toJson(opath);
        } catch (NoResultException | NonUniqueResultException e) {
            return (new Gson()).toJson("File not found");
        }
    }
    
    @GET
    @Path("getSourceBySubstrName")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSourceBySubstrName(@QueryParam("name") String substr) {
        try {
            Object result = em.createNativeQuery(
"select name.objid, name.stringvalue, path.objid\n" + 
"from attributevalue path, attributevalue file_ref, attributevalue name\n" +
"where file_ref.REFERENCEVALUE = path.objid\n" +
"and file_ref.objid = name.objid\n" +
"and file_ref.attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot\n" +
"where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'file_ref' and ot.name = 'Source')\n" +
"and path.attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot \n" +
"where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'path' and ot.name = 'File')\n" +
"and name.attrid = (select a.attrid from attributes a, attributeobjecttypes aot, objecttypes ot\n" +
"where aot.attrid = a.attrid and aot.otid = ot.otid and a.name = 'name' and ot.name = 'Source')\n" +
"and name.stringvalue like ?"
            )
                    .setParameter(1, "%" + substr + "%").getResultList();
            return (new Gson()).toJson(result);
        } catch (NoResultException | NonUniqueResultException e) {
            return (new Gson()).toJson("File not found");
        }
    }
    
}
