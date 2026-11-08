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

@Feature("Аутентификация по API-ключу")
public class ApiKeyTest extends BaseTest {

    @Test
    @Story("Отсутствие API-ключа")
    @DisplayName("Запрос без X-Api-Key возвращает ошибку и не вызывает внешний сервис")
    @Description("При отсутствии заголовка X-Api-Key сервис возвращает ошибку {result=ERROR} "
            + "и не обращается к внешнему сервису")
    void shouldReturnErrorWhenNoApiKey() {

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .formParam("token", token)
                .formParam("action", "LOGIN")
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
    @Story("Неверный API-ключ")
    @Disabled("Отсутствует ответ при неверном X-Api-Key (TimeoutException)")
    @DisplayName("[БАГ-2] Неверный X-Api-Key: приложение не возвращает ответ")
    @Description("Дефект: при неверном заголовке X-Api-Key приложение не даёт ответ "
            + "(RestAssured падает по SocketTimeoutException")
    void shouldReturnErrorWhenWrongApiKey() {

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", "wrong-key")
                .formParam("token", token)
                .formParam("action", "LOGIN")
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