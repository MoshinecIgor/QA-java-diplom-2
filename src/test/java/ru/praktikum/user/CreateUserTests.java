package ru.praktikum.user;

import io.qameta.allure.Description;
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

@DisplayName("Тесты создания пользователей")
public class CreateUserTests {
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
    @DisplayName("Создание уникального пользователя")
    @Description("В данном тесте мы создаем уникального пользователя")
    public void createUniqueUserTest() {
        User uniqueUser = UserSteps.generateUniqueUser();

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(uniqueUser)
                .post(EndPoints.CREATE_USER);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(uniqueUser.getEmail()))
                .body("user.name", equalTo(uniqueUser.getName()));
        accessToken = response.jsonPath().getString("accessToken").substring(7);

    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("В данном тесте идет проверка того что будет ошибка при попытке создать пользователя с уже существующей почтой")
    public void createExistingUserTest() {
        User existingUser = UserSteps.generateUniqueUser();
        // Создание пользователя впервые
        UserSteps.createUser(existingUser);
        // Попытка создать того же пользователя
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(existingUser)
                .post(EndPoints.CREATE_USER);
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }
}
