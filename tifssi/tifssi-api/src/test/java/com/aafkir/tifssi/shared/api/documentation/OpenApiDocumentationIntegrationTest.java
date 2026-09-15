package com.aafkir.tifssi.shared.api.documentation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiDocsShouldExposeBusinessEndpointsAndTags() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/companies")))
                .andExpect(content().string(containsString("/api/profiles")))
                .andExpect(content().string(containsString("/api/actions")))
                .andExpect(content().string(containsString("/api/needs/{needId}/submissions")))
                .andExpect(content().string(containsString("/api/submissions/{id}/status")))
                .andExpect(content().string(containsString("/api/time-entries/summary")))
                .andExpect(content().string(containsString("/api/timesheets")))
                .andExpect(content().string(containsString("/api/timesheets/{id}")))
                .andExpect(content().string(containsString("/api/timesheets/{id}/submit")))
                .andExpect(content().string(containsString("/api/profiles/me")))
                .andExpect(content().string(containsString("/api/projects/{projectId}/invoices")))
                .andExpect(content().string(containsString("Creer un profil")))
                .andExpect(content().string(containsString("Creer une action")))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode tags = root.path("tags");
        Set<String> tagNames = new HashSet<>();
        tags.forEach(tag -> tagNames.add(tag.path("name").asText()));

        org.assertj.core.api.Assertions.assertThat(tagNames)
                .contains(
                        "Companies",
                        "Contacts",
                        "Actions",
                        "Needs",
                        "Profiles",
                        "Submissions",
                        "Projects",
                        "Missions",
                        "Time Entries",
                        "Timesheets",
                        "Expenses",
                        "Absences",
                        "Billing",
                        "Monitoring"
                )
                .doesNotContain("CRM", "Staffing");
        org.assertj.core.api.Assertions.assertThat(tagNames).hasSameSizeAs(tags);
    }

    @Test
    void shouldDocumentMonthlyWorkflowAndRequiredRejectionReason() throws Exception {
        var result = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        JsonNode document = objectMapper.readTree(result.getResponse().getContentAsString());
        for (String action : new String[]{"submit", "validate", "reject"}) {
            var operation = document.path("paths").path("/api/timesheets/{id}/" + action).path("post");
            org.assertj.core.api.Assertions.assertThat(operation.isMissingNode()).isFalse();
            org.assertj.core.api.Assertions.assertThat(operation.path("description").asText()).contains("SUBMITTED");
            org.assertj.core.api.Assertions.assertThat(operation.path("responses").has("409")).isTrue();
        }
        var request = document.path("components").path("schemas").path("TimesheetRejectRequest");
        org.assertj.core.api.Assertions.assertThat(request.path("required").toString()).contains("reason");
        org.assertj.core.api.Assertions.assertThat(request.path("properties").path("reason").path("maxLength").asInt()).isEqualTo(2000);
        org.assertj.core.api.Assertions.assertThat(document.path("components").path("schemas").path("TimeEntryResponse")
                .path("properties").path("status").path("deprecated").asBoolean()).isTrue();
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/openapi.json"), result.getResponse().getContentAsString());
    }

    @Test
    void timeEntryListShouldDocumentOptionalCombinedFiltersAndGlobalOrdering() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode operation = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("paths").path("/api/time-entries").path("get");
        org.assertj.core.api.Assertions.assertThat(operation.path("description").asText())
                .contains("sans parametre", "intersection", "pas paginee", "workDate puis id croissants");
        Set<String> parameters = new HashSet<>();
        operation.path("parameters").forEach(parameter -> {
            parameters.add(parameter.path("name").asText());
            org.assertj.core.api.Assertions.assertThat(parameter.path("required").asBoolean()).isFalse();
            org.assertj.core.api.Assertions.assertThat(parameter.path("in").asText()).isEqualTo("query");
        });
        org.assertj.core.api.Assertions.assertThat(parameters).containsExactlyInAnyOrder("missionId", "profileId");
        org.assertj.core.api.Assertions.assertThat(operation.path("responses").path("200")
                .path("content").path("*/*").path("schema").path("type").asText())
                .isEqualTo("array");
    }

    @Test
    void actuatorHealthShouldBeAvailableAndDocumented() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/actuator/health")))
                .andExpect(content().string(containsString("Monitoring")))
                .andExpect(content().string(containsString("Verifier l'etat de sante de l'application")));
    }

    @Test
    void profileCreateShouldBeDocumentedWithRequestResponseAndValidationErrors() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode createOperation = root.path("paths").path("/api/profiles").path("post");

        org.assertj.core.api.Assertions.assertThat(createOperation.path("summary").asText()).isEqualTo("Creer un profil");
        org.assertj.core.api.Assertions.assertThat(createOperation.path("requestBody").path("required").asBoolean()).isTrue();
        org.assertj.core.api.Assertions.assertThat(
                createOperation.path("requestBody")
                        .path("content")
                        .path("application/json")
                        .path("schema")
                        .path("$ref")
                        .asText()
        ).isEqualTo("#/components/schemas/ProfileCreateRequest");
        org.assertj.core.api.Assertions.assertThat(
                createOperation.path("responses")
                        .path("201")
                        .path("content")
                        .path("*/*")
                        .path("schema")
                        .path("$ref")
                        .asText()
        ).isEqualTo("#/components/schemas/ProfileResponse");
        org.assertj.core.api.Assertions.assertThat(
                createOperation.path("responses")
                        .path("400")
                        .path("description")
                        .asText()
        ).contains("Bean Validation");
    }

    @Test
    void profileSkillsShouldBeDocumentedWithResponseAndNotFoundError() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode getOperation = root.path("paths").path("/api/profiles/{profileId}/skills").path("get");

        org.assertj.core.api.Assertions.assertThat(getOperation.path("summary").asText())
                .isEqualTo("Lister les competences d'un profil");
        org.assertj.core.api.Assertions.assertThat(
                getOperation.path("responses")
                        .path("200")
                        .path("content")
                        .path("*/*")
                        .path("schema")
                        .path("items")
                        .path("$ref")
                        .asText()
        ).isEqualTo("#/components/schemas/ProfileSkillResponse");
        org.assertj.core.api.Assertions.assertThat(
                getOperation.path("responses").path("404").path("description").asText()
        ).isEqualTo("Profil introuvable.");
    }

    @Test
    void profilePatchShouldBeDocumentedWithRequestResponseAndErrors() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode patchOperation = root.path("paths").path("/api/profiles/{profileId}").path("patch");

        org.assertj.core.api.Assertions.assertThat(patchOperation.path("summary").asText())
                .isEqualTo("Modifier un profil");
        org.assertj.core.api.Assertions.assertThat(
                patchOperation.path("requestBody")
                        .path("content")
                        .path("application/json")
                        .path("schema")
                        .path("$ref")
                        .asText()
        ).isEqualTo("#/components/schemas/ProfilePatchRequest");
        org.assertj.core.api.Assertions.assertThat(
                patchOperation.path("responses")
                        .path("200")
                        .path("content")
                        .path("*/*")
                        .path("schema")
                        .path("$ref")
                        .asText()
        ).isEqualTo("#/components/schemas/ProfileResponse");
        org.assertj.core.api.Assertions.assertThat(patchOperation.path("responses").path("400").isMissingNode())
                .isFalse();
        org.assertj.core.api.Assertions.assertThat(patchOperation.path("responses").path("404").isMissingNode())
                .isFalse();
    }
}
