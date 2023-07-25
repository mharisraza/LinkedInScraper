package scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.Set;

public class Profile {

    private String name;
    private String about;
    private String description;
    private String experience;
    private String education;
    private String link;

    private Boolean isOpenToWork;

    private WebDriverHelper driverHelper;

    /**
     * Constructor for the Profile class.
     *
     * @param driverHelper An instance of WebDriverHelper to interact with the WebDriver.
     * @param profileLink  The profile link to scrape information from.
     */
    public Profile(WebDriverHelper driverHelper, String profileLink) {
        this.driverHelper = driverHelper;
        this.link = profileLink;
        driverHelper.getDriver().get(profileLink);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // log if necessary
        }
    }

    /**
     * Fetches and saves all profile information by calling individual methods.
     */
    public void fetchAndSaveProfileInformation() {
        getName();
        getAbout();
        getDescription();
        getExperience();
        getEducation();
    }

    /**
     * Gets the profile name.
     *
     * @return The profile name or "Unable to get profile name" if not found.
     */
    public String getName() {
        if(name != null) return name;

        WebElement profileNameElement = driverHelper.findElementIfExist(By.xpath("//h1[@class='text-heading-xlarge inline t-24 v-align-middle break-words']"));
        if (profileNameElement == null) return "Unable to get profile name";

        name = profileNameElement.getText();
        return name;
    }

    /**
     * Gets the profile about section.
     *
     * @return The profile about section or "Unable to get profile about" if not found.
     */
    public String getAbout() {
        if (about != null) return about;

        WebElement aboutElement = driverHelper.findElementIfExist(By.xpath("//div[@style='-webkit-line-clamp:4;']"));
        if (aboutElement == null) return "Unable to get profile about";

        about = removeDuplicateLines(aboutElement.getText());
        return about;
    }

    /**
     * Gets the profile description.
     *
     * @return The profile description or "Unable to get profile description" if not found.
     */
    public String getDescription() {
        if(description != null) return description;

        WebElement descriptionElement = driverHelper.findElementIfExist(By.xpath("//div[@class='text-body-medium break-words']"));
        if(descriptionElement == null) return "Unable to get profile description";

        description = removeDuplicateLines(descriptionElement.getText());
        return description;
    }

    /**
     * Gets the profile experience.
     *
     * @return The profile experience or "Unable to get experience" if not found.
     */
    public String getExperience() {
        if(this.experience != null) return experience;

        WebElement experienceElement = driverHelper.findElementIfExist(By.id("experience"));
        if(experienceElement == null) return "Unable to get experience";

        experience = removeDuplicateLines(experienceElement.findElement(By.xpath("./parent::*")).getText());
        return experience;
    }

    /**
     * Gets the profile education.
     *
     * @return The profile education or "Unable to get education" if not found.
     */
    public String getEducation() {
        if(this.education != null) return education;

        WebElement educationElement = driverHelper.findElementIfExist(By.id("education"));
        if(educationElement == null) return "Unable to get education";

        education = removeDuplicateLines(educationElement.findElement(By.xpath("./parent::*")).getText());
        return education;
    }

    /**
     * Checks if the profile is open to work.
     *
     * @return True if open to work, false otherwise.
     */
    public Boolean isOpenToWork() {
        if(isOpenToWork != null) return isOpenToWork;
        WebElement isOpenToWork = driverHelper.findElementIfExist(By.xpath("//main[@class='scaffold-layout__main']/section/section/div"));
        if(isOpenToWork != null) return true;
        return false;
    }

    /**
     * Gets the profile link.
     *
     * @return The profile link.
     */
    public String getLink() {
        return link;
    }

    /**
     * Removes duplicate words from the given string.
     *
     * @param str The input string.
     * @return The string with duplicate words removed.
     */
    private static String removeDuplicateLines(String str) {
        if (str == null) return "";

        StringBuilder newStr = new StringBuilder();
        String[] lines = str.split("\n");
        Set<String> uniqueLines = new HashSet<>();

        for (String line : lines) {
            if (uniqueLines.add(line.trim())) {
                newStr.append(line).append("\n");
            }
        }

        return newStr.toString().trim();
    }

}
