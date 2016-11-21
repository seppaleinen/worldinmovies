package se.david.selenium.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class DriverHelper {
    public static WebDriver getPhantomJS(String binary) {
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, binary);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {
                "--web-security=no",
                "--ignore-ssl-errors=yes",
                "--debug=no"
        });

        return new PhantomJSDriver(capabilities);
    }

    public static WebDriver getFirefox() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();

        return new FirefoxDriver(capabilities);
    }
}
