package com.music.bot.service;

import com.music.bot.assemblyAI.AssemblyAIService;
import com.music.bot.assemblyAI.AssemblyAIWebhookBody;
import com.music.bot.assemblyAI.AudioTranscriptionResponse;
import com.music.bot.dto.AudioMetadataDTO;
import com.music.bot.dto.AudioTranscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AssemblyAIService assemblyAIService;

    public void saveAudio(AudioMetadataDTO audioMetadataDTO) {
        elasticsearchOperations.save(audioMetadataDTO);
    }

    public void setLyrics(AssemblyAIWebhookBody body, String audioId) {
        switch (body.getStatus()) {
            case "error" -> {
                AudioMetadataDTO audioMetadataDTO = Optional.ofNullable(elasticsearchOperations.get(audioId, AudioMetadataDTO.class)).orElseThrow(() -> new RuntimeException("Cannot find audio with id " + audioId));
                audioMetadataDTO.setStatus(AudioTranscriptionStatus.FAILED);
                elasticsearchOperations.save(audioMetadataDTO);
            }
            case "completed" -> {
                AudioTranscriptionResponse transcriptionResult = assemblyAIService.getTranscriptionResult(body.getTranscriptId());
                AudioMetadataDTO audioMetadataDTO = Optional.ofNullable(elasticsearchOperations.get(audioId, AudioMetadataDTO.class)).orElseThrow(() -> new RuntimeException("Cannot find audio with id " + audioId));
                audioMetadataDTO.setStatus(AudioTranscriptionStatus.COMPLETE);
                audioMetadataDTO.setText(transcriptionResult.getText());
                elasticsearchOperations.save(audioMetadataDTO);
            }
            default -> throw new IllegalArgumentException("Something goes wrong, received unknown status " + body.getStatus());
        }
    }

    public String searchAudioByLyrics(String lyrics) {
        return elasticsearchOperations
                .search(new CriteriaQuery(new Criteria().and("text").expression(lyrics)), AudioMetadataDTO.class)
                .get()
                .max(Comparator.comparing(SearchHit::getScore))
                .map(hit -> hit.getContent().getId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find audio containing lyrics \"%s\"", lyrics)));
    }
}
