package org.opensrp.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class BkashPaymentController {

    @RequestMapping(method = GET, value = "/bkash-payment")
    public ModelAndView bkashPayment()
    {
        Map<String, Object> model = new HashMap<String, Object>();
        return new ModelAndView("bkash-payment", model);
    }
}
