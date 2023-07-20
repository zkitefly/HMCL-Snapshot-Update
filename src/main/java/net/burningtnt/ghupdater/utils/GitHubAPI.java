package net.burningtnt.ghupdater.utils;

import com.google.gson.annotations.SerializedName;
import net.burningtnt.ghupdater.utils.io.HttpRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class GitHubAPI {
    private final String token;

    public GitHubAPI(String token) {
        this.token = "Bearer " + token;
    }

    public static class GitHubWorkflowRunLookup {
        public static class GitHubWorkflowRun {
            @SerializedName("id")
            private long id;

            public long getId() {
                return this.id;
            }
        }

        @SerializedName("workflow_runs")
        private GitHubWorkflowRun[] runs;

        public GitHubWorkflowRun[] getRuns() {
            return this.runs;
        }
    }

    public static class GitHubArtifactsLookup {
        public static class GitHubArtifact {
            @SerializedName("name")
            private String name;

            @SerializedName("archive_download_url")
            private String donloadURL;

            public String getName() {
                return this.name;
            }

            public String getDonloadURL() {
                return this.donloadURL;
            }
        }

        @SerializedName("artifacts")
        private GitHubArtifact[] artifacts;

        public GitHubArtifact[] getArtifacts() {
            return this.artifacts;
        }
    }

    public long getLatestWorkflowID(String owner, String repository, String workflowID, String branch) throws IOException {
        GitHubWorkflowRunLookup lookup = HttpRequest.GET(
                        String.format("https://api.github.com/repos/%s/%s/actions/workflows/%s/runs", owner, repository, workflowID),
                        Map.of("branch", branch, "event", "push", "page", "1", "per_page", "1")
                )
                .header("Accept", "application/vnd.github+json")
                .authorization(this.token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .getJson(GitHubWorkflowRunLookup.class);

        if (lookup.runs.length == 0) {
            throw new IOException("Invalid data.");
        }

        return lookup.runs[0].id;
    }

    public List<GitHubArtifactsLookup.GitHubArtifact> getArtifacts(String owner, String repository, long runID) throws IOException {
        GitHubArtifactsLookup lookup = HttpRequest.GET(
                        String.format("https://api.github.com/repos/%s/%s/actions/runs/%s/artifacts", owner, repository, runID)
                )
                .header("Accept", "application/vnd.github+json")
                .authorization(this.token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .getJson(GitHubArtifactsLookup.class);

        return List.of(lookup.artifacts);
    }

    public byte[] getArtifactData(GitHubArtifactsLookup.GitHubArtifact gitHubArtifact) throws IOException {
        return HttpRequest.GET(gitHubArtifact.donloadURL)
                .header("Accept", "application/vnd.github+json")
                .authorization(this.token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .getRawData();
    }
}
