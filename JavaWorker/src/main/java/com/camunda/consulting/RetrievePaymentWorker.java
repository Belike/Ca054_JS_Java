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

@Configuration
@Slf4j
public class RetrievePaymentWorker {

    @Bean
    @ExternalTaskSubscription(topicName = "paymentExampleWorker")
    public ExternalTaskHandler retrievePayment() {
        return (externalTask, externalTaskService) -> {

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> map = new HashMap<>();
            map.put("messageName", "paymentDemoMessage");
            map.put("businessKey", externalTask.getBusinessKey());
            map.put("processVariables", Collections.singletonMap("amount", externalTask.getVariable("amount")));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8080/engine-rest/message", entity, String.class);

            if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
                externalTaskService.handleFailure(externalTask, "RestFailure", "Exptected Response 204, but receiveid " + responseEntity.getStatusCode(), 0, 0);
            }else{
                externalTaskService.complete(externalTask);
            }
        };
    }
}
