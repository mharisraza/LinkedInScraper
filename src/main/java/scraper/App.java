package scraper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class App {
    public static void main(String[] args) {
        System.out.print(
                "<===================================>\nStarting Scraper....\n<===================================>\nHello Dear, hope you doin fine.\nTo get started you need to enter your Linkedin Username and Password and yes it is safe cuz you running it on your computer. :)\n\n");
        System.out.print("Please enter your LinkedIn Email Address: ");

        Scanner scanner = new Scanner(System.in);
        String emailAddress = scanner.nextLine().trim();

        if (emailAddress.isBlank()) {
            System.out.println(
                    "\nPlease enter correct email address. (Program will be terminate if you don't enter correct email address this time.)");

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

        System.out.print(
                "\nReceived! now tell me should ChromeBrowser should show on the screen or not (true or false, Default: False): ");
        boolean shouldHeadless = true;

        try {
            shouldHeadless = scanner.nextBoolean();
        } catch (Exception e) {
            if (e instanceof InputMismatchException) {
                System.out.println("You need to enter true or false, please enter again: ");
                scanner.nextLine();
                shouldHeadless = scanner.nextBoolean();
            }
        }

        System.out.println("\nHow many profiles you want to retrieve or enter -1 to retrieve as many as possible: ");
        int totalProfilesToRetrieve = scanner.nextInt();

        try {
            startScraper(shouldHeadless, emailAddress, password, totalProfilesToRetrieve);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void startScraper(boolean shouldHeadless, String emailAddress, String password, int totalProfilesToRetrieve) throws InterruptedException {

        // recording the starting time.
        long startTime = System.currentTimeMillis();

        WebDriver webDriver = new ScraperConfiguration().setupWebDriver(shouldHeadless);

        boolean isLoginSuccess = loginToLinkedIn(webDriver, emailAddress, password);

        if(!isLoginSuccess) {
            System.out.println("The credentials you provided were wrong, terminating the program...");
            System.exit(0);
        }

        Set<String> profileLinks = retrieveProfileLinks(webDriver, totalProfilesToRetrieve);
        Set<Profile> profiles = scrapProfiles(webDriver, profileLinks);
        saveDataToExcelFile(profiles);

        // recording the ending time
        long endTime = System.currentTimeMillis();

        // Elapsed time.
        long elapsedTimeInSeconds = (endTime - startTime) / 1000;
        long elapsedTimeInMinutes = (endTime - startTime) / 1000 / 60;
        System.out.print(String.format("Scraper took %s minutes or %s seconds for scraping the data.", elapsedTimeInMinutes, elapsedTimeInSeconds));
    }

    // using this method we'll not need to surround each web element in try-catch
    // block because each web-element may or may not exist.
    private static <T> T runWithExceptionHandling(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
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

    private static Set<Profile> scrapProfiles(WebDriver webDriver, Set<String> profileLinks) throws InterruptedException {
         Set<Profile> profiles = new HashSet<>();
        int profilesProcessed = 0;
        int totalProfiles = profileLinks.size();

        for (String profileLink : profileLinks) {

            webDriver.get(profileLink);
            Thread.sleep(1000);

            profilesProcessed++;

            String profileName = webDriver.findElement(By.xpath("//h1[@class='text-heading-xlarge inline t-24 v-align-middle break-words']")).getText();
            String profileDescription = webDriver.findElement(By.xpath("//div[@class='text-body-medium break-words']")).getText();

            String profileExperience = "None";
            String profileEducation = "None";
            String profileAbout = "None";
            boolean isOpenToWork = false;

            profileExperience = runWithExceptionHandling(() -> webDriver.findElement(By.id("experience")).findElement(By.xpath("./parent::*")).getText());
            profileEducation = runWithExceptionHandling(() -> webDriver.findElement(By.id("education")).findElement(By.xpath("./parent::*")).getText());
            profileAbout = runWithExceptionHandling(() -> webDriver.findElement(By.xpath("//div[@style='-webkit-line-clamp:4;']")).getText());

            WebElement isOpenToWorkElement = runWithExceptionHandling(() -> webDriver.findElement(By.xpath("//main[@class='scaffold-layout__main']/section/section/div")));

            if (null != isOpenToWorkElement) {
                isOpenToWork = true;
            }

            Profile profile = new Profile(profileName, profileAbout, profileDescription, profileExperience, profileEducation, profileLink, isOpenToWork);
            profiles.add(profile);
            System.out.println(String.format("Scraping profile %d of %d", profilesProcessed, totalProfiles));
        }
        return profiles;
    }

    private static Set<String> retrieveProfileLinks(WebDriver webDriver, int totalProfilesToRetrieve) throws InterruptedException {
        Set<String> profileLinks = new HashSet<>();
        boolean isNextPageAvailable = true;
        int pageToGoNext = 1;
        int profilesRetrieved = 0;

        if(totalProfilesToRetrieve != -1) {
            System.out.println("Retriving profiles in total: " + totalProfilesToRetrieve);
        } else {
            System.out.println("Retriving profils as many as possible");
        }

        while (isNextPageAvailable && profilesRetrieved < totalProfilesToRetrieve || isNextPageAvailable && totalProfilesToRetrieve == -1) {

            // going to search results
            webDriver.get(String.format("https://www.linkedin.com/search/results/people/?page=%d", pageToGoNext));
            Thread.sleep(1000);

            List<WebElement> profilesElements = webDriver .findElements(By.xpath("//a[contains(@class, 'app-aware-link') and contains(@href, '/in/')]"));

            for(WebElement profile: profilesElements) {

                String profileLink = profile.getAttribute("href");
                int endIndex = profileLink.indexOf("?miniProfile");

                if (endIndex != -1) {
                    profileLink = profileLink.substring(0, endIndex);
                    profileLinks.add(profileLink);
                    profilesRetrieved++;
                }

                profilesRetrieved++;

                if(profilesRetrieved >= totalProfilesToRetrieve && totalProfilesToRetrieve != -1) {
                    break;
                }

                profileLinks.add(profileLink);
            }

            pageToGoNext = pageToGoNext + 1;
            WebElement noResultPageElement = runWithExceptionHandling(() -> webDriver.findElement(By.xpath("//div[@class='search-reusable-search-no-results artdeco-card mb2']")));

            if (null != noResultPageElement) {
                isNextPageAvailable = false;
            }

            System.out.println(String.format("Total Profiles retrieved yet: %d", profileLinks.size()));

        }
         return profileLinks;
    }

    private static boolean loginToLinkedIn(WebDriver webDriver, String emailAddress, String password) throws InterruptedException {
        // going to linkedIn login page.
        webDriver.get("https://www.linkedin.com/login");
        Thread.sleep(1000);

        // entering details and then clicking on login button to logged in.
        webDriver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[1]/input")).sendKeys(emailAddress);
        webDriver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[2]/input")).sendKeys(password);
        webDriver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[3]/button")).click();
        Thread.sleep(1000);

        WebElement wrongCredentialsElement = runWithExceptionHandling(() -> webDriver.findElement(By.id("error-for-password")));

        if(wrongCredentialsElement != null) {
            return false;
        }

        return true;
    }
}
