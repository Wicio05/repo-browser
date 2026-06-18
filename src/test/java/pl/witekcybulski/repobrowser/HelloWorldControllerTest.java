//package pl.witekcybulski.repobrowser;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestClient;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class HelloWorldControllerTest {
//
//	@LocalServerPort
//	private int port;
//
//	@Autowired
//	private RestClient.Builder restClientBuilder;
//
//	@Test
//	void shouldReturnHelloWorld() {
//		// given
//		RestClient restClient = restClientBuilder.baseUrl("http://localhost:" + port).build();
//
//		// when
//		ResponseEntity<String> response = restClient.get()
//			.uri("/hello")
//			.retrieve()
//			.toEntity(String.class);
//
//		// then
//		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//		assertThat(response.getBody()).isEqualTo("Hello World!");
//	}
//}

package pl.witekcybulski.repobrowser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloWorldControllerTest
{

	@LocalServerPort
	private int port;

	private static final String BASE_URL = "http://localhost:";

	@Test
	void shouldReturnHelloWorld()
	{
		RestClient restClient = RestClient.create(BASE_URL + port);

		ResponseEntity<String> response = restClient.get()
			.uri("/hello")
			.retrieve()
			.toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo("Hello World!");
	}
}