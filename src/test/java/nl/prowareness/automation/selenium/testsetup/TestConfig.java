package nl.prowareness.automation.selenium.testsetup;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource({"test_environment.properties"})
@ComponentScan(basePackages={"nl.prowareness.automation.selenium.testsetup","nl.prowareness.automation.selenium.webdriver", "nl.prowareness.automation.selenium.pageinitializers"})

public class TestConfig {


}

