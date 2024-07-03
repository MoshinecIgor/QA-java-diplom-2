package ru.praktikum.user;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.praktikum.EndPoints;
import ru.praktikum.model.User;
import ru.praktikum.steps.UserSteps;

import java.util.Collection;

@RunWith(Parameterized.class)
@DisplayName("Параметризированые тесты создания пользователя")
public class ParameterizedCreateUserTests {

    private final User user;
    private final String expectedMessage;

    public ParameterizedCreateUserTests(User user, String expectedMessage) {
        this.user = user;
        this.expectedMessage = expectedMessage;
    }

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = EndPoints.BASE_URL;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return UserSteps.generateInvalidUsers();
    }

    @Test
    @DisplayName("Создание пользователя с отсутствующим обязательным полем")
    @Description("Параметризированый тест о попытке создания пользователя с отсутствием какого-то из обязательныъ полей")
    public void createUserWithMissingFieldTest() {
        UserSteps.createAndValidateUserWithMissingField(user, expectedMessage);
    }
}
