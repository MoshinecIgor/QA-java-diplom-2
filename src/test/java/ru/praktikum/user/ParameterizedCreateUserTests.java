package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserSteps;

import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
@DisplayName("Параметризированые тесты создания пользователя")
public class ParameterizedCreateUserTests {

    private final User user;
    private final String expectedMessage;

    public ParameterizedCreateUserTests(User user, String expectedMessage) {
        this.user = user;
        this.expectedMessage = expectedMessage;
    }

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return UserSteps.generateInvalidUsers();
    }

    @Test
    @DisplayName("Создание пользователя с отсутствующим обязательным полем")
    @Description("Параметризированый тест о попытке создания пользователя с отсутствием какого-то из обязательныъ полей")
    public void createUserWithMissingFieldTest() {
        createAndValidateUserWithMissingField(user, expectedMessage);
    }

    @Step("Создание пользователя с отсутствующим обязательным полем")
    private void createAndValidateUserWithMissingField(User user, String expectedMessage) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user)
                .log().all() // Логирование запроса
                .post(EndPoints.CREATE_USER);

        response.then()
                .statusCode(403)
                .log().all() // Логирование ответа
                .body("success", equalTo(false))
                .body("message", equalTo(expectedMessage));
    }
}
