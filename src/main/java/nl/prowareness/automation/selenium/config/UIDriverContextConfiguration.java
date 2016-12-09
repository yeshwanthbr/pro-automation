package nl.prowareness.automation.selenium.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Loads all related objects into spring context
 *
 */

@Configuration
@ComponentScan(basePackages={"nl.prowareness.automation.selenium.webdriver","nl.prowareness.automation.selenium.webanalytics"})
public interface UIDriverContextConfiguration {

}
