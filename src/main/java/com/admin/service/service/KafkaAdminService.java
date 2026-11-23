package com.admin.service.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.admin.service.exception.RecursoNoEncontradoException;
import com.admin.service.exception.RecursoYaExisteException;

@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);

    /** Lag de una partición para un consumer group. */
    public record PartitionLag(String topic, int partition, long committedOffset, long latestOffset, long lag) {
    }

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    // ---------------------------------------------------------------- tópicos

    public void createTopic(String name, int partitions, short replicas, Map<String, String> configs) {
        try {
            NewTopic newTopic = new NewTopic(name, partitions, replicas);
            if (configs != null && !configs.isEmpty()) {
                newTopic.configs(configs);
            }
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("[KAFKA ADMIN] Tópico creado exitosamente: {}", name);
        } catch (Exception e) {
            if (hasCause(e, TopicExistsException.class)) {
                throw new RecursoYaExisteException("El tópico '" + name + "' ya existe");
            }
            throw error("Error al crear tópico Kafka", e);
        }
    }

    public Set<String> listTopics() {
        try {
            return adminClient.listTopics().names().get();
        } catch (Exception e) {
            throw error("Error al listar tópicos Kafka", e);
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
            if (hasCause(e, UnknownTopicOrPartitionException.class)) {
                throw new RecursoNoEncontradoException("Tópico '" + name + "' no encontrado");
            }
            throw error("Error al describir tópico Kafka", e);
        }
    }

    public void updateTopicConfig(String name, Map<String, String> newConfigs) {
        try {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
            List<AlterConfigOp> ops = new ArrayList<>();
            newConfigs.forEach((k, v) -> ops.add(new AlterConfigOp(new ConfigEntry(k, v), AlterConfigOp.OpType.SET)));

            Map<ConfigResource, Collection<AlterConfigOp>> updateMap = new HashMap<>();
            updateMap.put(resource, ops);

            adminClient.incrementalAlterConfigs(updateMap).all().get();
            log.info("[KAFKA ADMIN] Configuración actualizada para tópico: {}", name);
        } catch (Exception e) {
            if (hasCause(e, UnknownTopicOrPartitionException.class)) {
                throw new RecursoNoEncontradoException("Tópico '" + name + "' no encontrado");
            }
            throw error("Error al actualizar configuración de tópico", e);
        }
    }

    public void increasePartitions(String name, int newTotalPartitions) {
        try {
            Map<String, NewPartitions> newPartitionsMap = new HashMap<>();
            newPartitionsMap.put(name, NewPartitions.increaseTo(newTotalPartitions));
            adminClient.createPartitions(newPartitionsMap).all().get();
            log.info("[KAFKA ADMIN] Particiones aumentadas a {} para tópico: {}", newTotalPartitions, name);
        } catch (Exception e) {
            if (hasCause(e, UnknownTopicOrPartitionException.class)) {
                throw new RecursoNoEncontradoException("Tópico '" + name + "' no encontrado");
            }
            if (hasCause(e, InvalidPartitionsException.class)) {
                throw new IllegalArgumentException(
                        "El nuevo total de particiones debe ser mayor al actual del tópico '" + name + "'");
            }
            throw error("Error al aumentar particiones", e);
        }
    }

    public void deleteTopic(String name) {
        try {
            adminClient.deleteTopics(Collections.singleton(name)).all().get();
            log.info("[KAFKA ADMIN] Tópico eliminado: {}", name);
        } catch (Exception e) {
            if (hasCause(e, UnknownTopicOrPartitionException.class)) {
                throw new RecursoNoEncontradoException("Tópico '" + name + "' no encontrado");
            }
            throw error("Error al eliminar tópico", e);
        }
    }

    // ---------------------------------------------------------- consumer groups

    public Collection<ConsumerGroupListing> listConsumerGroups() {
        try {
            return adminClient.listConsumerGroups().all().get();
        } catch (Exception e) {
            throw error("Error al listar consumer groups", e);
        }
    }

    public Map<String, Object> describeConsumerGroup(String groupId) {
        try {
            var descriptions = adminClient.describeConsumerGroups(Collections.singleton(groupId)).all().get();
            var desc = descriptions.get(groupId);
            // Kafka devuelve estado "Dead" para un grupo que no existe
            if (desc == null || "dead".equalsIgnoreCase(String.valueOf(desc.state()))) {
                throw new RecursoNoEncontradoException("Consumer group '" + groupId + "' no encontrado");
            }

            Map<String, Object> info = new HashMap<>();
            info.put("groupId", desc.groupId());
            info.put("state", desc.state());
            info.put("members", desc.members());
            info.put("partitionAssignor", desc.partitionAssignor());
            info.put("coordinator", desc.coordinator());
            info.put("lag", calculateLag(groupId));
            return info;
        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            if (hasCause(e, GroupIdNotFoundException.class)) {
                throw new RecursoNoEncontradoException("Consumer group '" + groupId + "' no encontrado");
            }
            throw error("Error al describir consumer group", e);
        }
    }

    /** lag = último offset de la partición - offset confirmado por el grupo. */
    private List<PartitionLag> calculateLag(String groupId) throws InterruptedException, ExecutionException {
        Map<TopicPartition, OffsetAndMetadata> committed = adminClient
                .listConsumerGroupOffsets(groupId)
                .partitionsToOffsetAndMetadata()
                .get();

        if (committed == null || committed.isEmpty()) {
            return Collections.emptyList();
        }

        Map<TopicPartition, OffsetSpec> request = new HashMap<>();
        committed.keySet().forEach(tp -> request.put(tp, OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResultInfo> latest = adminClient.listOffsets(request).all().get();

        List<PartitionLag> result = new ArrayList<>();
        committed.forEach((tp, meta) -> {
            if (meta == null) {
                return;
            }
            long committedOffset = meta.offset();
            long latestOffset = latest.get(tp).offset();
            result.add(new PartitionLag(tp.topic(), tp.partition(), committedOffset, latestOffset,
                    Math.max(0L, latestOffset - committedOffset)));
        });
        return result;
    }

    // ------------------------------------------------------------------ cluster

    public Map<String, Object> getClusterInfo() {
        try {
            var cluster = adminClient.describeCluster();
            Map<String, Object> info = new HashMap<>();
            info.put("clusterId", cluster.clusterId().get());
            info.put("controller", cluster.controller().get());
            info.put("nodes", cluster.nodes().get());
            return info;
        } catch (Exception e) {
            throw error("Error al obtener información del cluster Kafka", e);
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Recorre la cadena de causas (ExecutionException, etc.) buscando un tipo de excepción. */
    private static boolean hasCause(Throwable t, Class<? extends Throwable> type) {
        while (t != null) {
            if (type.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private static RuntimeException error(String message, Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return new RuntimeException(message + ": " + e.getMessage(), e);
    }
}
