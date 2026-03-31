package com.project.contribution.domain.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Policy가 참조하는 {@link ContributionScoreCodes} 값이 {@code contributionScores.json}에 존재하는지 검증한다.
 */
class ContributionScoreCodesJsonContractTest {

    @Test
    @DisplayName("ContributionScoreCodes의 코드가 contributionScores.json에 모두 있다")
    void policyCodesExistInSeedJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("contributionScores.json");
        try (InputStream in = resource.getInputStream()) {
            List<JsonNode> rows = mapper.readValue(in, new TypeReference<>() {});
            Set<String> jsonCodes = new HashSet<>();
            for (JsonNode row : rows) {
                jsonCodes.add(row.get("code").asText());
            }

            for (Field f : ContributionScoreCodes.class.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType() != String.class) {
                    continue;
                }
                String code = (String) f.get(null);
                assertThat(jsonCodes)
                        .as("JSON에 code=%s 가 있어야 Policy와 시드가 일치합니다", code)
                        .contains(code);
            }
        }
    }
}
