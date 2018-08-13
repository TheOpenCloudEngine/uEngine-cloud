/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uengine.zuul.ratelimit;

import com.google.common.collect.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.uengine.zuul.ratelimit.config.RateLimitKeyGenerator;
import org.uengine.zuul.ratelimit.config.RateLimitUtils;
import org.uengine.zuul.ratelimit.config.RateLimiter;
import org.uengine.zuul.ratelimit.config.properties.RateLimitProperties;
import org.uengine.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import org.uengine.zuul.ratelimit.config.repository.*;
import org.uengine.zuul.ratelimit.filters.RateLimitPostFilter;
import org.uengine.zuul.ratelimit.filters.RateLimitPreFilter;
import org.uengine.zuul.ratelimit.support.DefaultRateLimitKeyGenerator;
import org.uengine.zuul.ratelimit.support.DefaultRateLimitUtils;
import org.uengine.zuul.ratelimit.support.StringToMatchTypeConverter;
import com.netflix.zuul.ZuulFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.uengine.zuul.ratelimit.config.properties.RateLimitProperties.PREFIX;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    @Bean
    @ConfigurationPropertiesBinding
    public StringToMatchTypeConverter stringToMatchTypeConverter() {
        return new StringToMatchTypeConverter();
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterErrorHandler.class)
    public RateLimiterErrorHandler rateLimiterErrorHandler() {
        return new DefaultRateLimiterErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitUtils.class)
    public RateLimitUtils rateLimitUtils(RateLimitProperties rateLimitProperties) {
        return new DefaultRateLimitUtils(rateLimitProperties);
    }

    @Bean
    public ZuulFilter rateLimiterPreFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                           RouteLocator routeLocator, RateLimitKeyGenerator rateLimitKeyGenerator,
                                           RateLimitUtils rateLimitUtils) {
        return new RateLimitPreFilter(rateLimitProperties, routeLocator, new UrlPathHelper(), rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    public ZuulFilter rateLimiterPostFilter(RateLimiter rateLimiter, RateLimitProperties rateLimitProperties,
                                            RouteLocator routeLocator, RateLimitKeyGenerator rateLimitKeyGenerator,
                                            RateLimitUtils rateLimitUtils) {
        return new RateLimitPostFilter(rateLimitProperties, routeLocator, new UrlPathHelper(), rateLimiter,
                rateLimitKeyGenerator, rateLimitUtils);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public RateLimitKeyGenerator ratelimitKeyGenerator(RateLimitProperties properties, RateLimitUtils rateLimitUtils) {
        return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils);
    }

    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "REDIS")
    public static class RedisConfiguration {

        @Bean("rateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler,
                                            @Qualifier("rateLimiterRedisTemplate") RedisTemplate redisTemplate) {
            return new RedisRateLimiter(rateLimiterErrorHandler, redisTemplate);
        }
    }

    @Configuration
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = PREFIX, name = "repository", havingValue = "IN_MEMORY", matchIfMissing = true)
    public static class InMemoryConfiguration {

        @Bean
        public RateLimiter inMemoryRateLimiter(RateLimiterErrorHandler rateLimiterErrorHandler) {
            return new InMemoryRateLimiter(rateLimiterErrorHandler);
        }
    }

    @Configuration
    @RequiredArgsConstructor
    protected static class RateLimitPropertiesAdjuster {

        private final RateLimitProperties rateLimitProperties;

        @PostConstruct
        public void init() {
            Policy defaultPolicy = rateLimitProperties.getDefaultPolicy();
            if (defaultPolicy != null) {
                List<Policy> defaultPolicies = Lists.newArrayList(defaultPolicy);
                defaultPolicies.addAll(rateLimitProperties.getDefaultPolicyList());
                rateLimitProperties.setDefaultPolicyList(defaultPolicies);
            }
            rateLimitProperties.getPolicies().forEach((route, policy) ->
                rateLimitProperties.getPolicyList().compute(route, (key, policies) -> getPolicies(policy, policies)));
        }

        private List<Policy> getPolicies(Policy policy, List<Policy> policies) {
            List<Policy> combinedPolicies = Lists.newArrayList(policy);
            if (policies != null) {
                combinedPolicies.addAll(policies);
            }
            return combinedPolicies;
        }
    }
}
