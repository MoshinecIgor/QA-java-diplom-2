package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
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
        UserSteps.deleteUserIfTokenExists(accessToken);
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Тест об успешном логине под существующим пользователем")
    public void loginUserTest() {
        User existingUser = UserSteps.generateUniqueUser();
        UserSteps.createUser(existingUser);
        Response response = UserSteps.loginUserAndGetResponse(existingUser);
        accessToken = UserSteps.verifyLoginResponse(response, existingUser);
    }

    @Test
    @DisplayName("Логин с неверным логином и паролем")
    @Description("Тест о том что невозможно залогиниться с несуществующим пользователем")
    public void loginWithInvalidCredentialsTest() {
        User invalidUser = new User("invalid@example.com", "wrongpassword", "InvalidUser");
        UserSteps.loginUserWithInvalidCredentials(invalidUser);
    }
}
