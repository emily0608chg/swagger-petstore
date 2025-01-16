package ip.swagger.petstore.performance;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.http.HttpDsl.http;

public class BasePerformanceTest extends Simulation {

    /// Define the base HTTP configuration
    protected HttpProtocolBuilder httpConf;

    public BasePerformanceTest() {
        /// Read system properties or use default values
        String baseURI = System.getProperty("BASE_URI", "http://localhost");
        String basePath = System.getProperty("BASE_PATH", "/api/v3");
        int port = Integer.parseInt(System.getProperty("PORT", "8080"));

        /// Default API key
        String apiKey = System.getProperty("API_KEY", "default_api_key_value");

        /// Base HTTP configuration for Gatling
        httpConf = http
                .baseUrl(baseURI + ":" + port + basePath)
                .acceptHeader("application/json")
                .contentTypeHeader("application/json")
                .header("api_key", apiKey)               /// Attach default API key
                .shareConnections(); /// Reuse connections for better performance
    }
}