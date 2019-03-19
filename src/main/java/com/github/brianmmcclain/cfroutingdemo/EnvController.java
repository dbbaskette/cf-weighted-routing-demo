package com.github.brianmmcclain.cfroutingdemo;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvController {

    @GetMapping("/env")
    String env() {
        String jvmVersion = System.getProperty("java.vm.version");
        String jvmName = System.getProperty("java.vm.name");

        String ret = "<h2>JVM: " + jvmName + " " + jvmVersion + "</h2><br /><br />";
        Map<String, String> env = System.getenv();
        for (String key : env.keySet()) {
            ret += "<b>" + key + "</b>: " + env.get(key) + "<br />";
        }
        return ret;
    }
}