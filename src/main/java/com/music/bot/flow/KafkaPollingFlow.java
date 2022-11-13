package com.music.bot.flow;

import com.music.bot.AudioMetadata;
import com.music.bot.dto.AudioMetadataDTO;
import com.music.bot.dto.AudioTranscriptionStatus;
import com.music.bot.assemblyAI.UploadFileResponse;
import com.music.bot.assemblyAI.AssemblyAIService;
import com.music.bot.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.transformer.Transformer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.messaging.support.GenericMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaPollingFlow {

    private static final String MUSIC_METADATA_TOPIC = "com.music.bot.MusicMetadata";
    private static final String STORING_AUDIO_CHANNEL = "storingAudioChannel";
    private static final String UPLOAD_FILE_CHANNEL = "uploadFileChannel";
    private static final String START_TRANSCRIPTION_CHANNEL = "startTranscriptionChannel";
    private static final String AUDIO_ID_HEADER = "audioId";
    private final ConsumerFactory<String, AudioMetadata> consumerFactory;
    private final AssemblyAIService assemblyAIService;
    private final AudioService audioService;

    @Bean
    public IntegrationFlow pollData() {
        return IntegrationFlows
                .from(Kafka.messageDrivenChannelAdapter(consumerFactory, MUSIC_METADATA_TOPIC))
                .log(message -> MessageFormat.format("Received new message {0}", message.getPayload()))
                .channel(STORING_AUDIO_CHANNEL)
                .get();
    }

    @Bean
    public IntegrationFlow storeAudio() {
        return IntegrationFlows.from(STORING_AUDIO_CHANNEL)
                .transform(message -> {
                    AudioMetadata audioMetadata = (AudioMetadata) message;
                    AudioMetadataDTO audioMetadataDTO = AudioMetadataDTO.builder()
                            .id(audioMetadata.getId())
                            .source(audioMetadata.getSource())
                            .title(audioMetadata.getTitle())
                            .author(audioMetadata.getAuthor())
                            .status(AudioTranscriptionStatus.PROCESSING)
                            .build();
                    audioService.saveAudio(audioMetadataDTO);
                    return new GenericMessage<>(audioMetadataDTO);
                })
                .log(message -> MessageFormat.format("Saved audio {0} to ES for further processing", ((AudioMetadataDTO) message.getPayload()).getId()))
                .channel(UPLOAD_FILE_CHANNEL)
                .get();
    }

    @Bean
    public IntegrationFlow uploadFile() {
        return IntegrationFlows.from(UPLOAD_FILE_CHANNEL)
                .log(message -> MessageFormat.format("Uploading audio file {0} to AssemblyAI", ((AudioMetadataDTO) message.getPayload()).getSource()))
                .transform((Transformer) message -> {
                    AudioMetadataDTO audioMetadataDTO = (AudioMetadataDTO) message.getPayload();
                    byte[] fileBytes = getFileBytes(audioMetadataDTO.getSource());
                    UploadFileResponse uploadFileResponse = assemblyAIService.uploadFile(fileBytes);
                    Map<String, Object> headers = new HashMap<>();
                    headers.put(AUDIO_ID_HEADER, audioMetadataDTO.getId());
                    return new GenericMessage<>(uploadFileResponse, headers);
                })
                .log(message -> MessageFormat.format("Received file upload response {0}", message.getPayload()))
                .channel(START_TRANSCRIPTION_CHANNEL)
                .get();
    }

    @Bean
    public IntegrationFlow startTranscription() {
        return IntegrationFlows.from(START_TRANSCRIPTION_CHANNEL)
                .log(message -> MessageFormat.format("Starting transcription for audio {0}", message.getHeaders().get(AUDIO_ID_HEADER)))
                .handle((payload, headers) -> assemblyAIService.startTranscription((UploadFileResponse) payload, headers.get(AUDIO_ID_HEADER, String.class)))
                .nullChannel();
    }

    private byte[] getFileBytes(String source) {
        try (FileInputStream fileInputStream = new FileInputStream(source)) {
            return fileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file under path " + source, e);
        }
    }
}
