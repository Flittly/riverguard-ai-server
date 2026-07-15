package io.riverguard.module.ai.controller;

import io.agentscope.core.event.TextBlockDeltaEvent;
import io.riverguard.module.ai.dto.ChatRequest;
import io.riverguard.module.ai.service.AiAgentManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiAgentManager aiAgentManager;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid ChatRequest request, Principal principal) {
        String sessionId = request.getSessionId() != null
                ? principal.getName() + "-" + request.getSessionId()
                : principal.getName() + "-" + UUID.randomUUID();

        SseEmitter emitter = new SseEmitter(300000L);

        aiAgentManager.chatStream(sessionId, request.getMessage())
                .subscribe(
                        event -> {
                            try {
                                if (event instanceof TextBlockDeltaEvent e && e.getDelta() != null) {
                                    emitter.send(SseEmitter.event()
                                            .name("delta")
                                            .data(e.getDelta()));
                                }
                            } catch (IOException ex) {
                                log.warn("SSE send failed: {}", ex.getMessage());
                            }
                        },
                        error -> {
                            log.error("Chat stream error for session {}: {}", sessionId, error.getMessage());
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            } catch (IOException ignored) {
                            }
                            emitter.complete();
                        }
                );

        return emitter;
    }
}
