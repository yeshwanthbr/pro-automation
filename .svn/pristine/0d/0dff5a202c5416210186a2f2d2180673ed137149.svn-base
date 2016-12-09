package nl.prowareness.automation.selenium.objectparser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import nl.prowareness.automation.selenium.exceptions.AutomationDriverException;
import nl.prowareness.automation.selenium.objectparser.ObjectRepository.Field;
import nl.prowareness.automation.selenium.objectparser.ObjectRepository.Page;

/**
 *
 * Handles Parsing the multiple object repository files under "resources " folder
 * Exposes getField() method to get the Object of an Element on the Application Page
 *
 */
public class ObjectRepositoryManager {

    private static ObjectRepository repository;
    private static Map<String, Map<String, Field>> pagesMap = new HashMap<String, Map<String, Field>>();

	private static final Logger LOGGER = Logger.getLogger(ObjectRepositoryManager.class);



    /**
     * Parses the *ObjectRepository.xml files and creates HashMap which holds all the pages in the ObjectRepository
     *
     * @param objRepoFile
     * @throws AutomationDriverException
     */
    public void parseFile(File objRepoFile) throws AutomationDriverException{
        JAXBContext context;
        try{
            context = JAXBContext.newInstance(ObjectRepository.class);
            Unmarshaller um = context.createUnmarshaller();
            repository= (ObjectRepository) um.unmarshal(objRepoFile);
            setRepositoryPagesToMap();
        }catch (JAXBException e){
            throw new AutomationDriverException("Unable to Load Object Repository", e);
        }
    }

    private void setRepositoryPagesToMap() throws AutomationDriverException{
        boolean invalidLocatorFlag = false;
        StringBuilder invalidLocatorList=new StringBuilder();

        for (Page page:repository.getPage()){
            Map<String, Field> fieldMap = new HashMap<String, Field>();
            for (Field field:page.getField()){

                try{
                    field.getFindBy();
                }catch(IllegalArgumentException e){
                     invalidLocatorFlag= true;
                     invalidLocatorList.append("Field: "+field.findBy+" on page: "+page.getName()+" ;");
                     LOGGER.error("Invalid locator types in ObjectRepository:",e);
                }

                    fieldMap.put(field.getName(), field);
                }
            pagesMap.put(page.getName(), fieldMap);
        }

        if(invalidLocatorFlag)
        throw new AutomationDriverException("Invalid locator types in ObjectRepository: "+invalidLocatorList);
    }


    public Field getField(String pageName, String fieldName) throws AutomationDriverException{
        if(pagesMap.get(pageName)==null || pagesMap.get(pageName).get(fieldName)==null){
            throw new AutomationDriverException("Could not find Webelement definition with PageName:'"+pageName+"' and FieldName:'"+fieldName+"' in any of the repositories");
        }
        return pagesMap.get(pageName).get(fieldName);
    }
}
