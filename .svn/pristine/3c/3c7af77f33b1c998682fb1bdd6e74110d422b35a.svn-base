package nl.prowareness.automation.selenium.fields;


import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

public class HtmlElement extends BaseElement{

    public HtmlElement(SeleniumWebDriver webDriver, FindBy findBy, String findByValue) {
        super(webDriver, findBy, findByValue);
    }


    @Override
    public HtmlElement replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;

    }
}