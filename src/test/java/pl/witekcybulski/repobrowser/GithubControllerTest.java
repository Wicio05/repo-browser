package pl.witekcybulski.repobrowser;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubControllerTest
{
	@LocalServerPort
	private int port;

	private RestClient testClient;

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
		.options(wireMockConfig().dynamicPort())
		.build();

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry)
	{
		registry.add("github.api.url", wireMock::baseUrl);
	}

	@BeforeEach
	void setUp()
	{
		this.testClient = RestClient.create("http://localhost:" + port);
	}

	@Test
	void shouldReturn200AndCorrectJsonStructureWithFilteredForks()
	{
		String reposBody = """
        [
          { "name": "valid-repo", "owner": { "login": "jankowalski" }, "fork": false },
          { "name": "forked-repo", "owner": { "login": "jankowalski" }, "fork": true }
        ]
        """;

		String branchesBody = """
        [
          { "name": "main", "commit": { "sha": "12345abcde" } }
        ]
        """;

		wireMock.stubFor(get(urlPathEqualTo("/users/jankowalski/repos"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(reposBody)));

		wireMock.stubFor(get(urlPathEqualTo("/repos/jankowalski/valid-repo/branches"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(branchesBody)));

		List<RepositoryResponse> repos = testClient.get()
			.uri("/api/repositories/jankowalski")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {});

		assertThat(repos).isNotNull().hasSize(1);

		RepositoryResponse repo = repos.getFirst();
		assertThat(repo.repositoryName()).isEqualTo("valid-repo");
		assertThat(repo.ownerLogin()).isEqualTo("jankowalski");
		assertThat(repo.branches()).hasSize(1);
		assertThat(repo.branches().getFirst().name()).isEqualTo("main");
		assertThat(repo.branches().getFirst().lastCommitSha()).isEqualTo("12345abcde");

		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/repos/jankowalski/forked-repo/branches")));
	}

	@Test
	void shouldReturn404AndCustomErrorFormatWhenUserDoesNotExist()
	{
		wireMock.stubFor(get(urlPathEqualTo("/users/ghostuser/repos"))
			.willReturn(aResponse()
				.withStatus(404)
				.withHeader("Content-Type", "application/json")
				.withBody("{ \"message\": \"Not Found\" }")));

		ErrorResponse error = testClient.get()
			.uri("/api/repositories/ghostuser")
			.exchange((request, response) -> {
				assertThat(response.getStatusCode().value()).isEqualTo(404);
				return response.bodyTo(ErrorResponse.class);
			});

		assertThat(error).isNotNull();
		assertThat(error.status()).isEqualTo(404);
		assertThat(error.message()).isEqualTo("Github user does not exist with given login");
	}

	@Test
	void shouldReturnEmptyListWhenUserHasOnlyForks()
	{
		String body = """
        [
          { "name": "fork1", "owner": { "login": "fork-only-user" }, "fork": true },
          { "name": "fork2", "owner": { "login": "fork-only-user" }, "fork": true }
        ]
        """;

		wireMock.stubFor(get(urlPathEqualTo("/users/fork-only-user/repos"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(body)));

		List<RepositoryResponse> repos = testClient.get()
			.uri("/api/repositories/fork-only-user")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {});

		assertThat(repos).isNotNull().isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenUserHasNoRepositories()
	{
		wireMock.stubFor(get(urlPathEqualTo("/users/new-user/repos"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("[]")));

		List<RepositoryResponse> repos = testClient.get()
			.uri("/api/repositories/new-user")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {});

		assertThat(repos).isNotNull().isEmpty();
	}

	@Test
	void shouldReturnErrorFormatForOtherGithubApiErrors()
	{
		wireMock.stubFor(get(urlPathEqualTo("/users/ratelimited/repos"))
			.willReturn(aResponse()
				.withStatus(403)
				.withHeader("Content-Type", "application/json")
				.withBody("{ \"message\": \"API rate limit exceeded\" }")));

		ErrorResponse error = testClient.get()
			.uri("/api/repositories/ratelimited")
			.exchange((request, response) -> {
				assertThat(response.getStatusCode().value()).isEqualTo(403);
				return response.bodyTo(ErrorResponse.class);
			});

		assertThat(error).isNotNull();
		assertThat(error.status()).isEqualTo(403);
		assertThat(error.message()).contains("Unexpected error while communicating with GitHuba API:");
	}
}