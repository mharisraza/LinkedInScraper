package scraper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

        System.out.print("\nReceived! now tell me should ChromeBrowser should show on the screen or not (true or false, Default: False): ");
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
        final int totalProfilesToRetrieve = scanner.nextInt();

        // terminate the program if user didn't enter correct value.
        if(totalProfilesToRetrieve < -1 || totalProfilesToRetrieve == 0) {
            System.out.println("Enter -1 to retrieve profiles as many as possible or name the number to retrieve the profiles, Terminating the program...");
            System.exit(0);
        }

        final boolean shouldHeadlessFinal = shouldHeadless;
        final String emailAddressFinal = emailAddress;
        final String passwordFinal = password;
        

        // Create a fixed-size thread pool with a specified number of threads
        int numThreads = 10; // Adjust the number of threads as per your requirement
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Start the scraping process asynchronously
        CompletableFuture<Void> scraperFuture = CompletableFuture.supplyAsync(() -> {
            try {
                WebDriver webDriver = new ScraperConfiguration().setupWebDriver(shouldHeadlessFinal);
                return startScraper(webDriver, shouldHeadlessFinal, emailAddressFinal, passwordFinal, totalProfilesToRetrieve);
            } catch (Exception e) {
                System.out.println("Exception occurred");
                return null;
            }
        }, executor).thenCompose(Function.identity());

        scraperFuture.join(); // Wait for the scraping process to complete

        // Shut down the thread pool
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scanner.close();
    }
    
    /**
     * Start the LinkedIn profile scraping process.
     *
     * @param webDriver              The Selenium WebDriver instance.
     * @param shouldHeadless         A boolean indicating whether the Chrome browser should be displayed on the screen.
     * @param emailAddress          The LinkedIn email address.
     * @param password               The LinkedIn password.
     * @param totalProfilesToRetrieve The number of profiles to retrieve. Use -1 to retrieve as many as possible.
     * @return A CompletableFuture representing the asynchronous execution of the scraping process.
     * @throws InterruptedException If the execution is interrupted.
     */
    private static CompletableFuture<Void> startScraper(WebDriver webDriver, boolean shouldHeadless, String emailAddress, String password,int totalProfilesToRetrieve) throws InterruptedException  {
        
        // recording the starting time.
        long startTime = System.currentTimeMillis();

        boolean isLoginSuccess = loginToLinkedIn(webDriver, emailAddress, password);

        if (!isLoginSuccess) {
            System.out.println("The credentials you provided were wrong, terminating the program...");
            System.exit(0);
        }

        CompletableFuture<Set<String>> profileLinksFuture = retrieveProfileLinks(webDriver, totalProfilesToRetrieve);
        CompletableFuture<Set<Profile>> profilesFuture = profileLinksFuture.thenCompose(profileLinks -> scrapProfiles(webDriver, profileLinks));

        return profilesFuture.thenAccept(profiles -> {
            saveDataToExcelFile(profiles);
        
            // recording the ending time
            long endTime = System.currentTimeMillis();
        
            // Elapsed time.
            long elapsedTimeInSeconds = (endTime - startTime) / 1000;
            long elapsedTimeInMinutes = (endTime - startTime) / 1000 / 60;
            System.out.print(String.format("Scraper took %s minutes or %s seconds for scraping the data.",elapsedTimeInMinutes, elapsedTimeInSeconds));
        }).thenRun(webDriver::quit);
    }

    /**
     * Run a supplier with exception handling, returning null if an exception occurs.
     *
     * @param supplier The supplier to execute.
     * @param <T>      The type of value returned by the supplier.
     * @return The value returned by the supplier, or null if an exception occurs.
     */
    private static <T> T runWithExceptionHandling(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

     /**
     * Save the scraped profile data to an Excel file.
     *
     * @param profiles The set of Profile objects to save.
     */
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

    /**
     * Scrap the LinkedIn profiles using the provided WebDriver and profile links.
     *
     * @param webDriver    The Selenium WebDriver instance.
     * @param profileLinks The set of profile links to scrape.
     * @return A CompletableFuture containing the set of scraped Profile objects.
     */
    private static CompletableFuture<Set<Profile>> scrapProfiles(WebDriver webDriver, Set<String> profileLinks) {
        return CompletableFuture.supplyAsync(() -> {
            Set<Profile> profiles = new HashSet<>();
            int profilesProcessed = 0;
            int totalProfiles = profileLinks.size();

            for (String profileLink : profileLinks) {

                webDriver.get(profileLink);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

                Profile profile = new Profile(profileName, profileAbout, profileDescription, profileExperience,profileEducation, profileLink, isOpenToWork);
                profiles.add(profile);
                System.out.println(String.format("Scraping profile %d of %d", profilesProcessed, totalProfiles));
            }
            return profiles;
        });
    }

    /**
     * Retrieve the profile links from LinkedIn search results.
     *
     * @param webDriver               The Selenium WebDriver instance.
     * @param totalProfilesToRetrieve The number of profiles to retrieve. Use -1 to retrieve as many as possible.
     * @return A CompletableFuture containing the set of retrieved profile links.
     */
    private static CompletableFuture<Set<String>> retrieveProfileLinks(WebDriver webDriver, int totalProfilesToRetrieve) {
        return CompletableFuture.supplyAsync(() -> {

            Set<String> profileLinks = new HashSet<>();
            boolean isNextPageAvailable = true;
            int pageToGoNext = 1;
            int profilesRetrieved = 0;

            if (totalProfilesToRetrieve != -1) {
                System.out.println("Retrieving profiles in total: " + totalProfilesToRetrieve);
            } else {
                System.out.println("Retrieving profiles as many as possible");
            }

            while ((isNextPageAvailable && profilesRetrieved < totalProfilesToRetrieve)|| (isNextPageAvailable && totalProfilesToRetrieve == -1)) {

                // going to search results
                webDriver.get(String.format("https://www.linkedin.com/search/results/people/?page=%d", pageToGoNext));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Handle InterruptedException appropriately
                    e.printStackTrace();
                }

                List<WebElement> profilesElements = webDriver.findElements(By.xpath("//a[contains(@class, 'app-aware-link') and contains(@href, '/in/')]"));

                for (WebElement profile : profilesElements) {

                    String profileLink = profile.getAttribute("href");
                    int endIndex = profileLink.indexOf("?miniProfile");

                    if (endIndex != -1) {
                        String modifiedProfileLink = profileLink.substring(0, endIndex);
                        if (profileLinks.add(modifiedProfileLink)) {
                            profilesRetrieved++;
                        }
                    }
                    if (profilesRetrieved >= totalProfilesToRetrieve && totalProfilesToRetrieve != -1) {
                        break;
                    }
                }

                pageToGoNext++;
                WebElement noResultPageElement = runWithExceptionHandling(() -> webDriver.findElement(By.xpath("//div[@class='search-reusable-search-no-results artdeco-card mb2']")));

                if (noResultPageElement != null) {
                    isNextPageAvailable = false;
                }

                System.out.println(String.format("Total Profiles retrieved yet: %d", profilesRetrieved));
            }

            return profileLinks;
        });
    }

    /**
     * Log in to LinkedIn using the provided WebDriver, email address, and password.
     *
     * @param webDriver    The Selenium WebDriver instance.
     * @param emailAddress The LinkedIn email address.
     * @param password     The LinkedIn password.
     * @return True if the login is successful, false otherwise.
     * @throws InterruptedException If the execution is interrupted.
     */
    private static boolean loginToLinkedIn(WebDriver webDriver, String emailAddress, String password)
            throws InterruptedException {
        // going to linkedIn login page.
        webDriver.get("https://www.linkedin.com/login");
        Thread.sleep(1000);

        // entering details and then clicking on login button to logged in.
        webDriver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[1]/input")).sendKeys(emailAddress);
        webDriver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[2]/input")).sendKeys(password);
        webDriver.findElement(By.xpath("/html/body/div/main/div[2]/div[1]/form/div[3]/button")).click();

        WebElement wrongCredentialsElement = runWithExceptionHandling(
                () -> webDriver.findElement(By.id("error-for-password")));

        if (wrongCredentialsElement != null) {
            return false;
        }

        return true;
    }
}
