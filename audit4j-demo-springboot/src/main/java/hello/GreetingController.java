package hello;

import org.audit4j.core.annotation.Audit;
import org.audit4j.core.annotation.AuditField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class GreetingController {
	
	static Logger LOGGER = LoggerFactory.getLogger(GreetingController.class);
	
	@Audit
    @RequestMapping("/hello")
    public String hello() {
    	LOGGER.info("hello called");
        return "hello";
    }

	@Audit
	@RequestMapping("/audit1")
	public String foo1(@AuditField(field="stringOne") String string1, @AuditField(field="stringTwo") String string2) {
	    LOGGER.info("audit2 called");
	    return "hello";
	}
	
	@Audit
	@RequestMapping("/audit2")
	public String foo2(String string1, String string2) {
	    LOGGER.info("audit2 called");
        return "hello";		
	}

}
