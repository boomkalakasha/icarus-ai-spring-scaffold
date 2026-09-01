package io.github.boomkalakasha.icarus.scaffold.server;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import io.github.boomkalakasha.icarus.scaffold.core.model.ScaffoldRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(ScaffoldController.class)
@Import({RequestSizeFilter.class, SecurityHeadersFilter.class, ScaffoldExceptionHandler.class})
class ScaffoldControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScaffoldGenerator generator;

    @Test
    void parsesTheDocumentedCommaSeparatedAllowList() {
        assertEquals(java.util.Set.of("default", "private"),
                ScaffoldController.parseAllowedTemplatePacks(" default, private "));
    }

    @Test
    void returnsZipBytesAndSecurityHeaders() throws Exception {
        when(generator.generate(any())).thenReturn(new byte[]{'P', 'K', 3, 4});

        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"A safe service"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/zip")))
                .andExpect(content().bytes(new byte[]{'P', 'K', 3, 4}))
                .andExpect(header().string("Content-Disposition", "attachment; filename=orders.zip"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "no-store"));

        verify(generator).generate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "A safe service",
                null, null, null, "default"));
    }

    @Test
    void mapsJsonTemplatePackToTheCoreRequest() throws Exception {
        when(generator.generate(any())).thenReturn(new byte[]{'P', 'K', 3, 4});

        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","templatePack":"default"}
                                """))
                .andExpect(status().isOk());

        verify(generator).generate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "safe",
                null, null, null, "default"));
    }

    @Test
    void mapsJsonProfileToTheCoreRequest() throws Exception {
        when(generator.generate(any())).thenReturn(new byte[]{'P', 'K', 3, 4});

        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","profile":"simple"}
                                """))
                .andExpect(status().isOk());

        verify(generator).generate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "safe",
                null, null, null, "default", "simple"));
    }

    @Test
    void rejectsATemplatePackOutsideTheConfiguredAllowListBeforeGeneration() throws Exception {
        ScaffoldController controller = new ScaffoldController(generator, java.util.Set.of("default"));
        org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ScaffoldExceptionHandler())
                .build()
                .perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","templatePack":"private"}
                                """))
                .andExpect(status().isBadRequest());

        verify(generator, never()).generate(any());
    }

    @Test
    void stockAllowListRejectsAValidButUnlistedPack() throws Exception {
        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","templatePack":"private"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(generator);
    }

    @Test
    void rejectsFilesystemAndOverwriteFields() throws Exception {
        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","outputPath":"out.zip","overwrite":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnsafeRequestBeforeGeneration() throws Exception {
        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"../escape","group":"com.example","package":"com.example.orders","port":8080,"description":"safe"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAPartialLicenseDeclarationBeforeGeneration() throws Exception {
        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","license":"MIT"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mapsACompleteLicenseDeclarationToTheCoreRequest() throws Exception {
        when(generator.generate(any())).thenReturn(new byte[]{'P', 'K', 3, 4});

        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"artifact":"orders","group":"com.example","package":"com.example.orders","port":8080,"description":"safe","license":"MIT","copyrightHolder":"Example Authors","copyrightYear":2026}
                                """))
                .andExpect(status().isOk());

        verify(generator).generate(new ScaffoldRequest(
                "orders", "com.example", "com.example.orders", 8080, "safe",
                "MIT", "Example Authors", 2026));
    }

    @Test
    void rejectsJsonNullBeforeGeneration() throws Exception {
        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(generator);
    }
}
