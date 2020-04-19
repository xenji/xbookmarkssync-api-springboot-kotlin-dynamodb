package com.xenji.xbookmarksync

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanProvider {
    @Bean
    fun amazonDynamoDB(amazonClientCredentials: AWSCredentialsProvider): AmazonDynamoDB =
        AmazonDynamoDBAsyncClientBuilder
            .standard()
            .withCredentials(amazonClientCredentials)
            .build()

    @Bean
    fun amazonAWSCredentials(): AWSCredentialsProvider {
        return DefaultAWSCredentialsProviderChain.getInstance()
    }
}
