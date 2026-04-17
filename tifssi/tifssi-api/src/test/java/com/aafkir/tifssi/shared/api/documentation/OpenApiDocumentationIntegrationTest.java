package com.aafkir.tifssi.shared.api.documentation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsShouldExposeBusinessEndpointsAndTags() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"name\":\"CRM\"")))
                .andExpect(content().string(containsString("\"name\":\"Staffing\"")))
                .andExpect(content().string(containsString("\"name\":\"Billing\"")))
                .andExpect(content().string(containsString("/api/companies")))
                .andExpect(content().string(containsString("/api/submissions/{id}/status")))
                .andExpect(content().string(containsString("/api/time-entries/summary")))
                .andExpect(content().string(containsString("/api/projects/{projectId}/invoices")))
                .andExpect(content().string(containsString("Creer une entreprise")));
    }

    @Test
    void actuatorHealthShouldBeAvailableAndDocumented() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/actuator/health")))
                .andExpect(content().string(containsString("Monitoring")))
                .andExpect(content().string(containsString("Verifier l'etat de sante de l'application")));
    }
}
