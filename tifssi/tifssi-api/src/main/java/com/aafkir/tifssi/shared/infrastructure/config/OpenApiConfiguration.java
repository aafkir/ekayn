package com.aafkir.tifssi.shared.infrastructure.config;

import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI tifssiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tifssi API")
                        .version("v1")
                        .description("Documentation OpenAPI des APIs CRM, staffing, projets, timesheets, expenses, absences, billing et monitoring."))
                .tags(List.of(
                        new Tag().name("CRM").description("Gestion des entreprises clientes et de leurs contacts."),
                        new Tag().name("Staffing").description("Gestion des besoins, profils et positionnements."),
                        new Tag().name("Projects").description("Gestion des projets et des missions."),
                        new Tag().name("Timesheets").description("Gestion des saisies de temps et des syntheses de periode."),
                        new Tag().name("Expenses").description("Gestion des notes de frais et des syntheses de periode."),
                        new Tag().name("Absences").description("Gestion des absences collaborateurs et de leurs syntheses annuelles."),
                        new Tag().name("Billing").description("Gestion des factures projet et des lignes de facturation."),
                        new Tag().name("Monitoring").description("Supervision technique et etat de sante de l'application.")
                ));
    }

    @Bean
    public OpenApiCustomizer defaultErrorResponsesCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values()
                    .forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                        ApiResponses responses = operation.getResponses();
                        if (responses == null) {
                            responses = new ApiResponses();
                            operation.setResponses(responses);
                        }
                        ensureErrorResponse(responses, "400", "Requete invalide ou parametres incoherents.");
                        ensureErrorResponse(responses, "500", "Erreur technique inattendue.");
                    }));
        };
    }

    @Bean
    public OpenApiCustomizer actuatorHealthCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            PathItem healthPath = openApi.getPaths().get("/actuator/health");
            if (healthPath == null || healthPath.getGet() == null) {
                return;
            }

            healthPath.getGet().setTags(List.of("Monitoring"));
            healthPath.getGet().setSummary("Verifier l'etat de sante de l'application");
            healthPath.getGet().setDescription("Expose l'etat global de l'application et des composants techniques via Spring Boot Actuator.");

            ApiResponses responses = healthPath.getGet().getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                healthPath.getGet().setResponses(responses);
            }

            ApiResponse okResponse = responses.containsKey("200")
                    ? responses.get("200")
                    : new ApiResponse();
            okResponse.setDescription("Application demarree et endpoint de health accessible.");
            responses.addApiResponse("200", okResponse);

            ApiResponse unavailableResponse = responses.containsKey("503")
                    ? responses.get("503")
                    : new ApiResponse();
            unavailableResponse.setDescription("Un indicateur de sante remonte un etat indisponible.");
            responses.addApiResponse("503", unavailableResponse);
        };
    }

    private void ensureErrorResponse(ApiResponses responses, String code, String description) {
        if (responses.containsKey(code)) {
            return;
        }

        responses.addApiResponse(code, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/" + ApiErrorResponse.class.getSimpleName())))));
    }
}
