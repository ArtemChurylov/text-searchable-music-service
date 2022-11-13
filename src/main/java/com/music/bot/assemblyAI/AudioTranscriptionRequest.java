package com.music.bot.assemblyAI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AudioTranscriptionRequest {

    @JsonProperty("audio_url")
    String audioUrl;
    @JsonProperty("language_detection")
    boolean languageDetection;
    @JsonProperty("webhook_url")
    String webhookUrl;
    @JsonProperty("webhook_auth_header_name")
    String customHeaderName;
    @JsonProperty("webhook_auth_header_value")
    String customHeaderValue;
}
