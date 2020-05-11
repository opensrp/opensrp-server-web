package org.opensrp.web.controller.fake;

import java.io.IOException;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FakeTimeController {
    @RequestMapping("/time/set")
    public String setTime(@RequestParam("offset") Integer offsetInSeconds) throws IOException {
        System.setProperty("faketime.offset.seconds", offsetInSeconds.toString());
        return String.valueOf(DateTime.now().getMillis());
    }
}
