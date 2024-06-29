package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderSteps {

    @Step("Получение списка ингредиентов")
    public static List<String> getIngredientIds() {
        Response response = RestAssured.given()
                .get(EndPoints.GET_INGREDIENTS);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();
        List<String> ingredientIds = response.jsonPath().getList("data._id");
        return ingredientIds;
    }
    @Step("Создание заказа без авторизации")
    public static void createOrderWithoutAuthorization(List<String> ingredients) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("ingredients", ingredients))
                .log().all()
                .post(EndPoints.CREATE_ORDER);
        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }


    @Step("Получение случайных ингредиентов")
    public static List<String> getRandomIngredients(int count) {
        List<String> ingredientIds = getIngredientIds();
        Collections.shuffle(ingredientIds);
        return ingredientIds.subList(0, count);
    }

    @Step("Создание заказа с авторизацией")
    public static void createOrderWithAuthorization(List<String> ingredients, String accessToken) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("ingredients", ingredients))
                .log().all()
                .post(EndPoints.CREATE_ORDER);

        if (ingredients.stream().anyMatch(ing -> ing.startsWith("invalid"))) {
            response.then()
                    .statusCode(500)
                    .log().all()
                    .body(containsString("Internal Server Error"));
        } else if (ingredients.isEmpty()) {
            response.then()
                    .statusCode(400)
                    .log().all()
                    .body("success", equalTo(false))
                    .body("message", equalTo("Ingredient ids must be provided"));
        } else {
            response.then()
                    .statusCode(200)
                    .log().all()
                    .body("success", equalTo(true))
                    .body("order.number", notNullValue());
        }
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

    @Step("Получение заказов пользователя")
    public static void getUserOrders(String accessToken) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get(EndPoints.GET_ORDERS);

        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }
}
