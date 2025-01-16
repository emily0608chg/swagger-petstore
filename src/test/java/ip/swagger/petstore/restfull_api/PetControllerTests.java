package ip.swagger.petstore.restfull_api;


import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PetControllerTests extends BaseAPITest {

    @BeforeMethod
    public void ensurePetExists() {
        /// Verify if the pet already exist
        Response response = RestAssured
                .given()
                .when()
                .get("/pet/1002");

        if (response.getStatusCode() == 404) {
            /// if not it create it
            Response createResponse = RestAssured
                    .given()
                    .contentType("application/json")
                    .body("{\"id\": 1002, \"name\": \"Fluffy\", \"status\": \"available\"}")
                    .when()
                    .post("/pet");
            Assert.assertEquals(createResponse.getStatusCode(), 200, "The test pet could not be created.");
        }
    }

    @Test
    void testFindPetsByStatus() {
        /// Search for pets by state and validate that all those found are in that state
        given()
                .queryParam("status", "available")
                .when()
                .get("/pet/findByStatus")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Make sure there is at least one result
                .body("status", everyItem(equalTo("available"))); // Validate that all results have "status" -> "available"
    }

    @Test
    void testGetPetById() {
        long petId = 1002;

        /// Get the pet's information by ID and validate all attributes
        given()
                .pathParam("petId", petId)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(200)
                .body("id", equalTo((int) petId)) /// Pet ID
                .body("name", equalTo("Fluffy")) /// Pet Name
                .body("status", equalTo("available")) /// State
                .body("photoUrls", hasSize(0)) /// Check that there are no images uploaded
                .body("tags", hasSize(0)); /// Verify that there are no associated tags
    }

    @Test
    void testUpdatePet() {
        String updatedRequestBody = """
                    {
                        "id": 1002,
                        "name": "Updated Pet Name",
                        "photoUrls": ["https://www.google.com/imgres?imgurl=https%3A%2F%2Fthumbs.dreamstime.com%2Fb%2Fportrait-cute-brown-white-puppy-dog-welsh-pembroke-corgi-looking-camera-sitting-legs-unrecognizable-woman-wearing-291822934.jpg&tbnid=twPxc-Qk8H9TKM&vet=10CAoQxiAoAWoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc..i&imgrefurl=https%3A%2F%2Fwww.dreamstime.com%2Fphotos-images%2Fwoman-petting-dog.html%3Fpg%3D33&docid=oQnEyItI05E8GM&w=800&h=533&itg=1&q=photos%20pet&ved=0CAoQxiAoAWoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc", "https://www.google.com/imgres?imgurl=https%3A%2F%2Fwww.eurogroupforanimals.org%2Ffiles%2Feurogroupforanimals%2Fglazed_builder_images%2FPuppyDogvertical.jpg%3Ffid%3D6500&tbnid=6UK1DSvaTSk7vM&vet=10CBIQxiAoAmoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc..i&imgrefurl=https%3A%2F%2Fwww.eurogroupforanimals.org%2Fwhat-we-do%2Fareas-of-concern%2Fonline-pet-trade&docid=6sWDRmzE2WvdLM&w=1920&h=2880&itg=1&q=photos%20pet&ved=0CBIQxiAoAmoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc"],
                        "status": "sold"
                    }
                """;

        /// Update the pet and validate the response
        given()
                .contentType("application/json")
                .body(updatedRequestBody)
                .when()
                .put("/pet")
                .then()
                .statusCode(200)
                .body("id", equalTo(1002)) /// Pet Id
                .body("name", equalTo("Updated Pet Name")) /// Update pet name
                .body("status", equalTo("sold")) /// Status
                .body("photoUrls", hasSize(2)) /// Validate that there are 2 images
                .body("photoUrls", contains("https://www.google.com/imgres?imgurl=https%3A%2F%2Fthumbs.dreamstime.com%2Fb%2Fportrait-cute-brown-white-puppy-dog-welsh-pembroke-corgi-looking-camera-sitting-legs-unrecognizable-woman-wearing-291822934.jpg&tbnid=twPxc-Qk8H9TKM&vet=10CAoQxiAoAWoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc..i&imgrefurl=https%3A%2F%2Fwww.dreamstime.com%2Fphotos-images%2Fwoman-petting-dog.html%3Fpg%3D33&docid=oQnEyItI05E8GM&w=800&h=533&itg=1&q=photos%20pet&ved=0CAoQxiAoAWoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc", "https://www.google.com/imgres?imgurl=https%3A%2F%2Fwww.eurogroupforanimals.org%2Ffiles%2Feurogroupforanimals%2Fglazed_builder_images%2FPuppyDogvertical.jpg%3Ffid%3D6500&tbnid=6UK1DSvaTSk7vM&vet=10CBIQxiAoAmoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc..i&imgrefurl=https%3A%2F%2Fwww.eurogroupforanimals.org%2Fwhat-we-do%2Fareas-of-concern%2Fonline-pet-trade&docid=6sWDRmzE2WvdLM&w=1920&h=2880&itg=1&q=photos%20pet&ved=0CBIQxiAoAmoXChMI8IOsv_f3igMVAAAAAB0AAAAAEAc"));
    }

    @Test
    public void uploadPetImageTest() {
        /// File to upload from the resources folder
        File file = new File("src/test/resources/download.jpeg");
        Assert.assertTrue(file.exists(), "El archivo no existe en la ruta especificada: " + file.getAbsolutePath());

        /// Configure RestAssured to remove character set in Content-Type
        RestAssured.config = RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));

        /// Upload the image
        given()
                .contentType("application/octet-stream")
                .body(file)
                .when()
                .post("/pet/1002/uploadImage")
                .then()
                .statusCode(200)
                .body("id", equalTo(1002)) /// Validate that the ID corresponds to the pet
                .body("photoUrls", not(empty())); /// Validate that at least one URL was added

        /// Confirm that the pet now has associated images
        given()
                .pathParam("petId", 1002)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(200)
                .body("photoUrls", not(empty())); /// Validate that there are image URLs in the mascot
    }

    @Test
    void testFindPetByInvalidId() {
        long invalidPetId = 999999;

        /// Try to fetch a pet with a non-existing ID
        given()
                .pathParam("petId", invalidPetId)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(404) // Verifies the status code is 404
                .body(equalTo("Pet not found")); // Verifies the API response message
    }

    @Test
    void testUploadImageWithoutFile() {
        /// Configure RestAssured
        RestAssured.config = RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));

        given()
                .contentType("application/octet-stream")
                .body(new byte[0])
                .when()
                .post("/pet//uploadImage")
                .then()
                .statusCode(404);
    }

    @Test
    void testFindPetsWithInvalidStatus() {
        given()
                .queryParam("status", "")
                .when()
                .get("/pet/findByStatus")
                .then()
                .statusCode(500);
    }

    @AfterTest
    void testDeletePet() {
        long petId = 1002;

        /// Delete Pet
        given()
                .pathParam("petId", petId)
                .when()
                .delete("/pet/{petId}")
                .then()
                .statusCode(200)
                .body(equalTo("Pet deleted"));

        /// Verify Pet was deleted
        given()
                .pathParam("petId", petId)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(404)
                .body(equalTo("Pet not found"));
    }
}
