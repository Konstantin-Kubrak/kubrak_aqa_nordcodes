package aqa.kubrak.tests;

import aqa.kubrak.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Feature("Валидация действия")
public class ActionValidationTest extends BaseTest {

    @ParameterizedTest(name = "action=\"{0}\"")
    @ValueSource(strings = {"", "login", "logout", "ACTIONS"})
    @Story("Невалидное действие")
    @DisplayName("Невалидное действие отклоняется и не вызывает внешний сервис")
    @Description("Действие, не входящее в LOGIN/ACTION/LOGOUT, возвращает ошибку {result=ERROR} " +
            "и не обращается ко внешнему сервису")
    void shouldRejectInvalidAction(String invalidAction) {

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .formParam("action", invalidAction)
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(clientError())
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(0, postRequestedFor(urlPathMatching(AUTH_PATH)));
        verify(0, postRequestedFor(urlPathMatching(DO_ACTION_PATH)));
    }

    @Test
    @Story("Отсутствующее действие")
    @DisplayName("Запрос с отсутствующим параметром action отклоняется сервисом")
    @Description("Если параметр action не передан, сервис возвращает ошибку {result=ERROR} и не обращается ко внешнему сервису")
    void shouldRejectMissingAction() {

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", token)
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(clientError())
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(0, postRequestedFor(urlPathMatching(AUTH_PATH)));
        verify(0, postRequestedFor(urlPathMatching(DO_ACTION_PATH)));
    }
}