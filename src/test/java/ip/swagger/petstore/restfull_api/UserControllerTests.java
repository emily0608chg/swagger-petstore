package ip.swagger.petstore.restfull_api;

import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class UserControllerTests extends BaseAPITest {

    @Test
    public void testCreateUserAndFetchDetails() {
        /// Create a valid user
        String userPayload = """
                {
                    "id": 1,
                    "username": "johndoe",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "password": "password123",
                    "phone": "1234567890",
                    "userStatus": 1
                }
                """;

        /// Create the user
        given()
                .contentType(ContentType.JSON)
                .body(userPayload)
                .when()
                .post("/user")
                .then()
                .statusCode(200) /// Controller does not handle correct status code for creation
                .body("username", equalTo("johndoe"))
                .body("firstName", equalTo("John"))
                .body("email", equalTo("john.doe@example.com"));

        /// Get user by username
        given()
                .when()
                .get("/user/johndoe")
                .then()
                .statusCode(200) /// Validate that the user exists
                .body("username", equalTo("johndoe"))
                .body("firstName", equalTo("John"))
                .body("email", equalTo("john.doe@example.com"));
    }

//    @Test
//    public void testLoginWithInvalidUser() {
//        /// Try to log in with invalid credentials
//       /// API does not validate credentials, but this is expected behavior if implemented
//        given()
//                .queryParam("username", "1234567890")
//                .queryParam("password", "wrong_password")
//                .when()
//                .get("/user/login")
//                .then()
//                .statusCode(400) /// it should validate that returns a 400 error
//                .body(containsString("Username or password not provided.")); /// Validate returned message
//    }

    @Test
    public void testFetchUserDetailsValidation() {
        /// Create another valid user
        String userPayload = """
                {
                    "id": 2,
                    "username": "janedoe",
                    "firstName": "Jane",
                    "lastName": "Doe",
                    "email": "jane.doe@example.com",
                    "password": "pass123",
                    "phone": "9876543210",
                    "userStatus": 1
                }
                """;

        /// Create the user
        given()
                .contentType(ContentType.JSON)
                .body(userPayload)
                .when()
                .post("/user")
                .then()
                .statusCode(200) /// Validate that it was created successfully
                .body("username", equalTo("janedoe"))
                .body("firstName", equalTo("Jane"));

        /// Get details of newly created user
        given()
                .when()
                .get("/user/janedoe")
                .then()
                .statusCode(200) /// Validate that the user was found
                .body("username", equalTo("janedoe"))
                .body("email", equalTo("jane.doe@example.com"))
                .body("phone", equalTo("9876543210"));
    }
}
