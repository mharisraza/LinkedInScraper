package scraper;

public class Profile {
    
    private String name;
    private String about;
    private String description;
    private String experience;
    private String education;
    private String link;

    private boolean isOpenToWork;

     public Profile(String name, String about, String description, String experience, String education, String link, boolean isOpenToWork) {
        this.name = name;
        this.about = about;
        this.description = description;
        this.experience = experience;
        this.education = education;
        this.link = link;
        this.isOpenToWork = isOpenToWork;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public boolean isOpenToWork() {
        return isOpenToWork;
    }

    public void setOpenToWork(boolean isOpenToWork) {
        this.isOpenToWork = isOpenToWork;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
}
