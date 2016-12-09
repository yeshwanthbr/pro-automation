package nl.prowareness.automation.selenium.webdriver;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;
import nl.prowareness.automation.selenium.exceptions.AutomationElementNotFoundException;
import nl.prowareness.automation.selenium.exceptions.AutomationElementTimeOutException;
import nl.prowareness.automation.selenium.objectparser.ObjectRepositoryManager;
import nl.prowareness.automation.selenium.utilities.FindBy;

/**
 * creates selenium Wrapper object
 *
 */
@Component
public class SeleniumWebDriver {

	private static final String FIREFOX_PREFERENCES="firefox.preferences";
	private static final String WAIT_ON_ELEMENT_VISIBILITY="wait.onElementVisibility";


	private static final Logger LOGGER = Logger.getLogger(SeleniumWebDriver.class);
	private static String driverExeDirectory="src/main/resources/drivers";
	private int implicitWait = 10;
	
	private ThreadLocal<WebDriver> nativeWebDriver = new ThreadLocal<WebDriver>();
	private  Environment properties;
	private ObjectRepositoryManager objRepoManager = new ObjectRepositoryManager();
	private ThreadLocal<List<File>> screenShotFileList = new ThreadLocal<List<File>>();
	private ThreadLocal<Actions> actions = new ThreadLocal<Actions>();
	private ThreadLocal<Proxy> seleniumProxy = new ThreadLocal<Proxy>();
	private Set<WebDriver> activeDrivers = new HashSet<>();
	private ThreadLocal<Boolean> takeScreenshotOnException = new ThreadLocal<Boolean>(){
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	public Set<WebDriver> getActiveDrivers() {
		return activeDrivers;
	}

	private ThreadLocal<Boolean> takeScreenshotOnTimeOutException = new ThreadLocal<Boolean>(){
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	
	public boolean isTakeScreenshotOnException() {
		return takeScreenshotOnException.get();
	}

	public void setTakeScreenshotOnException(boolean takeScreenshotOnException) {
		this.takeScreenshotOnException.set(takeScreenshotOnException);
	}

	public boolean isTakeScreenshotOnTimeOutException() {
		return takeScreenshotOnTimeOutException.get();
	}

	public void setTakeScreenshotOnTimeOutException(boolean takeScreenshotOnTimeOutException) {
		this.takeScreenshotOnTimeOutException.set(takeScreenshotOnTimeOutException);
	}

	@Autowired
	public SeleniumWebDriver(Environment environment) throws AutomationDriverException {
		properties=environment;
		setRepository();
	}
	
	public void setSeleniumProxy(Proxy seleniumProxy){
		this.seleniumProxy.set(seleniumProxy);
	}

	public List<File> getScreenShotFileList() {
		return screenShotFileList.get();
	}

	public Actions getActions() {
		return actions.get();
	}

	public WebDriver getNativeWebDriver() {
		return nativeWebDriver.get();
	}

	public Environment getProperties() {
		return properties;
	}

	public void setProperties(Environment properties) {
		this.properties = properties;
	}

	public ObjectRepositoryManager getObjRepoManager() {
		return objRepoManager;
	}

	public void setObjRepoManager(ObjectRepositoryManager objRepoManager) {
		this.objRepoManager = objRepoManager;
	}

	public int getImplicitWait() {
		return implicitWait;
	}

	public void setImplicitWait(int implicitWait) {
		this.implicitWait = implicitWait;
		nativeWebDriver.get().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
	}

	private void setRepository() throws AutomationDriverException {
		String root = StringUtils.isEmpty(properties.getProperty("selenium.objectRepository.directory"))?"src/main/resources/objectRepository":properties.getProperty("selenium.objectRepository.directory");
		File folder = new File(root);
		List<File> listOfFiles = (List<File>) FileUtils.listFiles(folder, new WildcardFileFilter("*.xml"), null);
		for(File file:listOfFiles){
			objRepoManager.parseFile(file);
		}
	}

	public void setUpBrowser() throws AutomationDriverException{
		initializeScreenShot();
		if(!StringUtils.isEmpty(properties.getProperty("selenium.drivers.directory"))){
			driverExeDirectory=properties.getProperty("selenium.drivers.directory");
		}
		initializeWebDriver();
		try {
			setImplicitWait(Integer.parseInt(properties.getProperty("selenium.implicitWait")));
		} catch (NumberFormatException e) {
			throw new AutomationDriverException("Implicit Wait should be a number", e);
		}
		initializeActions();
		manageBrowser();
		setPageLoadTimeout();
		
	}
	
	public void setUpBrowser(String remoteURL, String browserName, String browserVersion,
			String operatingSystemPlatform) throws AutomationDriverException{
		initializeScreenShot();
		if(!StringUtils.isEmpty(properties.getProperty("selenium.drivers.directory"))){
			driverExeDirectory=properties.getProperty("selenium.drivers.directory");
		}
		try{
			initializeSeleniumDriver(remoteURL, browserName, browserVersion, operatingSystemPlatform);
		}catch(MalformedURLException e){
			throw new AutomationDriverException("Invalid Remote Webdriver URL", e);
		}
		try {
			setImplicitWait(Integer.parseInt(properties.getProperty("selenium.implicitWait")));
		} catch (NumberFormatException e) {
			throw new AutomationDriverException("Implicit Wait should be a number", e);
		}
		initializeActions();
		manageBrowser();
		setPageLoadTimeout();
		
	}

	private void initializeScreenShot() {
		if("TRUE".equalsIgnoreCase(properties.getProperty("selenium.takeScreenshotOnException"))){
			takeScreenshotOnException.set((Boolean) true);
		}
		if("TRUE".equalsIgnoreCase(properties.getProperty("selenium.takeScreenshotOnTimeOutException"))){
			takeScreenshotOnTimeOutException.set((Boolean) true);
		}
	}
	
	public void setUpBrowser(WebDriver driver) throws AutomationDriverException{
		initializeScreenShot();
		nativeWebDriver.set(driver);
		try {
			setImplicitWait(Integer.parseInt(properties.getProperty("selenium.implicitWait")));
		} catch (NumberFormatException e) {
			throw new AutomationDriverException("Implicit Wait should be a number", e);
		}
		initializeActions();
		manageBrowser();
		setPageLoadTimeout();
	}

	private void setPageLoadTimeout() throws AutomationDriverException {
		if(!StringUtils.isEmpty(properties.getProperty("selenium.pageLoad"))){
			try{
				nativeWebDriver.get().manage().timeouts().pageLoadTimeout(Integer.parseInt(properties.getProperty("selenium.pageLoad")), TimeUnit.SECONDS);
			}catch (NumberFormatException e) {
				throw new AutomationDriverException("Page Load Wait should be a number", e);
			}
		}
	}

	private void initializeActions() {
		actions.set(new Actions(nativeWebDriver.get()));
	}

	private void initializeWebDriver() throws AutomationDriverException  {
		String remoteURL="";
		String browserName="";
		String browserVersion="";
		String operatingSystemPlatform="";
		try{
			browserName = properties.getProperty("selenium.browser").toUpperCase();
		}catch(NullPointerException e){
			throw new AutomationDriverException("Missing properties, ensure that the following properties are added to property file:selenium.remoteUrl, selenium.browser, selenium.browserVersion, selenium.operatingSystem", e);
		}
		if(properties.containsProperty("selenium.remoteUrl")){
			remoteURL = properties.getProperty("selenium.remoteUrl");
		}
		if(properties.containsProperty("selenium.browserVersion")){
			browserVersion = properties.getProperty("selenium.browserVersion");
		}

		if(properties.containsProperty("selenium.operatingSystem")){
			operatingSystemPlatform = properties.getProperty("selenium.operatingSystem");
		}
		try{
			initializeSeleniumDriver(remoteURL, browserName, browserVersion, operatingSystemPlatform);
		}catch(MalformedURLException e){
			throw new AutomationDriverException("Invalid Remote Webdriver URL", e);
		}
	}

	public void initializeSeleniumDriver(String remoteURL, String browserName, String browserVersion,
			String operatingSystemPlatform) throws MalformedURLException {

		if(StringUtils.isEmpty(remoteURL)){
			switch (browserName){
			case "FIREFOX":
				setFirefoxDriver();
				break;
			case "CHROME":
				setChromeDriver();
				break;
			/*case "HTMLDRIVER":
				setHtmlUnitDriver();
				break;
			*/default:
				setInternetExplorerDriver();
			}
		}
		else{
			DesiredCapabilities capabilities = null;

			switch (browserName) {
			case "HTMLDRIVER":
				capabilities = DesiredCapabilities.htmlUnit();
				break;
			case "CHROME":
				capabilities = DesiredCapabilities.chrome();
				break;
			case "FIREFOX":
				capabilities = DesiredCapabilities.firefox();
				capabilities.setCapability(FirefoxDriver.PROFILE, getFireFoxProfile());
				break;
			default:
				capabilities = DesiredCapabilities.internetExplorer();
				break;
			}
			if (!StringUtils.isEmpty(browserVersion)) {
				capabilities.setVersion(browserVersion);
			}
			capabilities.setPlatform(getPlatform(operatingSystemPlatform));
			if(this.seleniumProxy.get()!=null){
				capabilities.setCapability(CapabilityType.PROXY, seleniumProxy.get());
			}
			nativeWebDriver.set(new RemoteWebDriver(new URL(remoteURL), capabilities));
			((RemoteWebDriver) nativeWebDriver.get()).setFileDetector(new LocalFileDetector());
		}

		activeDrivers.add(nativeWebDriver.get());
	}

	private void setInternetExplorerDriver() {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.internetExplorer();
		if(this.seleniumProxy.get()!=null){
			capabilities.setCapability(CapabilityType.PROXY, seleniumProxy.get());
		}
		System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY,"./"+driverExeDirectory+"/IEDriverServer.exe");
		nativeWebDriver.set(new InternetExplorerDriver(capabilities));
	}

/*	private void setHtmlUnitDriver() {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.htmlUnit();
		if(this.seleniumProxy.get()!=null){
			capabilities.setCapability(CapabilityType.PROXY, seleniumProxy.get());
		}
		capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
		nativeWebDriver.set(new HtmlUnitDriver(capabilities));
	}*/

	private void setChromeDriver() {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.chrome();
		if(this.seleniumProxy.get()!=null){
			capabilities.setCapability(CapabilityType.PROXY, seleniumProxy.get());
		}
		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,"./"+driverExeDirectory+"/chromedriver.exe");
		nativeWebDriver.set(new ChromeDriver(capabilities));
	}

