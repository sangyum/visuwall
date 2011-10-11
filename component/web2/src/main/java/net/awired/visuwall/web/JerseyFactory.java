package net.awired.visuwall.web;

import java.util.Hashtable;
import javax.servlet.Servlet;
import org.osgi.service.http.HttpService;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class JerseyFactory {
    private String classNames;
    private String rootContext;

    public JerseyFactory() {
        System.out.println("HERE");
    }

    // Spring uses the setters to inject our stuff
    public void setJerseyClassNames(String names) {
        classNames = names;
    }

    public void setRootContext(String ctx) {
        rootContext = ctx;
    }

    public void setHttpService(HttpService httpService) throws Exception {
        Hashtable<String, String> initParams = new Hashtable<String, String>();
        //        initParams.put("com.sun.ws.rest.config.property.resourceConfigClass",
        //                "net.awired.visuwall.web.OSGiResourceConfig");
        //        initParams.put("jersey_osgi.classnames", classNames);

        Servlet jerseyServlet = new ServletContainer();
        httpService.registerServlet(rootContext, jerseyServlet, initParams, null);
    }
}
