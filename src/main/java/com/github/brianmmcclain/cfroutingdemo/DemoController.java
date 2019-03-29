package com.github.brianmmcclain.cfroutingdemo;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {

    private int hits = 0;

    @GetMapping("/")
    public String index(Model model) {
        // Use the DEMO_LOGO environment variable to determine which logo to hit
        String logoImg = "emacs";
        if(System.getenv().containsKey("DEMO_LOGO") && System.getenv("DEMO_LOGO").equals("vim")) {
            logoImg = "vim";
        }

        model.addAttribute("logo", logoImg);
        model.addAttribute("hits", ++this.hits);
        return "index";
    }

    @GetMapping("/env")
    @ResponseBody
    public String env() {
        String jvmVersion = System.getProperty("java.vm.version");
        String jvmName = System.getProperty("java.vm.name");

        String ret = "<h2>JVM: " + jvmName + " " + jvmVersion + "</h2><br /><br />";
        Map<String, String> env = System.getenv();
        for (String key : env.keySet()) {
            ret += "<b>" + key + "</b>: " + env.get(key) + "<br />";
        }
        return ret;
    }

    @GetMapping("/reset")
    @ResponseBody
    public String reset() {
        this.hits = 0;
        return "Counter Reset";
    }
}