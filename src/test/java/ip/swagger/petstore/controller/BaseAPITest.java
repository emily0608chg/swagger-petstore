package ip.swagger.petstore.controller;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeTest;

public class BaseAPITest {


    @BeforeTest
     void setup() {
        String baseURI = System.getProperty("BASE_URI", "http://localhost");
        int port = Integer.parseInt(System.getProperty("PORT", "8080"));
        String basePath = System.getProperty("BASE_PATH", "/api/v3");
        String apiKey = System.getProperty("API_KEY", "default_api_key_value");

        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        RestAssured.basePath = basePath;

        // Optionally, set default headers
        RestAssured.requestSpecification = RestAssured.given()
                .header("api_key", apiKey); // Default API key
    }
}
