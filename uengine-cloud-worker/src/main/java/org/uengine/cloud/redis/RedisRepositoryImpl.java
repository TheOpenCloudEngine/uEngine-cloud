package org.uengine.cloud.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class RedisRepositoryImpl implements RedisRepository {

    private static final String KEY = "Movie";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations hashOperations;

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void add(final Movie movie) {
        hashOperations.put(KEY, movie.getId(), movie.getName());
    }

    @Override
    public void delete(final String id) {
        hashOperations.delete(KEY, id);
    }

    @Override
    public Movie findMovie(final String id) {
        return (Movie) hashOperations.get(KEY, id);
    }

    @Override
    public Map<Object, Object> findAllMovies() {
        return hashOperations.entries(KEY);
    }
}

