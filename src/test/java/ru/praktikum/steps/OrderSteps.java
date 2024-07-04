package ru.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import ru.praktikum.EndPoints;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderSteps {

    @Step("Получение списка ингредиентов")
    public static List<String> getIngredientIds() {
        Response response = given()
                .get(EndPoints.GET_INGREDIENTS);
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().all();
        return response.jsonPath().getList("data._id");
    }

    @Step("Создание заказа без авторизации")
    public static void createOrderWithoutAuthorization(List<String> ingredients) {
        Response response = given()
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

    @Step("Получение заказов пользователя")
    public static void getUserOrders(String accessToken) {
        Response response = given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .get(EndPoints.GET_ORDERS);

        response.then()
                .statusCode(200)
                .log().all()
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Step("Получение случайных ингредиентов")
    public static List<String> getRandomIngredients(int count) {
        List<String> ingredientIds = getIngredientIds();
        Collections.shuffle(ingredientIds);
        return ingredientIds.subList(0, count);
    }

    @Step("Создание заказа с авторизацией")
    public static void createOrderWithAuthorization(List<String> ingredients, String accessToken) {
        Response response = given()
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

    @Step("Получение заказов пользователя без авторизации")
    public static Response getUserOrdersWithoutAuthorization() {
        return given()
                .when()
                .get(EndPoints.GET_ORDERS)
                .then()
                .log().all()
                .extract()
                .response();
    }
}
