package com.example.Microservicio_Gestion_Venta.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Microservicio_Gestion_Venta.model.Devolucion;
import com.example.Microservicio_Gestion_Venta.repository.DevolucionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional

public class DevolucionService {

    @Autowired
    private  DevolucionRepository devolucionRepository;

    public List<Devolucion> listarDev() {
        return  devolucionRepository.findAll();
    }

    public Devolucion guardarDev(Devolucion devolucion){
        return devolucionRepository.save(devolucion);
    }
    

    public void eliminarDev(Long id) {
        devolucionRepository.deleteById(id);
    }

    public Devolucion obtenerDevolucionPorId(Long id) {
        return devolucionRepository.findById(id).orElse(null);
    }    

     


}
