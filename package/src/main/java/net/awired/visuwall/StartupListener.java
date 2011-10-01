package net.awired.visuwall;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.awired.bootstrap.karaf.KarafService;

public final class StartupListener implements ServletContextListener {

    private KarafService service;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String rootString = servletContext.getRealPath("/WEB-INF/karaf");
        File root = new File(rootString);
        if (!root.exists()) {
            throw new RuntimeException("Karaf root folder not found : " + rootString + ". If path is a context,"
                    + " your servlet container do not unpack war. "
                    + "Non unpacked wars servlet containers is currently not supported");
        }
        this.service = new KarafService(root);
        service.setServletContext(servletContext);
        this.service.start();

        //        } catch (IOException e) {
        //            throw new RuntimeException("Karaf root folder : '" + resource
        //                    + "' is not a file, ");
        //        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.service.stop();
    }
}
