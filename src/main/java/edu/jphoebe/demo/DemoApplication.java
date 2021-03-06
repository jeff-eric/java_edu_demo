package edu.jphoebe.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Hashtable;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

//    @Autowired
//    private DemoService1 demoService1;
//    @Autowired
//    private DemoService2 demoService2;
//    @Autowired
//    private DemoService3 demoService3;

//    public DemoApplication(DemoService1 demoService1, DemoService2 demoService2, DemoService3 demoService3) {
//        this.demoService1 = demoService1;
//        this.demoService2 = demoService2;
//        this.demoService3 = demoService3;
//    }

    @PostMapping("/test")
    public String test(@RequestBody Hashtable map) {
        return "aaaaaaaaa";
    }

}