	private void setFirefoxDriver() {
		DesiredCapabilities capabilities;
		capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability(FirefoxDriver.PROFILE, getFireFoxProfile());
		if(this.seleniumProxy.get()!=null){
			capabilities.setCapability(CapabilityType.PROXY, seleniumProxy.get());
		}
		System.setProperty("webdriver.gecko.driver", "./"+driverExeDirectory+"/geckodriver.exe");
		nativeWebDriver.set(new FirefoxDriver(capabilities));
	}
	private FirefoxProfile getFireFoxProfile() {
		FirefoxProfile profile = new FirefoxProfile();
		if(!StringUtils.isEmpty(properties.getProperty(FIREFOX_PREFERENCES))){
			String[] preferences = properties.getProperty(FIREFOX_PREFERENCES).split(",");
			for (String pref : preferences){
				String[] preference = properties.getProperty(pref).split("::");
				if("STRING".equalsIgnoreCase(preference[0])){
					profile.setPreference(pref, preference[1]);
				}else if("INTEGER".equalsIgnoreCase(preference[0])){
					profile.setPreference(pref, Integer.parseInt(preference[1]));
				}
				else if("BOOLEAN".equalsIgnoreCase(preference[0])){
					profile.setPreference(pref, Boolean.parseBoolean(preference[1]));
				}
			}
		}
		return profile;
	}

