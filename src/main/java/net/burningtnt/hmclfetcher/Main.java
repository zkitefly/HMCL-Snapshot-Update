package net.burningtnt.hmclfetcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.hmclfetcher.utils.GitHubAPI;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Main {
    private Main() {
    }

    private record SourceBranch(String owner, String repository, String branch, String workflow) {
    }

    private static final SourceBranch[] GITHUB_BRANCHES = {
            new SourceBranch("huanghongxun", "HMCL", "javafx", "gradle.yml"),
            new SourceBranch("burningtnt", "HMCL", "prs", "gradle.yml")
    };

    private static final String OFFICIAL_DOWNLOAD_LINK = "https://github.com/burningtnt/HMCL-SNAPSHOT-UPDATE/raw/v2/generated/";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static void removeFile(Path root) throws IOException {
        if (Files.isDirectory(root)) {
            try (Stream<Path> files = Files.list(root)) {
                for (Path path : (Iterable<Path>) files::iterator) {
                    removeFile(path);
                }
            }
        }
        Files.delete(root);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        String GITHUB_TOKEN = System.getenv("HMCL_GITHUB_TOKEN");
        GitHubAPI GITHUB_API = new GitHubAPI(GITHUB_TOKEN);
        Path ARTIFACT_ROOT = Path.of("generated").toAbsolutePath();
        List<URI> DOWNLOAD_LINKS = new ArrayList<>();
        DOWNLOAD_LINKS.add(new URI(OFFICIAL_DOWNLOAD_LINK));
        for (String proxy : System.getenv("HMCL_GITHUB_PROXYS").split(";")) {
            DOWNLOAD_LINKS.add(new URI(proxy + OFFICIAL_DOWNLOAD_LINK));
        }

        if (Files.exists(ARTIFACT_ROOT)) {
            removeFile(ARTIFACT_ROOT);
        }
        Files.createDirectory(ARTIFACT_ROOT);

        for (SourceBranch source : GITHUB_BRANCHES) {
            String branch = String.format("%08x", source.hashCode());
            Path root = ARTIFACT_ROOT.resolve(branch);
            Files.createDirectory(root);

            long runID = GITHUB_API.getLatestWorkflowID(source.owner, source.repository, source.workflow, source.branch);
            GitHubAPI.GitHubArtifact artifact = GITHUB_API.getArtifacts(source.owner, source.repository, runID)[0];

            String exeName = null, exeHash = null;
            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(GITHUB_API.getArtifactData(artifact)))) {
                ZipArchiveEntry entry;
                while ((entry = zis.getNextZipEntry()) != null) {
                    String entryPath = entry.getName();
                    if (entryPath.endsWith(".jar")) {
                        exeName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                        try (OutputStream os = Files.newOutputStream(root.resolve(exeName))) {
                            zis.transferTo(os);
                        }
                    } else if (entryPath.endsWith(".jar.sha1")) {
                        exeHash = new String(zis.readNBytes(40));
                    } else if (entryPath.endsWith(".exe")) {
                        try (OutputStream os = Files.newOutputStream(root.resolve(entry.getName().substring(entry.getName().lastIndexOf('/') + 1)))) {
                            zis.transferTo(os);
                        }
                    }
                }
            }

            if (exeName == null || exeHash == null) {
                throw new IllegalStateException("Broken Artifact!");
            }

            JsonObject update = new JsonObject();
            update.add("jarsha1", new JsonPrimitive(exeHash));
            update.add("version", new JsonPrimitive(exeName.substring(5, exeName.length() - 4))); // Remove "HMCL-" prefix and ".exe" suffix.
            update.add("universal", new JsonPrimitive("https://www.mcbbs.net/forum.php?mod=viewthread&tid=142335"));

            for (URI downloadLink : DOWNLOAD_LINKS) {
                String fileLink = downloadLink.resolve(branch + "/" + exeName).toString();
                update.add("jar", new JsonPrimitive(fileLink));

                try (BufferedWriter writer = Files.newBufferedWriter(root.resolve(String.format("%08x.json", downloadLink.hashCode())))) {
                    GSON.toJson(update, writer);
                }
            }
        }
    }
}
