package com.GoldenBurger.gestionContacto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@RestController
@RequestMapping("/api")
public class TestConexionController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/testConexion")
    public String testConexion() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbInfo = String.format(
                "Conexión exitosa a la base de datos: %s %s (Usuario: %s, URL: %s)",
                metaData.getDatabaseProductName(),
                metaData.getDatabaseProductVersion(),
                metaData.getUserName(),
                metaData.getURL()
            );
            return dbInfo;
        } catch (Exception e) {
            return "Error al conectar con la base de datos: " + e.getMessage();
        }
    }
}
