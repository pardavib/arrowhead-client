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

	/**
	 * Consumer's ArrowheadSystem.
	 */
	private ArrowheadSystem arrowheadSystem = new ArrowheadSystem("BUTE", "ConsumerSystem", "localhost", "8080",
			"authenticationInfo");

	/**
	 * Stores the OrchestrationForm obtained from the Orchestrator service.
	 */
	private static OrchestrationForm providerForm = null;

	/**
	 * Default constructor.
	 */
	public TemperatureConsumer() {
		super();
	}

	/**
	 * This function invokes the consumer to contact the Orchestration service
	 * for a suitable Temperature Provider.
	 * 
	 * @return String
	 */
	@GET
	@Path("/invoke")
	public String invokeConsumer() {
		ServiceRequestForm serviceRequestForm = new ServiceRequestForm();
		Map<String, Boolean> orchestrationFlags = new HashMap<>();

		// Preparing ServiceRequestForm
		serviceRequestForm.setRequestedService(getTemperatureService());
		serviceRequestForm.setRequestedQoS("BEST_EFFORT");
		serviceRequestForm.setRequesterSystem(this.arrowheadSystem);

		// Preparing Orchestration Flags for the ServiceRequestForm
		orchestrationFlags.put("Matchmaking", false);
		orchestrationFlags.put("ExternalServiceRequest", false);
		orchestrationFlags.put("TriggerInterCloud", false);
		orchestrationFlags.put("MetadataSearch", false);
		orchestrationFlags.put("PingProvider", false);

		serviceRequestForm.setOrchestrationFlags(orchestrationFlags);

		// Invoke the orchestration process and store the reponse
		getOrchestrationResponse(serviceRequestForm);

		return "Provider successfully obtained from Orchestrator.";
	}

	/**
	 * This function invokes the consumer to query the TemperatureProvider for
	 * the current temperature data.
	 * 
	 * @return String
	 */
	@GET
	@Path("/query")
	public String queryProvider() {
		return getCurrentTemperature();
	}

	/**
	 * This function handles the necessary communication through REST to get the
	 * current temperature data.
	 * 
	 * @return String Temperature data.
	 */
	private String getCurrentTemperature() {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri(providerForm.getServiceURI()).path("current").build();

		WebTarget target = client.target(uri);
		Response response = target.request().get();

		return response.readEntity(String.class);
	}

	/**
	 * This function handles the necessary communication through REST to get the
	 * orchestration response from the Orchestrator service.
	 * 
	 * @return void
	 */
	private void getOrchestrationResponse(ServiceRequestForm serviceRequestForm) {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri(this.arrowheadSystem.getIPAddress() + ":" + this.arrowheadSystem.getPort())
				.path("orchestrator").path("orchestration").build();

		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json")
				.post(Entity.json(serviceRequestForm));

		providerForm = response.readEntity(OrchestrationResponse.class).getResponse().get(0);

		return;
	}

	/**
	 * This function provides an ArrowheadService required to create a suitable
	 * ServiceRequestForm.
	 * 
	 * @return ArrowheadService
	 */
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
