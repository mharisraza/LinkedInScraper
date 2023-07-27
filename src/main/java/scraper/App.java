package scraper;

import java.util.*;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class App {

    public static void main(String[] args) {
        System.out.print("<===================================>\nStarting Scraper....\n<===================================>\nHello Dear, hope you doing fine.\nTo get started you need to enter your Linkedin Username and Password and yes it is safe cuz you running it on your computer. :)\n\n");

        ScraperConfig scraperConfig = new ScraperConfig();
        scraperConfig.getUserInput();

        // setting up web driver.
        WebDriver driver = scraperConfig.setupWebDriver(scraperConfig.isHeadlessMode());
        WebDriverHelper driverHelper = new WebDriverHelper(driver);

        try {

            Scraper scraper = new Scraper();
            scraper.startScraper(driverHelper, scraperConfig.getEmailAddress(), scraperConfig.getPassword(), scraperConfig.getTotalProfilesToRetrieve());

        } catch (Exception e) {
            System.out.println("Something went wrong, terminating scraper...");
            System.exit(1);
        } finally {
            driver.quit();
        }


    }

}
