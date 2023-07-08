# LinkedInScraper
Scrapping profiles and their information using Java and Selenium Library.


![Screenshot 2023-07-08 055726](https://github.com/mharisraza/LinkedInScraper/assets/111365348/89809c29-09f7-4d7b-94a6-f4703cd2be3e)

## Tech Used:
- Java
- Selenium Library
- Apache POI (Excel)

## How to Use?
This is an simple maven-app, clone the app using git or download it, run `App.java` and then it'll start the application.
It'll ask credentials for Your LinkedIn Email address and Password in terminal. 

And it'll ask you to about Chrome whether you want to open chrome or without opening chrome. (it'll perform action automatically in both)

There's no configuration required.

When it'll finished scrapping it'll print out the following statement:
`Scraper took n minutes or n seconds for scraping the data.`

After finished, your scrapped data will be automatically saved in `profile_data.xlsx` file (automatically created).
