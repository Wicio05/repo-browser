package pl.witekcybulski.repobrowser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloWorldController
{
	@GetMapping("/hello")
	public String sayHello() {
		return "Hello World!";
	}
}