package com.music.bot.repository;

import com.music.bot.dto.AudioMetadataDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<AudioMetadataDTO, String> {
}
