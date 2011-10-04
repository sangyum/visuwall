package net.awired.visuwall;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.awired.ajsl.core.io.FileUtils;
import net.awired.ajsl.core.servlet.ContextUtils;
import net.awired.bootstrap.karaf.KarafService;

public final class StartupListener implements ServletContextListener {

    private static final String KARAF_RESOURCE_HOME = "/WEB-INF/karaf";

    private KarafService service;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        File tempDir = FileUtils.createTempDirectoryWithDeleteOnExit("karaf_home");
        String karafRoot = tempDir + KARAF_RESOURCE_HOME;
        try {
            ContextUtils.servletContextResourceToFile(event.getServletContext(), KARAF_RESOURCE_HOME,
                    tempDir.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy karaf home resource to temp directory", e);
        }
        this.service = new KarafService(karafRoot);
        service.setServletContext(event.getServletContext());
        this.service.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.service.stop();
    }
}
