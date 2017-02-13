package se.david.backend.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.david.backend.WorldInMoviesApplication;
import se.david.backend.domain.Movie;
import se.david.backend.domain.User;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = WorldInMoviesApplication.class,
        properties = "classpath:application-test.properties")
@TestPropertySource(locations="classpath:application-test.properties")
public class UserRepositoryIT {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;

    @Before
    public void setup() {
        userRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @Test
    public void expectUnableToSaveNullUser() {
        User nullUser = null;
        try {
            userRepository.save(nullUser);
            fail("Should fail");
        } catch (IllegalArgumentException e) {
            assertEquals("Entity must not be null!", e.getMessage());
        }
    }

    @Test
    public void expectUnableToSaveUser_NullUsername() {
        User user = User.builder().
                            password("password").
                            movies(new ArrayList<>()).
                            build();

        try {
            userRepository.save(user);
            fail("Should fail");
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
            assertEquals("may not be empty", e.getConstraintViolations().iterator().next().getMessage());
            assertEquals("username", e.getConstraintViolations().iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    public void expectUnableToSaveUser_EmptyUsername() {
        User user = User.builder().
                            username("").
                            password("password").
                            movies(new ArrayList<>()).
                            build();

        try {
            userRepository.save(user);
            fail("Should fail");
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
            assertEquals("may not be empty", e.getConstraintViolations().iterator().next().getMessage());
            assertEquals("username", e.getConstraintViolations().iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    public void expectUnableToSaveUser_NullPassword() {
        User user = User.builder().
                            username("username").
                            movies(new ArrayList<>()).
                            build();

        try {
            userRepository.save(user);
            fail("Should fail");
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
            assertEquals("may not be empty", e.getConstraintViolations().iterator().next().getMessage());
            assertEquals("password", e.getConstraintViolations().iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    public void expectUnableToSaveUser_EmptyPassword() {
        User user = User.builder().
                            username("username").
                            password("").
                            movies(new ArrayList<>()).
                            build();

        try {
            userRepository.save(user);
            fail("Should fail");
        } catch (ConstraintViolationException e) {
            assertEquals(1, e.getConstraintViolations().size());
            assertEquals("may not be empty", e.getConstraintViolations().iterator().next().getMessage());
            assertEquals("password", e.getConstraintViolations().iterator().next().getPropertyPath().toString());
        }
    }

    @Test
    public void expectConnectedMoviesToComeWithUser() {
        Movie movie = Movie.builder().name("NAME").year("YEAR").id("NAME:YEAR").build();
        movieRepository.save(movie);

        User user = User.builder().
                username("username").
                password("password").
                movies(new ArrayList<>(Collections.singletonList(movie))).
                build();
        userRepository.save(user);

        User foundUser = userRepository.findOne(user.getUsername());

        assertNotNull(foundUser);
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertFalse(foundUser.getMovies().isEmpty());
        assertEquals(movie.getId(), foundUser.getMovies().get(0).getId());
    }
}