	private void manageBrowser(){
		nativeWebDriver.get().manage().window().maximize();
	}

	public String getPageTitle() {
		return nativeWebDriver.get().getTitle();
	}

	public String getCurrentUrl() {
		return nativeWebDriver.get().getCurrentUrl();
	}
	public String getPageSource() {
		return nativeWebDriver.get().getPageSource();
	}

	public void connect(final String url) {
		nativeWebDriver.get().get(url);
	}

	public void loadURL(String url) throws AutomationDriverException{
		try{
			nativeWebDriver.get().get(url);
		}catch(WebDriverException e){
			LOGGER.error("Unable to launch url "+url,e);
			throw new AutomationDriverException("Unable to launch url "+url, e);
		}
	}

	public void navigateToURL(String url) throws AutomationDriverException{
		try{
			nativeWebDriver.get().navigate().to(url);
		}catch(WebDriverException e){
			LOGGER.error("Unable to navigate to url "+url,e);
			throw new AutomationDriverException("Unable to navigate to url "+url, e);
		}
	}

	public void disconnect() {
		nativeWebDriver.get().manage().deleteAllCookies();
		nativeWebDriver.get().quit();

	}


	private Platform getPlatform(final String osType) {
		Platform platform;
		if(StringUtils.isEmpty(osType)){
			return Platform.ANY;
		}
		switch (osType.toUpperCase()) {
		case "WINDOWS":
			platform = Platform.WINDOWS;
			break;
		case "LINUX":
			platform = Platform.LINUX;
			break;
		case "UNIX":
			platform = Platform.UNIX;
			break;
		case "MAC":
			platform = Platform.MAC;
			break;
		default:
			platform = Platform.ANY;
			break;
		}
		return platform;
	}

