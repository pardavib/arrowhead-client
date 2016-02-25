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
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;

@Path("temperature")
@Produces(MediaType.TEXT_PLAIN)
public class TemperatureProvider extends ArrowheadService {

	public TemperatureProvider() {
		super();
		ArrayList<String> interfaces = new ArrayList<String>();
		this.setServiceGroup("Temperature");
		this.setServiceDefinition("IndoorTemperature");
		this.setMetaData("Dummy metadata");
		interfaces.add("REST_JSON");
		this.setInterfaces(interfaces);
	}

	@GET
    @Path("/invokeRegister")
    public String invokeRegister() {
    	private ServiceRegistryEntry = 
        return "Provider successfully registered to Service Registry.";
    }

	@GET
	@Path("/current")
	public String getCurrentTemperature() {
		return "21";
	}

	public int registerProvider(ServiceRegistryEntry serviceRegistryEntry, URI coreURI) {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri(coreURI).path("ServiceRegistry").path(this.getServiceGroup())
				.path(this.getServiceDefinition()).path(this.getInterfaces().get(0)).build();
		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json")
				.put(Entity.json(serviceRegistryEntry));
		return response.getStatus();
	}

}
