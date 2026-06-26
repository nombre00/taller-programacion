package com.GoldenBurger.gestionContacto.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MENSAJECONTACTO")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MENSAJE_SEQ")
    @SequenceGenerator(
        name = "MENSAJE_SEQ",
        sequenceName = "ISEQ$$_155974", // usa la secuencia real de Oracle
        allocationSize = 1
    )
    @Column(name = "ID_MENSAJE", updatable = false, nullable = false)
    private Long idMensaje;

    @Column(name = "NOMBRE_REMITENTE", nullable = false, length = 255)
    private String nombreRemitente;

    @Column(name = "EMAIL_REMITENTE", nullable = false, length = 255)
    private String emailRemitente;

    @Column(name = "ASUNTO", length = 255)
    private String asunto;

    @Column(name = "MENSAJE", nullable = false, length = 4000)
    private String mensaje;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_RECIBIDO", nullable = false)
    private Date fechaRecibido = new Date();

    @Column(name = "LEIDO", nullable = false)
    private Integer leido = 0;

    public MensajeContacto() {}

    public MensajeContacto(String nombreRemitente, String emailRemitente, String asunto, String mensaje) {
        this.nombreRemitente = nombreRemitente;
        this.emailRemitente = emailRemitente;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaRecibido = new Date();
        this.leido = 0;
    }



    // ===========================
    // GETTERS Y SETTERS
    // ===========================
    public Long getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Long idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getNombreRemitente() {
        return nombreRemitente;
    }

    public void setNombreRemitente(String nombreRemitente) {
        this.nombreRemitente = nombreRemitente;
    }

    public String getEmailRemitente() {
        return emailRemitente;
    }

    public void setEmailRemitente(String emailRemitente) {
        this.emailRemitente = emailRemitente;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Date getFechaRecibido() {
        return fechaRecibido;
    }

    public void setFechaRecibido(Date fechaRecibido) {
        this.fechaRecibido = fechaRecibido;
    }

    public Integer getLeido() {
        return leido;
    }

    public void setLeido(Integer leido) {
        this.leido = leido;
    }

    // ===========================
    // MÉTODO AUTOMÁTICO
    // ===========================
    @PrePersist
    protected void onCreate() {
        if (this.fechaRecibido == null) {
            this.fechaRecibido = new Date();
        }
        if (this.leido == null) {
            this.leido = 0;
        }
    }
}
