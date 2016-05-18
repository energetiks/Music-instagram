/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 *
 * @author Test
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        
        // !!!!!!!!!!!!!!!!!!!! NEXT LINE IS VERY IMPORTANT FOR FILE UPLOADING !!!!!!!!!!!!!!!!!!!!
        resources.add(MultiPartFeature.class);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! END OF VERY IMPORTANT LINE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(ru.bystrov.musicinstagram.service.DirectoryService.class);
        resources.add(ru.bystrov.musicinstagram.service.MainResource.class);
        resources.add(ru.bystrov.musicinstagram.service.SampleResource.class);
        resources.add(ru.bystrov.musicinstagram.service.SourceResource.class);
        resources.add(ru.bystrov.musicinstagram.service.UserResource.class);
    }
    
}
