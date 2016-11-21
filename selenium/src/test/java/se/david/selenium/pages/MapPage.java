package se.david.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MapPage extends Commons {
    private WebDriver driver;
    private static final By VMAP = By.id("vmap");

    public MapPage(WebDriver driver) {
        this.driver = driver;
    }

    public MapPage verifyPageLoaded() {
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(VMAP));
        return this;
    }
}
