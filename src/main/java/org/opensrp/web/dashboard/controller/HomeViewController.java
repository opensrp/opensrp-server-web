package org.opensrp.web.dashboard.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("home.html")
public class HomeViewController {
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView home(HttpServletRequest request, HttpSession session, Model model, Locale locale) {
		//TODO
		//Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		//User user = (User) auth.getPrincipal();
		
		return new ModelAndView("vital_home");
		
	}
}
