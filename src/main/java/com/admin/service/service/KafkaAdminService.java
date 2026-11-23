package com.admin.service.service;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.admin.service.exception.RecursoNoEncontradoException;

import java.util.*;

@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);
    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public void createTopic(String name, int partitions, short replicas, Map<String, String> configs) {
        try {
            NewTopic newTopic = new NewTopic(name, partitions, replicas);
            if (configs != null && !configs.isEmpty()) {
                newTopic.configs(configs);
            }
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("[KAFKA ADMIN] Tópico creado exitosamente: {}", name);
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                throw new RuntimeException("El tópico '" + name + "' ya existe", e);
            }
            throw new RuntimeException("Error al crear tópico Kafka: " + e.getMessage(), e);
        }
    }

    public Set<String> listTopics() {
        try {
            return adminClient.listTopics().names().get();
        } catch (Exception e) {
            throw new RuntimeException("Error al listar tópicos Kafka: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> describeTopic(String name) {
        try {
            var descriptions = adminClient.describeTopics(Collections.singleton(name)).allTopicNames().get();
            var desc = descriptions.get(name);
            if (desc == null) {
                throw new RecursoNoEncontradoException("Tópico '" + name + "' no encontrado");
            }

            Map<String, Object> info = new HashMap<>();
            info.put("name", desc.name());
            info.put("isInternal", desc.isInternal());
            info.put("partitions", desc.partitions().size());
            info.put("partitionDetails", desc.partitions());

            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
            var configResult = adminClient.describeConfigs(Collections.singleton(resource)).all().get();
            var config = configResult.get(resource);
            Map<String, String> configsMap = new HashMap<>();
            if (config != null) {
                config.entries().forEach(entry -> configsMap.put(entry.name(), entry.value()));
            }
            info.put("configs", configsMap);

            return info;
        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                throw new RecursoNoEncontradoException("Tópico '" + name + "' no encontrado");
            }
            throw new RuntimeException("Error al describir tópico Kafka: " + e.getMessage(), e);
        }
    }

    public void updateTopicConfig(String name, Map<String, String> newConfigs) {
        try {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
            List<AlterConfigOp> ops = new ArrayList<>();
            newConfigs.forEach((k, v) -> {
                ConfigEntry entry = new ConfigEntry(k, v);
                ops.add(new AlterConfigOp(entry, AlterConfigOp.OpType.SET));
            });

            Map<ConfigResource, Collection<AlterConfigOp>> updateMap = new HashMap<>();
            updateMap.put(resource, ops);

            adminClient.incrementalAlterConfigs(updateMap).all().get();
            log.info("[KAFKA ADMIN] Configuración actualizada para tópico: {}", name);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar configuración de tópico: " + e.getMessage(), e);
        }
    }

    public void increasePartitions(String name, int newTotalPartitions) {
        try {
            Map<String, NewPartitions> newPartitionsMap = new HashMap<>();
            newPartitionsMap.put(name, NewPartitions.increaseTo(newTotalPartitions));
            adminClient.createPartitions(newPartitionsMap).all().get();
            log.info("[KAFKA ADMIN] Particiones aumentadas a {} para tópico: {}", newTotalPartitions, name);
        } catch (Exception e) {
            throw new RuntimeException("Error al aumentar particiones: " + e.getMessage(), e);
        }
    }

    public void deleteTopic(String name) {
        try {
            adminClient.deleteTopics(Collections.singleton(name)).all().get();
            log.info("[KAFKA ADMIN] Tópico eliminado: {}", name);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar tópico: " + e.getMessage(), e);
        }
    }

    public Collection<ConsumerGroupListing> listConsumerGroups() {
        try {
            return adminClient.listConsumerGroups().all().get();
        } catch (Exception e) {
            throw new RuntimeException("Error al listar consumer groups: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> describeConsumerGroup(String groupId) {
        try {
            var descResult = adminClient.describeConsumerGroups(Collections.singleton(groupId)).all().get();
            var desc = descResult.get(groupId);
            if (desc == null) {
                throw new RecursoNoEncontradoException("Consumer group '" + groupId + "' no encontrado");
            }

            Map<String, Object> info = new HashMap<>();
            info.put("groupId", desc.groupId());
            info.put("state", desc.state());
            info.put("members", desc.members());
            info.put("partitionAssignor", desc.partitionAssignor());
            info.put("coordinator", desc.coordinator());

            return info;
        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al describir consumer group: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getClusterInfo() {
        try {
            var cluster = adminClient.describeCluster();
            Map<String, Object> info = new HashMap<>();
            info.put("clusterId", cluster.clusterId().get());
            info.put("controller", cluster.controller().get());
            info.put("nodes", cluster.nodes().get());
            return info;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener información del cluster Kafka: " + e.getMessage(), e);
        }
    }
}
