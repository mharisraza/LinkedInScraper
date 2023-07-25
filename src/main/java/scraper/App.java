package scraper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class App {

    private static int totalProfilesProcessed;

    public static void main(String[] args) {
        System.out.print("<===================================>\nStarting Scraper....\n<===================================>\nHello Dear, hope you doing fine.\nTo get started you need to enter your Linkedin Username and Password and yes it is safe cuz you running it on your computer. :)\n\n");
        System.out.print("Please enter your LinkedIn Email Address: ");

        Scanner scanner = new Scanner(System.in);
        String emailAddress = scanner.nextLine().trim();

        if (emailAddress.isBlank()) {
            System.out.println("\nPlease enter correct email address. (Program will be terminate if you don't enter correct email address this time.)");

            emailAddress = scanner.nextLine().trim();

            // terminate the program deliberately if user didn't enter correct email
            // address.
            if (emailAddress.isBlank())
                System.exit(0);
        }

        System.out.println("\nPlease enter your LinkedIn Password:");
        String password = scanner.nextLine().trim();

        if (password.isBlank())
            System.exit(0);

        System.out.print("\nReceived! now tell me should ChromeBrowser should show on the screen or not (true or false, Default: False): ");
        boolean headlessMode = true;

        try {
            headlessMode = scanner.nextBoolean();
        } catch (Exception e) {
            if (e instanceof InputMismatchException) {
                System.out.println("You need to enter true or false, please enter again: ");
                scanner.nextLine();
                headlessMode = scanner.nextBoolean();
            }
        }

        System.out.println("\nHow many profiles you want to retrieve or enter -1 to retrieve as many as possible: ");
        final int totalProfilesToRetrieve = scanner.nextInt();

        // terminate the program if user didn't enter correct value.
        if(totalProfilesToRetrieve < -1 || totalProfilesToRetrieve == 0) {
            System.out.println("Enter -1 to retrieve profiles as many as possible or name the number to retrieve the profiles, Terminating the program...");
            System.exit(0);
        }

        startScraper(headlessMode, emailAddress, password, totalProfilesToRetrieve);

    }

    private static Set<Profile> scrapProfiles(WebDriverHelper driverHelper, Set<String> profileLinks) throws InterruptedException {
        Set<Profile> profiles = new HashSet<>();

        for (String profileLink : profileLinks) {
                profiles.add(scrapeProfile(driverHelper, profileLink));
        }
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

            for (WebElement profile : profilesElements) {
                String profileLink = profile.getAttribute("href");
                int endIndex = profileLink.indexOf("?miniProfile");

                if (endIndex != -1) {
                    String modifiedProfileLink = profileLink.substring(0, endIndex);
                    if (profileLinks.add(modifiedProfileLink)) {
                        profilesRetrieved++;
                        if (profilesRetrieved >= totalProfilesToRetrieve && totalProfilesToRetrieve != -1) {
                            break;
                        }
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
        totalProfilesProcessed++;
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

    private static void saveDataToExcelFile(Set<Profile> profiles) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Profiles Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Profile Name");
        headerRow.createCell(1).setCellValue("Profile About");
        headerRow.createCell(2).setCellValue("Profile Description");
        headerRow.createCell(3).setCellValue("Profile Experience");
        headerRow.createCell(4).setCellValue("Profile Education");
        headerRow.createCell(5).setCellValue("Profile Open To Work");
        headerRow.createCell(6).setCellValue("Profile Link");

        // Fill data rows
        int rowIndex = 1;
        for (Profile profile : profiles) {
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.createCell(0).setCellValue(profile.getName());
            dataRow.createCell(1).setCellValue(profile.getAbout());
            dataRow.createCell(2).setCellValue(profile.getDescription());
            dataRow.createCell(3).setCellValue(profile.getExperience());
            dataRow.createCell(4).setCellValue(profile.getEducation());
            dataRow.createCell(5).setCellValue(profile.isOpenToWork() ? "Yes" : "No");
            dataRow.createCell(6).setCellValue(profile.getLink());

            rowIndex++;
        }

        // Auto-size columns
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream("profiles_data.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startScraper(boolean headlessMode, String emailAddress, String password, int totalProfilesToRetrieve) {
        try {
            WebDriver webDriver = new ScraperConfiguration().setupWebDriver(headlessMode);
            WebDriverHelper driverHelper = new WebDriverHelper(webDriver);

            boolean isLoginSuccess = loginToLinkedIn(driverHelper, emailAddress, password);

            if (!isLoginSuccess) {
                System.out.println("The credentials you provided were wrong, terminating the program...");
                System.exit(0);
            }

            // recording the start time
            long startTime = System.currentTimeMillis();

            Set<String> profileLinks = retrieveProfileLinks(driverHelper, totalProfilesToRetrieve);
            Set<Profile> profiles = scrapProfiles(driverHelper, profileLinks);

            saveDataToExcelFile(profiles);

            // recording the ending time
            long endTime = System.currentTimeMillis();

            // Elapsed time.
            long elapsedTimeInSeconds = (endTime - startTime) / 1000;
            double elapsedTimeInMinutesAndSeconds = elapsedTimeInSeconds / 60.0;

            System.out.print(String.format("Scraper took %.2f minutes or %d seconds for scraping %d profiles.", elapsedTimeInMinutesAndSeconds, elapsedTimeInSeconds, totalProfilesProcessed));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
