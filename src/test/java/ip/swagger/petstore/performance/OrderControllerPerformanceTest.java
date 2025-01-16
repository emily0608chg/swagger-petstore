package ip.swagger.petstore.performance;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.util.HashMap;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class OrderControllerPerformanceTest extends BasePerformanceTest {

    // Full order lifecycle scenario
    private ScenarioBuilder fullOrderLifecycleScenario = scenario("Full Order Lifecycle")
            // Step 1: Place Order and save orderId
            .exec(
                    http("Place Order") // Placing the order request
                            .post("/store/order")
                            .body(StringBody(session -> {
                                HashMap<String, Object> order = new HashMap<>();
                                int dynamicId = (int) (Math.random() * 1000); // Generate a random integer for the ID
                                order.put("id", dynamicId);
                                order.put("petId", 123); // Example pet ID
                                order.put("quantity", 1);
                                order.put("shipDate", "2023-12-01T00:00:00.000+00:00");
                                order.put("status", "placed");
                                order.put("complete", true);
                                try {
                                    return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(order);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }))
                            .asJson()
                            .check(status().is(200)) // Verify request succeeded (HTTP 200)
                            .check(jsonPath("$.id").saveAs("orderId")) // Save the dynamic orderId to the session
                            .check(bodyString().saveAs("responseBody")) // Save the entire response for debugging
            )
            // Debug step: Log the response & verify orderId was saved
            .exec(session -> {
                System.out.println("--- Debugging Place Order ---");
                System.out.println("Response Body: " + session.getString("responseBody")); // Log response
                if (session.contains("orderId")) {
                    System.out.println("Order ID saved: " + session.getString("orderId"));
                } else {
                    System.out.println("Order ID not found in response!");
                }
                return session;
            })
            .exec(
                    http("Get Order By ID")
                            .get("/store/order/#{orderId}")
                            .check(status().is(200)) // Ensure the response is successful
                            .check(jsonPath("$.id").saveAs("responseOrderId")) // Save the response id to compare manually
                            .check(bodyString().saveAs("getOrderResponse")) // Save the full response for debugging
            )
            .exec(session -> {
                // Compare orderId stored in the session with the one in the response
                int savedOrderId = Integer.parseInt(session.getString("orderId"));
                int responseOrderId = Integer.parseInt(session.getString("responseOrderId"));

                if (savedOrderId != responseOrderId) {
                    throw new RuntimeException(
                            String.format("Order ID mismatch! Expected: %d, Got: %d", savedOrderId, responseOrderId)
                    );
                }

                System.out.println("--- Debugging Get Order By ID ---");
                System.out.println("Get Order Response: " + session.getString("getOrderResponse"));
                System.out.println("Order ID Matches: " + savedOrderId);
                return session;
            })
            // Step 3: Delete Order
            .exec(
                    http("Delete Order")
                            .delete("/store/order/#{orderId}") // Use saved orderId
                            .check(status().is(200)) // Ensure order is deleted
                            .check(bodyString().saveAs("deleteResponse")) // Save response for debugging
            )
            .exec(session -> {
                System.out.println("--- Debugging Delete Order ---");
                System.out.println("Delete Response: " + session.getString("deleteResponse")); // Log response
                return session;
            })
            // Step 4: Verify Deletion
            .exec(
                    http("Verify Deletion")
                            .get("/store/order/#{orderId}") // Try to retrieve the deleted order
                            .check(status().is(404)) // Verify the order is no longer found
                            .check(bodyString().is("Order not found")) // Optional: check error message
                            .check(bodyString().saveAs("verifyDeletionResponse")) // Save response for debugging
            )
            .exec(session -> {
                System.out.println("--- Debugging Verify Deletion ---");
                System.out.println("Verify Deletion Response: " + session.getString("verifyDeletionResponse"));
                return session;
            });

    // Simulation setup with independent injection profiles
    {
        setUp(
                // 5 users all at once
                fullOrderLifecycleScenario.injectOpen(atOnceUsers(5)).protocols(httpConf),

                // Ramp up to 100 users over 30 seconds as a separate profile
                fullOrderLifecycleScenario.injectOpen(rampUsers(100).during(30)
                        ).protocols(httpConf)
        );
    }
}