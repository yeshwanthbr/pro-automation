package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * contains  methods related to TextBox Actions
 *
 */
public class TextBox extends BaseElement{
    public TextBox(final SeleniumWebDriver webDriver, final FindBy findBy, final String findByValue){
        super(webDriver, findBy, findByValue);
    }
    public void typeText(final String text) throws AutomationElementNotFoundException{
        webDriver.typeText(findBy, findByValue.get(), text);
    }

    public void clear() throws AutomationElementNotFoundException{
        webDriver.clearText(findBy, findByValue.get());
    }

    public void click() throws AutomationElementNotFoundException{
        webDriver.click(findBy, findByValue.get());
    }


    @Override
    public TextBox replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;

    }

}