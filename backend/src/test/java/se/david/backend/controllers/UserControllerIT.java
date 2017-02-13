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
import se.david.backend.repository.MovieRepository;
import se.david.backend.repository.UserRepository;
import se.david.backend.domain.Movie;
import se.david.backend.domain.User;

import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "classpath:application-test.properties")
@TestPropertySource(locations="classpath:application-test.properties")
public class UserControllerIT {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        userRepository.deleteAll();
        movieRepository.deleteAll();
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
    public void can_not_SignupAlreadyExistingUsername() {
        User existingUser = User.builder().
                        username("username").
                        password("password").
                        build();
        userRepository.save(existingUser);

        User newUser = User.builder().
                username(existingUser.getUsername()).
                password("newpassword").
                movies(new ArrayList<>(Collections.singletonList(Movie.builder().id("movie:year").build()))).
                build();

        Response response = given().contentType(ContentType.JSON).body(newUser).when().post(UserController.SIGNUP_URL);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode());

        assertEquals(1, userRepository.count());

        User foundUser = userRepository.findOne(existingUser.getUsername());
        assertNotNull(foundUser);
        assertEquals(existingUser, foundUser);
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

    @Test
    public void can_find_user_info() {
        Movie movie = Movie.builder().
                        id("name:year").
                        name("name").
                        year("year").
                        countrySet(new HashSet<>()).
                        build();
        movieRepository.save(movie);

        User user = User.builder().
                username("username").
                password("password").
                movies(new ArrayList<>(Collections.singletonList(movie))).
                build();
        userRepository.save(user);


        Response response = given().param("username", "username").when().post(UserController.GET_USER_DATA);

        Movie[] movieList = response.getBody().as(Movie[].class);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(1, movieList.length);
        movie.setId(null);
        assertEquals(movie, movieList[0]);
    }

    @Test
    public void given_no_user_expect_empty_result() {
        Response response = given().param("username", "username").when().post(UserController.GET_USER_DATA);

        assertEquals(response.getBody().prettyPrint(), HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertEquals("", response.getBody().prettyPrint());
    }
}
