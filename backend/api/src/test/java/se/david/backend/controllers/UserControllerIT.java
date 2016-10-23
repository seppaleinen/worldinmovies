package se.david.backend.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.controllers.repository.UserRepository;
import se.david.backend.controllers.repository.entities.User;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
@TestPropertySource(locations="classpath:application-test.properties")
public class UserControllerIT {
    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        userRepository.deleteAll();
    }

    @Test
    public void canSignupUser() {
        User user = User.builder().
                        username("username").
                        password("password").
                        build();

        Response response = given().contentType(ContentType.JSON).body(user).when().post(UserController.SIGNUP_URL);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.CREATED.value(), response.getStatusCode());

        assertEquals(1, userRepository.count());

        User foundUser = userRepository.findOne(user.getUsername());
        assertNotNull(foundUser);
        assertEquals(user, foundUser);
    }

    @Test
    public void canLoginUser_WithCorrectPassword() {
        User user = User.builder().
                username("username").
                password("password").
                build();
        userRepository.save(user);

        Response response = given().contentType(ContentType.JSON).body(user).when().post(UserController.LOGIN_URL);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.ACCEPTED.value(), response.getStatusCode());
        assertEquals(1, userRepository.count());
    }

    @Test
    public void cannotLoginUser_WithIncorrectPassword() {
        User user = User.builder().
                username("username").
                password("password").
                build();
        userRepository.save(user);

        User wrongUser = User.builder().
                username("username").
                password("wrongpassword").
                build();

        Response response = given().contentType(ContentType.JSON).body(wrongUser).when().post(UserController.LOGIN_URL);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
        assertEquals(1, userRepository.count());
    }

    @Test
    public void cannotLoginUser_WithIncorrectUsername() {
        User user = User.builder().
                username("username").
                password("password").
                build();
        userRepository.save(user);

        User wrongUser = User.builder().
                username("wrongusername").
                password("password").
                build();

        Response response = given().contentType(ContentType.JSON).body(wrongUser).when().post(UserController.LOGIN_URL);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
        assertEquals(1, userRepository.count());
    }
}
