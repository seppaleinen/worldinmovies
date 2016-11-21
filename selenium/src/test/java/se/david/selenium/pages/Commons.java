package se.david.selenium.pages;

import org.openqa.selenium.By;

public class Commons {
    public static final By HOME = By.xpath("//a[text()='Home']");
    public static final By MAP = By.xpath("//a[text()='Map']");
    public static final By CHART = By.xpath("//a[text()='Chart']");

    public static final By OPTIONS = By.xpath("//a[@class='dropdown-toggle']");
    public static final By SIGNUP = By.id("register_user_link");
    public static final By LOGIN = By.id("login_user_link");
    public static final By SIGNOUT = By.id("signout_user_link");
    public static final By IMDB_RATINGS = By.id("imdbRatings");
}
