package net.burningtnt.ghupdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Main {
    private Main() {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private record ArtifactFile(String name, byte[] data) {
    }

    public static void main(String[] args) throws IOException {
        new GithubUpdater(
                Profile.newBuilder()
                        .setOwner("huanghongxun")
                        .setRepository("HMCL")
                        .setBranch("javafx")
                        .setWorkflowID("gradle.yml")
                        .setToken(System.getenv("HMCL_GITHUB_TOKEN"))
                        .build(),
                gitHubArtifacts -> gitHubArtifacts.get(0),
                data -> {
                    ArtifactFile jarRaw = null;
                    ArtifactFile jarSha1 = null;

                    try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(data))) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        while (zipEntry != null) {
                            if (zipEntry.getName().endsWith(".jar")) {
                                jarRaw = new ArtifactFile(zipEntry.getName().substring(zipEntry.getName().lastIndexOf('/') + 1), zipInputStream.readAllBytes());
                            }

                            if (zipEntry.getName().endsWith(".jar.sha1")) {
                                jarSha1 = new ArtifactFile(zipEntry.getName().substring(zipEntry.getName().lastIndexOf('/') + 1), zipInputStream.readAllBytes());
                            }

                            zipEntry = zipInputStream.getNextEntry();
                        }
                    }
                    data = null;

                    if (jarRaw == null || jarSha1 == null) {
                        throw new IOException("Invalid artifact.");
                    }

                    Path outputRoot = Path.of("datas").toAbsolutePath();

                    if (Files.exists(outputRoot)) {
                        try (Stream<Path> stream = Files.list(outputRoot)) {
                            for (Path subPath : stream.toList()) {
                                Files.delete(subPath);
                            }
                        }
                    } else {
                        Files.createDirectory(outputRoot);
                    }

                    Path outputJar = outputRoot.resolve(jarRaw.name);
                    Path outputJarDirect = outputRoot.resolve("HMCL-dev.jar");
                    Path outputJson = outputRoot.resolve("snapshot.json");

                    if (Files.exists(outputJson)) {
                        JsonObject inputJsonObject = GSON.fromJson(Files.readString(outputJson), JsonObject.class);
                        if (inputJsonObject.get("jarsha1").getAsJsonPrimitive().getAsString().equals(new String(jarSha1.data, 0, 40))) {
                            return;
                        }
                    }

                    JsonObject outputJsonObject = new JsonObject();
                    outputJsonObject.add("jar", new JsonPrimitive(String.format("https://github.com/burningtnt/HMCL-Snapshot-Update/raw/master/datas/%s", jarRaw.name)));
                    outputJsonObject.add("jarsha1", new JsonPrimitive(new String(jarSha1.data, 0, 40)));
                    outputJsonObject.add("version", new JsonPrimitive(jarRaw.name.substring("HMCL-".length(), jarRaw.name.length() - ".jar".length())));
                    outputJsonObject.add("universal", new JsonPrimitive("https://www.mcbbs.net/forum.php?mod=viewthread&tid=142335"));
                    Files.writeString(outputJson, GSON.toJson(outputJsonObject));

                    Files.write(outputJar, jarRaw.data);
                    Files.write(outputJarDirect, jarRaw.data);
                }
        ).run();
    }
}
