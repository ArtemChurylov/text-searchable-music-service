package com.music.bot.assemblyAI;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AssemblyAIService {

    private static final String UPLOAD_AUDIO_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_AUDIO_URL = "https://api.assemblyai.com/v2/transcript";
    private static final String TRANSCRIPTION_RESULT_URL = "https://api.assemblyai.com/v2/transcript/%s";

    @Value("${assembly-ai.authorization-token}")
    private String authorizationToken;
    @Value("${assembly-ai.webhook-url}")
    private String webhookUrl;
    private final RestTemplate restTemplate;

    public UploadFileResponse uploadFile(byte[] fileBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authorizationToken);
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(fileBytes, headers);
        ResponseEntity<UploadFileResponse> uploadFileResponseEntity = restTemplate.exchange(UPLOAD_AUDIO_URL, HttpMethod.POST, httpEntity, UploadFileResponse.class);
        return uploadFileResponseEntity.getBody();
    }

    public AudioTranscriptionResponse startTranscription(UploadFileResponse response, String audioId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authorizationToken);
        AudioTranscriptionRequest request = AudioTranscriptionRequest.builder()
                .audioUrl(response.getUploadUrl())
                .languageDetection(true)
                .webhookUrl(webhookUrl)
                .customHeaderName("audio_id")
                .customHeaderValue(audioId)
                .build();
        HttpEntity<AudioTranscriptionRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<AudioTranscriptionResponse> audioTranscriptionResponseEntity = restTemplate.exchange(TRANSCRIPT_AUDIO_URL, HttpMethod.POST, httpEntity, AudioTranscriptionResponse.class);
        return audioTranscriptionResponseEntity.getBody();
    }

    public AudioTranscriptionResponse getTranscriptionResult(String transcriptId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authorizationToken);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<AudioTranscriptionResponse> audioTranscriptionResponseEntity = restTemplate.exchange(String.format(TRANSCRIPTION_RESULT_URL, transcriptId), HttpMethod.GET, httpEntity, AudioTranscriptionResponse.class);
        return audioTranscriptionResponseEntity.getBody();
    }
}
