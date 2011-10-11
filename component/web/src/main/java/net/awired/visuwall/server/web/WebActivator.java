package net.awired.visuwall.server.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class WebActivator implements BundleActivator {

    private static final String STATIC = "/visuwall";

    HttpService service;

    @Override
    public void start(BundleContext context) throws Exception {
        ServiceReference sRef = context.getServiceReference(HttpService.class.getName());
        service = (HttpService) context.getService(sRef);
        service.registerServlet("/", new VisuwallResource(), null, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        service.unregister(STATIC);
    }

}
