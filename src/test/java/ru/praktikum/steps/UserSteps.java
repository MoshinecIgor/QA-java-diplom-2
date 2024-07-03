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
    public static String accessToken;
    public static User user;

    public static User generateUniqueUser() {
        String uniqueEmail = "user" + UUID.randomUUID() + "@example.com";
        String password = "password123";
        String name = "User" + UUID.randomUUID();
        return new User(uniqueEmail, password, name);
    }

    public static Collection<Object[]> generateInvalidUsers() {
        return Arrays.asList(new Object[][]{
                {new User(null, "password123", "UserWithoutEmail"), "Email, password and name are required fields"},
                {new User("userwithoutpassword@example.com", null, "UserWithoutPassword"), "Email, password and name are required fields"},
                {new User("userwithoutname@example.com", "password123", null), "Email, password and name are required fields"}
        });
    }

    @Step("Создание пользователя")
    public static void createUser(User user) {
        sendPostRequest(user, EndPoints.CREATE_USER)
                .then()
                .statusCode(200);
    }

    @Step("Попытка создания уже существующего пользователя")
    public static void attemptToCreateExistingUser(User user) {
        sendPostRequest(user, EndPoints.CREATE_USER)
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Логин пользователя и получение токена")
    public static String loginUserAndGetToken(User user) {
        Response response = sendPostRequest(user, EndPoints.LOGIN_USER);
        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true));
        return extractTokenFromResponse(response);
    }

    @Step("Логин пользователя и получение ответа")
    public static Response loginUserAndGetResponse(User user) {
        Response response = sendPostRequest(user, EndPoints.LOGIN_USER);
        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true));
        return response;
    }

    @Step("Создание и логин пользователя")
    public static User createUserAndLogin() {
        User user = generateUniqueUser();
        createUser(user);
        accessToken = loginUserAndGetToken(user);
        return user;
    }

    @Step("Удаление пользователя с токеном доступа")
    public static void deleteUserWithAccessToken(String accessToken) {
        sendDeleteRequest(EndPoints.DELETE_USER, accessToken)
                .then()
                .statusCode(202)
                .log().all()
                .body("success", equalTo(true));
    }

    @Step("Удалить созданного пользователя")
    public static void deleteUserIfTokenExists(String accessToken) {
        if (accessToken != null) {
            deleteUserWithAccessToken(accessToken);
        }
    }

    @Step("Изменение данных пользователя без авторизации")
    public static void changeUserDataWithoutAuthorization(User updatedUser) {
        sendPatchRequest(updatedUser, EndPoints.UPDATE_USER, null)
                .then()
                .statusCode(401)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Изменение данных пользователя с авторизацией")
    public static void changeUserDataWithAuthorization(User updatedUser, String accessToken) {
        sendPatchRequest(updatedUser, EndPoints.UPDATE_USER, accessToken)
                .then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("user.name", equalTo(updatedUser.getName()));
    }

    @Step("Проверка ответа логина и извлечение токена")
    public static String verifyLoginResponse(Response response, User existingUser) {
        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("user.email", equalTo(existingUser.getEmail()))
                .body("user.name", equalTo(existingUser.getName()));

        return extractTokenFromResponse(response);
    }

    @Step("Логин с неверным логином и паролем")
    public static void loginUserWithInvalidCredentials(User user) {
        sendPostRequest(user, EndPoints.LOGIN_USER)
                .then()
                .statusCode(401)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Авторизация пользователя")
    public static void authorizeUser(String accessToken) {
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all();
    }

    @Step("Изменение данных пользователя")
    public static Response updateUserData(User updatedUser, String accessToken) {
        return sendPatchRequest(updatedUser, EndPoints.UPDATE_USER, accessToken);
    }

    @Step("Создание пользователя с отсутствующим обязательным полем")
    public static void createAndValidateUserWithMissingField(User user, String expectedMessage) {
        sendPostRequest(user, EndPoints.CREATE_USER)
                .then()
                .statusCode(403)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo(expectedMessage));
    }
    private static Response sendPostRequest(Object body, String endpoint) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(endpoint);
    }

    private static Response sendPatchRequest(Object body, String endpoint, String accessToken) {
        return RestAssured.given()
                .header("Authorization", accessToken != null ? "Bearer " + accessToken : "")
                .contentType(ContentType.JSON)
                .body(body)
                .patch(endpoint);
    }

    private static Response sendDeleteRequest(String endpoint, String accessToken) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .delete(endpoint);
    }

    @Step("Извлечение токена из ответа")
    public static String extractTokenFromResponse(Response response) {
        return response.jsonPath().getString("accessToken").substring(7); // Убираем "Bearer " из токена
    }
}
