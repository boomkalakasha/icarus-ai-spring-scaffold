package io.github.boomkalakasha.icarus.scaffold.server;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScaffoldController.class)
@Import({RequestSizeFilter.class, SecurityHeadersFilter.class, ScaffoldExceptionHandler.class})
class ScaffoldControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScaffoldGenerator generator;

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
    void rejectsJsonNullBeforeGeneration() throws Exception {
        mockMvc.perform(post("/api/scaffolds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(generator);
    }
}
