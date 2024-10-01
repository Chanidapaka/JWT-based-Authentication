package sit.int202.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppController {
    @RequestMapping("/home")
    public String homePage() {
        return "home"; //คือชื่อ view --ต้องมีhome.html ด้วยนะ

    }


}
