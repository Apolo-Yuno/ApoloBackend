package com.hackathon.yuno.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackIntegrationService {

    @Value("${slack.bot.token}")
    private String botToken;

    private final App app;
    private final SocketModeApp socketModeApp;
    private final MerchantService merchantService;
    private final SlackMessageAnalyzerService messageAnalyzerService;

    @EventListener(ApplicationReadyEvent.class)
    public void startSlackListener() {
        try {
            registerEvents();
            socketModeApp.startAsync();
            log.info("✅ Slack Socket Mode App iniciado correctamente");
        } catch (Exception e) {
            log.error("❌ Error iniciando Slack Integration", e);
        }
    }

    private void registerEvents() {
        app.event(MessageEvent.class, (req, ctx) -> {
            MessageEvent event = req.getEvent();

            // Ignorar mensajes de bots
            if (event.getBotId() != null) {
                return ctx.ack();
            }

            // Procesamiento asíncrono
            CompletableFuture.runAsync(() -> processMessage(event, ctx));

            // Respuesta inmediata a Slack
            return ctx.ack();
        });
    }

    private void processMessage(MessageEvent event, com.slack.api.bolt.context.builtin.EventContext ctx) {
        try {
            String text = event.getText();
            String user = event.getUser();
            String channelId = event.getChannel();
            String threadTs = event.getThreadTs() != null ? event.getThreadTs() : event.getTs();

            log.info("📩 Mensaje recibido de {} en {}", user, channelId);

            // Reacción de procesamiento
            ctx.client().reactionsAdd(r -> r.token(botToken).channel(channelId).timestamp(event.getTs()).name("eyes"));

            // Descargar archivo
            InputStream fileStream = null;
            String fileName = null;
            if (event.getFiles() != null && !event.getFiles().isEmpty()) {
                String downloadUrl = event.getFiles().get(0).getUrlPrivate();
                fileName = event.getFiles().get(0).getName();
                log.info("📎 Archivo detectado: {}", fileName);
                fileStream = downloadFileFromSlack(downloadUrl);
            }

            // Guardar en BD
            merchantService.proccessFromEmail("SlackUser-" + user, text, fileStream);

            // Análisis de IA
            String styledMessage = messageAnalyzerService.analyzeAndBuildResponse(text, fileName, user);

            // Enviar respuesta
            ctx.client().chatPostMessage(r -> r
                    .token(botToken)
                    .channel(channelId)
                    .threadTs(threadTs)
                    .mrkdwn(true)
                    .text(styledMessage)
            );

            // Cambiar reacción
            ctx.client().reactionsRemove(r -> r.token(botToken).channel(channelId).timestamp(event.getTs()).name("eyes"));
            ctx.client().reactionsAdd(r -> r.token(botToken).channel(channelId).timestamp(event.getTs()).name("white_check_mark"));

            log.info("✅ Mensaje procesado correctamente");

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje de Slack", e);
            try {
                ctx.client().chatPostMessage(r -> r.token(botToken).channel(event.getChannel()).text("⚠️ Error procesando solicitud"));
            } catch (Exception ex) {
                log.error("Error enviando mensaje de error", ex);
            }
        }
    }

    private InputStream downloadFileFromSlack(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + botToken);
            return new ByteArrayInputStream(connection.getInputStream().readAllBytes());
        } catch (Exception e) {
            log.error("Error descargando archivo de Slack", e);
            return null;
        }
    }
}