	private WebElement findWebElement(final FindBy by, final String locator) {
		WebElement element = null;
		switch(by){
		case ID :
			element = nativeWebDriver.get().findElement(By.id(locator));
			break;
		case NAME :
			element = nativeWebDriver.get().findElement(By.name(locator));
			break;
		case CLASS_NAME :
			element = nativeWebDriver.get().findElement(By.className(locator));
			break;
		case XPATH :
			element = nativeWebDriver.get().findElement(By.xpath(locator));
			break;
		case TAG_NAME :
			element = nativeWebDriver.get().findElement(By.tagName(locator));
			break;
		case CSS_SELECTOR :
			element = nativeWebDriver.get().findElement(By.cssSelector(locator));
			break;
		case LINKTEXT :
			element = nativeWebDriver.get().findElement(By.linkText(locator));
			break;
		case PARTIAL_LINKTEXT :
			element = nativeWebDriver.get().findElement(By.partialLinkText(locator));
			break;
		default:
			element = nativeWebDriver.get().findElement(By.id(locator));
			break;
		}
		return element;
	}


	private By getByLocator(final FindBy by, final String locator) {
		By tempBy = null;
		switch(by){
		case ID :
			tempBy = By.id(locator);
			break;
		case NAME :
			tempBy = By.name(locator);
			break;
		case CLASS_NAME :
			tempBy = By.className(locator);
			break;
		case XPATH :
			tempBy = By.xpath(locator);
			break;
		case TAG_NAME :
			tempBy = By.tagName(locator);
			break;
		case CSS_SELECTOR :
			tempBy = By.cssSelector(locator);
			break;
		case LINKTEXT :
			tempBy = By.linkText(locator);
			break;
		case PARTIAL_LINKTEXT :
			tempBy = By.partialLinkText(locator);
			break;
		default:
			tempBy = By.id(locator);
			break;
		}
		return tempBy;
	}

	public List<WebElement> findWebElements(final FindBy by, final String locator) {
		List<WebElement> elements = null;
		switch(by){
		case ID :
			elements = nativeWebDriver.get().findElements(By.id(locator));
			break;
		case NAME :
			elements = nativeWebDriver.get().findElements(By.name(locator));
			break;
		case CLASS_NAME :
			elements = nativeWebDriver.get().findElements(By.className(locator));
			break;
		case XPATH :
			elements = nativeWebDriver.get().findElements(By.xpath(locator));
			break;
		case TAG_NAME :
			elements = nativeWebDriver.get().findElements(By.tagName(locator));
			break;
		case CSS_SELECTOR :
			elements = nativeWebDriver.get().findElements(By.cssSelector(locator));
			break;
		case LINKTEXT :
			elements = nativeWebDriver.get().findElements(By.linkText(locator));
			break;
		case PARTIAL_LINKTEXT :
			elements = nativeWebDriver.get().findElements(By.partialLinkText(locator));
			break;
		default:
			elements = nativeWebDriver.get().findElements(By.id(locator));
			break;
		}
		return elements;
	}

	public void click(final FindBy by, final String locator) throws AutomationElementNotFoundException  {
		try {
			WebElement element = findWebElement(by, locator);
			element.click();
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
	}

	public void typeText(final FindBy by, final String locator, final String text) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(by, locator);
			element.sendKeys(text);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
	}

	private void handleNoSuchElement(final FindBy by, final String locator,
			NoSuchElementException e) throws AutomationElementNotFoundException {
		if(takeScreenshotOnException.get()){
			this.captureScreenshot();
		}
		throw new AutomationElementNotFoundException(by, locator, e);
	}
	private void handleTimeOutException(final FindBy by, final String locator,
			TimeoutException e) throws AutomationElementTimeOutException  {
		if(takeScreenshotOnTimeOutException.get()){
			this.captureScreenshot();
		}
		throw new AutomationElementTimeOutException(by, locator, e);
	}

	public void submitElement(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(by, locator);
			element.submit();
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
	}


