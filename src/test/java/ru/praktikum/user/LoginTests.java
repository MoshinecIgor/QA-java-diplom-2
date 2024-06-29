package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserSteps;

import static org.hamcrest.Matchers.equalTo;

@DisplayName("Тесты логина пользователя")
public class LoginTests {
    private String accessToken;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }
    @After
    public void deleteUser() {
        if (accessToken != null) {
            RestAssured.given()
                    .header("Authorization", "Bearer " + accessToken)
                    .log().all()
                    .delete(EndPoints.DELETE_USER)
                    .then()
                    .statusCode(202)
                    .log().all()
                    .body("success", equalTo(true));
        }
    }
    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Тест об успешном логине под существующим пользователем")
    public void loginUserTest() {
        User existingUser = UserSteps.generateUniqueUser();
        // Ensure the user exists
        UserSteps.createUser(existingUser);
        // Login with existing user
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(existingUser)
                .log().all()
                .post(EndPoints.LOGIN_USER);
        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("user.email", equalTo(existingUser.getEmail()))
                .body("user.name", equalTo(existingUser.getName()));

        accessToken = response.jsonPath().getString("accessToken").substring(7); // Убираем "Bearer " из токена
    }

    @Test
    @DisplayName("Логин с неверным логином и паролем")
    @Description("Тест о том что невозможно залогиниться с несуществующим пользователем")
    public void loginWithInvalidCredentialsTest() {
        User invalidUser = new User("invalid@example.com", "wrongpassword", "InvalidUser");
        loginUserWithInvalidCredentials(invalidUser);
    }
    @Step("Логин с неверным логином и паролем")
    private void loginUserWithInvalidCredentials(User user) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user)
                .log().all()
                .post(EndPoints.LOGIN_USER);
        response.then()
                .statusCode(401)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
