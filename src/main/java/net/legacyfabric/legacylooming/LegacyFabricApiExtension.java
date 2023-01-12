package net.legacyfabric.legacylooming;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.util.download.DownloadException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public class LegacyFabricApiExtension {
    private final Project project;

    public LegacyFabricApiExtension(Project project) {
        this.project = project;
    }

    private static final HashMap<String, Map<String, String>> moduleVersionCache = new HashMap<>();

    public Dependency module(String moduleName, String fabricApiVersion) {
        return project.getDependencies()
                .create(getDependencyNotation(moduleName, fabricApiVersion));
    }

    public String moduleVersion(String moduleName, String fabricApiVersion) {
        String moduleVersion = moduleVersionCache
                .computeIfAbsent(fabricApiVersion, this::populateModuleVersionMap)
                .get(moduleName);

        if (moduleVersion == null) {
            throw new RuntimeException("Failed to find module version for module: " + moduleName);
        }

        return moduleVersion;
    }

    private String getDependencyNotation(String moduleName, String fabricApiVersion) {
        return String.format("net.legacyfabric.legacy-fabric-api:%s:%s", moduleName, moduleVersion(moduleName, fabricApiVersion));
    }

    private Map<String, String> populateModuleVersionMap(String fabricApiVersion) {
        File pomFile = getApiMavenPom(fabricApiVersion);

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document pom = docBuilder.parse(pomFile);

            Map<String, String> versionMap = new HashMap<>();

            NodeList dependencies = ((Element) pom.getElementsByTagName("dependencies").item(0)).getElementsByTagName("dependency");

            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dep = (Element) dependencies.item(i);
                Element artifact = (Element) dep.getElementsByTagName("artifactId").item(0);
                Element version = (Element) dep.getElementsByTagName("version").item(0);

                if (artifact == null || version == null) {
                    throw new RuntimeException("Failed to find artifact or version");
                }

                versionMap.put(artifact.getTextContent(), version.getTextContent());
            }

            return versionMap;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + pomFile.getName(), e);
        }
    }

    private File getApiMavenPom(String fabricApiVersion) {
        LoomGradleExtension extension = LoomGradleExtension.get(project);

        File mavenPom = new File(extension.getFiles().getUserCache(), "legacy-fabric-api/" + fabricApiVersion + ".pom");

        if (project.getGradle().getStartParameter().isOffline()) {
            if (!mavenPom.exists()) {
                throw new RuntimeException("Cannot retrieve legacy-fabric-api pom due to being offline");
            }

            return mavenPom;
        }

        try {
            extension.download(String.format(Constants.MAVEN + "net/legacyfabric/legacy-fabric-api/legacy-fabric-api/%1$s/legacy-fabric-api-%1$s.pom", fabricApiVersion))
                    .defaultCache()
                    .downloadPath(mavenPom.toPath());
        } catch (DownloadException e) {
            throw new UncheckedIOException("Failed to download maven info for " + fabricApiVersion, e);
        }

        return mavenPom;
    }
}
