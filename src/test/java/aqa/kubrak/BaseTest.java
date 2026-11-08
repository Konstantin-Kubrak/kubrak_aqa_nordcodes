package aqa.kubrak;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.Epic;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@Epic("Тестирование API сервиса")
public abstract class BaseTest {

    public static final String API_KEY = "qazWSXedc";
    public static final String ENDPOINT = "http://localhost:8080/endpoint";

    public static final String AUTH_PATH = "/auth";
    public static final String DO_ACTION_PATH = "/doAction";

    public static final WireMockServer wireMockServer;
    public String token;

    static {

        wireMockServer = new WireMockServer(options().port(8888));
        wireMockServer.addMockServiceRequestListener((request, response) ->
                System.out.println("WireMock: " + request.getMethod()
                        + " - " + request.getUrl()
                        + " - " + response.getStatus()));
        wireMockServer.start();
        configureFor("localhost", 8888);
        Runtime.getRuntime().addShutdownHook(new Thread(wireMockServer::stop));
    }

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static Matcher<Integer> clientError() {
        return anyOf(is(400), is(401), is(403), is(404), is(409), is(422));
    }

    public static Matcher<Integer> backendFailure() {
        return anyOf(is(200), is(400), is(500), is(502), is(503));
    }

    @BeforeEach
    void baseSetUp() {

        wireMockServer.resetAll();
        token = generateToken();
    }

    @AfterEach
    void baseTearDown() {

        try {
            given()
                    .contentType("application/x-www-form-urlencoded")
                    .accept("application/json")
                    .header("X-Api-Key", API_KEY)
                    .formParam("token", token)
                    .formParam("action", "LOGOUT")
                    .when()
                    .post(ENDPOINT);
        } catch (Exception ex) {
        }
    }
}