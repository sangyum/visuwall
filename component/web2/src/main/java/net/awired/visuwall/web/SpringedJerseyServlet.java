package net.awired.visuwall.web;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;

@Component
public class SpringedJerseyServlet extends ServletContainer {

    private static final long serialVersionUID = 5686655395749077671L;

    private static final Logger LOGGER = Logger.getLogger(SpringedJerseyServlet.class.getName());

    @Autowired
    private ApplicationContext context;

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig)
            throws ServletException {
        return new DefaultResourceConfig();
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        try {
            wa.initiate(rc, new SpringComponentProviderFactory(rc, (ConfigurableApplicationContext) context));
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Exception occurred when intialization", e);
            throw e;
        }
    }
}
