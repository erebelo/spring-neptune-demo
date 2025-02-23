package com.erebelo.springneptunedemo.config;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.neptune.auth.NeptuneNettyHttpSigV4Signer;
import com.amazonaws.neptune.auth.NeptuneSigV4SignerException;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.PartitionStrategy;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NeptuneConfiguration {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.neptune.embedded}")
    private boolean useEmbedded;

    @Value("${aws.neptune.endpoint}")
    private String endpoint;

    @Value("${aws.neptune.partition.key}")
    private String partitionKey;

    @Value("${aws.neptune.partition.name}")
    private String partitionName;

    @Bean
    public Cluster cluster() {
        return Cluster.build(endpoint).enableSsl(true).maxConnectionPoolSize(5).maxInProcessPerConnection(1)
                .maxSimultaneousUsagePerConnection(5).minSimultaneousUsagePerConnection(1).requestInterceptor(r -> {
                    try {
                        NeptuneNettyHttpSigV4Signer sigV4Signer = new NeptuneNettyHttpSigV4Signer(region,
                                new DefaultAWSCredentialsProviderChain());
                        sigV4Signer.signRequest(r);
                    } catch (NeptuneSigV4SignerException e) {
                        throw new IllegalStateException("Exception occurred while signing the request", e);

                    }
                    return r;
                }).create();
    }

    @Bean
    public RemoteConnection driverRemoteConnection(Cluster cluster) {
        return DriverRemoteConnection.using(cluster);
    }

    @Bean
    public PartitionStrategy partitionStrategy() {
        return PartitionStrategy.build().partitionKey(partitionKey).writePartition(partitionName)
                .readPartitions(partitionName).create();
    }

    @Bean
    public GraphTraversalSource graphTraversalSource(RemoteConnection remoteConnection,
            PartitionStrategy partitionStrategy) {
        if (useEmbedded) {
            return traversal().withEmbedded(TinkerGraph.open())/* TODO .withStrategies(partitionStrategy()) */;
        }

        return traversal().withRemote(remoteConnection)/* TODO .withStrategies(partitionStrategy) */;
    }
}