	public String getElementText(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		String elementText = null;
		try {
			WebElement element = findWebElement(by, locator);
			elementText = element.getText();
			if (StringUtils.isEmpty(elementText)) {
				elementText = element.getAttribute("value");
			}
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
		return elementText;
	}

	public boolean isTextPresent(final String text) {
		String source = nativeWebDriver.get().getPageSource();
		return source.contains(text);
	}

	public void clearText(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(by, locator);
			element.clear();
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
	}

	public String getAttribute(final FindBy by, final String locator, final String attributeName) throws AutomationElementNotFoundException {
		String attributeValue = null;
		try {
			WebElement element = findWebElement(by, locator);
			attributeValue = element.getAttribute(attributeName);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
		return attributeValue;
	}

	public String getCSSValue(final FindBy by, final String locator, final String propertyName) throws AutomationElementNotFoundException {
		String cssPropertyValue = null;
		try {
			WebElement element = findWebElement(by, locator);
			cssPropertyValue = element.getCssValue(propertyName);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
		return cssPropertyValue;
	}


	public boolean isElementChecked(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		boolean isElementChecked = false;
		nativeWebDriver.get().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			WebElement element = findWebElement(by, locator);
			if (element.isSelected()) {
				isElementChecked = true;
			}
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
		nativeWebDriver.get().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
		return isElementChecked;
	}

	public boolean isElementEnabled(final FindBy by, final String locator) {
		boolean isElementEnabled = false;
		nativeWebDriver.get().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			WebElement element = findWebElement(by, locator);
			if (element != null && element.isEnabled()) {
				isElementEnabled = true;
			}
		} catch (NoSuchElementException e) {
			LOGGER.error("NoSuchElementException:",e);
			isElementEnabled = false;
		}
		nativeWebDriver.get().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
		return isElementEnabled;
	}

	public boolean isElementPresent(final FindBy by, final String locator) {
		boolean isElementPresent = false;
		nativeWebDriver.get().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			WebElement element = findWebElement(by, locator);
			if (element != null) {
				isElementPresent = true;
			}
		} catch (NoSuchElementException e) {
			LOGGER.error("NoSuchElementException:",e);
			isElementPresent = false;
		}
		nativeWebDriver.get().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
		return isElementPresent;
	}

	public boolean isElementVisible(final FindBy by, final String locator) {
		boolean isElementPresent = false;
		nativeWebDriver.get().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			WebElement element = findWebElement(by, locator);
			if (element != null && element.isDisplayed()) {
				isElementPresent = true;
			}
		} catch (NoSuchElementException e) {
			isElementPresent = false;
			LOGGER.error("Invalid locator types in ObjectRepository:",e);

		}
		nativeWebDriver.get().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
		return isElementPresent;
	}

