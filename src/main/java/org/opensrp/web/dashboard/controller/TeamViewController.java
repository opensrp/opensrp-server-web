package org.opensrp.web.dashboard.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.Practitioner;
import org.opensrp.domain.PractitionerRole;
import org.opensrp.domain.PractitionerRoleCode;
import org.opensrp.domain.custom.User;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerRoleService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.custom.service.CustomUserRepository;
import org.opensrp.web.custom.service.CustomUserService;
import org.opensrp.web.custom.service.RoleRepository;
import org.opensrp.web.dashboard.service.DatabaseRepository;
import org.opensrp.web.dashboard.util.AssignedLocationsWrapper;
import org.opensrp.web.dashboard.util.TeamMemberWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

@RequestMapping(value = "team")
@Controller
@SessionAttributes("command")
public class TeamViewController {
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private CustomUserService userService;
	
	private CustomUserRepository userRepository;
	
	@Autowired
	private DatabaseRepository databaseRepository;
	
	@Autowired
	private PhysicalLocationService locationServiceImpl;
	
	@Autowired
	private PractitionerService practitionerService;
	
	@Autowired
	private PractitionerRoleService practitionerRoleService;
	
	@RequestMapping(value = "/list.html", method = RequestMethod.GET)
	public String listTeam(HttpServletRequest request, HttpSession session, Model model, Locale locale) {
		List<Organization> list = organizationService.getAllOrganizations();
		for (Organization organization : list) {

			// TODO Ugly way of doing this; check method getTeamId below
			int members = Integer.parseInt(databaseRepository.executeSelectQuery(
					"   SELECT COUNT(1) FROM team.practitioner_role"
							+ " WHERE organization_id='"+organization.getId()+"' ").get(0).toString());
			
			organization.setPartOf((long) members);

			List<AssignedLocations> llist = organizationService.findAssignedLocationsAndPlans(organization.getIdentifier());
			Set<AssignedLocations> locationSet = new HashSet<>();
			
			for (AssignedLocations aloc : llist) {
				PhysicalLocation location = locationServiceImpl.getLocation(aloc.getJurisdictionId(), false);
				locationSet.add(new AssignedLocationsWrapper(aloc, location));
			}
			organization.setAssignedLocations(locationSet);
		}
		
		model.addAttribute("organizations", list);
		return "team/index";
	}
	
	@RequestMapping(value = "/add.html", method = RequestMethod.GET)
	public ModelAndView saveTeam(ModelMap model, HttpSession session, Locale locale) throws JSONException {
		Organization org = new Organization();
		
		return new ModelAndView("team/add", "command", org);
	}
	
	@RequestMapping(value = "/add.html", method = RequestMethod.POST)
	public ModelAndView saveTeam(@RequestParam(value = "selectedLocations") String selectedLocations,
	                             @ModelAttribute("command") Organization team, BindingResult binding, ModelMap model,
	                             SessionStatus sessionStatus) throws Exception {		
		model.addAttribute("selectedLocations", selectedLocations);
		
		if(organizationService.getOrganization(team.getIdentifier()) != null){
			binding.rejectValue("identifier", null, "Identifier already exists");
		}

		String[] locations = selectedLocations.split(",");
		for (String loc : locations) {
			if(locationServiceImpl.getLocation(loc, false) == null){
				binding.reject(null, loc + " identifier does not map to any location");
			}
		}
		
		if(binding.hasErrors()){
			return new ModelAndView("team/add", "command", team);
		}
		
		team.setActive(true);
		
		organizationService.addOrganization(team);
		
		for (String loc : locations) {
			organizationService.assignLocationAndPlan(team.getIdentifier(), loc, null, DateTime.now().toDate(), null);
		}
		
		sessionStatus.setComplete();
		
		return new ModelAndView("redirect:/team/list.html");
		
	}
	
