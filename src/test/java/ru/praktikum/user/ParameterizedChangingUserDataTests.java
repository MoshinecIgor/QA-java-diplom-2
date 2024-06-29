package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserSteps;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;

@DisplayName("Параметризованные тесты изменения данных пользователя")
@RunWith(Parameterized.class)
public class ParameterizedChangingUserDataTests {
    private String accessToken;
    private User user;
    private final User updatedUser;
    private final boolean isAuthorized;
    private final int expectedStatusCode;
    private final String expectedMessage;

    public ParameterizedChangingUserDataTests(User updatedUser, boolean isAuthorized, int expectedStatusCode, String expectedMessage) {
        this.updatedUser = updatedUser;
        this.isAuthorized = isAuthorized;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedMessage = expectedMessage;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        UserSteps userSteps = new UserSteps();
        User baseUser = userSteps.generateUniqueUser();
        return Arrays.asList(new Object[][]{
                {new User(baseUser.getEmail(), baseUser.getPassword(), "UpdatedName"), true, 200, ""},
                {new User("updated.email@example.com", baseUser.getPassword(), baseUser.getName()), true, 200, ""},
                {new User(baseUser.getEmail(), "UpdatedPassword123", baseUser.getName()), true, 200, ""},
                {new User(baseUser.getEmail(), baseUser.getPassword(), "UpdatedNameWithoutAuth"), false, 401, "You should be authorised"},
                {new User("updated.email@example.com", baseUser.getPassword(), baseUser.getName()), false, 401, "You should be authorised"},
                {new User(baseUser.getEmail(), "UpdatedPassword123", baseUser.getName()), false, 401, "You should be authorised"}
        });
    }

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }

    @Before
    public void createUserAndLogin() {
        user = UserSteps.generateUniqueUser();
        UserSteps.createUser(user);
        accessToken = loginUserAndGetToken(user);
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            deleteUserWithAccessToken(accessToken);
        }
    }

    @Step("Удаление пользователя с токеном доступа")
    private void deleteUserWithAccessToken(String accessToken) {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .delete(EndPoints.DELETE_USER)
                .then()
                .statusCode(202)
                .log().all()
                .body("success", equalTo(true));
    }

    @Step("Логин пользователя и получение токена")
    private String loginUserAndGetToken(User user) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user)
                .post(EndPoints.LOGIN_USER);
        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true));
        return response.jsonPath().getString("accessToken").substring(7); // Убираем "Bearer " из токена
    }

    @Test
    @Description("Изменение данных пользователя")
    @DisplayName("Изменение данных пользователя")
    public void changeUserDataTest() {
        if (isAuthorized) {
            changeUserDataWithAuthorization(updatedUser);
        } else {
            changeUserDataWithoutAuthorization(updatedUser);
        }
    }

    @Step("Изменение данных пользователя с авторизацией")
    private void changeUserDataWithAuthorization(User updatedUser) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(updatedUser)
                .log().all()
                .patch(EndPoints.UPDATE_USER);
        response.then()
                .statusCode(expectedStatusCode)
                .log().all()
                .body("success", equalTo(expectedStatusCode == 200));
    }

    @Step("Изменение данных пользователя без авторизации")
    private void changeUserDataWithoutAuthorization(User updatedUser) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatedUser)
                .log().all()
                .patch(EndPoints.UPDATE_USER);
        response.then()
                .statusCode(expectedStatusCode)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo(expectedMessage));
    }
}
