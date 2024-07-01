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
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserSteps;

import static org.hamcrest.Matchers.equalTo;


@DisplayName("Тесты изменения данных пользователя")
public class ChangingUserDataTests {
    private String accessToken;
    private User user;

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
            UserSteps.deleteUserWithAccessToken(accessToken);
        }
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
    @Description("Изменение данных пользователя с авторизацией")
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void changeUserDataWithAuthorizationTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "UpdatedName");
        changeUserDataWithAuthorization(updatedUser);
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
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("user.name", equalTo(updatedUser.getName()));
    }
    @Test
    @Description("Изменение данных пользователя без авторизации")
    @DisplayName("Изменение данных пользователя без авторизации")
    public void changeUserDataWithoutAuthorizationTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "UpdatedNameWithoutAuth");
        changeUserDataWithoutAuthorization(updatedUser);
    }

    @Step("Изменение данных пользователя без авторизации")
    private void changeUserDataWithoutAuthorization(User updatedUser) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updatedUser)
                .log().all()
                .patch(EndPoints.UPDATE_USER);
        response.then()
                .statusCode(401)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
