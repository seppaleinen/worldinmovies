package se.david.selenium;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SeleniumTest {
    private WebDriver driver;

    @Before
    public void setup() {
        try {
            InetAddress ip = InetAddress.getByName("selenium-driver");
            DesiredCapabilities ds = DesiredCapabilities.firefox();

            FirefoxProfile profile = new FirefoxProfile();
            profile.setAssumeUntrustedCertificateIssuer(false);
            profile.setAcceptUntrustedCertificates(true);

            ds.setCapability(FirefoxDriver.PROFILE, profile);
            ds.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            ds.setCapability("trustAllSSLCertificates", true);

            driver = new RemoteWebDriver(new URL("http://" + ip.getHostAddress() + ":4444/wd/hub"), ds);
        } catch (MalformedURLException e) {
            fail("Couldn't lookup url: " + e.getMessage());
        } catch (UnknownHostException e) {
            fail("FAIL: " + e.getMessage());
        }
    }

    @Test
    public void test() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("nginx");
        //driver.get("https://" + ip.getHostAddress() + "/");
        driver.get("https://worldinmovies.duckdns.org/");

        assertEquals("Worldinmovies", driver.getTitle());
    }
}
