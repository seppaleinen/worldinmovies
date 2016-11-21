package se.david.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChartPage extends Commons {
    private WebDriver driver;
    private static final By CHART_AREA = By.id("chart-area");

    public ChartPage(WebDriver driver) {
        this.driver = driver;
    }

    public ChartPage verifyPageLoaded() {
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(CHART_AREA));
        return this;
    }
}
