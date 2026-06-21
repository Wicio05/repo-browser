package pl.witekcybulski.repobrowser;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GithubService
{
	private final GithubClient githubClient;

	GithubService(GithubClient githubClient)
	{
		this.githubClient = githubClient;
	}

	List<RepositoryResponse> getNonForkRepositories(String username)
	{
		return githubClient.fetchRepositories(username)
			.stream()
			.filter(repo -> !repo.fork())
			.map(repo -> {
				List<BranchResponse> branches = githubClient.fetchBranches(username, repo.name())
					.stream()
					.map(branch -> new BranchResponse(branch.name(), branch.commit().sha()))
					.toList();

				return new RepositoryResponse(
					repo.name(),
					repo.owner().login(),
					branches
				);
			})
			.toList();
	}
}