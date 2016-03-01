package eu.arrowhead.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
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
	
	
	/////////////////////////////////////////////
	//// CONFIG /////////////////////////////////
	/////////////////////////////////////////////
	private String target_IP = "152.66.245.167";
	private String serviceGroup = "TemperatureProba100";
	private String serviceDefinition = "IndoorTemperatureProba100";
	/////////////////////////////////////////////

	/**
	 * Consumer's ArrowheadSystem.
	 */
	private ArrowheadSystem arrowheadSystem = new ArrowheadSystem("PROBA", "ConsumerSystem", "localhost", "8080",
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
	public Response invokeConsumer() {
		ServiceRequestForm serviceRequestForm = new ServiceRequestForm();
		Map<String, Boolean> orchestrationFlags = new HashMap<>();
		Response response = null;

		// Preparing ServiceRequestForm
		serviceRequestForm.setRequestedService(getTemperatureService());
		serviceRequestForm.setRequestedQoS("BEST_EFFORT");
		serviceRequestForm.setRequesterSystem(this.arrowheadSystem);

		// Preparing Orchestration Flags for the ServiceRequestForm
		orchestrationFlags.put("matchmaking", false);
		orchestrationFlags.put("externalServiceRequest", false);
		orchestrationFlags.put("triggerInterCloud", true);
		orchestrationFlags.put("metadataSearch", false);
		orchestrationFlags.put("pingProvider", false);

		serviceRequestForm.setOrchestrationFlags(orchestrationFlags);

		// Invoke the orchestration process and store the reponse
		response = getOrchestrationResponse(serviceRequestForm);

		return response;
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
		if (providerForm == null) {
			return "No orchestrated service data could be found.";
		}
		
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
		URI uri = UriBuilder.fromPath(
				"http://" + providerForm.getProvider().getIPAddress() + ":" + providerForm.getProvider().getPort())
				.path(providerForm.getServiceURI()).build();
		System.out.println("Querying: " + uri.toString());

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
	private Response getOrchestrationResponse(ServiceRequestForm serviceRequestForm) {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromPath("http://" + target_IP + ":" + "8080").path("core").path("orchestrator")
				.path("orchestration").build();

		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json")
				.post(Entity.json(serviceRequestForm));

		try {
			for (OrchestrationForm form : response.readEntity(OrchestrationResponse.class).getResponse()) {
				if (form.getService().getServiceDefinition().equals(serviceDefinition)) {
					providerForm = form;
					System.out.println(form.getServiceURI());
				}
			}
			
			//providerForm = response.readEntity(OrchestrationResponse.class).getResponse().get(0);
			System.out.println("Provider Form saved successfully.");
		} catch (ProcessingException e) {
			System.out.println("Processing exception in consumer: " + e.getMessage());
		}

		return response;
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

		temperatureService.setServiceGroup(serviceGroup);
		temperatureService.setServiceDefinition(serviceDefinition);
		temperatureService.setMetaData("Dummy metadata");
		interfaces.add("RESTJSON");
		temperatureService.setInterfaces(interfaces);

		return temperatureService;
	}

}
