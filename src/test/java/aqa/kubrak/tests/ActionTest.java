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

@Feature("Выполнение действия (ACTION)")
public class ActionTest extends BaseTest {

    @Test
    @Story("ACTION без LOGIN")
    @DisplayName("ACTION без предварительного LOGIN возвращает 403 и не вызывает внешний сервис")
    @Description("Выполнение ACTION для токена, не прошедшего LOGIN, возвращает 403 c {result: ERROR}, "
            + "внешний сервис не вызывается")
    void shouldNotPerformActionWithoutLogin() {

        wireMockServer.stubFor(post(urlPathMatching(AUTH_PATH))
                .willReturn(aResponse().withStatus(200)));
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

        verify(0, postRequestedFor(urlPathMatching(AUTH_PATH)));
        verify(0, postRequestedFor(urlPathMatching(DO_ACTION_PATH)));
    }

    @Test
    @Story("ACTION с ошибкой внешнего сервиса")
    @Disabled("Приложение не отвечает при 5xx от внешнего сервиса /doAction. (TimeoutException)")
    @DisplayName("[БАГ-1] ACTION при 5xx от /doAction: приложение не возвращает ответ")
    @Description("Дефект: если /doAction отвечает 5xx, приложение не даёт ответ "
            + "(RestAssured падает по SocketTimeoutException")
    void shouldNotPerformActionWhenDoActionFails() {

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
                .withRequestBody(containing("token=" + token)));

        wireMockServer.stubFor(post(urlPathMatching(DO_ACTION_PATH))
                .willReturn(aResponse().withStatus(500)));

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", "ACTION")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(backendFailure())
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(postRequestedFor(urlPathMatching(DO_ACTION_PATH))
                .withRequestBody(containing("token=" + token)));
    }
}