package scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class WebDriverHelper {

    private WebDriver driver;

    public WebDriverHelper(WebDriver driver) {
        this.driver = driver;
    }

    private boolean isElementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    public WebElement findElementIfExist(By locator) {
        if (isElementPresent(locator)) return driver.findElement(locator);
        return null;
    }

    public WebDriver getDriver() {
        return driver;
    }

}
