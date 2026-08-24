package io.github.boomkalakasha.icarus.scaffold.server;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import io.github.boomkalakasha.icarus.scaffold.core.exception.InvalidScaffoldRequestException;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import io.github.boomkalakasha.icarus.scaffold.core.validation.ScaffoldRequestValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST surface intentionally has one output: an in-memory ZIP attachment. */
@RestController
@RequestMapping(path = {"/api/scaffolds", "/api/scaffold"})
public final class ScaffoldController {

    private final ScaffoldGenerator generator;
    private final ScaffoldRequestValidator validator = new ScaffoldRequestValidator();

    public ScaffoldController(ScaffoldGenerator generator) {
        this.generator = generator;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/zip")
    public ResponseEntity<byte[]> generate(@RequestBody ScaffoldRequestPayload payload) {
        if (payload == null) {
            throw new InvalidScaffoldRequestException("request must not be null");
        }
        ScaffoldRequest request = payload.toCoreRequest();
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
