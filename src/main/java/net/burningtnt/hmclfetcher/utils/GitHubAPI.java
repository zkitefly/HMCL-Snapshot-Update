package net.burningtnt.hmclfetcher.utils;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class GitHubAPI {
    private final String token;

    public GitHubAPI(String token) {
        this.token = "Bearer " + token;
    }

    private static class GitHubWorkflowRunLookup {
        public static class GitHubWorkflowRun {
            @SerializedName("id")
            private long id;

        }

        @SerializedName("workflow_runs")
        private GitHubWorkflowRun[] runs;
    }

    private static class GitHubArtifactsLookup {
        @SerializedName("artifacts")
        private GitHubAPI.GitHubArtifact[] artifacts;
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

    public GitHubArtifact[] getArtifacts(String owner, String repository, long runID) throws IOException {
        GitHubArtifactsLookup lookup = HttpRequest.GET(
                        String.format("https://api.github.com/repos/%s/%s/actions/runs/%s/artifacts", owner, repository, runID)
                )
                .header("Accept", "application/vnd.github+json")
                .authorization(this.token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .getJson(GitHubArtifactsLookup.class);

        return lookup.artifacts;
    }

    public InputStream getArtifactData(GitHubArtifact gitHubArtifact) throws IOException {
        return HttpRequest.GET(gitHubArtifact.donloadURL)
                .header("Accept", "application/vnd.github+json")
                .authorization(this.token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .getInputStream();
    }

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
}
