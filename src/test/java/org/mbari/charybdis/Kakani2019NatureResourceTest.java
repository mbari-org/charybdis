package org.mbari.charybdis;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class Kakani2019NatureResourceTest {

    @Test
    void testN0Endpoint() {
        given()
        .when().get("/n0")
        .then()
        .statusCode(200);
    }
}
