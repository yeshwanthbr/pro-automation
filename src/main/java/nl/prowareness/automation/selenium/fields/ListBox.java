package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * contains  methods related to DropDown Select
 *
 */
public class ListBox extends BaseElement{
    public ListBox(final SeleniumWebDriver webDriver, final FindBy findBy, final String findByValue) {
        super(webDriver, findBy, findByValue);
    }

    public void selectItemByVisibleText(final String visibleText) throws AutomationElementNotFoundException{
        webDriver.selectComboBoxItemByVisibleText(findBy, findByValue.get(), visibleText);
    }

    public void selectItemByValue(final String value) throws AutomationElementNotFoundException{
        webDriver.selectComboBoxItemByValue(findBy, findByValue.get(), value);
    }
    public void selectItemByIndex(final int index) throws AutomationElementNotFoundException{
        webDriver.selectComboBoxItemByIndex(findBy, findByValue.get(), index);
    }

    public String[] getItems() throws AutomationElementNotFoundException{
        return webDriver.getComboBoxElements(findBy, findByValue.get());
    }

    public String[] getSelectedItems() throws AutomationElementNotFoundException{
        return webDriver.getSelectedComboBoxElements(findBy, findByValue.get());
    }

    @Override
    public ListBox replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;
    }

}
