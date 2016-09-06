/**
 * 
 */
package biz.neustar.sdk.csp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import net.respectnetwork.sdk.csp.notification.NotificationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

/**
 * @author Noopur Pandey
 *
 */
public class FreemarkerUtil {

	private static FreemarkerUtil freemarkerUtil= null;
	private static Configuration cfg = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(FreemarkerUtil.class);
	private static Properties props = null;
	
	private FreemarkerUtil(){}		
	
	/** returns singleton instance of FreemarkerUtil
	 * @return FreemarkerUtil
	 */
	public static FreemarkerUtil getInstance() throws NotificationException{
		LOGGER.info("getting instance of FreemarkerUtil");
		
		if(freemarkerUtil == null){
			config();
			freemarkerUtil = new FreemarkerUtil();
		}
		return freemarkerUtil;
	}
	
	private static void config() throws NotificationException{		
		try{
			
			props = new Properties();
			String propFileName = "templates.properties";			
			
			InputStream inputStream = FreemarkerUtil.class.getClassLoader().getResourceAsStream(propFileName);
			props.load(inputStream);						
			
			String directory = props.getProperty("sdk.csp.notification.template.directory");
			String imagePath = props.getProperty("sdk.csp.images.path");
		
			LOGGER.debug("Directory of Template Content: {} Image Path: {}",directory, imagePath);
			if(cfg == null){
				if(!Strings.isNullOrEmpty(directory) && !Strings.isNullOrEmpty(imagePath)){
					LOGGER.debug("Instantiating Freemarker Configuration Instance");
					cfg = new Configuration(new Version(2, 3, 20));				
					cfg.setDirectoryForTemplateLoading(new File(directory));
					cfg.setDefaultEncoding("UTF-8");
				}else{
					LOGGER.error("Error while initializing FreemarkerUtil: Template Directory or Image Path is not defined");
					throw new NotificationException("Error while initializing FreemarkerUtil: Template Directory or Image Path is not defined");
				}
			}
		}catch(IOException io){
			LOGGER.error("Error while initializing FreemarkerUtil: {}", io);
			throw new NotificationException("Error while initializing FreemarkerUtil: "+io.getMessage());
		}		
	}
	
	/**
	 * 
	 * Getting template and replacing placeholder with give values
	 * @param event
	 * @param cspCloudName
	 * @param input
	 * @return
	 */
	public String getTemplateContent(String event, String cspCloudName, 
			Map<String, Object> input) throws NotificationException{
		LOGGER.info("Getting Template Content for cspCloudName:{} on event:{}",cspCloudName, event);
	    String content = new String();
		StringWriter stringWriter = new StringWriter();
		try {			
			if(cfg != null){
				String fileName = cspCloudName+"_"+event+".ftl";
				LOGGER.debug("Template FileName {}",fileName);
				Template template = cfg.getTemplate(fileName);
		   
				template.process(input, stringWriter);
				content = stringWriter.toString();
				LOGGER.debug("Template Content {}",content);								
			}
		} catch(IOException io){
		    LOGGER.error("Error while fetching template for cspCloudName:{} on event:{} {}", cspCloudName, event, io);
		    throw new NotificationException(io.getMessage());
		}catch(TemplateException te){
			LOGGER.error("Error while fetching template for cspCloudName:{} on event:{} {}", cspCloudName, event, te);
			throw new NotificationException(te.getMessage());
		}finally {
			try {		    		
				stringWriter.close();
			} catch (IOException e) {					
				LOGGER.error("Error in closing StringWriter while getting template content"
						+ " for cspCloudName:{} on event:{} {}", cspCloudName, event, e);
				throw new NotificationException(e.getMessage());
			}
		}
		if(Strings.isNullOrEmpty(content)){
			LOGGER.error("template content is null or blank for cspCloudName:{} on event:{}", cspCloudName, event);
			throw new NotificationException("template content is null or blank for cspCloudName: "+cspCloudName+" on event: "+ event);
		}
		return content;
	}	

	public String getSubject(){

		if(props != null){
			return props.getProperty("verify.email.subject");
		}
		return null;
	 }
}