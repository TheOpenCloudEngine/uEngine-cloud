package org.uengine.cloud.redis;

import java.util.Map;

public interface RedisRepository {
    Map<Object, Object> findAllMovies();

    void add(Movie movie);

    void delete(String id);

    Movie findMovie(String id);
}
