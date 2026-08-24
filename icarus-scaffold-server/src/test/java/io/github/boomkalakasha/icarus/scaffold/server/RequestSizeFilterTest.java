package io.github.boomkalakasha.icarus.scaffold.server;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RequestSizeFilterTest {

    @Test
    void exposesASmallFixedRequestLimit() {
        assertEquals(16_384, RequestSizeFilter.MAX_REQUEST_BYTES);
    }

    @Test
    void rejectsOversizedBodyWhenContentLengthIsUnknown() throws Exception {
        RequestSizeFilter filter = new RequestSizeFilter();
        MockHttpServletRequest request = requestWithUnknownLength(new byte[RequestSizeFilter.MAX_REQUEST_BYTES + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> fail("filter chain must not run"));

        assertEquals(413, response.getStatus());
    }

    @Test
    void replaysAllowedBodyToTheControllerChain() throws Exception {
        RequestSizeFilter filter = new RequestSizeFilter();
        byte[] body = "{\"artifact\":\"demo\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = requestWithUnknownLength(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> observed = new AtomicReference<>();

        filter.doFilter(request, response, (wrapped, ignoredResponse) ->
                observed.set(new String(wrapped.getInputStream().readAllBytes(), StandardCharsets.UTF_8)));

        assertEquals(new String(body, StandardCharsets.UTF_8), observed.get());
    }

    private static MockHttpServletRequest requestWithUnknownLength(byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        request.setRequestURI("/api/scaffolds");
        request.setContentType("application/json");
        request.setContent(body);
        return request;
    }
}
