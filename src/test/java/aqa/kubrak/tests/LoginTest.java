package aqa.kubrak.tests;

import aqa.kubrak.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Feature("Аутентификация (LOGIN)")
public class LoginTest extends BaseTest {

    @Test
    @Story("Успешный LOGIN")
    @DisplayName("Успешный LOGIN сохраняет токен, после чего ACTION выполняется")
    @Description("При успешном ответе /auth токен сохраняется, LOGIN возвращает {result: OK}, "
            + "последующий ACTION успешно вызывает /doAction с тем же токеном")
    void shouldLoginSuccessfully() {

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

        verify(postRequestedFor(urlPathMatching(AUTH_PATH))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(containing("token=" + token)));

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
                .statusCode(200)
                .body("result", is("OK"));

        verify(postRequestedFor(urlPathMatching(DO_ACTION_PATH))
                .withRequestBody(containing("token=" + token)));
    }

    @Test
    @Story("Неуспешный LOGIN")
    @Disabled("Приложение не отвечает при 5xx от внешнего сервиса /auth. (TimeoutException)")
    @DisplayName("[БАГ-1] LOGIN при 5xx от /auth: приложение не возвращает ответ")
    @Description("Дефект: если /auth отвечает 5xx, приложение не даёт ответ. "
            + "(RestAssured падает по SocketTimeoutException")
    void shouldNotLoginWhenAuthFails() {

        wireMockServer.stubFor(post(urlPathMatching(AUTH_PATH))
                .willReturn(aResponse().withStatus(500)));

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(backendFailure())
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(postRequestedFor(urlPathMatching(AUTH_PATH)));

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
}