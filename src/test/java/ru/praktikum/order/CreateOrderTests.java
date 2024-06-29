package ru.praktikum.order;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.OrderSteps;
import ru.praktikum.steps.UserSteps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Epic("Создание заказов")
@DisplayName("Тесты создания заказов")
public class CreateOrderTests {
    private String accessToken;
    private User user;
    private List<String> ingredientIds;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }

    @Before
    public void createUserAndLogin() {
        user = UserSteps.generateUniqueUser();
        UserSteps.createUser(user);
        accessToken = OrderSteps.loginUserAndGetToken(user);
        ingredientIds = OrderSteps.getIngredientIds();
    }

    @After
    public void deleteUser() {
        if (accessToken != null) {
            OrderSteps.deleteUserWithAccessToken(accessToken);
        }
    }

    private List<String> getRandomIngredients(int count) {
        Collections.shuffle(ingredientIds);
        return ingredientIds.subList(0, count);
    }

    @Test
    @Description("Создание заказа с авторизацией")
    @DisplayName("Создание заказа с авторизацией")
    public void createOrderWithAuthorizationTest() {
        List<String> ingredients = getRandomIngredients(2);
        OrderSteps.createOrderWithAuthorization(ingredients, accessToken);
    }

    @Test
    @Description("Создание заказа без авторизации")
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthorizationTest() {
        List<String> ingredients = getRandomIngredients(2);
        OrderSteps.createOrderWithoutAuthorization(ingredients);
    }


    @Test
    @Description("Создание заказа с ингредиентами")
    @DisplayName("Создание заказа с ингредиентами")
    public void createOrderWithIngredientsTest() {
        List<String> ingredients = getRandomIngredients(2);
        OrderSteps.createOrderWithAuthorization(ingredients, accessToken);
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        OrderSteps.createOrderWithAuthorization(new ArrayList<>(), accessToken);
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientsTest() {
        List<String> ingredients = new ArrayList<>();
        ingredients.add("invalidIngredient1");
        ingredients.add("invalidIngredient2");
        OrderSteps.createOrderWithAuthorization(ingredients, accessToken);
    }
}
