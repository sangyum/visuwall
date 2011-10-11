package jersey_osgi.common;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Servlet;
import net.awired.visuwall.web.JerseyApplication;
import net.awired.visuwall.web.SpringedJerseyServlet;
import org.osgi.service.http.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class JerseyServletFactory {

    @Autowired
    HttpService httpService;

    @Autowired
    ApplicationContext applicationContext;

    Servlet servlet;

    //    @Value(value = "${authentication.home.domain}")
    String rootContext;

    public void setRootContext(String rootContext) {
        this.rootContext = rootContext;
    }

    @PostConstruct
    void postConstruct() throws Exception {
        servlet = new SpringedJerseyServlet(new JerseyApplication(), applicationContext);

        httpService.registerServlet(rootContext, servlet, null, null);
        System.out.println("Registered servlet at: " + rootContext);
    }

    @PreDestroy
    void preDestroy() {
    }

}
