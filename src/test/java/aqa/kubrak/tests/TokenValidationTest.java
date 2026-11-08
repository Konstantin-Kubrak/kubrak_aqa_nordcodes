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

@Feature("Валидация токена")
public class TokenValidationTest extends BaseTest {

    @ParameterizedTest(name = "token=\"{0}\"")
    @ValueSource(strings = {
            "",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234",  // 31 символ
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456",// 33 символа
            "abcdefghijklmnopqrstuvwxyz012345", // строчные буквы
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234!", // спецсимвол
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234 ", // пробел в конце
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234-", // дефис
            "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG", // 32 символа, но не hex
    })
    @Story("Невалидный токен")
    @DisplayName("Невалидный токен отклоняется и не вызывает внешний сервис")
    @Description("Сервис, при отправке токена не соответствующего формату ^[0-9A-F]{32}$, возвращает {result=ERROR} "
            + "и не обращается ко внешнему сервису")
    void shouldRejectInvalidToken(String invalidToken) {
        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("token", invalidToken)
                .formParam("action", "LOGIN")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(clientError())
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(0, postRequestedFor(urlPathMatching(AUTH_PATH)));
    }

    @Test
    @Story("Отсутствующий токен")
    @DisplayName("Отсутствие параметра token возвращает ошибку")
    @Description("Если параметр token не передан, сервис возвращает {result=ERROR} и не обращается ко внешнему сервису")
    void shouldRejectMissingToken() {

        given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", API_KEY)
                .formParam("action", "LOGIN")
                .when()
                .post(ENDPOINT)
                .then()
                .statusCode(clientError())
                .body("result", is("ERROR"))
                .body("message", not(emptyOrNullString()));

        verify(0, postRequestedFor(urlPathMatching(AUTH_PATH)));
    }
}