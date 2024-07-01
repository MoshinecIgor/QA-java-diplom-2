package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class UserSteps {
    public static User generateUniqueUser() {
        String uniqueEmail = "user" + UUID.randomUUID() + "@example.com";
        String password = "password123";
        String name = "User" + UUID.randomUUID();
        return new User(uniqueEmail, password, name);
    }

    public static Collection<Object[]> generateInvalidUsers() {
        return Arrays.asList(new Object[][] {
                { new User(null, "password123", "UserWithoutEmail"), "Email, password and name are required fields" },
                { new User("userwithoutpassword@example.com", null, "UserWithoutPassword"), "Email, password and name are required fields" },
                { new User("userwithoutname@example.com", "password123", null), "Email, password and name are required fields" }
        });
    }

    public static void createUser(User user) {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(user)
                .post(EndPoints.CREATE_USER)
                .then()
                .statusCode(200);
    }
    @Step("Логин пользователя и получение токена")
    public static String loginUserAndGetToken(User user) {
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
    @Step("Удаление пользователя с токеном доступа")
    public static void deleteUserWithAccessToken(String accessToken) {
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