	public void changeCheckBoxSelection(final FindBy by, final String locator, final boolean isSelected) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(by, locator);
			if (element.isSelected() && !isSelected) {
				element.click();
			} else if (!element.isSelected() && isSelected) {
				element.click();
			}
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
	}

	public void selectComboBoxItemByVisibleText(final FindBy comboBoxLocatorBy, final String comboBoxLocator, final String visibleText) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(comboBoxLocatorBy, comboBoxLocator);
			Select selectBox = new Select(element);
			selectBox.selectByVisibleText(visibleText);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(comboBoxLocatorBy, comboBoxLocator, e);
		}
	}


	public void selectComboBoxItemByValue(final FindBy comboBoxLocatorBy, final String comboBoxLocator, final String comboValue) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(comboBoxLocatorBy, comboBoxLocator);
			Select selectBox = new Select(element);
			selectBox.selectByValue(comboValue);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(comboBoxLocatorBy, comboBoxLocator, e);
		}
	}

	public void selectComboBoxItemByIndex(final FindBy comboBoxLocatorBy, final String comboBoxLocator, final int index) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(comboBoxLocatorBy, comboBoxLocator);
			Select selectBox = new Select(element);
			selectBox.selectByIndex(index);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(comboBoxLocatorBy, comboBoxLocator, e);
		}
	}

	public String[] getComboBoxElements(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		String[] returnArray = new String[0];
		List<String> elementTextList = new ArrayList<String>();
		try {
			WebElement element = findWebElement(by, locator);
			Select selectBox = new Select(element);
			List<WebElement> options = selectBox.getOptions();
			for (WebElement webElement : options) {
				elementTextList.add(webElement.getText());
			}
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
		return elementTextList.toArray(returnArray);
	}

	public String[] getSelectedComboBoxElements(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		String[] returnArray = new String[0];
		List<String> elementTextList = new ArrayList<String>();
		try {
			WebElement element = findWebElement(by, locator);
			Select selectBox = new Select(element);
			List<WebElement> options = selectBox.getAllSelectedOptions();
			for (WebElement webElement : options) {
				elementTextList.add(webElement.getText());
			}
		} catch (NoSuchElementException e) {
			handleNoSuchElement(by, locator, e);
		}
		return elementTextList.toArray(returnArray);
	}

	public void deselectComboBoxItem(final FindBy comboBoxLocatorBy, final String comboBoxLocator, final String comboValue) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(comboBoxLocatorBy, comboBoxLocator);
			Select selectBox = new Select(element);
			selectBox.deselectByVisibleText(comboValue);
		} catch (NoSuchElementException e) {
			handleNoSuchElement(comboBoxLocatorBy, comboBoxLocator, e);
		}
	}

	public void deselectAllComboBoxItems(final FindBy comboBoxLocatorBy, final String comboBoxLocator) throws AutomationElementNotFoundException {
		try {
			WebElement element = findWebElement(comboBoxLocatorBy, comboBoxLocator);
			Select selectBox = new Select(element);
			selectBox.deselectAll();
		} catch (NoSuchElementException rte) {
			handleNoSuchElement(comboBoxLocatorBy, comboBoxLocator, rte);
		}
	}

	public void captureScreenshot(){
		File scrFile = ((TakesScreenshot)nativeWebDriver.get()).getScreenshotAs(OutputType.FILE);
		if(screenShotFileList.get()==null){
			screenShotFileList.set(new ArrayList<File>());
		}screenShotFileList.get().add(scrFile);
	}

	public void generateScreenshotsFromList(String foldername) throws AutomationDriverException {
		if(screenShotFileList.get()==null){
			return;
		}
		try{

			for (int i = 0; i < screenShotFileList.get().size(); i++) {
				FileUtils.copyFile(screenShotFileList.get().get(i), new File(foldername+"/"+i+".png"));
			}
		}catch(IOException e){
			throw new AutomationDriverException("Error generating screenshots", e);
		}
	}

	public void clearScreenshotsList(){
		if(screenShotFileList.get()!=null){
			screenShotFileList.get().clear();
		}
	}

	public void activateWindow(final String windowHandle) {
		nativeWebDriver.get().switchTo().window(windowHandle);
	}

	public Set<String> getWindowHandles() {
		return Collections.unmodifiableSet(nativeWebDriver.get().getWindowHandles());
	}

	public String getCurrentWindowHandle() {
		return nativeWebDriver.get().getWindowHandle();
	}

	public void switchToWindow(String windowHandle){
		nativeWebDriver.get().switchTo().window(windowHandle);
	}

	public void switchToDefaultWindow(){
		nativeWebDriver.get().switchTo().defaultContent();
	}

	public void switchToFrame(int index) {
		nativeWebDriver.get().switchTo().frame(index);
	}

	public void switchToFrame(final String locator) {
		nativeWebDriver.get().switchTo().frame(locator);
	}

	public void switchToFrame(final WebElement element) {
		nativeWebDriver.get().switchTo().frame(element);
	}

	public void switchToParentFrame() {
		nativeWebDriver.get().switchTo().parentFrame();
	}

	public void switchToDefaultFrame() {
		nativeWebDriver.get().switchTo().defaultContent();
	}

	public void deleteAllCookies() {
		nativeWebDriver.get().manage().deleteAllCookies();
	}

	public Set<Cookie> getAllCookies() {
		return nativeWebDriver.get().manage().getCookies();
	}
	public Cookie getCookie(String cookieName) {
		return nativeWebDriver.get().manage().getCookieNamed(cookieName);
	}
	public void deleteCookie(final String name) {
		nativeWebDriver.get().manage().deleteCookieNamed(name);
	}

	public void addCookie(final String name, final String value, final String domain, final String path, final Date expires, final boolean isSecure) {
		Cookie cookie = new Cookie(name, value, domain, path, expires, isSecure);
		nativeWebDriver.get().manage().addCookie(cookie);
	}

	public void clearBrowserCache(){
		actions.get().sendKeys(Keys.CONTROL).sendKeys(Keys.SHIFT).sendKeys(Keys.chord("R")).build().perform();
	}

	public void acceptAlert() {
		nativeWebDriver.get().switchTo().alert().accept();
	}

	public void dismissAlert() {
		nativeWebDriver.get().switchTo().alert().dismiss();
	}

	public String getAlertText() {
		return nativeWebDriver.get().switchTo().alert().getText();
	}

	public boolean isAlertPresent() throws AutomationDriverException{
		boolean isAlertPresent = false;
		try {
			Alert alert = nativeWebDriver.get().switchTo().alert();
			if (alert != null && alert.getText().length() > 0) {
				isAlertPresent = true;
			}
		} catch (NoAlertPresentException e) {
			return false;
		}
		return isAlertPresent;
	}


	public boolean waitUntilJQueryLoad(){
		WebDriverWait wait = new WebDriverWait(nativeWebDriver.get(), Long.parseLong(properties.getProperty(WAIT_ON_ELEMENT_VISIBILITY)));

		ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return (Boolean) ((JavascriptExecutor)driver).executeScript("return jQuery.active == 0");
				}
				catch (WebDriverException e) {
					LOGGER.error("WebDriverException:",e);
					return true;
				}
			}
		};

		return wait.until(jQueryLoad);
	}


	public boolean waitUntilJavaScriptLoad(){

		WebDriverWait wait = new WebDriverWait(nativeWebDriver.get(), Long.parseLong(properties.getProperty(WAIT_ON_ELEMENT_VISIBILITY)));

		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return "complete".equals(((JavascriptExecutor)driver).executeScript("return document.readyState"));
			}
		};

		return wait.until(jsLoad);

	}

	public Object executeScript(final String scriptToExecute, final Object... args) {
		JavascriptExecutor js = (JavascriptExecutor) nativeWebDriver.get();
		if (args == null) {
			return js.executeScript(scriptToExecute);
		} else {
			return js.executeScript(scriptToExecute, args);
		}

	}

	public Object executeAsyncScript(final String scriptToExecute, final Object... args) {
		JavascriptExecutor js = (JavascriptExecutor) nativeWebDriver.get();
		if (args == null) {
			return js.executeAsyncScript(scriptToExecute);
		} else {
			return js.executeAsyncScript(scriptToExecute, args);
		}
	}

	public void clickByJavaScript(final FindBy by, final String locator) throws AutomationElementNotFoundException {
		try{
			JavascriptExecutor js = (JavascriptExecutor)nativeWebDriver.get();
			WebElement element = findWebElement(by, locator);
			js.executeScript("arguments[0].click();", element);
		}catch(NoSuchElementException e){
			handleNoSuchElement(by, locator, e);
		}
	}

	public void javaScriptScroll(String pixelsToScroll) {
		JavascriptExecutor js = (JavascriptExecutor)nativeWebDriver.get();
		js.executeScript("window.scrollBy(0,"+pixelsToScroll+")", "");
	}

	public void javaScriptScrollToElement(final FindBy by, final String locator) throws AutomationElementNotFoundException{
		try{
			WebElement element = findWebElement(by, locator);
			JavascriptExecutor js = (JavascriptExecutor)nativeWebDriver.get();
			js.executeScript("arguments[0].scrollIntoView(true);", element);
		}catch(NoSuchElementException e){
			handleNoSuchElement(by, locator, e);
		}
	}





	public void waitForVisibilityOfElementLocatedBy(final FindBy by, final String locator) throws AutomationElementTimeOutException {
		waitForVisibilityOfElementLocatedBy(by, locator, Integer.parseInt(properties.getProperty(WAIT_ON_ELEMENT_VISIBILITY)));

	}

	public void waitForInVisibilityOfElementLocatedBy(final FindBy by, final String locator)throws AutomationElementTimeOutException {
		waitForInVisibilityOfElementLocatedBy(by, locator, Integer.parseInt(properties.getProperty(WAIT_ON_ELEMENT_VISIBILITY)));

	}

	public void waitForPresenceOfElementLocatedBy(final FindBy by, final String locator) throws AutomationElementTimeOutException {
		waitForPresenceOfElementLocatedBy(by, locator, Integer.parseInt(properties.getProperty(WAIT_ON_ELEMENT_VISIBILITY)));
	}

	public void waitForElementToBeClickableLocatedBy(final FindBy by, final String locator) throws AutomationElementTimeOutException{
		waitForElementToBeClickableLocatedBy(by, locator, Integer.parseInt(properties.getProperty(WAIT_ON_ELEMENT_VISIBILITY)));
	}




	public void waitForVisibilityOfElementLocatedBy(final FindBy by, final String locator, final int waitSeconds) throws AutomationElementTimeOutException {
		WebDriverWait waitForElement = new WebDriverWait(nativeWebDriver.get(), waitSeconds);
		try{
			waitForElement.until(ExpectedConditions.visibilityOfElementLocated(getByLocator(by, locator)));
		}catch (TimeoutException e){
			handleTimeOutException(by, locator, e);
		}

	}

	public void waitForInVisibilityOfElementLocatedBy(final FindBy by, final String locator, final int waitSeconds) throws AutomationElementTimeOutException {
		WebDriverWait waitForElement = new WebDriverWait(nativeWebDriver.get(), waitSeconds);
		try{
			waitForElement.until(ExpectedConditions.invisibilityOfElementLocated(getByLocator(by, locator)));
		}catch (TimeoutException e){
			handleTimeOutException(by, locator, e);
		}

	}

	public void waitForPresenceOfElementLocatedBy(final FindBy by, final String locator, final int waitSeconds) throws AutomationElementTimeOutException {
		WebDriverWait waitForElement = new WebDriverWait(nativeWebDriver.get(), waitSeconds);
		try{
			waitForElement.until(ExpectedConditions.presenceOfElementLocated(getByLocator(by, locator)));
		}catch (TimeoutException e){
			handleTimeOutException(by, locator, e);
		}
	}

	public void waitForElementToBeClickableLocatedBy(final FindBy by, final String locator, final int waitSeconds) throws AutomationElementTimeOutException {
		WebDriverWait waitForElement = new WebDriverWait(nativeWebDriver.get(), waitSeconds);
		try{
			waitForElement.until(ExpectedConditions.elementToBeClickable(getByLocator(by, locator)));
		}catch (TimeoutException e){
			handleTimeOutException(by, locator, e);
		}
	}


	public void mouseHover(final FindBy by, final String locator) throws AutomationElementNotFoundException{
		try{
			WebElement element = findWebElement(by, locator);
			actions.get().moveToElement(element).perform();
		}catch(NoSuchElementException e){
			handleNoSuchElement(by, locator, e);
		}
	}

    public void mouseHoverAndClick(final FindBy by, final String locator, final FindBy clickBy, final String clickLocator) throws AutomationElementNotFoundException {
        try {
            WebElement hoverElement = findWebElement(by, locator);
            WebElement clickElement = findWebElement(clickBy, clickLocator);
            actions.get().moveToElement(hoverElement).moveToElement(clickElement).click(clickElement).build().perform();

        } catch (NoSuchElementException e) {
        	handleNoSuchElement(by, locator, e);
        }
    }

    public void rightClick(final FindBy findBy, final String locator) throws AutomationElementNotFoundException {

        try {
            WebElement hoverElement = findWebElement(findBy, locator);
            actions.get().contextClick(hoverElement).build().perform();
        } catch (NoSuchElementException e) {
        	handleNoSuchElement(findBy, locator, e);
        }
    }

    public void doubleClick(final FindBy findBy, final String locator) throws AutomationElementNotFoundException {
        try {
            WebElement hoverElement = findWebElement(findBy, locator);
            actions.get().doubleClick(hoverElement).build().perform();
        } catch (NoSuchElementException e) {
        	handleNoSuchElement(findBy, locator, e);
        }
    }

    public void navigateBack()
    {
    	nativeWebDriver.get().navigate().back();
    }

	public boolean hasQuit() {
		try{
			this.nativeWebDriver.get().getTitle();
			return true;
		}catch(Exception e){
			return false;
		}
	}
		
	/*private HtmlTable extractTable(final WebElement tableElement, final boolean parseTableHeader, final int importantHeaderRow, final int parseRowCount) {
        HtmlTable table = null;
        List<WebElement> headerRows = tableElement.findElements(By.xpath("thead/tr"));//By.tagName(HTMLConstants.TR));
        List<WebElement> rows = tableElement.findElements(By.xpath("tbody/tr"));//By.tagName(HTMLConstants.TR));
        table = new HtmlTable(headerRows.size() + rows.size(), 0);
        if (parseTableHeader) {
            table = new HtmlTable(headerRows.size() - importantHeaderRow, 0);
        }
        int rowCount = importantHeaderRow;
        if (parseTableHeader) {
            for (int i = 0; i < headerRows.size(); i++) {
                WebElement e = headerRows.get(i);

                if (i == (rowCount - 1)) {
                    List<WebElement> tableHeaders = e.findElements(By.xpath("*"));//By.tagName(HTMLConstants.TH));
                    if (!tableHeaders.isEmpty()) {
                        for (int j = 0; j < tableHeaders.size(); j++) {
                            table.setColumnHeader(j + 1, tableHeaders.get(j).getText());
                        }
                    }
                }
            }
        }

        int loopUntil = (parseRowCount == -1 ? rows.size() : parseRowCount);
        for (int i = 0; i < loopUntil; i++) {
            WebElement e = rows.get(i);
            List<WebElement> eRows = e.findElements(By.xpath("td"));//By.tagName(HTMLConstants.TD));
            for (int j = 0; j < eRows.size(); j++) {
                WebElement td = eRows.get(j);
                String tableText = td.getText();
                table.put(i + 1, j + 1, tableText == null ? "" : tableText);
            }
        }
        return table;
    }*/
}
