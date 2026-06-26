package com.example.Microservicio_Gestion_Venta.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Microservicio_Gestion_Venta.model.Boleta;
import com.example.Microservicio_Gestion_Venta.repository.BoletaRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional


public class BoletaService {

     @Autowired //para inyectar el repositorio
    private BoletaRepository boletaRepository;
    

    //CRUD

    public List<Boleta> listarBoleta() {
        return boletaRepository.findAll();
    }

    public Boleta guardarBoleta(Boleta boleta){
        return boletaRepository.save(boleta);
    }
    

    public void eliminarBoleta(Long id) {
        boletaRepository.deleteById(id);
    }

    public Boleta obtenerBoletaPorId(Long id) {
        return boletaRepository.findById(id).orElse(null);
    }

    
}
