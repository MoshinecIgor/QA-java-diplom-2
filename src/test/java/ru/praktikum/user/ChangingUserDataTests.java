package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserSteps;

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
        user = UserSteps.createUserAndLogin();
        accessToken = UserSteps.loginUserAndGetToken(user);
    }

    @After
    public void deleteUser() {
        UserSteps.deleteUserIfTokenExists(accessToken);
    }

    @Test
    @Description("Изменение данных пользователя с авторизацией")
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void changeUserDataWithAuthorizationTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "UpdatedName");
        UserSteps.changeUserDataWithAuthorization(updatedUser, accessToken);
    }

    @Test
    @Description("Изменение данных пользователя без авторизации")
    @DisplayName("Изменение данных пользователя без авторизации")
    public void changeUserDataWithoutAuthorizationTest() {
        User updatedUser = new User(user.getEmail(), user.getPassword(), "UpdatedNameWithoutAuth");
        UserSteps.changeUserDataWithoutAuthorization(updatedUser);
    }
}
