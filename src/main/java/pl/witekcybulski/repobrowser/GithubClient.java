package pl.witekcybulski.repobrowser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
class GithubClient
{
	private final RestClient restClient;

	GithubClient(RestClient.Builder restClientBuilder,
	             @Value("${github.api.url:https://api.github.com}") String githubApiUrl)
	{
		this.restClient = restClientBuilder
			.baseUrl(githubApiUrl)
			.defaultHeader("Accept", "application/vnd.github.v3+json")
			.build();
	}

	List<GithubRepositoryDto> fetchRepositories(String username)
	{
		return restClient
			.get()
			.uri("/users/{username}/repos", username)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {});
	}
}