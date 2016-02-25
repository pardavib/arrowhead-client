package eu.arrowhead.client;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;

@Path("temperature")
@Produces(MediaType.TEXT_PLAIN)
public class TemperatureProvider extends ArrowheadService {

	/**
	 * Provider's ArrowheadSystem.
	 */
	private ArrowheadSystem arrowheadSystem = new ArrowheadSystem("BUTE", "ProviderSystem", "localhost", "8080",
			"authenticationInfo");

	/**
	 * Constructor setting initial parameters of superclass.
	 */
	public TemperatureProvider() {
		super();
		ArrayList<String> interfaces = new ArrayList<String>();
		this.setServiceGroup("Temperature");
		this.setServiceDefinition("IndoorTemperature");
		this.setMetaData("Dummy metadata");
		interfaces.add("REST_JSON");
		this.setInterfaces(interfaces);
	}

	/**
	 * This function invokes the provider to register itself to the Service
	 * Registry.
	 * 
	 * @return String
	 */
	@GET
	@Path("/register")
	public String invokeRegister() {
		ServiceRegistryEntry serviceRegistryEntry = new ServiceRegistryEntry();

		// Preparing ServiceRegistryEntry
		serviceRegistryEntry.setProvider(arrowheadSystem);
		serviceRegistryEntry.setServiceURI(
				this.arrowheadSystem.getIPAddress() + ":" + this.arrowheadSystem.getPort() + "/temperature");
		serviceRegistryEntry.setServiceMetadata(this.getMetaData());
		serviceRegistryEntry.settSIG_key("tSIG_key");

		if (registerProvider(serviceRegistryEntry) == 200) {
			return "Provider successfully registered to Service Registry.";
		}

		return "Error occured during Provider registration to Service Registry.";
	}

	/**
	 * This function returns the current temperature data.
	 * 
	 * @return String
	 */
	@GET
	@Path("/current")
	public String getCurrentTemperature() {
		return "21";
	}

	/**
	 * This function handles the necessary communication through REST to
	 * register the TemperatureProvider to the Service Registry.
	 * 
	 * @return String Temperature data.
	 */
	public int registerProvider(ServiceRegistryEntry serviceRegistryEntry) {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri(this.arrowheadSystem.getIPAddress() + ":" + this.arrowheadSystem.getPort())
				.path("ServiceRegistry").path(this.getServiceGroup()).path(this.getServiceDefinition()).build();
		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json")
				.put(Entity.json(serviceRegistryEntry));
		return response.getStatus();
	}

}
