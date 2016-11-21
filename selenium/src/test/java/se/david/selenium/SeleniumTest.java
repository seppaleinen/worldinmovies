package se.david.selenium;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import se.david.selenium.pages.IndexPage;
import se.david.selenium.util.DriverHelper;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class SeleniumTest {
    private WebDriver driver;
    private String url = "http://worldinmovies.duckdns.org/";
    private IndexPage indexPage;

    @Before
    public void setup() {
        String binary = System.getProperty("phantomjs.binary");
        assertNotNull("phantomjs.binary property must not be null", binary);
        assertTrue("Binary file must exist: " + binary, new File(binary).exists());

        String envUrl = System.getenv("NGINX_URL");
        url = envUrl != null ? envUrl : url;

        driver = DriverHelper.getPhantomJS(binary);
        indexPage = new IndexPage(driver);
    }

    @Test
    public void testMapPage() {
        driver.get(url);

        indexPage.verifyPageLoaded().
                moveToMap().
                verifyPageLoaded();
    }

    @Test
    public void testChartPage() {
        driver.get(url);

        indexPage.verifyPageLoaded().
                moveToChart().
                verifyPageLoaded();
    }

    @Test
    public void testHomePage() {
        driver.get(url);

        indexPage.verifyPageLoaded().
                moveToHome().
                verifyPageLoaded();
    }
}
