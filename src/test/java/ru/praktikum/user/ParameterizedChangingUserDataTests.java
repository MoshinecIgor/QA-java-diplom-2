package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
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

import static org.hamcrest.CoreMatchers.equalTo;

@DisplayName("Параметризованные тесты изменения данных пользователя")
@RunWith(Parameterized.class)
public class ParameterizedChangingUserDataTests {
    private String accessToken;
    private final User updatedUser;

    public ParameterizedChangingUserDataTests(User updatedUser) {
        this.updatedUser = updatedUser;
    }

    @Parameterized.Parameters(name = "{index}: Updating User Data with {0}")
    public static Collection<Object[]> data() {
        User baseUser = UserSteps.generateUniqueUser();
        return Arrays.asList(new Object[][]{
                {new User(baseUser.getEmail(), baseUser.getPassword(), "UpdatedName")},
                {new User("updated.email@example.com", baseUser.getPassword(), baseUser.getName())},
                {new User(baseUser.getEmail(), "UpdatedPassword123", baseUser.getName())}
        });
    }

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }

    @Before
    public void createUserAndLogin() {
        User user = UserSteps.createUserAndLogin();
        accessToken = UserSteps.extractTokenFromResponse(UserSteps.loginUserAndGetResponse(user));
    }

    @After
    public void deleteUser() {
        UserSteps.deleteUserIfTokenExists(accessToken);
    }

    @Test
    @Description("Изменение данных пользователя с авторизацией")
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void changeUserDataWithAuthorizationTest() {
        UserSteps.authorizeUser(accessToken);
        Response response = UserSteps.updateUserData(updatedUser, accessToken);
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
        Response response = UserSteps.updateUserData(updatedUser, null);
        response.then()
                .statusCode(401)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
