package com.erebelo.springneptunedemo.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.neptune.auth.NeptuneNettyHttpSigV4Signer;
import com.amazonaws.neptune.auth.NeptuneSigV4SignerException;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.PartitionStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Configuration
@Profile("!local")
public class NeptuneConfiguration {

    @Value("${aws.neptune.endpoint}")
    private String endpoint;

    @Value("${aws.neptune.partition.key}")
    private String partitionKey;

    @Value("${aws.neptune.partition.name}")
    private String partitionName;

    @Value("${aws.region}")
    private String region;

    @Bean
    public Cluster cluster() {
        return Cluster.build(endpoint)
                .enableSsl(true)
                .maxConnectionPoolSize(5)
                .maxInProcessPerConnection(1)
                .maxSimultaneousUsagePerConnection(5)
                .minSimultaneousUsagePerConnection(1)
                .handshakeInterceptor(r ->
                {
                    try {
                        var sigV4Signer = new NeptuneNettyHttpSigV4Signer(region, new DefaultAWSCredentialsProviderChain());
                        sigV4Signer.signRequest(r);
                    } catch (NeptuneSigV4SignerException e) {
                        throw new RuntimeException("Exception occurred while signing the request", e);

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
        return PartitionStrategy.build()
                .partitionKey(partitionKey)
                .writePartition(partitionName)
                .readPartitions(partitionName)
                .create();
    }

    @Bean
    public GraphTraversalSource graphTraversalSource(RemoteConnection remoteConnection, PartitionStrategy partitionStrategy) {
        return traversal().withRemote(remoteConnection).withStrategies(partitionStrategy);
    }
}
