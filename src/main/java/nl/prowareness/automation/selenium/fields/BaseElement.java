package nl.prowareness.automation.selenium.fields;

import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.exceptions.AutomationElementTimeOutException;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;



/**
 * Initializes instances of driver, findBy and findByValue
 *
 */
public abstract class BaseElement {
    protected SeleniumWebDriver webDriver;
    protected FindBy findBy;
    protected ThreadLocal<String> findByValue = new ThreadLocal<String>();
    protected String findByValueStatic;

    protected BaseElement(final SeleniumWebDriver webDriver, final FindBy findBy, final String findByValue) {
        this.webDriver = webDriver;
        this.findBy = findBy;
        this.findByValue.set(findByValue);
        findByValueStatic = findByValue;
    }

    public SeleniumWebDriver getWebDriver() {
        return webDriver;
    }

    public void setWebDriver(SeleniumWebDriver webDriver) {
        this.webDriver = webDriver;
    }



    public FindBy getFindBy() {
        return findBy;
    }
    public void setFindBy(FindBy findBy) {
        this.findBy = findBy;
    }
    public String getFindByValue() {
        return findByValue.get();
    }
    public void setFindByValue(String fieldName) {
        this.findByValue.set(fieldName);
    }

    public String getAttribute(final String attributeName) throws AutomationElementNotFoundException {
        return webDriver.getAttribute(findBy, findByValue.get(), attributeName);
    }

    public abstract BaseElement replaceSubStringOfFindByValue(String subStrToMatch, String subStrToReplaceWith);

    protected void replaceSubString(String subStrToMatch, String subStrToReplaceWith){
        findByValue.set(findByValueStatic.replace(subStrToMatch, subStrToReplaceWith));
    }


    public boolean isPresent(){
        return webDriver.isElementPresent(findBy, findByValue.get());
    }

    public boolean isVisible(){
        return webDriver.isElementVisible(findBy, findByValue.get());
    }

    public boolean isEnabled(){
        return webDriver.isElementEnabled(findBy, findByValue.get());
    }

    public void mouseHover() throws AutomationElementNotFoundException {
        webDriver.mouseHover(findBy, findByValue.get());
    }

    public String getText() throws AutomationElementNotFoundException{
        return webDriver.getElementText(findBy, findByValue.get());
    }
    
    public String getCSSValue(String propertyName) throws AutomationElementNotFoundException{
    	return webDriver.getCSSValue(findBy, findByValue.get(), propertyName);
    }

    public void waitForVisibilityOfElement() throws AutomationElementTimeOutException{
        webDriver.waitForVisibilityOfElementLocatedBy(findBy, findByValue.get());
    }

    public void waitForInVisibilityOfElement() throws AutomationElementTimeOutException{
        webDriver.waitForInVisibilityOfElementLocatedBy(findBy, findByValue.get());
    }

    public void waitForPresenceOfElement() throws AutomationElementTimeOutException{
        webDriver.waitForPresenceOfElementLocatedBy(findBy, findByValue.get());
    }
    
    public void waitForElementToBeClickable() throws AutomationElementTimeOutException{
        webDriver.waitForElementToBeClickableLocatedBy(findBy, findByValue.get());
    }

    
    public void waitForVisibilityOfElement(int waitSeconds) throws AutomationElementTimeOutException{
        webDriver.waitForVisibilityOfElementLocatedBy(findBy, findByValue.get(), waitSeconds);
    }

    public void waitForInVisibilityOfElement(int waitSeconds) throws AutomationElementTimeOutException{
        webDriver.waitForInVisibilityOfElementLocatedBy(findBy, findByValue.get(), waitSeconds);
    }

    public void waitForPresenceOfElement(int waitSeconds) throws AutomationElementTimeOutException{
        webDriver.waitForPresenceOfElementLocatedBy(findBy, findByValue.get(), waitSeconds);
    }

    public void waitForElementToBeClickable(int waitSeconds) throws AutomationElementTimeOutException{
        webDriver.waitForElementToBeClickableLocatedBy(findBy, findByValue.get(), waitSeconds);
    }


    public void clickByJavaScript() throws AutomationElementNotFoundException{
        webDriver.clickByJavaScript(findBy, findByValue.get());
    }

    public void clearCache(){
    	webDriver.clearBrowserCache();
    }

    public void javaScriptScrollUntilVisible() throws AutomationElementNotFoundException{
    	webDriver.javaScriptScrollToElement(findBy, findByValue.get());
    }

}