	@RequestMapping(value = "/{id}/edit.html", method = RequestMethod.GET)
	public ModelAndView editTeam(ModelMap model,@PathVariable("id") String id) {
		Organization organization = getTeam(id);
		
		return new ModelAndView("team/edit", "command", organization);
	}
	
	@RequestMapping(value = "/{id}/edit.html", method = RequestMethod.POST)
	public ModelAndView editTeam(@ModelAttribute("command") Organization team, BindingResult binding, ModelMap model,
	                             SessionStatus session) throws Exception {
		organizationService.updateOrganization(team);
		
		session.setComplete();
		
		return new ModelAndView("redirect:/team/list.html");
		
	}
	
	@RequestMapping(value = "/{teamId}/member/list.html", method = RequestMethod.GET)
	public String locationList(@PathVariable("teamId") String teamId, HttpSession session, Model model) {
		List<Map<String, Object>> members = new ArrayList<>();
		
		List<Practitioner> list = practitionerService.getPractitionersByOrgIdentifier(teamId);
		for (Practitioner practitioner : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("member", practitioner);
			// ugly way opensrp core is implementing this
			map.put("role", practitionerRoleService.getPgRolesForPractitioner(practitioner.getIdentifier()));
			
			members.add(map);
		}
		model.addAttribute("members", members);
		
		model.addAttribute("team", getTeam(teamId));
		
		return "teammember/index";
	}
	
	private Organization getTeam(String teamId) {
		//TODO use better approach
		Organization team = organizationService.getOrganization(teamId);
		List<AssignedLocations> llist = organizationService.findAssignedLocationsAndPlans(team.getIdentifier());
		Set<AssignedLocations> locationSet = new HashSet<>();
		
		for (AssignedLocations aloc : llist) {
			PhysicalLocation location = locationServiceImpl.getLocation(aloc.getJurisdictionId(), false);
			locationSet.add(new AssignedLocationsWrapper(aloc, location));
		}
		team.setAssignedLocations(locationSet);
		
		return team;
	}
	
	@RequestMapping(value = "{teamId}/member/add.html", method = RequestMethod.GET)
	public ModelAndView saveTeamMember(@PathVariable("teamId") String teamId, ModelMap model, HttpSession session, Locale locale) throws JSONException {
		model.addAttribute("team", getTeam(teamId));
		model.addAttribute("roles", roleRepository.findAll());
		
		return new ModelAndView("teammember/add", "command", new TeamMemberWrapper());
	}
	
	@RequestMapping(value = "{teamId}/member/add.html", method = RequestMethod.POST)
	public ModelAndView saveTeamMember(@PathVariable("teamId") String teamId, 
			@ModelAttribute("command") TeamMemberWrapper teamMember, BindingResult binding,
	                                   ModelMap model, SessionStatus session) throws Exception {
		if(userService.findByUserName(teamMember.getUsername()) != null){
			binding.rejectValue("username", null, "Username already occupied");
		}
		
		if(teamMember.getPassword().isEmpty() || teamMember.getPassword().length() < 5){//TODO
			binding.rejectValue("password", null, "Password must be atleast 5 characters");
		}
		else if(!teamMember.getPassword().contentEquals(teamMember.getConfirmedPassword())){//TODO
			binding.rejectValue("password", null, "Confirmed password must match");
		}
		
		if(binding.hasErrors()){
			return new ModelAndView("teammember/add", "command", teamMember);
		}
		
		User user = new User();
		user.setActive(true);
		user.setEmail(teamMember.getEmail());
		user.setName(teamMember.getPractitioner().getName());
		user.setOpenTextPassword(teamMember.getPassword());
		user.setUserName(teamMember.getUsername());
		user.setCreatedBy("AUTO");
		
		User savedUser = userService.registerUser(user, Arrays.asList(teamMember.getRole()));
		
		teamMember.getPractitioner().setActive(true);
		teamMember.getPractitioner().setIdentifier(UUID.randomUUID().toString());
		teamMember.getPractitioner().setUserId(savedUser.getId().toString());
		teamMember.getPractitioner().setUsername(user.getUserName());
		
		practitionerService.addOrUpdatePractitioner(teamMember.getPractitioner());
		
		PractitionerRole prole = new PractitionerRole();
		prole.setActive(true);
		prole.setIdentifier(teamMember.getRole());
		prole.setOrganizationIdentifier(teamId);
		prole.setPractitionerIdentifier(teamMember.getPractitioner().getIdentifier());
		
		PractitionerRoleCode rolecode = new PractitionerRoleCode();
		rolecode.setText(teamMember.getRole());
		
		prole.setCode(rolecode);
		
		practitionerRoleService.addOrUpdatePractitionerRole(prole);
		
		session.setComplete();
		
		return new ModelAndView("redirect:/team/"+teamId+"/member/list.html");
		
	}
	
