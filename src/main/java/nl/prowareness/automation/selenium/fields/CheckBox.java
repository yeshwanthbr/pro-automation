package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * contains  methods related to CheckBox Actions
 *
 */
public class CheckBox extends BaseElement{
    public CheckBox(final SeleniumWebDriver webDriver, final FindBy findBy, final String findByValue){
        super(webDriver, findBy, findByValue);
    }

    public void click() throws AutomationElementNotFoundException{
        webDriver.click(findBy, findByValue.get());
    }

    public boolean isChecked() throws AutomationElementNotFoundException {
        return webDriver.isElementChecked(findBy, findByValue.get());
    }

    public void check() throws AutomationElementNotFoundException{
        webDriver.changeCheckBoxSelection(findBy, findByValue.get(), true);
    }

    public void uncheck() throws AutomationElementNotFoundException{
        webDriver.changeCheckBoxSelection(findBy, findByValue.get(), false);
    }

    @Override
    public CheckBox replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;
    }      

}
