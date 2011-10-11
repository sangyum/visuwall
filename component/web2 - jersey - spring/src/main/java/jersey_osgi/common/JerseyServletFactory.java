package jersey_osgi.common;

import java.util.Hashtable;
import javax.annotation.PostConstruct;
import javax.servlet.Servlet;
import net.awired.visuwall.web.JerseyApplication;
import net.awired.visuwall.web.SpringedJerseyServlet;
import org.osgi.service.http.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class JerseyServletFactory {

    @Autowired
    HttpService httpService;

    Servlet servlet;
    //    String classNames;
    String rootContext;

    public void setRootContext(String rootContext) {
        this.rootContext = rootContext;
    }

    @PostConstruct
    void postconstruct() throws Exception {
        Hashtable<String, String> initParams = new Hashtable<String, String>();
        //        initParams.put("com.sun.jersey.config.property.packages", "jersey_osgi");
        //        initParams.put("com.sun.jersey.config.property.resourceConfigClass", "jersey_osgi.common.OSGiResourceConfig");
        //        initParams.put("com.sun.jersey.config.property.resourceConfigClass",
        //                "com.sun.jersey.api.core.ClassnamesResourceConfig");
        //        ClassNamesResourceConfig d = new ClassNamesResourceConfig(NumberResource.class.getName());
        //        d.equals(null);
        //        initParams.put("com.sun.jersey.config.property.classnames", NumberResource.class.getName());

        ServletContainer jerseyServlet = new ServletContainer(new JerseyApplication());
        //        servlet = new SpringServlet();

        servlet = new SpringedJerseyServlet(new JerseyApplication());

        httpService.registerServlet(rootContext, servlet, initParams, null);
        System.out.println("Registered servlet at: " + rootContext);
        System.out.println("With initialization  : " + initParams);
    }

}
