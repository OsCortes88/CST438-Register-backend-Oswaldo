package com.cst438;

import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class SystemTestRegistrationService {

    public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver-win64/chromedriver.exe";
    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.
    WebDriver driver;

    @BeforeEach
    public void testSetup() throws Exception {
        // if you are not using Chrome,
        // the following lines will be different.
        System.setProperty(
                "webdriver.chrome.driver",
                CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }

    @Test
    public void addStudent() throws Exception {
        // Get admin link
        WebElement adminLink = driver.findElement(By.xpath("//a[@href='/admin']"));
        // Go to admin page
        adminLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Click the "Add Student" button to open the dialog
        WebElement addStudentBtn = driver.findElement(By.id("addStudent"));
        addStudentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        // Find the input elements by id
        WebElement name = driver.findElement(By.id("studentName"));
        WebElement email = driver.findElement(By.id("studentEmail"));
        WebElement status = driver.findElement(By.id("studentStatus"));
        WebElement statusCode = driver.findElement(By.id("studentStatusCode"));

        // Enter student information to the input fields
        name.sendKeys("Steven Hawking");
        email.sendKeys("shawking@csumb.edu");
        status.sendKeys("Cleared");
        statusCode.sendKeys(Integer.toString(0));

        // FInd the submit element button and click it
        WebElement submit = driver.findElement(By.id("add"));
        submit.click();
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> newStudent = driver.findElements(By.xpath("//tr[last()]//td"));
        assertThat(newStudent.get(0).getText()).isEqualTo("Steven Hawking");
        assertThat(newStudent.get(1).getText()).isEqualTo("shawking@csumb.edu");
        assertThat(newStudent.get(2).getText()).isEqualTo("0");
        assertThat(newStudent.get(3).getText()).isEqualTo("Cleared");

        // drop the student
        WebElement dropButton = driver.findElement(By.xpath("//tr[td='shawking@csumb.edu']//td[last()]"));
        assertNotNull(dropButton);
        dropButton.click();

        // the drop course action causes an alert to occur.
        WebDriverWait wait = new WebDriverWait(driver, 1);
        wait.until(ExpectedConditions.alertIsPresent());

        // Switch to the alert and accept it
        Alert simpleAlert = driver.switchTo().alert();
        simpleAlert.accept();
        Thread.sleep(SLEEP_DURATION);

        // Verify successful deletion
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.xpath("//tr[td='shawking@csumb.edu']"));
        });

    }

    @Test
    public void updateStudent() throws Exception {
        // find web elements by id= or name=
        WebElement adminLink = driver.findElement(By.xpath("//a[@href='/admin']"));
        // Go to admin page
        adminLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Click the "Edit" button to open the dialog
        List<WebElement> columns = driver.findElements(By.xpath("//tr//td"));
        WebElement editStudentBtn = columns.get(4);
        editStudentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        // Get the input elements
        WebElement name = driver.findElement(By.id("newStudentName"));
        WebElement email = driver.findElement(By.id("newStudentEmail"));
        WebElement status = driver.findElement(By.id("newStudentStatus"));
        WebElement statusCode = driver.findElement(By.id("newStudentStatusCode"));

        // Add the new info to the input elements retrieved
        name.clear();
        name.sendKeys("Benny Rotten");
        email.clear();
        email.sendKeys("bRotten@csumb.edu");
        status.clear();
        status.sendKeys("Hold");
        statusCode.clear();
        statusCode.sendKeys(Integer.toString(1));

        // Click the save button
        WebElement submit = driver.findElement(By.id("saveChanges"));
        submit.click();
        Thread.sleep(SLEEP_DURATION);

        // Verify changes
        assertThat(columns.get(0).getText()).isEqualTo("Benny Rotten");
        assertThat(columns.get(1).getText()).isEqualTo("bRotten@csumb.edu");
        assertThat(columns.get(2).getText()).isEqualTo("1");
        assertThat(columns.get(3).getText()).isEqualTo("Hold");

        // Set student info back to its original state
        editStudentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        name = driver.findElement(By.id("newStudentName"));
        email = driver.findElement(By.id("newStudentEmail"));
        status = driver.findElement(By.id("newStudentStatus"));
        statusCode = driver.findElement(By.id("newStudentStatusCode"));

        name.clear();
        name.sendKeys("test");
        email.clear();
        email.sendKeys("test@csumb.edu");
        // The following simulates deleting the filled in text with key strokes
        status.sendKeys(Keys.CONTROL, "a", Keys.BACK_SPACE);
        statusCode.clear();
        statusCode.sendKeys(Integer.toString(0));

        submit = driver.findElement(By.id("saveChanges"));
        submit.click();
        Thread.sleep(SLEEP_DURATION);

        // Verify the students info was set back to normal
        assertThat(columns.get(0).getText()).isEqualTo("test");
        assertThat(columns.get(1).getText()).isEqualTo("test@csumb.edu");
        assertThat(columns.get(2).getText()).isEqualTo("0");
        assertThat(columns.get(3).getText()).isEqualTo("");


    }

    @Test
    public void forceDeleteStudent() throws Exception {

        // Get admin link
        WebElement adminLink = driver.findElement(By.xpath("//a[@href='/admin']"));
        // Go to admin page
        adminLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Click the "Delete" button to open the dialog
        WebElement addStudentBtn = driver.findElement(By.id("addStudent"));
        addStudentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        // Find the input elements by id
        WebElement name = driver.findElement(By.id("studentName"));
        WebElement email = driver.findElement(By.id("studentEmail"));
        WebElement status = driver.findElement(By.id("studentStatus"));
        WebElement statusCode = driver.findElement(By.id("studentStatusCode"));

        // Enter student information to the input fields
        name.sendKeys("Steven Hawking");
        email.sendKeys("shawking@csumb.edu");
        status.sendKeys("Cleared");
        statusCode.sendKeys(Integer.toString(0));

        // FInd the submit element button and click it
        WebElement submit = driver.findElement(By.id("add"));
        submit.click();
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> newStudent = driver.findElements(By.xpath("//tr[last()]//td"));
        assertThat(newStudent.get(0).getText()).isEqualTo("Steven Hawking");
        assertThat(newStudent.get(1).getText()).isEqualTo("shawking@csumb.edu");
        assertThat(newStudent.get(2).getText()).isEqualTo("0");
        assertThat(newStudent.get(3).getText()).isEqualTo("Cleared");

        // Click the "Delete" button for the enrolled student
        List<WebElement> columns = driver.findElements(By.xpath("//tr[last()]//td"));
        WebElement deleteStudentBtn = columns.get(5);
        deleteStudentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        Alert alert = driver.switchTo().alert();
        alert.accept();
        Thread.sleep(SLEEP_DURATION);

        // Verify successful deletion
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.xpath("//tr[td='shawking@csumb.edu']"));
        });
    }

    @AfterEach
    public void cleanup() {
        if (driver!=null) {
            driver.close();
            driver.quit();
            driver=null;
        }
    }
}
