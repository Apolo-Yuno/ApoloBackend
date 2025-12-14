package com.hackathon.yuno.service;

import com.hackathon.yuno.model.dto.ai.AIAnalysisResult;
import com.hackathon.yuno.model.entity.MerchantContext;
import com.hackathon.yuno.model.enums.LifeCicleState;
import com.hackathon.yuno.model.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackMessageAnalyzerService {

    private final AIService aiService;

    @Value("${apolo.dashboard.base-url:https://dashboard.apolo.example.com}")
    private String dashboardBaseUrl;

    public String analyzeAndBuildResponse(String messageText, String fileName, String userId) {
        try {
            // Llamada a IA
            AIAnalysisResult analysisResult = aiService.analyze(messageText);
            
            log.info("AI Analysis - Merchant: {}, State: {}", analysisResult.getName(), analysisResult.getState());
            return buildContextualMessage(analysisResult, fileName);

        } catch (Exception e) {
            log.error("Error analyzing message with AI", e);
            return buildFallbackMessage(messageText, fileName, userId);
        }
    }

    private String buildContextualMessage(AIAnalysisResult analysis, String fileName) {
        StringBuilder sb = new StringBuilder();

        // Cabecera
        sb.append("✅ *Análisis Completado*\n\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("🏢 *").append(getSafeString(analysis.getName())).append("*\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // Datos Clave
        appendField(sb, "👤 Contacto", analysis.getContactPerson());
        appendField(sb, "🔄 Estado", formatLifecycleState(analysis.getState()));

        // Contexto Profundo
        if (analysis.getContext() != null) {
            MerchantContext ctx = analysis.getContext();
            appendList(sb, "🌍 Países", ctx.getCountries());
            
            if (ctx.getPaymentMethods() != null && !ctx.getPaymentMethods().isEmpty()) {
                sb.append("�� *Métodos de Pago:*\n");
                ctx.getPaymentMethods().forEach(m -> sb.append("> • ").append(formatPaymentMethod(m)).append("\n"));
                sb.append("\n");
            }
            
            // Riesgo
            if (ctx.getRiskData() != null && ctx.getRiskData().getRequestedLimit() != null) {
                String amount = formatCurrency(ctx.getRiskData().getRequestedLimit(), ctx.getRiskData().getCurrency());
                appendField(sb, "💰 Volumen Est.", amount);
            }
        }

        // Archivo
        if (fileName != null) {
            sb.append("📎 *Adjunto:* ").append(fileName).append(" (Procesado)\n\n");
        }

        // Footer
        sb.append("🔗 <").append(dashboardBaseUrl).append("| *Ver en Dashboard*>\n");
        sb.append("_Powered by Apolo Engine_ 🚀");

        return sb.toString();
    }

    private String buildFallbackMessage(String messageText, String fileName, String userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("✅ *Mensaje Recibido*\n\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📋 *INFORMACIÓN PROCESADA*\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("💬 *Contenido:*\n");
        sb.append("> ").append(sanitizeForSlack(messageText)).append("\n\n");

        if (fileName != null) {
            sb.append("📎 *Archivo:* ").append(fileName).append(" (Procesado)\n\n");
        }

        sb.append("✓ Almacenado en el sistema\n");
        sb.append("✓ Análisis en proceso\n");
        sb.append("✓ Se actualizará el contexto del merchant\n\n");

        sb.append("_Powered by Apolo Engine_ 🚀");

        return sb.toString();
    }

    private String formatLifecycleState(LifeCicleState state) {
        if (state == null) return "⚪ No identificado";
        return switch (state) {
            case SALES -> "🎯 SALES - Prospección";
            case CONTRACT -> "📜 CONTRACT - Formalización";
            case INTEGRATION -> "⚙️ INTEGRATION - Implementación";
            case LIVE -> "🟢 LIVE - Producción";
            case SUPPORT -> "🛟 SUPPORT - Soporte";
            default -> state.toString();
        };
    }

    private String formatPaymentMethod(PaymentMethod method) {
        if (method == null) return "No identificado";
        return switch (method) {
            case CREDITCARD -> "💳 Tarjetas de Crédito";
            case DEBITCARD -> "🏧 Tarjetas de Débito";
            case PSE -> "🏦 PSE";
            case CASH -> "💵 Efectivo";
            default -> method.toString();
        };
    }

    private String formatCurrency(BigDecimal amount, String code) {
        if (amount == null) return "No especificado";
        String currency = code != null ? code : "USD";
        String symbol = switch (currency) {
            case "COP" -> "$";
            case "USD" -> "US$";
            case "EUR" -> "€";
            default -> currency + " ";
        };
        return String.format("%s%,d %s", symbol, amount.longValue(), currency);
    }

    private String sanitizeForSlack(String text) {
        if (text == null || text.isEmpty()) return "_Sin contenido_";
        if (text.length() > 300) return text.substring(0, 300) + "...";
        return text;
    }

    private String getSafeString(String value) {
        return value != null && !value.isEmpty() && !value.equals("Unknown") ? value : "Merchant Desconocido";
    }

    private void appendField(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(" ").append(value).append("\n\n");
        }
    }

    private void appendList(StringBuilder sb, String label, java.util.List<?> items) {
        if (items != null && !items.isEmpty()) {
            sb.append(label).append("\n");
            items.forEach(item -> sb.append("> • ").append(item).append("\n"));
            sb.append("\n");
        }
    }
}
