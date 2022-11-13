package com.music.bot.service;

import com.music.bot.assemblyAI.AssemblyAIService;
import com.music.bot.assemblyAI.AssemblyAIWebhookBody;
import com.music.bot.assemblyAI.AudioTranscriptionResponse;
import com.music.bot.dto.AudioMetadataDTO;
import com.music.bot.dto.AudioTranscriptionStatus;
import com.music.bot.repository.ElasticSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final ElasticSearchRepository elasticSearchRepository;
    private final AssemblyAIService assemblyAIService;

    public void saveAudio(AudioMetadataDTO audioMetadataDTO) {
        elasticSearchRepository.save(audioMetadataDTO);
    }

    public void setLyrics(AssemblyAIWebhookBody body, String audioId) {
        switch (body.getStatus()) {
            case "error" -> {
                AudioMetadataDTO audioMetadataDTO = elasticSearchRepository.findById(audioId).orElseThrow(() -> new RuntimeException("Cannot find audio with id " + audioId));
                audioMetadataDTO.setStatus(AudioTranscriptionStatus.FAILED);
                elasticSearchRepository.save(audioMetadataDTO);
            }
            case "completed" -> {
                AudioTranscriptionResponse transcriptionResult = assemblyAIService.getTranscriptionResult(body.getTranscriptId());
                AudioMetadataDTO audioMetadataDTO = elasticSearchRepository.findById(audioId).orElseThrow(() -> new RuntimeException("Cannot find audio with id " + audioId));
                audioMetadataDTO.setStatus(AudioTranscriptionStatus.COMPLETE);
                audioMetadataDTO.setText(transcriptionResult.getText());
                elasticSearchRepository.save(audioMetadataDTO);
            }
            default -> throw new IllegalArgumentException("Something goes wrong, received unknown status " + body.getStatus());
        }
    }
}
