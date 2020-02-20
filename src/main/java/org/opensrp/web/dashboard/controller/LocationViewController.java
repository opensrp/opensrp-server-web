package org.opensrp.web.dashboard.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.domain.Geometry;
import org.opensrp.domain.LocationProperty;
import org.opensrp.domain.LocationProperty.PropertyStatus;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.SettingRepository;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.web.utils.LocationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes(value = "command")
public class LocationViewController {
	
	@Autowired
	private PhysicalLocationService locationService;
	
	@Autowired
	private SettingRepository settingRepository;

	@RequestMapping(value = "location/list.html", method = RequestMethod.GET)
	public String locationList(HttpServletRequest request, ModelMap model, Locale locale) {
		String id = request.getParameter("id");
		
		Map<String, String> properties = new HashMap<>();
		
		String parentId = null;
		if(StringUtils.isNotBlank(id) && !id.equalsIgnoreCase("null")){
			PhysicalLocation givenLoc = locationService.getLocation(id, false);
			if (givenLoc != null) {
				parentId = givenLoc.getId();
				
				model.addAttribute("location", givenLoc);
			}
		}
		else {
			properties.put("geographicLevel", "1");//TODO
		}

		model.addAttribute("children", locationService.findLocationsByProperties(false, parentId, properties));

		model.addAttribute("id", id);
		
		return "location/index";
	}
	
	@RequestMapping(value = "location/add.html", method = RequestMethod.GET)
	public ModelAndView saveLocation(ModelMap model, HttpServletRequest request, Locale locale) {
		String parent = request.getParameter("parent");
		
		PhysicalLocation location = new PhysicalLocation();
		location.setProperties(new LocationProperty());
		location.getProperties().setParentId(parent);
		
		String parentLocationName = StringUtils.isEmpty(parent)?"Root":locationService.getLocation(parent, false).getProperties().getName();
		
		model.addAttribute("parentLocationName", parentLocationName);
		
		return new ModelAndView("location/add", "command", location);
		
	}
	
	@RequestMapping(value = "/location/add.html", method = RequestMethod.POST)
	public ModelAndView saveLocation(@RequestParam(value = "parent", required = false) String parent,
	                                 @RequestParam(value = "parentLocationName", required = false) String parentLocationName,
	                                 @ModelAttribute("command") PhysicalLocation location, BindingResult binding,
	                                 ModelMap model, HttpSession session, Locale locale,
	                                 SessionStatus sessionStatus) {
		// if root location then name must be unique in system
		// if non root location must be unique within parent
		Map<String, String> properties = new HashMap<>();
		properties.put("name", location.getProperties().getName());
		
		
		if((StringUtils.isNotBlank(parent) && !locationService.findLocationsByNames(location.getProperties().getName(), 0).isEmpty())
				|| (StringUtils.isBlank(parent) && !locationService.findLocationsByProperties(false, null, properties).isEmpty())){
			binding.rejectValue("properties.name", null, "Name must be unique in given hierarchy");
			model.addAttribute("parentLocationName", parentLocationName);
			
			return new ModelAndView("location/add", "command", location);
		}
		
		String id = UUID.randomUUID().toString();

		location.setId(id);
		location.setGeometry(new Geometry());
		location.setJurisdiction(true);
		location.setServerVersion(System.currentTimeMillis());
		location.getProperties().setEffectiveStartDate(new Date());
		location.getProperties().setStatus(PropertyStatus.ACTIVE);
		location.getProperties().setType(location.getType());
		location.getProperties().setUid(id);

		locationService.add(location);
		
		sessionStatus.setComplete();
		
		return new ModelAndView("redirect:/location/list.html?id="+parent);
		
	}
	
	@RequestMapping(value = "location/{id}/edit.html", method = RequestMethod.GET)
	public ModelAndView editLocation(ModelMap model, @PathVariable("id") String id) {
		PhysicalLocation location = locationService.getLocation(id, false);
	
		String parent = location.getProperties().getParentId();
		String parentLocationName = StringUtils.isEmpty(parent) || parent.equalsIgnoreCase("null")?"Root":locationService.getLocation(parent, false).getProperties().getName();
		
		model.addAttribute("parentLocationName", parentLocationName);
		
		return new ModelAndView("location/edit", "command", location);
	}
	
	@RequestMapping(value = "/location/{id}/edit.html", method = RequestMethod.POST)
	public ModelAndView editLocation(@RequestParam(value = "parentLocationName", required = false) String parentLocationName,
            @ModelAttribute("command") PhysicalLocation location, BindingResult binding,
            ModelMap model, HttpSession session, Locale locale, SessionStatus sessionStatus) {
		// if root location then name must be unique in system
		// if non root location must be unique within parent
		Map<String, String> properties = new HashMap<>();
		properties.put("name", location.getProperties().getName());
		
		location.getProperties().setType(location.getType());
		location.setServerVersion(System.currentTimeMillis());
		
		locationService.update(location);
		
		sessionStatus.setComplete();
		
		return new ModelAndView("redirect:/location/list.html?id="+location.getProperties().getParentId());

	}
	
