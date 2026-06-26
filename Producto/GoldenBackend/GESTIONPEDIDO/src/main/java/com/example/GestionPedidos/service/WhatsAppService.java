package com.example.GestionPedidos.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        logger.info("Twilio inicializado correctamente");
    }

    /**
     * Envía un mensaje de confirmación de pedido al cliente por WhatsApp.
     *
     * @param telefonoCliente número en formato internacional, ej: +56912345678
     * @param idPedido        ID del pedido confirmado
     * @param tiempoEspera    minutos estimados de espera
     */
    public void enviarConfirmacionPedido(String telefonoCliente, Long idPedido, int tiempoEspera) {
        try {
            String destinatario = "whatsapp:" + telefonoCliente;

            String mensaje = String.format(
                "¡Hola! 👋 Tu pedido *#%d* fue confirmado. " +
                "Estará listo en aproximadamente *%d minutos*. " +
                "¡Gracias por elegir Golden Burgers! 🍔",
                idPedido, tiempoEspera
            );

            Message message = Message.creator(
                new PhoneNumber(destinatario),
                new PhoneNumber(fromNumber),
                mensaje
            ).create();

            logger.info("WhatsApp enviado a {} — SID: {}", telefonoCliente, message.getSid());

        } catch (Exception e) {
            // No lanzamos excepción para no interrumpir el flujo principal del pedido
            logger.error("Error al enviar WhatsApp al cliente {}: {}", telefonoCliente, e.getMessage());
        }
    }
}