package nl.prowareness.automation.selenium.utilities;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindElement {

    String page() default "";
    String field() default "";
}
