package io.github.boomkalakasha.icarus.scaffold.server;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.boomkalakasha.icarus.scaffold.core.exception.InvalidScaffoldRequestException;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;

import java.util.LinkedHashSet;
import java.util.Set;

/** JSON-only transport type; unknown fields are rejected instead of ignored. */
public final class ScaffoldRequestPayload {

    private String artifact;
    private String group;
    private String packageName;
    private Integer port;
    private String description;
    private String templatePack;
    private String profile;
    private String license;
    private String copyrightHolder;
    private Integer copyrightYear;
    private final Set<String> unknownProperties = new LinkedHashSet<>();

    public ScaffoldRequestPayload() {
    }

    public ScaffoldRequestPayload(String artifact, String group, String packageName, Integer port, String description) {
        this.artifact = artifact;
        this.group = group;
        this.packageName = packageName;
        this.port = port;
        this.description = description;
    }

    @JsonProperty("artifact")
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    @JsonProperty("group")
    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty("package")
    @JsonAlias("packageName")
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("templatePack")
    public void setTemplatePack(String templatePack) {
        this.templatePack = templatePack;
    }

    @JsonProperty("profile")
    public void setProfile(String profile) {
        this.profile = profile;
    }

    @JsonProperty("license")
    public void setLicense(String license) {
        this.license = license;
    }

    @JsonProperty("copyrightHolder")
    public void setCopyrightHolder(String copyrightHolder) {
        this.copyrightHolder = copyrightHolder;
    }

    @JsonProperty("copyrightYear")
    public void setCopyrightYear(Integer copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    @JsonAnySetter
    public void captureUnknownProperty(String name, Object value) {
        unknownProperties.add(name);
    }

    ScaffoldRequest toCoreRequest() {
        if (!unknownProperties.isEmpty()) {
            throw new InvalidScaffoldRequestException("unsupported request field");
        }
        return new ScaffoldRequest(
                artifact, group, packageName, port == null ? 0 : port, description,
                license, copyrightHolder, copyrightYear, templatePack, profile);
    }
}
