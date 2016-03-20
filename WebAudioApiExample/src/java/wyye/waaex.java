/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wyye;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * REST Web Service
 *
 * @author ad
 */
@Path("generic")
public class waaex {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of waaex
     */
    public waaex() {
    }

    /*
     * http://localhost:8080/WebAudioApiExample/webresources/generic/upload
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(
            @FormDataParam("uploadedFile") InputStream uploadedInputStream,
            @FormDataParam("fileName") String fileName
    ) throws FileNotFoundException, IOException {
        String niceHardCodedPath = "/home/ad/NetBeansProjects/WebAudioApiExample/web/upload/".concat(fileName);
        writeToFile(uploadedInputStream, niceHardCodedPath);
        return (new Gson()).toJson("Written to server disk");
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
}
