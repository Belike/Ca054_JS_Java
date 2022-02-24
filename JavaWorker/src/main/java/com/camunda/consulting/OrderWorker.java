package com.camunda.consulting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class OrderWorker {

    @Bean
    @ExternalTaskSubscription(topicName = "startingPayment")
    public ExternalTaskHandler startingPayment() {
        return (externalTask, externalTaskService) -> {
            try {

                log.info("Starting to request payment");

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HashMap<String, Object> amountMap = new HashMap<>();
                amountMap.put("value", (double) externalTask.getVariable("amount"));
                amountMap.put("type", "Double");
                HashMap<String, Object> shouldFailMap = new HashMap<>();
                shouldFailMap.put("value", false);
                shouldFailMap.put("type", "Boolean");
                HashMap<String, Object> shouldBpmnError = new HashMap<>();
                shouldFailMap.put("value", false);
                shouldFailMap.put("type", "Boolean");

                HashMap<String, Object> processVariables = new HashMap<>();
                processVariables.put("amount", amountMap);
                processVariables.put("shouldFail", shouldFailMap);
                processVariables.put("shouldBpmnError", shouldBpmnError);

                HashMap<String, Object> map = new HashMap<>();
                map.put("messageName", "payment-message");
                map.put("businessKey", externalTask.getBusinessKey());
                map.put("processVariables", processVariables);

                //ObjectMapper mapper = new ObjectMapper();
                //String jsonPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
                //HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

                ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8080/engine-rest/message", entity, String.class);

                if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
                    externalTaskService.handleFailure(externalTask, "RestFailure", "Exptected Response 204, but receiveid " + responseEntity.getStatusCode(), 0, 0);
                } else {
                    externalTaskService.complete(externalTask);
                }
            }catch (Exception e){
                externalTaskService.handleFailure(externalTask, e.getCause().toString(), e.getMessage(), 0, 0L);
            }

            log.info("Request Payment finished");
        };
    }
}
