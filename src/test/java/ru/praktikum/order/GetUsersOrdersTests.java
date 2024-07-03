package ru.praktikum.order;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.OrderSteps;
import ru.praktikum.steps.UserSteps;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

@DisplayName("Тесты создания заказов")
public class GetUsersOrdersTests {
    private String accessToken;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            UserSteps.deleteUserWithAccessToken(accessToken);
        }
    }

    @Test
    @Description("Получение заказов конкретного пользователя (авторизованный пользователь)")
    @DisplayName("Получение заказов конкретного пользователя")
    public void getUserOrdersAuthorizedTest() {
        User user = UserSteps.generateUniqueUser();
        UserSteps.createUser(user);
        accessToken = UserSteps.loginUserAndGetToken(user);
        List<String> ingredients = OrderSteps.getRandomIngredients(2);
        OrderSteps.createOrderWithAuthorization(ingredients, accessToken);
        OrderSteps.getUserOrders(accessToken);
    }

    @Test
    @Description("Получение заказов неавторизованным пользователем")
    @DisplayName("Получение заказов неавторизованным пользователем")
    public void getUserOrdersWithoutAuthorizationTest() {
        Response response = OrderSteps.getUserOrdersWithoutAuthorization();
        response.then()
                .statusCode(401)
                .log().all()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
