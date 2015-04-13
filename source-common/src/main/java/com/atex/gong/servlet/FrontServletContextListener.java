package com.atex.gong.servlet;

import com.atex.onecms.content.repository.CouchbaseClientComponent;
import com.polopoly.application.Application;
import com.polopoly.application.ApplicationStatusReporter;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.ConnectionPropertiesException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.LegacyDaemonThreadsStopper;
import com.polopoly.application.StandardApplication;
import com.polopoly.application.config.ConfigurationRuntimeException;
import com.polopoly.application.config.ResourceConfig;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cache.LRUSynchronizedUpdateCache;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.client.DiskCacheSettings;
import com.polopoly.cm.client.HttpCmClientHelper;
import com.polopoly.cm.client.HttpFileServiceClient;
import com.polopoly.poll.PollServer;
import com.polopoly.poll.client.PollClient;
import com.polopoly.poll.client.PollCouchClient;
import com.polopoly.search.solr.SolrIndexName;
import com.polopoly.search.solr.SolrSearchClient;
import com.polopoly.statistics.client.StatisticsClient;
import com.polopoly.statistics.message.logging.UDPLogMsgClient;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates, inits and destroys the Application used in the front webapp. The Application is also inserted in the
 * ServletContext under the name specified in web.xml.
 */
public class FrontServletContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(FrontServletContextListener.class.getName());

    private Application application;

    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            ServletContext servletContext = servletContextEvent.getServletContext();

            // Application with local JMX registry.
            application = new StandardApplication(ApplicationServletUtil.getApplicationName(servletContext));
            application.setManagedBeanRegistry(ApplicationServletUtil.getManagedBeanRegistry());

            ConnectionProperties connectionProperties = ApplicationServletUtil.getConnectionProperties(servletContext);
            CmClientBase cmClient = HttpCmClientHelper.createAndAddToApplication(application, connectionProperties);
            CouchbaseClientComponent couchbaseClientComponent =
                    CouchbaseClientComponent.createAndAddToApplicationIfConfigured(application, connectionProperties);
            PollCouchClient pollCouchClient = null;

            if (couchbaseClientComponent != null) {
                pollCouchClient = createPollCouchClient(cmClient, couchbaseClientComponent);
                application.addApplicationComponent(pollCouchClient);
            }

            // Reports status back to the cm server
            application.addApplicationComponent(new ApplicationStatusReporter(cmClient));

            // NOTE: The CmClient needs a heartbeat to get updates from the change
            // list and for reconnecting to the server.
            // In a web application the heartbeat is triggered by the
            // ApplicationHeartbeatFilter mapped under the name 'pacemaker'
            // in web.mxl.
            // For a standalone CmClient see example in the com.polopoly.application
            // package javadoc.

            addSolrSearchClient(cmClient);

            // Statistics client.
            StatisticsClient statisticsClient = new StatisticsClient();
            application.addApplicationComponent(statisticsClient);

            // Poll client.
            PollClient pollClient = new PollClient();
            application.addApplicationComponent(pollClient);

            // Log msg client client.
            UDPLogMsgClient logMsgClient = new UDPLogMsgClient();
            application.addApplicationComponent(logMsgClient);

            // Sync cache.
            LRUSynchronizedUpdateCache syncCache = new LRUSynchronizedUpdateCache();
            application.addApplicationComponent(syncCache);

            HttpFileServiceClient fileServiceClient = new HttpFileServiceClient();
            application.addApplicationComponent(fileServiceClient);

            // Read connection properties.
            application.readConnectionProperties(connectionProperties);

            // Read and apply config in xml resources. Since this webapp uses the same source for
            // preview and front we use the application name to distinguish between preview
            // and front config.
            ResourceConfig config = new ResourceConfig(application.getName());
            config.apply(application);

            configureDiskCacheSettings(servletContextEvent, cmClient);

            application.init();

            // Put in global scope.
            ApplicationServletUtil.setApplication(servletContext, application);

            if (pollCouchClient != null) {
                try {
                    PollServer.setPollManager(pollCouchClient.getPollManager());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not setup Poll using Couchbase, please setup Couchbase server"
                            + " correctly and restart web container", e);
                }
            }
        } catch (IllegalApplicationStateException e) {
            throw new RuntimeException("This is a programming error, should never happen.", e);
        } catch (ConnectionPropertiesException e) {
            LOGGER.log(Level.SEVERE, "Could not get, read or apply connection properties.", e);
        } catch (ConfigurationRuntimeException e) {
            LOGGER.log(Level.SEVERE, "Could not read or apply configuration.", e);
        }
    }

    private PollCouchClient createPollCouchClient(final CmClientBase cmClient,
                                                  final CouchbaseClientComponent couchbaseClientComponent) {
        return new PollCouchClient(
                PollCouchClient.DEFAULT_MODULE_NAME,
                PollCouchClient.DEFAULT_COMPONENT_NAME,
                cmClient,
                couchbaseClientComponent);
    }

    private void configureDiskCacheSettings(final ServletContextEvent sce,
                                            final CmClientBase cmClient) throws IllegalApplicationStateException {
        DiskCacheSettings settings = cmClient.getDiskCacheSettings();
        ApplicationServletUtil.configureDiskCacheBaseDir(
                sce.getServletContext(), settings, application.getName(), cmClient.getModuleName());
        cmClient.setDiskCacheSettings(settings);
    }

    // Solr search client (Searches in the public index).
    private void addSolrSearchClient(final CmClientBase cmClient) throws IllegalApplicationStateException {
        SolrSearchClient solrSearchClient =
                new SolrSearchClient(SolrSearchClient.DEFAULT_MODULE_NAME,
                        SolrSearchClient.DEFAULT_COMPONENT_NAME,
                        cmClient);
        solrSearchClient.setIndexName(new SolrIndexName("public"));
        application.addApplicationComponent(solrSearchClient);
    }

    public void contextDestroyed(final ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        // Remove from global scope.
        ApplicationServletUtil.setApplication(sc, null);

        // Destroy.
        application.destroy();

        LegacyDaemonThreadsStopper.stopStaticDaemons();
    }
}
