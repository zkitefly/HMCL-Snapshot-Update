package net.burningtnt.ghupdater;

import net.burningtnt.ghupdater.utils.GitHubAPI;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public final class GithubUpdater {
    private final Profile profile;

    private final Function<List<GitHubAPI.GitHubArtifactsLookup.GitHubArtifact>, GitHubAPI.GitHubArtifactsLookup.GitHubArtifact> selector;

    private final ArtifactDataConsumer artifactDataConsumer;

    @FunctionalInterface
    public interface ArtifactDataConsumer {
        void apply(byte[] data) throws IOException;
    }

    public GithubUpdater(Profile profile, Function<List<GitHubAPI.GitHubArtifactsLookup.GitHubArtifact>, GitHubAPI.GitHubArtifactsLookup.GitHubArtifact> selector, ArtifactDataConsumer artifactDataConsumer) {
        this.profile = profile;
        this.selector = selector;
        this.artifactDataConsumer = artifactDataConsumer;
    }

    public void run() throws IOException {
        GitHubAPI api = new GitHubAPI(profile.getToken());

        long runID = api.getLatestWorkflowID(profile.getOwner(), profile.getRepository(), profile.getWorkflowID(), profile.getBranch());

        List<GitHubAPI.GitHubArtifactsLookup.GitHubArtifact> gitHubArtifacts = api.getArtifacts(profile.getOwner(), profile.getRepository(), runID);

        GitHubAPI.GitHubArtifactsLookup.GitHubArtifact selected = selector.apply(gitHubArtifacts);

        byte[] data = api.getArtifactData(selected);

        artifactDataConsumer.apply(data);
    }
}
