package org.uengine.cloud.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class CatalogCacheService {

    @Autowired
    private CatalogService catalogService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogCacheService.class);

    /**
     * 카테고리 정보를 업데이트 한다.
     *
     * @return
     * @throws Exception
     */
    //From gitlab hook
    @CachePut(value = "categories", key = "'categories'")
    @Transactional
    public List<Category> updateCategoriesCache() throws Exception {
        LOGGER.info("update categories to redis");
        return catalogService.getCategories();
    }

    /**
     * 카테고리 정보를 가져온다.
     *
     * @return
     * @throws Exception
     */
    @Cacheable(value = "categories", key = "'categories'")
    public List<Category> getCategoriesCache() throws Exception {
        LOGGER.info("get categories from gitlab");
        return catalogService.getCategories();
    }
}
