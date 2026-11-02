package edu.dvc.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.io.File;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Full role-to-role browser workflow. Point DVC_SELENIUM_BASE_URL at the isolated test UI. */
class EmergencyWorkflowSeleniumTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void startBrowser() {
        String baseUrl = System.getenv("DVC_SELENIUM_BASE_URL");
        Assumptions.assumeTrue(baseUrl != null && !baseUrl.isBlank(),
            "Set DVC_SELENIUM_BASE_URL to run Selenium against an isolated test backend/UI");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--window-size=1440,1100");
        ChromeDriverService service = new ChromeDriverService.Builder()
            .withLogFile(new File("target/selenium-chromedriver.log")).withVerbose(true).build();
        driver = new org.openqa.selenium.chrome.ChromeDriver(service, options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(baseUrl);
    }

    @AfterEach
    void closeBrowser() { if (driver != null) driver.quit(); }

    @Test
    void userRequestAdminAssignmentVolunteerCompletionAndFeedback() {
        String title = "Selenium response " + UUID.randomUUID().toString().substring(0, 8);
        login("user@gmail.com", "User@12345");
        driver.get(base() + "/dashboard/new-request");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("request-title"))).sendKeys(title);
        driver.findElement(By.id("request-description")).sendKeys("Automated end-to-end verification of an emergency response.");
        driver.findElement(By.id("request-location")).sendKeys("Test location");
        new Select(driver.findElement(By.id("request-urgency"))).selectByValue("HIGH");
        driver.findElement(By.id("request-skills")).sendKeys("First Aid");
        driver.findElement(By.cssSelector(".request-form button")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".workspace-alert"), "Your emergency request was submitted"));
        logout();

        login("admin@gmail.com", "Admin@12345");
        driver.get(base() + "/dashboard/requests");
        WebElement requestCard = requestCard(title);
        new Select(requestCard.findElement(By.cssSelector(".assignment-controls select"))).selectByIndex(1);
        requestCard.findElement(By.cssSelector(".assignment-controls button")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".workspace-alert"), "Volunteer assigned"));
        logout();

        login("volunteer@gmail.com", "Volunteer@12345");
        driver.get(base() + "/dashboard/assignments");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", taskAction(title, "Accept task"));
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".workspace-alert"), "Task marked accepted"));
        } catch (org.openqa.selenium.TimeoutException error) {
            throw new AssertionError("Accept did not update the task. Page text: " + driver.findElement(By.tagName("body")).getText()
                + "; browser logs: " + driver.manage().logs().get(LogType.BROWSER).getAll(), error);
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", taskAction(title, "Start task"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath(
            "//article[contains(@class,'request-card')][.//h3[contains(normalize-space(), '" + title + "')]]//span[contains(@class,'request-status')]"), "IN PROGRESS"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", taskAction(title, "Mark completed"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath(
            "//article[contains(@class,'request-card')][.//h3[contains(normalize-space(), '" + title + "')]]//span[contains(@class,'request-status')]"), "COMPLETED"));
        WebElement feedbackCard = requestCard(title);
        feedbackCard.findElement(By.cssSelector("input[aria-label='Feedback comments']")).sendKeys("Selenium workflow feedback");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", feedbackCard.findElement(By.xpath(".//button[normalize-space()='Send feedback']")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".workspace-alert"), "Your feedback was submitted"));
        logout();

        login("admin@gmail.com", "Admin@12345");
        driver.get(base() + "/dashboard/feedback");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(), '" + title + "')]")));
        assertTrue(driver.getPageSource().contains("Selenium workflow feedback"));
    }

    private String base() { return System.getenv("DVC_SELENIUM_BASE_URL").replaceAll("/$", ""); }

    private void login(String email, String password) {
        driver.get(base() + "/login");
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        emailInput.clear();
        emailInput.sendKeys(email);
        WebElement passwordInput = driver.findElement(By.id("login-password"));
        passwordInput.clear();
        passwordInput.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".auth-form button[type='submit']"))).click();
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        } catch (org.openqa.selenium.TimeoutException error) {
            throw new AssertionError("Could not sign in " + email + ". Page text: "
                + driver.findElement(By.tagName("body")).getText(), error);
        }
    }

    private void logout() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click()",
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".side-logout"))));
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    private WebElement requestCard(String title) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            "//article[contains(@class,'request-card')][.//h3[contains(normalize-space(), '" + title + "')]]")));
    }

    private WebElement taskAction(String title, String action) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
            "//article[contains(@class,'request-card')][.//h3[contains(normalize-space(), '" + title + "')]]//button[normalize-space()='" + action + "']")));
    }
}
