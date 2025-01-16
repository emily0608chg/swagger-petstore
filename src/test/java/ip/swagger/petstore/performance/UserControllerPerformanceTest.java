package ip.swagger.petstore.performance;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class UserControllerPerformanceTest extends BasePerformanceTest {

    /// Pre-generate a unique dynamic user ID and username before starting the scenarios
    private final int dynamicUserId = (int) (Math.random() * 10000); // Example: unique random ID for the user
    private final String dynamicUsername = "user" + dynamicUserId;   // Unique username based on the ID
    private final String dynamicEmail = dynamicUsername + "@example.com"; // Email based on the username

    /// Scenario: Create, Retrieve, and Validate a User
    ScenarioBuilder createAndManageUserScenario = scenario("Create, Retrieve, and Validate User Scenario")
            /// Create a new user with the pre-generated dynamic ID
            .exec(
                    http("Create a New User")
                            .post("/user")
                            .body(StringBody(
                                    """
                                    {
                                        "id": %d,
                                        "username": "%s",
                                        "firstName": "PerformanceFirstName",
                                        "lastName": "PerformanceLastName",
                                        "email": "%s",
                                        "password": "securePassword1!",
                                        "phone": "1234567890",
                                        "userStatus": 1
                                    }
                                    """.formatted(dynamicUserId, dynamicUsername, dynamicEmail) // Inject pre-generated values
                            )).asJson()
                            .check(status().is(200),
                                    jsonPath("$.id").is(String.valueOf(dynamicUserId)), // Verify user creation
                                    jsonPath("$.username").is(dynamicUsername))
            )
            .pause(1) /// Add small pause for consistency

            /// Retrieve the created user by username
            .exec(
                    http("Get User by Username")
                            .get("/user/" + dynamicUsername) /// Use the dynamic username directly
                            .check(status().is(200),
                                    jsonPath("$.id").is(String.valueOf(dynamicUserId)), // Ensure correct user is retrieved
                                    jsonPath("$.email").is(dynamicEmail))
            )
            .pause(1) /// Add a pause before the next step

            /// Update user information
            .exec(
                    http("Update User Information")
                            .put("/user/" + dynamicUsername) /// Update user by dynamic username
                            .body(StringBody(
                                    """
                                    {
                                        "id": %d,
                                        "username": "%s",
                                        "firstName": "UpdatedFirstName",
                                        "lastName": "UpdatedLastName",
                                        "email": "%s",
                                        "password": "newSecurePassword2!",
                                        "phone": "9876543210",
                                        "userStatus": 1
                                    }
                                    """.formatted(dynamicUserId, dynamicUsername, dynamicEmail)
                            )).asJson()
                            .check(status().is(200),
                                    jsonPath("$.firstName").is("UpdatedFirstName")) /// Ensure firstName is correctly updated
            );

    /// Define test execution logic
    @Override
    public void before() {
        System.out.println("Starting UserController Performance Test with dynamic ID: "
                + dynamicUserId + ", username: " + dynamicUsername + ", email: " + dynamicEmail);
    }

    {
        /// Set up population workloads for the scenarios
        PopulationBuilder userLoad = createAndManageUserScenario.injectOpen(rampUsers(5).during(10));

        setUp(userLoad).protocols(httpConf);
    }
}