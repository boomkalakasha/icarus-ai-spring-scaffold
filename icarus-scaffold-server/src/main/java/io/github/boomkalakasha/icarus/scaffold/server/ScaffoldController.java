package io.github.boomkalakasha.icarus.scaffold.server;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import io.github.boomkalakasha.icarus.scaffold.core.exception.InvalidScaffoldRequestException;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import io.github.boomkalakasha.icarus.scaffold.core.validation.ScaffoldRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** REST surface intentionally has one output: an in-memory ZIP attachment. */
@RestController
@RequestMapping(path = {"/api/scaffolds", "/api/scaffold"})
public final class ScaffoldController {

    private final ScaffoldGenerator generator;
    private final ScaffoldRequestValidator validator = new ScaffoldRequestValidator();
    private final Set<String> allowedTemplatePacks;

    @Autowired
    public ScaffoldController(ScaffoldGenerator generator,
                              @Value("${icarus.scaffold.allowed-template-packs:default}") String allowedTemplatePacks) {
        this(generator, parseAllowedTemplatePacks(allowedTemplatePacks));
    }

    public ScaffoldController(ScaffoldGenerator generator) {
        this(generator, Set.of("default"));
    }

    public ScaffoldController(ScaffoldGenerator generator, Collection<String> allowedTemplatePacks) {
        this.generator = generator;
        if (allowedTemplatePacks == null
                || allowedTemplatePacks.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new InvalidScaffoldRequestException("allowed template packs must not be blank");
        }
        this.allowedTemplatePacks = Set.copyOf(new LinkedHashSet<>(allowedTemplatePacks));
    }

    static Set<String> parseAllowedTemplatePacks(String configured) {
        if (configured == null) {
            throw new InvalidScaffoldRequestException("allowed template packs must not be blank");
        }
        Set<String> parsed = new LinkedHashSet<>();
        for (String candidate : configured.split(",", -1)) {
            String id = candidate.trim();
            if (id.isBlank()) {
                throw new InvalidScaffoldRequestException("allowed template packs must not be blank");
            }
            parsed.add(id);
        }
        return Set.copyOf(parsed);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/zip")
    public ResponseEntity<byte[]> generate(@RequestBody ScaffoldRequestPayload payload) {
        if (payload == null) {
            throw new InvalidScaffoldRequestException("request must not be null");
        }
        ScaffoldRequest request = payload.toCoreRequest();
        if (!allowedTemplatePacks.contains(request.templatePack())) {
            throw new InvalidScaffoldRequestException("template pack is not allowed");
        }
        validator.validate(request);
        byte[] zip = generator.generate(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentLength(zip.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.artifact() + ".zip");
        headers.setCacheControl("no-store");
        return ResponseEntity.ok().headers(headers).body(zip);
    }
}
