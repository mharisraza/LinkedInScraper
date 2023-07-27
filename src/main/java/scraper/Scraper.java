package scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Scraper {

    private static int totalProfilesProcessed;
    private static long startTime;
    private static long endTime;

    private static Set<Profile> scrapProfiles(WebDriverHelper driverHelper, Set<String> profileLinks) throws InterruptedException {
        Set<Profile> profiles = profileLinks.stream().map(profileLink -> scrapeProfile(driverHelper, profileLink)).collect(Collectors.toSet());
        return profiles;
    }

    private static Set<String> retrieveProfileLinks(WebDriverHelper driverHelper, int totalProfilesToRetrieve) throws InterruptedException {

        Set<String> profileLinks = new HashSet<>();
        boolean isNextPageAvailable = true;
        int pageToGoNext = 1;
        int profilesRetrieved = 0;

        while ((isNextPageAvailable && profilesRetrieved < totalProfilesToRetrieve) || (isNextPageAvailable && totalProfilesToRetrieve == -1)) {
            // going to search results
            driverHelper.getDriver().get(String.format("https://www.linkedin.com/search/results/people/?page=%d", pageToGoNext));
            Thread.sleep(1000);
            pageToGoNext++;

            List<WebElement> profilesElements = driverHelper.getDriver().findElements(By.xpath("//a[contains(@class, 'app-aware-link') and contains(@href, '/in/')]"));


            for (WebElement profileElement : profilesElements) {
                String profileLink = profileElement.getAttribute("href");
                int endIndex = profileLink.indexOf("?miniProfile");

                if (endIndex != -1 && profileLinks.add(profileLink.substring(0, endIndex))) {
                    profilesRetrieved++;
                    if (profilesRetrieved >= totalProfilesToRetrieve && totalProfilesToRetrieve != -1) {
                        break;
                    }
                }
            }

            WebElement noResultPageElement = driverHelper.findElementIfExist(By.xpath("//div[@class='search-reusable-search-no-results artdeco-card mb2']"));
            if(noResultPageElement != null) isNextPageAvailable = false;
            System.out.println(String.format("Total Profiles retrieved yet: %d", profilesRetrieved));
        }

        return profileLinks;
    }

    private static Profile scrapeProfile(WebDriverHelper driverHelper, String profileLink) {
        Profile profile = new Profile(driverHelper, profileLink);
        profile.fetchAndSaveProfileInformation();
        return profile;
    }

    private static boolean loginToLinkedIn(WebDriverHelper driverHelper, String emailAddress, String password) throws InterruptedException {
        // going to LinkedIn login page.
        driverHelper.getDriver().get("https://www.linkedin.com/login");
        Thread.sleep(1000);

        // entering details and then clicking on login button to logged in.
        driverHelper.getDriver().findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[1]/input")).sendKeys(emailAddress);
        driverHelper.getDriver().findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[2]/input")).sendKeys(password);
        driverHelper.getDriver().findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[3]/button")).click();
        Thread.sleep(2500);

        // if verification captcha required make it sleep for 15 seconds to verify manually.
        if(driverHelper.getDriver().getCurrentUrl().contains("checkpoint")) Thread.sleep(15000);

        WebElement wrongCredentialsElement = driverHelper.findElementIfExist(By.id("error-for-password"));
        return wrongCredentialsElement == null;
    }

    public static void startScraper(WebDriverHelper driverHelper, String emailAddress, String password, int totalProfilesToRetrieve) throws InterruptedException {

            boolean isLoginSuccess = loginToLinkedIn(driverHelper, emailAddress, password);

            if (!isLoginSuccess) {
                System.out.println("The credentials you provided were wrong, terminating the program...");
                System.exit(0);
            }

            // recording the start time
            startTime = System.currentTimeMillis();

            Set<String> profileLinks = retrieveProfileLinks(driverHelper, totalProfilesToRetrieve);
            Set<Profile> profiles = scrapProfiles(driverHelper, profileLinks);

            WorkbookManager workbookManager = new WorkbookManager();
            workbookManager.saveProfilesToWorkBook(profiles);

            // recording the ending time
            endTime = System.currentTimeMillis();

            // Elapsed time.
            long elapsedTimeInSeconds = (endTime - startTime) / 1000;
            double elapsedTimeInMinutesAndSeconds = elapsedTimeInSeconds / 60.0;

            System.out.print(String.format("Scraper took %.2f minutes or %d seconds for scraping %d profiles.", elapsedTimeInMinutesAndSeconds, elapsedTimeInSeconds, totalProfilesProcessed));
    }
}
