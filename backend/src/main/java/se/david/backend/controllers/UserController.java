package se.david.backend.controllers;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.david.backend.repository.UserRepository;
import se.david.backend.domain.Movie;
import se.david.backend.domain.User;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.POST})
@Log
public class UserController {
    private static final String ROOT_URL = "/user";
    public static final String SIGNUP_URL = ROOT_URL + "/signup";
    public static final String LOGIN_URL = ROOT_URL + "/login";
    public static final String GET_USER_DATA = ROOT_URL + "/info";
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = GET_USER_DATA, method = RequestMethod.POST)
    public ResponseEntity<List<Movie>> info(@RequestParam @NotNull String username) {
        User user = userRepository.findOne(username);
        if(user != null) {
            return new ResponseEntity<>(user.getMovies(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = SIGNUP_URL, method = RequestMethod.POST)
    public ResponseEntity signup(@RequestBody @Valid @NotNull User user) {
        if(userRepository.findOne(user.getUsername()) == null) {
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @RequestMapping(value = LOGIN_URL, method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody @Valid @NotNull User user) {
        User existingUser = userRepository.findOne(user.getUsername());
        if(existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }
}
