package ip.swagger.petstore.performance;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class PetControllerPerformanceTest extends BasePerformanceTest {

    /// Generate a dynamic ID for the Pet before starting the scenarios
    private int dynamicPetId = (int) (Math.random() * 10000); // Generate a random ID between 0 and 9999

    /// Scenario for finding pets by status
    ScenarioBuilder findPetsByStatusScenario = scenario("Find Pets by Status Scenario")
            .exec(
                    http("Find Pets by Status: Available")
                            .get("/pet/findByStatus")
                            .queryParam("status", "available")
                            .check(status().is(200))
            );

    /// Scenario for creating and managing a pet
    ScenarioBuilder createAndManagePetScenario = scenario("Create and Manage Pet Scenario")
            .exec(
                    http("Create a New Pet")
                            .post("/pet")
                            .body(StringBody(
                                    """
                                    {
                                        "id": %d,
                                        "name": "PerformancePet",
                                        "status": "available"
                                    }
                                    """.formatted(dynamicPetId) /// Inject the dynamic ID
                            )).asJson()
                            .check(status().is(200),
                                    jsonPath("$.id").is(String.valueOf(dynamicPetId))) /// Check ID matches the generated ID
            )
            .pause(1)

            /// Retrieve the pet using dynamically saved ID
            .exec(
                    http("Get Pet by ID")
                            .get("/pet/%d".formatted(dynamicPetId)) /// Use the dynamically ID
                            .check(status().is(200),
                                    jsonPath("$.id").is(String.valueOf(dynamicPetId))) /// Validate dynamic ID matches
            )
            .pause(1)

            /// Update the pet information using the dynamic ID
            .exec(
                    http("Update Pet Information")
                            .put("/pet")
                            .body(StringBody(
                                    """
                                    {
                                        "id": %d,
                                        "name": "UpdatedPetName",
                                        "status": "sold",
                                        "photoUrls": ["https://cdn.pixabay.com/photo/2024/03/04/16/38/cat-8612685_1280.jpg"]
                                    }
                                    """.formatted(dynamicPetId) /// Inject the same dynamic ID
                            )).asJson()
                            .check(status().is(200),
                                    jsonPath("$.name").is("UpdatedPetName")) /// Validate the update is successful
            );

    @Override
    public void before() {
        System.out.println("Starting PetController Performance Test with dynamic ID: " + dynamicPetId);
    }

    {
        /// Configure the workload scenarios
        PopulationBuilder findPetsLoad = findPetsByStatusScenario.injectOpen(rampUsers(50).during(20));
        PopulationBuilder createAndManagePetLoad = createAndManagePetScenario.injectOpen(atOnceUsers(10));

        setUp(findPetsLoad, createAndManagePetLoad)
                .protocols(httpConf);
    }
}

