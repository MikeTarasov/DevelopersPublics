package main.com.skillbox.ru.developerspublics.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class DefaultController
{
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
