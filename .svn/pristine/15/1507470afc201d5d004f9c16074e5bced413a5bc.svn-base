package nl.prowareness.automation.selenium.exceptions;

import nl.prowareness.automation.selenium.utilities.FindBy;

/**
 * Constructs and initializes the AutomationDriverException
 * @param message
 * @param cause 
 */
public class AutomationElementTimeOutException extends RuntimeException{
    private static final long serialVersionUID = 0x1L;
   
    public AutomationElementTimeOutException(final String message, final Throwable cause){
        super(message, cause);
    }
    
    public AutomationElementTimeOutException(final FindBy findBy, final String locator,  final Throwable cause){
        super((new StringBuilder().append("Timed out waiting for element with ").append(findBy).append(" = ").append(locator)).toString(), cause);
    }
}

