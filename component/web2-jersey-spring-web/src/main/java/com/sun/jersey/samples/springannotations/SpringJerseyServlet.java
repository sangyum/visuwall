package com.sun.jersey.samples.springannotations;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;

public class SpringJerseyServlet extends ServletContainer {

    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Set of required properties (Strings) that must be supplied as
     * config parameters to this servlet.
     */
    private final Set<String> requiredProperties = new HashSet<String>();

    /**
     * Subclasses can invoke this method to specify that this property
     * (which must match a JavaBean property they expose) is mandatory,
     * and must be supplied as a config parameter. This should be called
     * from the constructor of a subclass.
     * <p>
     * This method is only relevant in case of traditional initialization driven by a ServletConfig instance.
     * 
     * @param property
     *            name of the required property
     */
    protected final void addRequiredProperty(String property) {
        this.requiredProperties.add(property);
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig)
            throws ServletException {
        return new DefaultResourceConfig();
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing servlet '" + getServletName() + "'");
        }

        // Set bean properties from init parameters.
        try {
            PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
            bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader));
            initBeanWrapper(bw);
            bw.setPropertyValues(pvs, true);

            // Let subclasses do whatever initialization they like.
            initServletBean();
        } catch (Exception ex) {
            logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
            throw new RuntimeException(ex);
        }

        try {
            wa.initiate(rc, new SpringComponentProviderFactory(rc,
                    (ConfigurableApplicationContext) initWebApplicationContext()));
        } catch (RuntimeException e) {
            logger.error("Exception occurred when intialization", e);
            throw e;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Servlet '" + getServletName() + "' configured successfully");
        }
    }

    /**
     * Initialize the BeanWrapper for this HttpServletBean,
     * possibly with custom editors.
     * <p>
     * This default implementation is empty.
     * 
     * @param bw
     *            the BeanWrapper to initialize
     * @throws BeansException
     *             if thrown by BeanWrapper methods
     * @see org.springframework.beans.BeanWrapper#registerCustomEditor
     */
    protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
    }

    /**
     * Overridden method that simply returns <code>null</code> when no
     * ServletConfig set yet.
     * 
     * @see #getServletConfig()
     */
    @Override
    public final String getServletName() {
        return (getServletConfig() != null ? getServletConfig().getServletName() : null);
    }

    /**
     * Overridden method that simply returns <code>null</code> when no
     * ServletConfig set yet.
     * 
     * @see #getServletConfig()
     */
    @Override
    public final ServletContext getServletContext() {
        return (getServletConfig() != null ? getServletConfig().getServletContext() : null);
    }

    /**
     * PropertyValues implementation created from ServletConfig init parameters.
     */
    private static class ServletConfigPropertyValues extends MutablePropertyValues {

        /**
         * Create new ServletConfigPropertyValues.
         * 
         * @param config
         *            ServletConfig we'll use to take PropertyValues from
         * @param requiredProperties
         *            set of property names we need, where
         *            we can't accept default values
         * @throws ServletException
         *             if any required properties are missing
         */
        public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties)
                throws ServletException {

            Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ? new HashSet<String>(
                    requiredProperties) : null;

            Enumeration en = config.getInitParameterNames();
            while (en.hasMoreElements()) {
                String property = (String) en.nextElement();
                Object value = config.getInitParameter(property);
                addPropertyValue(new PropertyValue(property, value));
                if (missingProps != null) {
                    missingProps.remove(property);
                }
            }

            // Fail if we are still missing properties.
            if (missingProps != null && missingProps.size() > 0) {
                throw new ServletException("Initialization from ServletConfig for servlet '"
                        + config.getServletName() + "' failed; the following required properties were missing: "
                        + StringUtils.collectionToDelimitedString(missingProps, ", "));
            }
        }
    }

    ////////////////////////////////////////////////////////////:
    ///////////////////////////////////////////:
    //////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////

    /**
     * Suffix for WebApplicationContext namespaces. If a servlet of this class is
     * given the name "test" in a context, the namespace used by the servlet will
     * resolve to "test-servlet".
     */
    public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

    /**
     * Default context class for FrameworkServlet.
     * 
     * @see org.springframework.web.context.support.XmlWebApplicationContext
     */
    public static final Class DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

    /**
     * Prefix for the ServletContext attribute for the WebApplicationContext.
     * The completion is the servlet name.
     */
    public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

    /** ServletContext attribute to find the WebApplicationContext in */
    private String contextAttribute;

    /** WebApplicationContext implementation class to create */
    private Class contextClass = DEFAULT_CONTEXT_CLASS;

    /** Namespace for this servlet */
    private String namespace;

    /** Explicit context config location */
    private String contextConfigLocation;

    /** Should we publish the context as a ServletContext attribute? */
    private boolean publishContext = true;

    /** Should we publish a ServletRequestHandledEvent at the end of each request? */
    private boolean publishEvents = true;

    /** Expose LocaleContext and RequestAttributes as inheritable for child threads? */
    private boolean threadContextInheritable = false;

    /** Should we dispatch an HTTP OPTIONS request to {@link #doService}? */
    private boolean dispatchOptionsRequest = false;

    /** Should we dispatch an HTTP TRACE request to {@link #doService}? */
    private boolean dispatchTraceRequest = false;

    /** WebApplicationContext for this servlet */
    private WebApplicationContext webApplicationContext;

    /** Flag used to detect whether onRefresh has already been called */
    private boolean refreshEventReceived = false;

    /**
     * Set the name of the ServletContext attribute which should be used to retrieve the {@link WebApplicationContext}
     * that this servlet is supposed to use.
     */
    public void setContextAttribute(String contextAttribute) {
        this.contextAttribute = contextAttribute;
    }

    /**
     * Return the name of the ServletContext attribute which should be used to retrieve the
     * {@link WebApplicationContext} that this servlet is supposed to use.
     */
    public String getContextAttribute() {
        return this.contextAttribute;
    }

    /**
     * Set a custom context class. This class must be of type
     * {@link org.springframework.web.context.WebApplicationContext}.
     * <p>
     * When using the default FrameworkServlet implementation, the context class must also implement the
     * {@link org.springframework.web.context.ConfigurableWebApplicationContext} interface.
     * 
     * @see #createWebApplicationContext
     */
    public void setContextClass(Class contextClass) {
        this.contextClass = contextClass;
    }

    /**
     * Return the custom context class.
     */
    public Class getContextClass() {
        return this.contextClass;
    }

    /**
     * Set a custom namespace for this servlet,
     * to be used for building a default context config location.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Return the namespace for this servlet, falling back to default scheme if
     * no custom namespace was set: e.g. "test-servlet" for a servlet named "test".
     */
    public String getNamespace() {
        return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
    }

    /**
     * Set the context config location explicitly, instead of relying on the default
     * location built from the namespace. This location string can consist of
     * multiple locations separated by any number of commas and spaces.
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * Return the explicit context config location, if any.
     */
    public String getContextConfigLocation() {
        return this.contextConfigLocation;
    }

    /**
     * Set whether to publish this servlet's context as a ServletContext attribute,
     * available to all objects in the web container. Default is "true".
     * <p>
     * This is especially handy during testing, although it is debatable whether it's good practice to let other
     * application objects access the context this way.
     */
    public void setPublishContext(boolean publishContext) {
        this.publishContext = publishContext;
    }

    /**
     * Set whether this servlet should publish a ServletRequestHandledEvent at the end
     * of each request. Default is "true"; can be turned off for a slight performance
     * improvement, provided that no ApplicationListeners rely on such events.
     * 
     * @see org.springframework.web.context.support.ServletRequestHandledEvent
     */
    public void setPublishEvents(boolean publishEvents) {
        this.publishEvents = publishEvents;
    }

    /**
     * Set whether to expose the LocaleContext and RequestAttributes as inheritable
     * for child threads (using an {@link java.lang.InheritableThreadLocal}).
     * <p>
     * Default is "false", to avoid side effects on spawned background threads. Switch this to "true" to enable
     * inheritance for custom child threads which are spawned during request processing and only used for this request
     * (that is, ending after their initial task, without reuse of the thread).
     * <p>
     * <b>WARNING:</b> Do not use inheritance for child threads if you are accessing a thread pool which is configured
     * to potentially add new threads on demand (e.g. a JDK {@link java.util.concurrent.ThreadPoolExecutor}), since
     * this will expose the inherited context to such a pooled thread.
     */
    public void setThreadContextInheritable(boolean threadContextInheritable) {
        this.threadContextInheritable = threadContextInheritable;
    }

    /**
     * Set whether this servlet should dispatch an HTTP OPTIONS request to
     * the {@link #doService} method.
     * <p>
     * Default is "false", applying {@link javax.servlet.http.HttpServlet}'s default behavior (i.e. enumerating all
     * standard HTTP request methods as a response to the OPTIONS request).
     * <p>
     * Turn this flag on if you prefer OPTIONS requests to go through the regular dispatching chain, just like other
     * HTTP requests. This usually means that your controllers will receive those requests; make sure that those
     * endpoints are actually able to handle an OPTIONS request.
     * <p>
     * Note that HttpServlet's default OPTIONS processing will be applied in any case. Your controllers are simply
     * available to override the default headers and optionally generate a response body.
     */
    public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
        this.dispatchOptionsRequest = dispatchOptionsRequest;
    }

    /**
     * Set whether this servlet should dispatch an HTTP TRACE request to
     * the {@link #doService} method.
     * <p>
     * Default is "false", applying {@link javax.servlet.http.HttpServlet}'s default behavior (i.e. reflecting the
     * message received back to the client).
     * <p>
     * Turn this flag on if you prefer TRACE requests to go through the regular dispatching chain, just like other HTTP
     * requests. This usually means that your controllers will receive those requests; make sure that those endpoints
     * are actually able to handle a TRACE request.
     * <p>
     * Note that HttpServlet's default TRACE processing will be applied in any case. Your controllers are simply
     * available to override the default headers and the default body, calling <code>response.reset()</code> if
     * necessary.
     */
    public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
        this.dispatchTraceRequest = dispatchTraceRequest;
    }

    /**
     * Overridden method of {@link HttpServletBean}, invoked after any bean properties
     * have been set. Creates this servlet's WebApplicationContext.
     */
    protected final void initServletBean() throws ServletException {
        getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
        if (this.logger.isInfoEnabled()) {
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            this.webApplicationContext = initWebApplicationContext();
            initFrameworkServlet();
        } catch (ServletException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        } catch (RuntimeException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        }

        if (this.logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in "
                    + elapsedTime + " ms");
        }
    }

    /**
     * Initialize and publish the WebApplicationContext for this servlet.
     * <p>
     * Delegates to {@link #createWebApplicationContext} for actual creation of the context. Can be overridden in
     * subclasses.
     * 
     * @return the WebApplicationContext instance
     * @see #setContextClass
     * @see #setContextConfigLocation
     */
    protected WebApplicationContext initWebApplicationContext() {
        WebApplicationContext wac = findWebApplicationContext();
        if (wac == null) {
            // No fixed context defined for this servlet - create a local one.
            WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            wac = createWebApplicationContext(parent);
        }

        if (!this.refreshEventReceived) {
            // Apparently not a ConfigurableApplicationContext with refresh support:
            // triggering initial onRefresh manually here.
            onRefresh(wac);
        }

        if (this.publishContext) {
            // Publish the context as a servlet context attribute.
            String attrName = getServletContextAttributeName();
            getServletContext().setAttribute(attrName, wac);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Published WebApplicationContext of servlet '" + getServletName()
                        + "' as ServletContext attribute with name [" + attrName + "]");
            }
        }

        return wac;
    }

    /**
     * Retrieve a <code>WebApplicationContext</code> from the <code>ServletContext</code> attribute with the
     * {@link #setContextAttribute configured name}. The <code>WebApplicationContext</code> must have already been
     * loaded and stored in the <code>ServletContext</code> before this servlet gets initialized (or invoked).
     * <p>
     * Subclasses may override this method to provide a different <code>WebApplicationContext</code> retrieval
     * strategy.
     * 
     * @return the WebApplicationContext for this servlet, or <code>null</code> if not found
     * @see #getContextAttribute()
     */
    protected WebApplicationContext findWebApplicationContext() {
        String attrName = getContextAttribute();
        if (attrName == null) {
            return null;
        }
        WebApplicationContext wac = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext(), attrName);
        if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
        }
        return wac;
    }

    /**
     * Instantiate the WebApplicationContext for this servlet, either a default
     * {@link org.springframework.web.context.support.XmlWebApplicationContext} or a {@link #setContextClass custom
     * context class}, if set.
     * <p>
     * This implementation expects custom contexts to implement the
     * {@link org.springframework.web.context.ConfigurableWebApplicationContext} interface. Can be overridden in
     * subclasses.
     * <p>
     * Do not forget to register this servlet instance as application listener on the created context (for triggering
     * its {@link #onRefresh callback}, and to call
     * {@link org.springframework.context.ConfigurableApplicationContext#refresh()} before returning the context
     * instance.
     * 
     * @param parent
     *            the parent ApplicationContext to use, or <code>null</code> if none
     * @return the WebApplicationContext for this servlet
     * @see org.springframework.web.context.support.XmlWebApplicationContext
     */
    protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {
        Class<?> contextClass = getContextClass();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Servlet with name '" + getServletName()
                    + "' will try to create custom WebApplicationContext context of class '" + contextClass.getName()
                    + "'" + ", using parent context [" + parent + "]");
        }
        if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
            throw new ApplicationContextException("Fatal initialization error in servlet with name '"
                    + getServletName() + "': custom WebApplicationContext class [" + contextClass.getName()
                    + "] is not of type ConfigurableWebApplicationContext");
        }
        ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext) BeanUtils
                .instantiateClass(contextClass);

        // Assign the best possible id value.
        ServletContext sc = getServletContext();
        if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
            // Servlet <= 2.4: resort to name specified in web.xml, if any.
            String servletContextName = sc.getServletContextName();
            if (servletContextName != null) {
                wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + servletContextName + "."
                        + getServletName());
            } else {
                wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + getServletName());
            }
        } else {
            // Servlet 2.5's getContextPath available!
            wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + sc.getContextPath() + "/"
                    + getServletName());
        }

        wac.setParent(parent);
        wac.setServletContext(getServletContext());
        wac.setServletConfig(getServletConfig());
        wac.setNamespace(getNamespace());
        wac.setConfigLocation(getContextConfigLocation());
        wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

        postProcessWebApplicationContext(wac);
        wac.refresh();

        return wac;
    }

    /**
     * Instantiate the WebApplicationContext for this servlet, either a default
     * {@link org.springframework.web.context.support.XmlWebApplicationContext} or a {@link #setContextClass custom
     * context class}, if set.
     * Delegates to #createWebApplicationContext(ApplicationContext).
     * 
     * @param parent
     *            the parent WebApplicationContext to use, or <code>null</code> if none
     * @return the WebApplicationContext for this servlet
     * @see org.springframework.web.context.support.XmlWebApplicationContext
     * @see #createWebApplicationContext(ApplicationContext)
     */
    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
        return createWebApplicationContext((ApplicationContext) parent);
    }

    /**
     * Post-process the given WebApplicationContext before it is refreshed
     * and activated as context for this servlet.
     * <p>
     * The default implementation is empty. <code>refresh()</code> will be called automatically after this method
     * returns.
     * 
     * @param wac
     *            the configured WebApplicationContext (not refreshed yet)
     * @see #createWebApplicationContext
     * @see ConfigurableWebApplicationContext#refresh()
     */
    protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
    }

    /**
     * Return the ServletContext attribute name for this servlet's WebApplicationContext.
     * <p>
     * The default implementation returns <code>SERVLET_CONTEXT_PREFIX + servlet name</code>.
     * 
     * @see #SERVLET_CONTEXT_PREFIX
     * @see #getServletName
     */
    public String getServletContextAttributeName() {
        return SERVLET_CONTEXT_PREFIX + getServletName();
    }

    /**
     * Return this servlet's WebApplicationContext.
     */
    public final WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    /**
     * This method will be invoked after any bean properties have been set and
     * the WebApplicationContext has been loaded. The default implementation is empty;
     * subclasses may override this method to perform any initialization they require.
     * 
     * @throws ServletException
     *             in case of an initialization exception
     */
    protected void initFrameworkServlet() throws ServletException {
    }

    /**
     * Refresh this servlet's application context, as well as the
     * dependent state of the servlet.
     * 
     * @see #getWebApplicationContext()
     * @see org.springframework.context.ConfigurableApplicationContext#refresh()
     */
    public void refresh() {
        WebApplicationContext wac = getWebApplicationContext();
        if (!(wac instanceof ConfigurableApplicationContext)) {
            throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
        }
        ((ConfigurableApplicationContext) wac).refresh();
    }

    /**
     * Callback that receives refresh events from this servlet's WebApplicationContext.
     * <p>
     * The default implementation calls {@link #onRefresh}, triggering a refresh of this servlet's context-dependent
     * state.
     * 
     * @param event
     *            the incoming ApplicationContext event
     */
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.refreshEventReceived = true;
        onRefresh(event.getApplicationContext());
    }

    /**
     * Determine the username for the given request.
     * <p>
     * The default implementation takes the name of the UserPrincipal, if any. Can be overridden in subclasses.
     * 
     * @param request
     *            current HTTP request
     * @return the username, or <code>null</code> if none found
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    protected String getUsernameForRequest(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        return (userPrincipal != null ? userPrincipal.getName() : null);
    }

    /**
     * Close the WebApplicationContext of this servlet.
     * 
     * @see org.springframework.context.ConfigurableApplicationContext#close()
     */
    @Override
    public void destroy() {
        getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
        if (this.webApplicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) this.webApplicationContext).close();
        }
    }

    /**
     * ApplicationListener endpoint that receives events from this servlet's WebApplicationContext
     * only, delegating to <code>onApplicationEvent</code> on the FrameworkServlet instance.
     */
    private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            SpringJerseyServlet.this.onApplicationEvent(event);
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /** Well-known name for the MultipartResolver object in the bean factory for this namespace. */
    public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

    /** Well-known name for the LocaleResolver object in the bean factory for this namespace. */
    public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

    /** Well-known name for the ThemeResolver object in the bean factory for this namespace. */
    public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

    /**
     * Well-known name for the HandlerMapping object in the bean factory for this namespace.
     * Only used when "detectAllHandlerMappings" is turned off.
     * 
     * @see #setDetectAllHandlerMappings
     */
    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

    /**
     * Well-known name for the HandlerAdapter object in the bean factory for this namespace.
     * Only used when "detectAllHandlerAdapters" is turned off.
     * 
     * @see #setDetectAllHandlerAdapters
     */
    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

    /**
     * Well-known name for the HandlerExceptionResolver object in the bean factory for this namespace.
     * Only used when "detectAllHandlerExceptionResolvers" is turned off.
     * 
     * @see #setDetectAllHandlerExceptionResolvers
     */
    public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

    /**
     * Well-known name for the RequestToViewNameTranslator object in the bean factory for this namespace.
     */
    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

    /**
     * Well-known name for the ViewResolver object in the bean factory for this namespace.
     * Only used when "detectAllViewResolvers" is turned off.
     * 
     * @see #setDetectAllViewResolvers
     */
    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

    /**
     * Request attribute to hold the current web application context.
     * Otherwise only the global web app context is obtainable by tags etc.
     * 
     * @see org.springframework.web.servlet.support.RequestContextUtils#getWebApplicationContext
     */
    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    /**
     * Request attribute to hold the current LocaleResolver, retrievable by views.
     * 
     * @see org.springframework.web.servlet.support.RequestContextUtils#getLocaleResolver
     */
    public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

    /**
     * Request attribute to hold the current ThemeResolver, retrievable by views.
     * 
     * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeResolver
     */
    public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

    /**
     * Request attribute to hold the current ThemeSource, retrievable by views.
     * 
     * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeSource
     */
    public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

    /** Log category to use when no mapped handler is found for a request. */
    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    /**
     * Name of the class path resource (relative to the DispatcherServlet class)
     * that defines DispatcherServlet's default strategy names.
     */
    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";

    /** Additional logger to use when no mapped handler is found for a request. */
    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private static final Properties defaultStrategies;

    static {
        // Load default strategy implementations from properties file.
        // This is currently strictly internal and not meant to be customized
        // by application developers.
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
        }
    }

    /** Detect all HandlerMappings or just expect "handlerMapping" bean? */
    private boolean detectAllHandlerMappings = true;

    /** Detect all HandlerAdapters or just expect "handlerAdapter" bean? */
    private boolean detectAllHandlerAdapters = true;

    /** Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean? */
    private boolean detectAllHandlerExceptionResolvers = true;

    /** Detect all ViewResolvers or just expect "viewResolver" bean? */
    private boolean detectAllViewResolvers = true;

    /** Perform cleanup of request attributes after include request? */
    private boolean cleanupAfterInclude = true;

    /** MultipartResolver used by this servlet */
    private MultipartResolver multipartResolver;

    /** LocaleResolver used by this servlet */
    private LocaleResolver localeResolver;

    /** ThemeResolver used by this servlet */
    private ThemeResolver themeResolver;

    /** List of HandlerMappings used by this servlet */
    private List<HandlerMapping> handlerMappings;

    /** List of HandlerAdapters used by this servlet */
    private List<HandlerAdapter> handlerAdapters;

    /** List of HandlerExceptionResolvers used by this servlet */
    private List<HandlerExceptionResolver> handlerExceptionResolvers;

    /** RequestToViewNameTranslator used by this servlet */
    private RequestToViewNameTranslator viewNameTranslator;

    /** List of ViewResolvers used by this servlet */
    private List<ViewResolver> viewResolvers;

    /**
     * Set whether to detect all HandlerMapping beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerMapping" will be expected.
     * <p>
     * Default is "true". Turn this off if you want this servlet to use a single HandlerMapping, despite multiple
     * HandlerMapping beans being defined in the context.
     */
    public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
        this.detectAllHandlerMappings = detectAllHandlerMappings;
    }

    /**
     * Set whether to detect all HandlerAdapter beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerAdapter" will be expected.
     * <p>
     * Default is "true". Turn this off if you want this servlet to use a single HandlerAdapter, despite multiple
     * HandlerAdapter beans being defined in the context.
     */
    public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
        this.detectAllHandlerAdapters = detectAllHandlerAdapters;
    }

    /**
     * Set whether to detect all HandlerExceptionResolver beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerExceptionResolver" will be expected.
     * <p>
     * Default is "true". Turn this off if you want this servlet to use a single HandlerExceptionResolver, despite
     * multiple HandlerExceptionResolver beans being defined in the context.
     */
    public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
        this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
    }

    /**
     * Set whether to detect all ViewResolver beans in this servlet's context. Otherwise,
     * just a single bean with name "viewResolver" will be expected.
     * <p>
     * Default is "true". Turn this off if you want this servlet to use a single ViewResolver, despite multiple
     * ViewResolver beans being defined in the context.
     */
    public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
        this.detectAllViewResolvers = detectAllViewResolvers;
    }

    /**
     * Set whether to perform cleanup of request attributes after an include request, that is,
     * whether to reset the original state of all request attributes after the DispatcherServlet
     * has processed within an include request. Otherwise, just the DispatcherServlet's own
     * request attributes will be reset, but not model attributes for JSPs or special attributes
     * set by views (for example, JSTL's).
     * <p>
     * Default is "true", which is strongly recommended. Views should not rely on request attributes having been set by
     * (dynamic) includes. This allows JSP views rendered by an included controller to use any model attributes, even
     * with the same names as in the main JSP, without causing side effects. Only turn this off for special needs, for
     * example to deliberately allow main JSPs to access attributes from JSP views rendered by an included controller.
     */
    public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
        this.cleanupAfterInclude = cleanupAfterInclude;
    }

    /**
     * This implementation calls {@link #initStrategies}.
     */
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    /**
     * Initialize the strategy objects that this servlet uses.
     * <p>
     * May be overridden in subclasses in order to initialize further strategy objects.
     */
    protected void initStrategies(ApplicationContext context) {
        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
    }

    /**
     * Initialize the MultipartResolver used by this class.
     * <p>
     * If no bean is defined with the given name in the BeanFactory for this namespace, no multipart handling is
     * provided.
     */
    private void initMultipartResolver(ApplicationContext context) {
        try {
            this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // Default is no multipart resolver.
            this.multipartResolver = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME
                        + "': no multipart request handling provided");
            }
        }
    }

    /**
     * Initialize the LocaleResolver used by this class.
     * <p>
     * If no bean is defined with the given name in the BeanFactory for this namespace, we default to
     * AcceptHeaderLocaleResolver.
     */
    private void initLocaleResolver(ApplicationContext context) {
        try {
            this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME
                        + "': using default [" + this.localeResolver + "]");
            }
        }
    }

    /**
     * Initialize the ThemeResolver used by this class.
     * <p>
     * If no bean is defined with the given name in the BeanFactory for this namespace, we default to a
     * FixedThemeResolver.
     */
    private void initThemeResolver(ApplicationContext context) {
        try {
            this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using ThemeResolver [" + this.themeResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME
                        + "': using default [" + this.themeResolver + "]");
            }
        }
    }

    /**
     * Initialize the HandlerMappings used by this class.
     * <p>
     * If no HandlerMapping beans are defined in the BeanFactory for this namespace, we default to
     * BeanNameUrlHandlerMapping.
     */
    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
                    HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                OrderComparator.sort(this.handlerMappings);
            }
        } else {
            try {
                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                this.handlerMappings = Collections.singletonList(hm);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * Initialize the HandlerAdapters used by this class.
     * <p>
     * If no HandlerAdapter beans are defined in the BeanFactory for this namespace, we default to
     * SimpleControllerHandlerAdapter.
     */
    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerAdapter> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
                    HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
                // We keep HandlerAdapters in sorted order.
                OrderComparator.sort(this.handlerAdapters);
            }
        } else {
            try {
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
                this.handlerAdapters = Collections.singletonList(ha);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
        if (this.handlerAdapters == null) {
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * Initialize the HandlerExceptionResolver used by this class.
     * <p>
     * If no bean is defined with the given name in the BeanFactory for this namespace, we default to no exception
     * resolver.
     */
    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        if (this.detectAllHandlerExceptionResolvers) {
            // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    context, HandlerExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
                // We keep HandlerExceptionResolvers in sorted order.
                OrderComparator.sort(this.handlerExceptionResolvers);
            }
        } else {
            try {
                HandlerExceptionResolver her = context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME,
                        HandlerExceptionResolver.class);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        // Ensure we have at least some HandlerExceptionResolvers, by registering
        // default HandlerExceptionResolvers if no other resolvers are found.
        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName()
                        + "': using default");
            }
        }
    }

    /**
     * Initialize the RequestToViewNameTranslator used by this servlet instance.
     * <p>
     * If no implementation is configured then we default to DefaultRequestToViewNameTranslator.
     */
    private void initRequestToViewNameTranslator(ApplicationContext context) {
        try {
            this.viewNameTranslator = context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME,
                    RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate RequestToViewNameTranslator with name '"
                        + REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator
                        + "]");
            }
        }
    }

    /**
     * Initialize the ViewResolvers used by this class.
     * <p>
     * If no ViewResolver beans are defined in the BeanFactory for this namespace, we default to
     * InternalResourceViewResolver.
     */
    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = null;

        if (this.detectAllViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
                    ViewResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                OrderComparator.sort(this.viewResolvers);
            }
        } else {
            try {
                ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
                this.viewResolvers = Collections.singletonList(vr);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            }
        }

        // Ensure we have at least one ViewResolver, by registering
        // a default ViewResolver if no other resolvers are found.
        if (this.viewResolvers == null) {
            this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * Return this servlet's ThemeSource, if any; else return <code>null</code>.
     * <p>
     * Default is to return the WebApplicationContext as ThemeSource, provided that it implements the ThemeSource
     * interface.
     * 
     * @return the ThemeSource, if any
     * @see #getWebApplicationContext()
     */
    public final ThemeSource getThemeSource() {
        if (getWebApplicationContext() instanceof ThemeSource) {
            return (ThemeSource) getWebApplicationContext();
        } else {
            return null;
        }
    }

    /**
     * Obtain this servlet's MultipartResolver, if any.
     * 
     * @return the MultipartResolver used by this servlet, or <code>null</code> if none
     *         (indicating that no multipart support is available)
     */
    public final MultipartResolver getMultipartResolver() {
        return this.multipartResolver;
    }

    /**
     * Return the default strategy object for the given strategy interface.
     * <p>
     * The default implementation delegates to {@link #getDefaultStrategies}, expecting a single object in the list.
     * 
     * @param context
     *            the current WebApplicationContext
     * @param strategyInterface
     *            the strategy interface
     * @return the corresponding strategy object
     * @see #getDefaultStrategies
     */
    protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
        List<T> strategies = getDefaultStrategies(context, strategyInterface);
        if (strategies.size() != 1) {
            throw new BeanInitializationException("DispatcherServlet needs exactly 1 strategy for interface ["
                    + strategyInterface.getName() + "]");
        }
        return strategies.get(0);
    }

    /**
     * Create a List of default strategy objects for the given strategy interface.
     * <p>
     * The default implementation uses the "DispatcherServlet.properties" file (in the same package as the
     * DispatcherServlet class) to determine the class names. It instantiates the strategy objects through the
     * context's BeanFactory.
     * 
     * @param context
     *            the current WebApplicationContext
     * @param strategyInterface
     *            the strategy interface
     * @return the List of corresponding strategy objects
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<T>(classNames.length);
            for (String className : classNames) {
                try {
                    Class clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                } catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException(
                            "Could not find DispatcherServlet's default strategy class [" + className
                                    + "] for interface [" + key + "]", ex);
                } catch (LinkageError err) {
                    throw new BeanInitializationException(
                            "Error loading DispatcherServlet's default strategy class [" + className
                                    + "] for interface [" + key + "]: problem with class file or dependent class",
                            err);
                }
            }
            return strategies;
        } else {
            return new LinkedList<T>();
        }
    }

    /**
     * Create a default strategy.
     * <p>
     * The default implementation uses
     * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean}.
     * 
     * @param context
     *            the current WebApplicationContext
     * @param clazz
     *            the strategy implementation class to instantiate
     * @return the fully configured strategy instance
     * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean
     */
    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }

    /**
     * Build a LocaleContext for the given request, exposing the request's primary locale as current locale.
     * <p>
     * The default implementation uses the dispatcher's LocaleResolver to obtain the current locale, which might change
     * during a request.
     * 
     * @param request
     *            current HTTP request
     * @return the corresponding LocaleContext
     */
    protected LocaleContext buildLocaleContext(final HttpServletRequest request) {
        return new LocaleContext() {
            @Override
            public Locale getLocale() {
                return localeResolver.resolveLocale(request);
            }

            @Override
            public String toString() {
                return getLocale().toString();
            }
        };
    }

    /**
     * Convert the request into a multipart request, and make multipart resolver available.
     * <p>
     * If no multipart resolver is set, simply use the existing request.
     * 
     * @param request
     *            current HTTP request
     * @return the processed request (multipart wrapper if necessary)
     * @see MultipartResolver#resolveMultipart
     */
    protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
        if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
            if (request instanceof MultipartHttpServletRequest) {
                logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, "
                        + "this typically results from an additional MultipartFilter in web.xml");
            } else {
                return this.multipartResolver.resolveMultipart(request);
            }
        }
        // If not returned before: return original request.
        return request;
    }

    /**
     * Clean up any resources used by the given multipart request (if any).
     * 
     * @param request
     *            current HTTP request
     * @see MultipartResolver#cleanupMultipart
     */
    protected void cleanupMultipart(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest) {
            this.multipartResolver.cleanupMultipart((MultipartHttpServletRequest) request);
        }
    }

    /**
     * Return the HandlerExecutionChain for this request. Try all handler mappings in order.
     * 
     * @param request
     *            current HTTP request
     * @param cache
     *            whether to cache the HandlerExecutionChain in a request attribute
     * @return the HandlerExecutionChain, or <code>null</code> if no handler could be found
     * @deprecated as of Spring 3.0.4, in favor of {@link #getHandler(javax.servlet.http.HttpServletRequest)},
     *             with this method's cache attribute now effectively getting ignored
     */
    @Deprecated
    protected HandlerExecutionChain getHandler(HttpServletRequest request, boolean cache) throws Exception {
        return getHandler(request);
    }

    /**
     * Return the HandlerExecutionChain for this request.
     * <p>
     * Tries all handler mappings in order.
     * 
     * @param request
     *            current HTTP request
     * @return the HandlerExecutionChain, or <code>null</code> if no handler could be found
     */
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName()
                        + "'");
            }
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    /**
     * No handler found -> set appropriate HTTP response status.
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @throws Exception
     *             if preparing the response failed
     */
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (pageNotFoundLogger.isWarnEnabled()) {
            String requestUri = urlPathHelper.getRequestUri(request);
            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + requestUri
                    + "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Return the HandlerAdapter for this handler object.
     * 
     * @param handler
     *            the handler object to find an adapter for
     * @throws ServletException
     *             if no HandlerAdapter can be found for the handler. This is a fatal error.
     */
    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler adapter [" + ha + "]");
            }
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler
                + "]: Does your handler implement a supported interface like Controller?");
    }

    /**
     * Determine an error ModelAndView via the registered HandlerExceptionResolvers.
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler, or <code>null</code> if none chosen at the time of the exception
     *            (for example, if multipart resolution failed)
     * @param ex
     *            the exception that got thrown during handler execution
     * @return a corresponding ModelAndView to forward to
     * @throws Exception
     *             if no error ModelAndView found
     */
    protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {

        // Check registered HandlerExceptionResolvers...
        ModelAndView exMv = null;
        for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
            exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
            if (exMv != null) {
                break;
            }
        }
        if (exMv != null) {
            if (exMv.isEmpty()) {
                return null;
            }
            // We might still need view name translation for a plain error model...
            if (!exMv.hasView()) {
                exMv.setViewName(getDefaultViewName(request));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv,
                        ex);
            }
            WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
            return exMv;
        }

        throw ex;
    }

    /**
     * Translate the supplied request into a default view name.
     * 
     * @param request
     *            current HTTP servlet request
     * @return the view name (or <code>null</code> if no default found)
     * @throws Exception
     *             if view name translation failed
     */
    protected String getDefaultViewName(HttpServletRequest request) throws Exception {
        return this.viewNameTranslator.getViewName(request);
    }

    /**
     * Resolve the given view name into a View object (to be rendered).
     * <p>
     * The default implementations asks all ViewResolvers of this dispatcher. Can be overridden for custom resolution
     * strategies, potentially based on specific model attributes or request parameters.
     * 
     * @param viewName
     *            the name of the view to resolve
     * @param model
     *            the model to be passed to the view
     * @param locale
     *            the current locale
     * @param request
     *            current HTTP servlet request
     * @return the View object, or <code>null</code> if none found
     * @throws Exception
     *             if the view cannot be resolved
     *             (typically in case of problems creating an actual View object)
     * @see ViewResolver#resolveViewName
     */
    protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale,
            HttpServletRequest request) throws Exception {

        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(viewName, locale);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    /**
     * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
     * Will just invoke afterCompletion for all interceptors whose preHandle invocation
     * has successfully completed and returned true.
     * 
     * @param mappedHandler
     *            the mapped HandlerExecutionChain
     * @param interceptorIndex
     *            index of last interceptor that successfully completed
     * @param ex
     *            Exception thrown on handler execution, or <code>null</code> if none
     * @see HandlerInterceptor#afterCompletion
     */
    private void triggerAfterCompletion(HandlerExecutionChain mappedHandler, int interceptorIndex,
            HttpServletRequest request, HttpServletResponse response, Exception ex) throws Exception {

        // Apply afterCompletion methods of registered interceptors.
        if (mappedHandler != null) {
            HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
            if (interceptors != null) {
                for (int i = interceptorIndex; i >= 0; i--) {
                    HandlerInterceptor interceptor = interceptors[i];
                    try {
                        interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
                    } catch (Throwable ex2) {
                        logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                    }
                }
            }
        }
    }

    /**
     * Restore the request attributes after an include.
     * 
     * @param request
     *            current HTTP request
     * @param attributesSnapshot
     *            the snapshot of the request attributes before the include
     */
    private void restoreAttributesAfterInclude(HttpServletRequest request, Map attributesSnapshot) {
        logger.debug("Restoring snapshot of request attributes after include");

        // Need to copy into separate Collection here, to avoid side effects
        // on the Enumeration when removing attributes.
        Set<String> attrsToCheck = new HashSet<String>();
        Enumeration attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
                attrsToCheck.add(attrName);
            }
        }

        // Iterate over the attributes to check, restoring the original value
        // or removing the attribute, respectively, if appropriate.
        for (String attrName : attrsToCheck) {
            Object attrValue = attributesSnapshot.get(attrName);
            if (attrValue == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing attribute [" + attrName + "] after include");
                }
                request.removeAttribute(attrName);
            } else if (attrValue != request.getAttribute(attrName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring original value of attribute [" + attrName + "] after include");
                }
                request.setAttribute(attrName, attrValue);
            }
        }
    }

}
