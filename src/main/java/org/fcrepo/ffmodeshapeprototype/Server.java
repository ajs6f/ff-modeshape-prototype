/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fcrepo.ffmodeshapeprototype;


import org.infinispan.schematic.document.ParsingException;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cabeer
 */
 
 
@Path("/")
public class Server {
    private ModeShapeEngine engine;
    private JcrRepository repository;
    private static Logger logger = LoggerFactory.getLogger(org.fcrepo.ffmodeshapeprototype.Server.class);
   
    public Server() throws Exception, ConfigurationException, RepositoryException {
                RepositoryConfiguration repository_config = null;
        try {
            repository_config = RepositoryConfiguration.read("my_repository.json");
            Problems problems = repository_config.validate();
            
            if (problems.hasErrors()) {
                System.err.println("Problems starting the engine.");
                System.err.println(problems);
            throw new Exception("Problems starting the engine.");
            }
            
        } catch (ParsingException ex) {
            logger.error(null, ex);
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        }
        
        
        this.engine = new ModeShapeEngine();
        
        if(this.engine == null || repository_config == null) {
            throw new Exception("Missing engine");
        }
        
        engine.start();
        this.repository = engine.deploy(repository_config);
    }
    
    @GET
    @Path("/describe")
    public Response describe() {
        return Response.status(200).entity(this.repository.getName()).build();
    } 
    
    @POST
    @Path("/objects/{pid}")
    public Response ingest(@PathParam("pid") String pid) throws RepositoryException {

        this.logger.debug("Running ingest");

        JcrSession session = this.repository.login();
      
        Node root = session.getRootNode();
        if(session.hasPermission("/" + pid, "add_node")) {
            Node obj = root.addNode(pid);
            session.save();
            session.logout();

            System.out.println("added " + obj.getIdentifier());
            session = this.repository.login();
            root = session.getRootNode();
            
            return Response.status(200).entity(root.getNode(pid).toString()).build();
        } else {
            return Response.status(401).entity("NO!").build();
        }
    }
    
    @GET
    @Path("/object/{pid}")
    public Response getObject(@PathParam("pid") String pid) throws RepositoryException { 
        JcrSession session = this.repository.login();
        Node root = session.getRootNode();

        return Response.status(200).entity(root.getNode(pid).toString()).build();
    }
}
