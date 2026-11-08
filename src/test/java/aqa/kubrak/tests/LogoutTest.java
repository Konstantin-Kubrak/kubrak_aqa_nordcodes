package aqa.kubrak.tests;

import aqa.kubrak.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Feature("Завершение сессии (LOGOUT)")
public class LogoutTest extends BaseTest {

    @Test
    @Story("LOGOUT с активной сессией")
    @DisplayName("LOGOUT удаляет токен, после чего ACTION недоступен")
    @Description("После успешного LOGOUT токен удаляется из хранилища: " +
            "LOGOUT возвращает {result: OK}, а последующий ACTION возвращает 403 {result: ERROR} без вызова /doAction")
    void shouldLogoutAndRemoveToken() {

        wireMockServer.stubFor(post(urlPathMatching(AUTH_PATH))
                .willReturn(aResponse().withStatus(200)));

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(200)
                .body("result", is("OK"));

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", "LOGOUT")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(200)
                .body("result", is("OK"));

        wireMockServer.stubFor(post(urlPathMatching(DO_ACTION_PATH))
                .willReturn(aResponse().withStatus(200)));

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", "ACTION")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(403)
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(0, postRequestedFor(urlPathMatching(DO_ACTION_PATH)));
    }

    @Test
    @Story("LOGOUT без LOGIN")
    @DisplayName("LOGOUT для неизвестного токена не вызывает внешний сервис")
    @Description("LOGOUT для токена, не проходившего LOGIN, не обращается ко внешнему сервису.")
    void shouldLogoutWithoutLogin() {

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", "LOGOUT")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(anyOf(is(200), clientError()));

        verify(0, postRequestedFor(urlPathMatching(AUTH_PATH)));
        verify(0, postRequestedFor(urlPathMatching(DO_ACTION_PATH)));
    }
}