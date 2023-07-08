package scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ScraperConfiguration {

    public WebDriver setupWebDriver(boolean shouldHeadless) {

        WebDriverManager.chromedriver().setup();
        WebDriver webDriver = new ChromeDriver();

        if(shouldHeadless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=chrome");
            webDriver = new ChromeDriver(options);
        }

        return webDriver;

    }
    
}
