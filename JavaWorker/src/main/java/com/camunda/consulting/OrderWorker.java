package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
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

            log.info("Starting to request payment");

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> processVariables = new HashMap<>();
            processVariables.put("amount", externalTask.getVariable("amount"));
            processVariables.put("shouldFail", false);
            processVariables.put("shouldBpmnError", false);

            Map<String, Object> map = new HashMap<>();
            map.put("messageName", "payment-message");
            map.put("businessKey", externalTask.getBusinessKey());
            //map.put("processVariables", );
            //map.put("processVariables", Collections.singletonMap("amount", (double) externalTask.getVariable("amount")));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8080/engine-rest/message", entity, String.class);

            if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
                externalTaskService.handleFailure(externalTask, "RestFailure", "Exptected Response 204, but receiveid " + responseEntity.getStatusCode(), 0, 0);
            }else{
                externalTaskService.complete(externalTask);
            }

            log.info("Request Payment finished");
        };
    }
}
