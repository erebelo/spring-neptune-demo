package com.erebelo.springneptunedemo.config;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.PartitionStrategy;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Configuration
@Profile("local")
public class NeptuneLocalConfiguration {

    @Value("${aws.neptune.partition.key}")
    private String partitionKey;

    @Value("${aws.neptune.partition.name}")
    private String partitionName;

    @Bean
    public GraphTraversalSource graphTraversalSource() {
        return traversal().withEmbedded(TinkerGraph.open()).withStrategies(partitionStrategy());
    }

    @Bean
    public PartitionStrategy partitionStrategy() {
        return PartitionStrategy.build()
                .partitionKey(partitionKey)
                .writePartition(partitionName)
                .readPartitions(partitionName)
                .create();
    }
}
