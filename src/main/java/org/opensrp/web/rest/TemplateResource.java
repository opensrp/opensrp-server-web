package org.opensrp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opensrp.domain.Template;
import org.opensrp.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/templates")
public class TemplateResource {

	private static Logger logger = LogManager.getLogger(PlanResource.class.toString());

	protected ObjectMapper objectMapper;

	private TemplateService templateService;

	private static final String TEMPLATE_ID = "templateId";

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Autowired
	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAll() throws JsonProcessingException {
		List<Template> templates = templateService.getAll();
		String templatesJson = objectMapper.writeValueAsString(templates);
		return new ResponseEntity<>(templatesJson, RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@RequestMapping(value = "/{templateId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> get(@PathVariable(TEMPLATE_ID) Integer templateId) throws JsonProcessingException {
		Template template = templateService.getTemplateByTemplateId(templateId);
		String templateJson = this.objectMapper.writeValueAsString(template);
		return new ResponseEntity<>(templateJson, RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
	public ResponseEntity<String> post(@RequestBody Template template){
		try {
			Template templateToAdd = getTemplateWithUpdatedVersion(template);
			templateService.addOrUpdateTemplate(templateToAdd);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}catch (Exception exception){
			logger.error(exception.getMessage());
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
	public ResponseEntity<String> put(@RequestBody Template template){
		try {
			Template templateToUpdate = getTemplateWithUpdatedVersion(template);
			templateService.addOrUpdateTemplate(templateToUpdate);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (Exception exception){
			logger.error(exception.getMessage());
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	private Template getTemplateWithUpdatedVersion(Template template) {
		Template existingTemplate = templateService.getTemplateByTemplateId(template.getTemplateId());
		int templateVersion = existingTemplate != null  ? existingTemplate.getVersion() + 1 : 0;
		template.setVersion(templateVersion);
		return template;
	}

}
