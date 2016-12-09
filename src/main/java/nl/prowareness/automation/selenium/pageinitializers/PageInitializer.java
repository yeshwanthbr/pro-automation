package nl.prowareness.automation.selenium.pageinitializers;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;
import nl.prowareness.automation.selenium.fields.BaseElement;
import nl.prowareness.automation.selenium.utilities.FindBy;
import nl.prowareness.automation.selenium.utilities.FindElement;
import nl.prowareness.automation.selenium.utilities.FindElements;
import nl.prowareness.automation.selenium.webdriver.SeleniumWebDriver;

/**
 * 
 * initialize : This method will accept page as the argument and initializes all the member variables annotated as FindElement
 * 
 *
 */

public class PageInitializer {
		private PageInitializer(){
		}

	   public static <T extends BasePage> T intializePage(Class<T> pageClass, SeleniumWebDriver driver) throws AutomationDriverException{
	        T objectInst= null;
	        try {
	            objectInst = pageClass.getConstructor().newInstance();
	            objectInst.setDriver(driver);
	            initialize(objectInst, driver);
	        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
	                | NoSuchMethodException | SecurityException e) {
	            throw new AutomationDriverException("Could not initialize page : ", e);
	        }    
	        return objectInst;
	    }

	    @SuppressWarnings("rawtypes")
		static void initialize(BasePage p, SeleniumWebDriver driver) throws AutomationDriverException {
	        try{
	            Field[] fields = p.getClass().getDeclaredFields();
	            String className = p.getClass().getSimpleName();
	            for(Field f : fields){
	            	if(f.isAnnotationPresent(FindElement.class)){
	                	f.setAccessible(true);
	            		FindElement ann = f.getAnnotation(FindElement.class);
	                	nl.prowareness.automation.selenium.objectparser.ObjectRepository.Field field = getRepoField(driver, className, f, ann.page(), ann.field());
	                    f.set(p, f.getType().getConstructor(SeleniumWebDriver.class, FindBy.class, String.class).newInstance(driver, field.getFindBy(), field.getFindByValue()));
	                    f.setAccessible(false);
	                } else if(f.isAnnotationPresent(FindElements.class)){
	                	f.setAccessible(true);
	                	FindElements ann = f.getAnnotation(FindElements.class);
	                    nl.prowareness.automation.selenium.objectparser.ObjectRepository.Field field = getRepoField(driver, className, f, ann.page(), ann.field());
	                    verifyFindBy(field.getFindBy());
	                    Class klass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
	                    f.set(p, f.getType().getConstructor(SeleniumWebDriver.class, FindBy.class, String.class, Class.class ).newInstance(driver, field.getFindBy(), field.getFindByValue(), klass));
	                    f.setAccessible(false);
	                }
	            }
	        }catch(InvocationTargetException | IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException e){
	            throw new AutomationDriverException("Could not initialize page : ", e);
	        }
	    }

		private static nl.prowareness.automation.selenium.objectparser.ObjectRepository.Field getRepoField(SeleniumWebDriver driver, String className, Field f, String pageName, String fieldName)throws AutomationDriverException {
			String field = "".equals(fieldName)?  f.getName():fieldName;
			String page = "".equals(pageName)?  className :pageName;
			return driver.getObjRepoManager().getField(page, field);
		}

	    private static void verifyFindBy(FindBy findBy) throws AutomationDriverException{
	        if(findBy!=FindBy.XPATH){
	            throw new AutomationDriverException("Fields annotated with @FindElements can only have xpath as FindBy");
	        }
	    }

	    public static void reInitialize(BasePage p, SeleniumWebDriver driver) throws AutomationDriverException {
	        Field[] fields = p.getClass().getDeclaredFields();
	        p.setDriver(driver);
	        for(Field f : fields){
	        	f.setAccessible(true);
	        	if(f.isAnnotationPresent(FindElement.class)){
	        		 try {
		                    ((BaseElement) f.get(p)).setWebDriver(driver);
		                } catch (IllegalArgumentException | IllegalAccessException e) {
		                    throw new AutomationDriverException("Could not reInitialize page : ", e);
		                }
	        	}
	        	f.setAccessible(false);
	               
	        }
	    }
}
