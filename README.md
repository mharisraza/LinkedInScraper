# LinkedInScraper
Scrapping profiles and their information using Java and Selenium Library.

![image](https://github.com/mharisraza/LinkedInScraper/assets/111365348/533a9ead-53be-44a6-a54c-b54607ba0d91)

## Tech Used:
- Java
- Selenium Library
- Apache POI (Excel)

## How to Use?
This is an simple maven-app, clone the app using git or download it, run `App.java` and then it'll start the application.
It'll ask credentials for Your LinkedIn Email address and Password in terminal. 
![image](https://github.com/mharisraza/LinkedInScraper/assets/111365348/c21dfb08-9323-4709-9e1c-5f11c8e89780)


And it'll ask you to about Chrome whether you want to open chrome (with opened chrome it can slow down the process) or without opening chrome (it'll perform action automatically in both) and it'll ask for number to fetch profiles. 

![image](https://github.com/mharisraza/LinkedInScraper/assets/111365348/1b213f75-61c2-4bb4-89cc-4cc157b2343c)


There's no configuration required.

When it'll finished scrapping it'll print out the following statement:
`Scraper took n minutes or n seconds for scraping the data.`
![image](https://github.com/mharisraza/LinkedInScraper/assets/111365348/01348b25-ae88-4fcd-bbc0-b1742eb5ca94)


After finished, your scrapped data will be automatically saved in `profile_data.xlsx` file (automatically created).

### 🚧 DISCLAIMER: This project violates LinkedIn's Terms and Conditions. Please use it only for educational purposes. 🚧
