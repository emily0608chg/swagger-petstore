package ip.swagger.petstore.restfull_api;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class OrderControllerTests extends BaseAPITest {

    private long testOrderId;

    @BeforeMethod
    public void createTestOrder() {
        // Set up a test order to ensure it exists before running tests
        String requestBody = """
                {
                    "id": 2001,
                    "petId": 1002,
                    "quantity": 3,
                    "shipDate": "2023-12-01T00:00:00.000+00:00",
                    "status": "placed",
                    "complete": true
                }
                """;

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/store/order");

        Assert.assertEquals(response.statusCode(), 200, "Failed to create test order");
        testOrderId = response.jsonPath().getLong("id");
    }

    @Test
    void testCreateOrder() {
        String requestBody = """
                {
                    "id": 2002,
                    "petId": 1003,
                    "quantity": 2,
                    "shipDate": "2023-12-02T00:00:00.000+00:00",
                    "status": "placed",
                    "complete": true
                }
                """;

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/store/order")
                .then()
                .statusCode(200)
                .body("id", equalTo(2002))
                .body("petId", equalTo(1003))
                .body("quantity", equalTo(2))
                .body("status", equalTo("placed"))
                .body("complete", equalTo(true));
    }

    @Test
    void testGetOrderById() {
        // Fetch the test order created in @BeforeMethod
        given()
                .pathParam("orderId", testOrderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(200)
                .body("id", equalTo((int) testOrderId))
                .body("petId", equalTo(1002))
                .body("quantity", equalTo(3))
                .body("status", equalTo("placed"))
                .body("complete", equalTo(true));
    }

    @Test
    void testGetOrderByInvalidId() {
        long invalidOrderId = 999999;

        given()
                .pathParam("orderId", invalidOrderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(404)
                .body(equalTo("Order not found"));
    }

    @Test
    void testDeleteOrder() {
        // Delete the test order
        given()
                .pathParam("orderId", testOrderId)
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .statusCode(200)
                .body(isEmptyOrNullString());

        // Attempt to fetch the deleted order to confirm deletion
        given()
                .pathParam("orderId", testOrderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(404)
                .body(equalTo("Order not found"));
    }

    @AfterMethod
    public void cleanupOrders() {
        // Clean up the created orders after each test if they still exist
        try {
            given()
                    .pathParam("orderId", testOrderId)
                    .delete("/store/order/{orderId}");
        } catch (Exception e) {
            // Ignore if already deleted
        }
    }
}
