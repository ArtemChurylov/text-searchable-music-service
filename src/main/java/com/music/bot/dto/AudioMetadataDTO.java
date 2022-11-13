package com.music.bot.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "audio_metadata")
public class AudioMetadataDTO {
    @Id
    String id;
    String source;
    String title;
    String author;
    String text;
    AudioTranscriptionStatus status;
}
