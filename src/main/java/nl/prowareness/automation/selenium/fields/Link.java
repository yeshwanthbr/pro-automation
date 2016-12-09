package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * contains  methods related to Links of a web Page
 *
 */
public class Link extends BaseElement{
    public Link(final SeleniumWebDriver webDriver, final FindBy findBy, final String findByValue){
        super(webDriver, findBy, findByValue);
    }

    public void click() throws AutomationElementNotFoundException{
        webDriver.click(findBy, findByValue.get());
    }  

    public String getHREF() throws AutomationElementNotFoundException{
        return getAttribute("href");
    }

    public String getName() throws AutomationElementNotFoundException{
        return getAttribute("name");
    }

    public String getOnClickScript() throws AutomationElementNotFoundException{
        return getAttribute("onclick");
    }
  

    @Override
    public Link replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;

    }

}
