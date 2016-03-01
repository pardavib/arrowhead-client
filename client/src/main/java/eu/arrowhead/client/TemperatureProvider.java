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
	 * Own ArrowheadSystem info.
	 */
	private ArrowheadSystem arrowheadSystem = new ArrowheadSystem("BUTE", "TemperatureTest", "127.0.0.1", "8080",
			"tempcert");

	/**
	 * Constructor setting initial parameters of superclass.
	 */
	public TemperatureProvider() {
		super();
		ArrayList<String> interfaces = new ArrayList<String>();
		this.setServiceGroup("Temperature");
		this.setServiceDefinition("IndoorTemperature");
		this.setMetaData("Dummy metadata");
		interfaces.add("RESTJSON");
		this.setInterfaces(interfaces);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Hello, I am the TemepratureProvider.";
	}

	/**
	 * This function invokes the provider to register itself to the Service
	 * Registry.
	 * 
	 * @return String
	 */
	@GET
	@Path("/register")
	public Response invokeRegister() {
		ServiceRegistryEntry serviceRegistryEntry = new ServiceRegistryEntry();

		// Preparing ServiceRegistryEntry
		serviceRegistryEntry.setProvider(arrowheadSystem);
		serviceRegistryEntry.setServiceURI(
				this.arrowheadSystem.getIPAddress() + ":" + this.arrowheadSystem.getPort() + "/temperature");
		serviceRegistryEntry.setServiceMetadata(this.getMetaData());
		serviceRegistryEntry.settSIG_key("RIuxP+vb5GjLXJo686NvKQ=="); // .168
		serviceRegistryEntry.setVersion("1.0");

		/*if (registerProvider(serviceRegistryEntry) == 200) {
			return "Provider successfully registered to Service Registry.";
		}*/

		return registerProvider(serviceRegistryEntry);
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
	public Response registerProvider(ServiceRegistryEntry serviceRegistryEntry) {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromPath("http://"+"152.66.245.168" + ":" + "8080").path("core")
				.path("serviceregistry").path(this.getServiceGroup()).path(this.getServiceDefinition()).path("RESTJSON").build();
		WebTarget target = client.target(uri);
		System.out.println(target.getUri().toString());
		Response response = target.request().header("Content-type", "application/json")
				.post(Entity.json(serviceRegistryEntry));
		return response;
	}

}
