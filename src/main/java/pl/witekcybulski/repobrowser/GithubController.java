package pl.witekcybulski.repobrowser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repositories/")
class GithubController
{
	private final GithubService githubService;

	GithubController(GithubService githubService)
	{
		this.githubService = githubService;
	}

	@GetMapping(path = {"/{username}", "/{username}/"})
	List<RepositoryResponse> getUserRepositories(@PathVariable String username)
	{
		return githubService.getNonForkRepositories(username);
	}
}