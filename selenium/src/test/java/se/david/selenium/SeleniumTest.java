package se.david.selenium;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class SeleniumTest {
    private WebDriver driver;

    @Before
    public void setup() {
        String binary = System.getProperty("phantomjs.binary");
        assertNotNull(binary);
        assertTrue(new File(binary).exists());

        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, binary);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {
                "--web-security=no",
                "--ignore-ssl-errors=yes",
                "--debug=yes"
        });

        driver = new PhantomJSDriver(capabilities);
    }

    @Test
    public void test() throws UnknownHostException {
        driver.get("https://nginx");

        assertEquals("Worldinmovies", driver.getTitle());
    }
}