	@RequestMapping(value = "{teamId}/member/{id}/edit.html", method = RequestMethod.GET)
	public ModelAndView editTeamMember(@PathVariable("id") String id, @PathVariable(value = "teamId") String teamId, 
			ModelMap model, Locale locale) throws JSONException {
		model.addAttribute("team", getTeam(teamId));

		Practitioner practitionar = practitionerService.getPractitioner(id);
		User user = userService.findByUserName(practitionar.getUsername());
		
		TeamMemberWrapper teamMember = new TeamMemberWrapper(practitionar, user.getUserName(), 
				user.getPassHash(), user.getRoles().iterator().next().getName());
		teamMember.setConfirmedPassword(user.getPassHash());
		teamMember.setActive(practitionar.getActive());
		
		return new ModelAndView("teammember/edit", "command", teamMember);
	}
	
	@RequestMapping(value = "{teamId}/member/{id}/edit.html", method = RequestMethod.POST)
	public ModelAndView editTeamMember(@PathVariable("id") String id, 
			@PathVariable("teamId") String teamId,
	        @ModelAttribute("command") TeamMemberWrapper teamMember, BindingResult binding,
	        ModelMap model, SessionStatus session, Locale locale) throws Exception {
		
		User user = userService.findByUserName(teamMember.getUsername());
		PractitionerRole prole = practitionerRoleService.getRolesForPractitioner(id).get(0);

		if(teamMember.getPassword().isEmpty() || teamMember.getPassword().length() < 5){//TODO
			binding.rejectValue("password", null, "Password must be atleast 5 characters");
		}
		else if(!teamMember.getPassword().contentEquals(teamMember.getConfirmedPassword())){//TODO
			binding.rejectValue("password", null, "Confirmed password must match");
		}
		
		if(binding.hasErrors()){
			return new ModelAndView("teammember/edit", "command", teamMember);
		}
		
		// if active status is changed
		if(teamMember.getActive() == null){
			teamMember.setActive(true);
		}
		
		if(teamMember.getActive()){
			teamMember.getPractitioner().setActive(true);
			user.setActive(true);
			prole.setActive(true);
		}
		else {
			teamMember.getPractitioner().setActive(false);
			user.setActive(false);
			prole.setActive(false);
		}
		
		// if password is changed
		if(!teamMember.getPassword().equalsIgnoreCase(user.getPassHash())){
			String salt = BCrypt.gensalt(12);//TODO change it
			String generatedSecuredPasswordHash = BCrypt.hashpw(teamMember.getPassword(), salt);

			user.setSalt(salt);
			user.setPassHash(generatedSecuredPasswordHash);		
		}
		
		practitionerService.addOrUpdatePractitioner(teamMember.getPractitioner());
		practitionerRoleService.addOrUpdatePractitionerRole(prole);
		userRepository.save(user);
		
		session.setComplete();
		
		return new ModelAndView("redirect:/team/"+teamId+"/member/list.html");
		
	}
}
