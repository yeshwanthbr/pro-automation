package nl.prowareness.automation.selenium.exceptions;

import nl.prowareness.automation.selenium.utilities.FindBy;

/**
 * Constructs and initializes the AutomationDriverException
 * @param message
 * @param cause 
 */
public class AutomationElementNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 0x1L;
   
    public AutomationElementNotFoundException(final String message, final Throwable cause){
        super(message, cause);
    }
    
    public AutomationElementNotFoundException(final FindBy findBy, final String locator,  final Throwable cause){
        super((new StringBuilder().append("Could not find element with ").append(findBy).append(" = ").append(locator)).toString(), cause);
    }
}

