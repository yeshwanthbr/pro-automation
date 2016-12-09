package nl.prowareness.automation.selenium.testsetup;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;
import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.fields.Button;
import nl.prowareness.automation.selenium.pageinitializers.BasePage;
import nl.prowareness.automation.selenium.utilities.FindElement;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestPageClass extends BasePage{
    private Logger log = Logger.getLogger(TestPageClass.class);
    @Autowired
    public TestPageClass(SeleniumWebDriver driver) throws AutomationDriverException {
        super(driver);

    }
    @FindElement(field="loginIcon", page="logon")
    private Button test;
    
    public void test() throws AutomationElementNotFoundException, AutomationDriverException{
        log.info("Testing");
        driver.typeText(test.getFindBy(), test.getFindByValue(), "Text");
       test.click();
       
       
    }
}
