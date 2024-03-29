package com.maveric.hr360.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Override
    public String getDatabaseName() {

        return database;
    }
    @Override
    public MongoClient mongoClient() {

        return MongoClients.create("mongodb://" + host + ":" + port);
    }
    @Bean
    public GridFSBucket gridFSBucket() {
        return GridFSBuckets.create(mongoClient().getDatabase(getDatabaseName()), "images");
    }
}
