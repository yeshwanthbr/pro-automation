package nl.prowareness.automation.selenium.tests;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Assert;
import org.junit.Test;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;
import nl.prowareness.automation.selenium.objectparser.ObjectRepository.Field;
import nl.prowareness.automation.selenium.objectparser.ObjectRepositoryManager;
import nl.prowareness.automation.selenium.utilities.FindBy;


public class ObjectRepoParserTest {
	@Test
	public void testParser() throws AutomationDriverException{
		ObjectRepositoryManager objRepoManager = new ObjectRepositoryManager();
		Field field=null;
		File folder = new File("src/main/resources/objectRepository");
		List<File> listOfFiles = (List<File>) FileUtils.listFiles(folder, new WildcardFileFilter("*.xml"), null);
		for(File file:listOfFiles){
			objRepoManager.parseFile(file);
		}
		field = objRepoManager.getField("File1Page1", "field1");
		Assert.assertEquals("field1Value", field.getFindByValue());
		Assert.assertEquals(FindBy.ID, field.getFindBy());
		
		field = objRepoManager.getField("File1Page2", "field2");
		Assert.assertEquals("field2Value", field.getFindByValue());
		Assert.assertEquals(FindBy.XPATH, field.getFindBy());		
	}			
}
