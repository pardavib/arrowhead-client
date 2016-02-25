package eu.arrowhead.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

@Path("consumer")
@Produces(MediaType.TEXT_PLAIN)
public class TemperatureConsumer {

	private String coreURI = "localhost:8080/core/";
	private ArrowheadSystem arrowheadSystem = new ArrowheadSystem("BUTE", "Demo", "127.0.0.1", "8080",
			"authenticationInfo");
	private static OrchestrationForm providerForm = null;
	private Map<String, Boolean> orchestrationFlags = new HashMap<>();

	public TemperatureConsumer() {
		super();
		orchestrationFlags.put("Matchmaking", false);
		orchestrationFlags.put("ExternalServiceRequest", false);
		orchestrationFlags.put("TriggerInterCloud", false);
		orchestrationFlags.put("MetadataSearch", false);
		orchestrationFlags.put("PingProvider", false);
	}

	@GET
	@Path("/consumer/invoke")
	public String invokeConsume() {
		ServiceRequestForm serviceRequestForm = new ServiceRequestForm();
		
		serviceRequestForm.setRequestedService(getTemperatureService());
		serviceRequestForm.setRequestedQoS("BEST_EFFORT");
		serviceRequestForm.setRequesterSystem(this.arrowheadSystem);
		serviceRequestForm.setOrchestrationFlags(orchestrationFlags);
		
		getOrchestrationResponse(serviceRequestForm);
		
		return "This is the Client consume stub.";
	}

	@GET
	@Path("/consumer/query")
	public String queryProvider() {
		return getCurrentTemperature();
	}

	private String getCurrentTemperature() {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri(providerForm.getServiceURI()).path("current").build();

		WebTarget target = client.target(uri);
		Response response = target.request().get();

		return response.readEntity(String.class);
	}

	private void getOrchestrationResponse(ServiceRequestForm serviceRequestForm) {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri(coreURI).path("orchestrator").path("orchestration").build();

		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json")
				.put(Entity.json(serviceRequestForm));

		providerForm = response.readEntity(OrchestrationResponse.class).getResponse().get(0);

		return;
	}

	private ArrowheadService getTemperatureService() {
		ArrowheadService temperatureService = new ArrowheadService();
		ArrayList<String> interfaces = new ArrayList<String>();

		temperatureService.setServiceGroup("Temperature");
		temperatureService.setServiceDefinition("IndoorTemperature");
		temperatureService.setMetaData("Dummy metadata");
		interfaces.add("REST_JSON");
		temperatureService.setInterfaces(interfaces);

		return temperatureService;
	}

}
