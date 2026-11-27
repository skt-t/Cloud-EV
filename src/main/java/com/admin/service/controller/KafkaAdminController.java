package com.admin.service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.admin.service.dto.PartitionsRequest;
import com.admin.service.dto.TopicConfigRequest;
import com.admin.service.dto.TopicRequest;
import com.admin.service.service.KafkaAdminService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/kafka")
public class KafkaAdminController {

    private final KafkaAdminService service;

    public KafkaAdminController(KafkaAdminService service) {
        this.service = service;
    }

    @PostMapping("/topics")
    public ResponseEntity<Map<String, String>> createTopic(@Valid @RequestBody TopicRequest request) {
        service.createTopic(request.name(), request.partitions(), request.replicas(), request.configs());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tópico '" + request.name() + "' creado exitosamente"));
    }

    @GetMapping("/topics")
    public ResponseEntity<Set<String>> listTopics() {
        return ResponseEntity.ok(service.listTopics());
    }

    @GetMapping("/topics/{name}")
    public ResponseEntity<Map<String, Object>> describeTopic(@PathVariable String name) {
        return ResponseEntity.ok(service.describeTopic(name));
    }

    @PutMapping("/topics/{name}/config")
    public ResponseEntity<Map<String, String>> updateTopicConfig(
            @PathVariable String name,
            @Valid @RequestBody TopicConfigRequest request) {
        service.updateTopicConfig(name, request.configs());
        return ResponseEntity.ok(Map.of("message", "Configuración del tópico '" + name + "' actualizada"));
    }

    @PutMapping("/topics/{name}/partitions")
    public ResponseEntity<Map<String, String>> increasePartitions(
            @PathVariable String name,
            @Valid @RequestBody PartitionsRequest request) {
        service.increasePartitions(name, request.totalPartitions());
        return ResponseEntity.ok(Map.of("message", "Particiones del tópico '" + name + "' aumentadas a " + request.totalPartitions()));
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Map<String, String>> deleteTopic(@PathVariable String name) {
        service.deleteTopic(name);
        return ResponseEntity.ok(Map.of("message", "Tópico '" + name + "' eliminado exitosamente"));
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<Collection<?>> listConsumerGroups() {
        return ResponseEntity.ok(service.listConsumerGroups());
    }

    @GetMapping("/consumer-groups/{id}")
    public ResponseEntity<Map<String, Object>> describeConsumerGroup(@PathVariable String id) {
        return ResponseEntity.ok(service.describeConsumerGroup(id));
    }

    @GetMapping("/consumer-groups/{id}/lag")
    public ResponseEntity<?> getConsumerGroupLag(@PathVariable String id) {
        return ResponseEntity.ok(service.getConsumerGroupLag(id));
    }

    @DeleteMapping("/consumer-groups/{id}")
    public ResponseEntity<Map<String, String>> deleteConsumerGroup(@PathVariable String id) {
        service.deleteConsumerGroup(id);
        return ResponseEntity.ok(Map.of("message", "Consumer group '" + id + "' eliminado exitosamente"));
    }

    @GetMapping("/cluster")
    public ResponseEntity<Map<String, Object>> getClusterInfo() {
        return ResponseEntity.ok(service.getClusterInfo());
    }
}
