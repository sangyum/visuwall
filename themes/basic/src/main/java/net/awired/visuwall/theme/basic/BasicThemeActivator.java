package net.awired.visuwall.theme.basic;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class BasicThemeActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("salutttt!!!");
        ServiceReference sRef = context.getServiceReference(HttpService.class.getName());
        if (sRef != null) {
            HttpService service = (HttpService) context.getService(sRef);
            service.registerResources("/res/js", "/js", null);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ServiceReference sRef = context.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) context.getService(sRef);
        service.unregister("/res/js");
    }

}
