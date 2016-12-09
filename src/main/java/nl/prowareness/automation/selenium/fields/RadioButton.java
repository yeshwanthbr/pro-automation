package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * contains  methods related to Radio Button Actions
 *
 */
public class RadioButton extends BaseElement{
    public RadioButton(final SeleniumWebDriver webDriver, final FindBy findBy, final String findByValue) {
        super(webDriver, findBy, findByValue);
    }

    public boolean isChecked() throws AutomationElementNotFoundException {
        return webDriver.isElementChecked(findBy, findByValue.get());
    }

    public void select() throws AutomationElementNotFoundException{
        webDriver.click(findBy, findByValue.get());

    }

    @Override
    public RadioButton replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;
    }

}