	@RequestMapping(value = "locationtag/list.html", method = RequestMethod.GET)
	public String locationTagList(HttpServletRequest request, ModelMap model, Locale locale) {
		SettingConfiguration type = getSettingConfig(LocationUtils.LOCATION_TYPES_SETTING_KEY, true);

		type.getSettings();

		model.addAttribute("locationtags", type.getSettings());

		return "locationtag/index";
	}
	
	@RequestMapping(value = "locationtag/add.html", method = RequestMethod.GET)
	public ModelAndView saveLocationTag(ModelMap model, HttpServletRequest request, Locale locale) {
		return new ModelAndView("locationtag/add", "command", new Setting());
	}
	
	@RequestMapping(value = "/locationtag/add.html", method = RequestMethod.POST)
	public ModelAndView saveLocationTag(@ModelAttribute("command") Setting setting, BindingResult binding,
	                                 ModelMap model, HttpSession session, Locale locale,
	                                 SessionStatus sessionStatus) {
		
		SettingConfiguration settingConfig = getSettingConfig(LocationUtils.LOCATION_TYPES_SETTING_KEY, false);
		
		if(settingConfig == null){
			binding.reject(null, "Invalid/Missing Setting ID for location type");
			
			return new ModelAndView("locationtag/add", "command", setting);
		}
		
		if(StringUtils.isBlank(setting.getKey())){
			binding.reject(null, "Location Type Key/ID can not be blank");
			
			return new ModelAndView("locationtag/add", "command", setting);
		}
		
		if(getSetting(settingConfig, setting.getKey()) != null){
			binding.reject(null, "Location Type with given Key/ID already exists");
			
			return new ModelAndView("locationtag/add", "command", setting);
		}
		
		setting.setValue(setting.getKey());
		settingConfig.getSettings().add(setting);

		settingRepository.update(settingConfig);
		
		sessionStatus.setComplete();
		
		return new ModelAndView("redirect:/locationtag/list.html");
		
	}
	
	@RequestMapping(value = "locationtag/{id}/edit.html", method = RequestMethod.GET)
	public ModelAndView editLocationTag(ModelMap model, @PathVariable("id") String id) {
		SettingConfiguration settingConfig = getSettingConfig(LocationUtils.LOCATION_TYPES_SETTING_KEY, false);
		
		Setting setting = getSetting(settingConfig, id);
		
		model.addAttribute("id", id);

		return new ModelAndView("locationtag/edit", "command", setting);
	}
	
	@RequestMapping(value = "/locationtag/{id}/edit.html", method = RequestMethod.POST)
	public ModelAndView editLocationTag(@PathVariable("id") String id, @ModelAttribute("command") Setting setting, 
			BindingResult binding, ModelMap model, HttpSession session, Locale locale, SessionStatus sessionStatus) {
		
		model.addAttribute("id", id);

		SettingConfiguration settingConfig = getSettingConfig(LocationUtils.LOCATION_TYPES_SETTING_KEY, false);
		
		if(settingConfig == null){
			binding.reject(null, "Invalid/Missing Setting ID for location type");
			
			return new ModelAndView("locationtag/edit", "command", setting);
		}
		
		if(StringUtils.isBlank(setting.getKey())){
			binding.reject(null, "Location Type Key/ID can not be blank");
			
			return new ModelAndView("locationtag/add", "command", setting);
		}
		
		Setting oldSetting = getSetting(settingConfig, id);
		
		if(oldSetting == null){
			binding.reject(null, "Location Type with given Key/ID does not exist");
			
			return new ModelAndView("locationtag/add", "command", setting);
		}
		
		setting.setValue(setting.getKey());

		settingConfig.getSettings().remove(oldSetting);
		settingConfig.getSettings().add(setting);

		settingRepository.update(settingConfig);
		
		sessionStatus.setComplete();
		
		return new ModelAndView("redirect:/locationtag/list.html");
	}
	
	// Ugly way of finding a setting but core does not allow search by setting identifier
	private SettingConfiguration getSettingConfig(String identifier, boolean createIfNotExists){
		SettingSearchBean searchBean = new SettingSearchBean();
		searchBean.setServerVersion(0L);
		
		List<SettingConfiguration> settingList = settingRepository.findSettings(searchBean);
		for (SettingConfiguration settingConfiguration : settingList) {
			if(settingConfiguration.getIdentifier().equalsIgnoreCase(identifier)){
				return settingConfiguration;
			}
		}
		
		SettingConfiguration setting = null;

		// create new setting if not exists
		if(createIfNotExists){
			setting = new SettingConfiguration();
			
			setting.setDateCreated(DateTime.now()); 
			setting.setIdentifier(identifier);
			setting.setServerVersion(DateTime.now().getMillis());
			setting.setSettings(new ArrayList<Setting>());
			setting.setVoided(false);
			
			settingRepository.add(setting);
		}
		
		return setting;
	}
	
	private Setting getSetting(SettingConfiguration settingConfig, String identifier){
		for (Setting setting: settingConfig.getSettings()) {
			if(setting.getKey().equalsIgnoreCase(identifier)){
				return setting;
			}
		}
		
		return null;
	}
}
