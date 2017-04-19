package no.npolar.common.forms;

//import java.sql.SQLException;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
//import org.opencms.main.CmsEventManager;
import org.opencms.main.OpenCms;
import org.opencms.main.CmsException;
import org.opencms.module.CmsModule;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.I_CmsReport;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsResource;
//import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsProject;
//import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.loader.CmsLoaderException;
import org.opencms.file.CmsObject;
//import org.opencms.file.CmsFile;
//import org.apache.commons.logging.Log;
//import org.opencms.main.CmsLog;
//import javax.servlet.ServletException;
//import java.util.List;

//import no.npolar.util.exception.DeleteResourceException;
//import no.npolar.util.exception.MalformedPropertyValueException;
//import no.npolar.util.exception.MissingPropertyException;
//import no.npolar.util.exception.PublishException;

/**
 * Actions and event listeners for the no.npolar.common.forms module.
 * <p>
 * <strong>Experimental</strong>.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class FormActions implements I_CmsModuleAction, I_CmsEventListener {
    /** Stores the CmsObject passed to initialize(...). */
    private CmsObject adminCmso;
    
    public void initialize(CmsObject adminCmso, CmsConfigurationManager configurationManager, CmsModule module) {
        // Do nothing
        OpenCms.getEventManager().addCmsEventListener(this);
        this.adminCmso = adminCmso;
    }
    
    public void moduleUninstall(CmsModule module) {
        // Do nothing
    }
    
    public void moduleUpdate(CmsModule module) {
        // Do nothing
    }
    
    public void publishProject(CmsObject cms, CmsPublishList publishList, int publishTag, I_CmsReport report) {
        // Do nothing
    }
    
    public void shutDown(CmsModule module) {
        // Do nothing
    }
    
    public void cmsEvent(CmsEvent e) {
        if (e.getType() == I_CmsEventListener.EVENT_RESOURCE_DELETED) {
            //e.getType() == I_CmsEventListener.EVENT_RESOURCE_CREATED
            //e.getType() == I_CmsEventListener.EVENT_RESOURCE_COPIED
            //e.getType() == I_CmsEventListener.EVENT_RESOURCE_MODIFIED
            //e.getType() == I_CmsEventListener.EVENT_RESOURCE_MOVED
            
            // Get the source resource
            CmsResource r = (CmsResource)e.getData().get(I_CmsEventListener.KEY_RESOURCE);//"resource"
            if (r != null) {
                try {
                    int formTypeId = OpenCms.getResourceManager().getResourceType("np_form").getTypeId();
                    if (r.getTypeId() == formTypeId) {
                        // Delete the DB table:
                        /*
                        CmsDefaultUsers defaultUsers = new CmsDefaultUsers();
                        CmsObject cmso = null;
                        CmsUser user = null;
                        CmsProject project = null;
                        //*/
                        try {
                            /*
                            cmso = OpenCms.initCmsObject(defaultUsers.getUserGuest());
                            user = cmso.getRequestContext().currentUser();
                            //cmso.loginUser("theuser", "thepassword");
                            project = cmso.getRequestContext().setCurrentProject(cmso.readProject("Offline"));
                            //*/
                            adminCmso.getRequestContext().setCurrentProject(adminCmso.readProject("Offline"));
                            //adminCmso.getRequestContext().setSiteRoot(OpenCms.initCmsObject(defaultUsers.getUserGuest()).getRequestContext().getSiteRoot());
                            //cmso.getRequestContext().setSiteRoot("/sites/default");
                        } catch (CmsException cmse) {
                            throw new  NullPointerException("Error initializing CmsObject / user / project upon capturing event 'resource deleted': " + cmse.getMessage());
                        }

                        try {
                            Form form = new Form(adminCmso.getSitePath(r), adminCmso);
                            form.deleteData();
                        } catch (Exception dele) {
                            throw new NullPointerException("An error occurred when attempting to delete form data for \"" + r.getRootPath() + "\": " + dele.getMessage());
                        }
                    } // if (resource type == form)
                } catch (CmsLoaderException cle) {
                    throw new NullPointerException("An error occurred when attempting to read the resource ID for type \"np_form\": " + cle.getMessage());
                }
            } // if (r != null)
        } // if (event == resource deleted)
    }

}
