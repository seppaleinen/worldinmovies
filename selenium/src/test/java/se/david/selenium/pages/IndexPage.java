package se.david.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IndexPage extends Commons {
    private static final By HEADER = By.xpath("//h1");

    private WebDriver driver;

    public IndexPage(WebDriver driver) {
        this.driver = driver;
    }

    public IndexPage verifyPageLoaded() {
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(HEADER));
        return this;
    }

    public MapPage moveToMap() {
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(MAP)).
                click().
                build().
                perform();
        actions.release();
        return new MapPage(driver);
    }

    public ChartPage moveToChart() {
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(CHART)).
                click().
                build().
                perform();
        actions.release();
        return new ChartPage(driver);
    }

    public IndexPage moveToHome() {
        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(HOME)).
                click().
                build().
                perform();
        actions.release();
        return this;
    }
}
