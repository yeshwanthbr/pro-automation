package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * contains  methods related to Button Actions
 *
 */
public class Button extends BaseElement{

    public Button(SeleniumWebDriver webDriver, FindBy findBy, String findByValue) {
        super(webDriver, findBy, findByValue);
    }

    public void click() throws AutomationElementNotFoundException{
        webDriver.click(findBy, findByValue.get());
    }

    @Override
    public Button replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;

    }
}
