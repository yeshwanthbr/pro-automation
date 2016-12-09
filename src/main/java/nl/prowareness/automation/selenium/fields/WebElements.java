package nl.prowareness.automation.selenium.fields;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

public class WebElements<T extends BaseElement> extends BaseElement {
    private Class<T> elementType;

    public WebElements(SeleniumWebDriver webDriver, FindBy findBy, String findByValue, Class<T> elementType) {
        super(webDriver, findBy, findByValue);
        this.elementType=elementType;
    }

    @Override
    public WebElements<T> replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith) {
        replaceSubString(subStrToMatch, subStrToReplaceWith);
        return this;
    }

    public List<T> getWebElements() throws AutomationDriverException {
        int size = webDriver.getNativeWebDriver().findElements(By.xpath(findByValue.get())).size();
        List<T> elements = new ArrayList<T>(); 
        for(int i=1;i<=size;i++){
            try {
                
            	elements.add(i-1, elementType.getConstructor(SeleniumWebDriver.class, FindBy.class, String.class).newInstance(webDriver, findBy, "("+findByValue.get()+")["+i+"]"));
            } catch (InstantiationException | IllegalAccessException 
                    | InvocationTargetException | NoSuchMethodException  e) {
                throw new AutomationDriverException("Error creating Webelement", e);
            }
        }
        return elements;
    }

}
