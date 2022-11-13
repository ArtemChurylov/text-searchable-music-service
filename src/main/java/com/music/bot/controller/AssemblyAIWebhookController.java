package com.music.bot.controller;

import com.music.bot.assemblyAI.AssemblyAIWebhookBody;
import com.music.bot.service.AudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.music.bot.controller.RestMapping.ASSEMBLY_AI_WEBHOOK_MAPPING;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ASSEMBLY_AI_WEBHOOK_MAPPING)
public class AssemblyAIWebhookController {

    private final AudioService audioService;

    @PostMapping
    public void handleTranscriptionResult(@RequestBody AssemblyAIWebhookBody body, @RequestHeader("audio_id") String audioId) {
        log.info("Received transcription result {} for audio {}", body.getStatus(), audioId);
        audioService.setLyrics(body, audioId);
    }
}
