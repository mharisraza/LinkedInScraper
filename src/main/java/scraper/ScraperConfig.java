package scraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ScraperConfig {

    private String emailAddress;
    private String password;
    private boolean headlessMode;
    private int totalProfilesToRetrieve;

    public WebDriver setupWebDriver(boolean headlessMode) {

        WebDriverManager.chromedriver().setup();
        WebDriver webDriver = new ChromeDriver();

        if(headlessMode) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            webDriver = new ChromeDriver(options);
        }

        return webDriver;

    }

    public void getUserInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter your LinkedIn Email Address: ");
        emailAddress = scanner.nextLine().trim();

        if (emailAddress.isBlank()) {
            System.out.println("\nPlease enter a correct email address. (Program will terminate if you don't enter the correct email address this time.)");
            emailAddress = scanner.nextLine().trim();

            if (emailAddress.isBlank())
                System.exit(0);
        }

        System.out.println("\nPlease enter your LinkedIn Password:");
        password = scanner.nextLine().trim();

        if (password.isBlank())
            System.exit(0);

        System.out.print("\nShould the Chrome browser be shown on the screen? (true or false, Default: False): ");

        try {
            headlessMode = scanner.nextBoolean();
        } catch (InputMismatchException e) {
            System.out.println("You need to enter true or false, please enter again: ");
            scanner.nextLine();
            headlessMode = scanner.nextBoolean();
        }

        System.out.println("\nHow many profiles do you want to retrieve? Enter -1 to retrieve as many as possible: ");
        totalProfilesToRetrieve = scanner.nextInt();

        if (totalProfilesToRetrieve < -1 || totalProfilesToRetrieve == 0) {
            System.out.println("Enter -1 to retrieve profiles as many as possible or name the number to retrieve the profiles. Terminating the program...");
            System.exit(0);
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHeadlessMode() {
        return headlessMode;
    }

    public int getTotalProfilesToRetrieve() {
        return totalProfilesToRetrieve;
    }
}
