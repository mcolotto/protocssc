package org.cssc.prototpe.net;

import java.net.UnknownHostException;
import org.cssc.prototpe.httpserver.ApplicationConfigurationServer;
import org.cssc.prototpe.net.interfaces.ServerManager;


/**
 * The main class for the application. 
 */
public class Application {

	private static Application instance = null;
	private ApplicationConfiguration applicationConfiguration;
	private ClientListener httpListener;
	private Logger logger;
	private ServerManager serverManager;
	private MonitoringService monitoringService;

	private static final String CONFIG_FILE = "src/main/resources/config/config.xml";

	private Application() {
		// This must be first, before anything else, as other parts of the
		// application require this instance.
		instance = this;

		// This MUST be second as other parts of the application require this configuration.
		applicationConfiguration = new ApplicationConfiguration(CONFIG_FILE);

		// TODO: Place here any parts of the application that are needed by other parts.
		logger = new Logger(applicationConfiguration.getLoggingFileName());
		serverManager = new PersistentSemaphorizedServerManager(
				applicationConfiguration.getMaxPersistantServerConnections(),
				applicationConfiguration.getMaxPersistantServerConnectionsPerServer());
		monitoringService = new MonitoringService();

		// This must be last, as it requires a configuration.
		httpListener = new HttpProxyClientListener(getApplicationConfiguration().getProxyPort());
	}
	
	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}

	public Logger getLogger() {
		return logger;
	}

	public ServerManager getServerManager() {
		return serverManager;
	}
	
	public MonitoringService getMonitoringService() {
		return monitoringService;
	}

	/**
	 * The main method of the Application class. Executes the application.
	 */
	public void launch() {
		httpListener.listen();
	}


	public static void main(String[] args) throws UnknownHostException {
		Application application = new Application();
		new ApplicationConfigurationServer(Application.getInstance().getApplicationConfiguration().getRemoteServicesPort());
		System.out.println("HTTP Proxy Server started.");
		application.launch();
	}

	public static Application getInstance() {
		return instance;
	}

}
