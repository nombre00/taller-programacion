package com.goldenburgers.catalogo.service;

import com.goldenburgers.catalogo.dto.CategoriaDTO;
import com.goldenburgers.catalogo.dto.ProductoDTO;
import com.goldenburgers.catalogo.model.CategoriaProducto;
import com.goldenburgers.catalogo.model.Producto;
import com.goldenburgers.catalogo.repository.CategoriaProductoRepository;
import com.goldenburgers.catalogo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ================================================================================
// SERVICIO DE CATÁLOGO - LÓGICA DE NEGOCIOS
// ================================================================================
// 
// Este Service es RESPONSABLE de:
// Ejecutar la lógica de negocios
// Comunicarse con la BD (mediante CategoriaProductoRepository)
// Trabajar con Firebase Storage
// Convertir entre Entidades y DTOs

// ================================================================================

@Slf4j  // Permite usar "log.info()", "log.error()", etc. para registrar eventos
@Service  // Le dice a Spring que esto es un Servicio (puede ser inyectado en otros)
@RequiredArgsConstructor  // Genera automáticamente el constructor con los parámetros final
public class CatalogoService {

    // ========== INYECCIONES DE DEPENDENCIAS ==========
    // Estos son los "servicios" que usa este Service
    
    private final ProductoRepository productoRepository;  // Para hablar con la BD de Productos
    private final CategoriaProductoRepository categoriaRepository;  // Para hablar con la BD de Categorías
    private final FirebaseStorageService firebaseStorageService;  // Para subir archivos a Firebase

    // ========== CONSTANTES ==========
    // Mapeo de categorías (ID → Nombre)
    private static final Map<Long, String> CATEGORIAS = Map.of(
        1L, "Burgers",
        2L, "Combos",
        3L, "Refrescos",
        4L, "Acompañamientos",
        5L, "Kids"
    );

    // ========================================================================
    // OPERACIONES DE LECTURA (GET) - SOLO LECTURA DE BD
    // ========================================================================

    // OBTENER TODOS LOS PRODUCTOS
    @Transactional(readOnly = true)  // Solo lectura = no modifica BD
    public List<ProductoDTO> obtenerTodos() {
        // Traer todos los productos de la BD
        return productoRepository.findAll()
                // Convertir cada Producto a ProductoDTO (para enviar al cliente)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    //  OBTENER SOLO PRODUCTOS DISPONIBLES
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerDisponibles() {
        // Buscar productos donde disponible = 1 (disponible)
        return productoRepository.findByDisponible(1)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    //  OBTENER PRODUCTOS POR CATEGORÍA
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerPorCategoria(Long idCategoria) {
        // Buscar productos de una categoría específica que estén disponibles
        return productoRepository.findByIdCategoriaAndDisponible(idCategoria, 1)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // BUSCAR PRODUCTOS POR NOMBRE
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        // Buscar productos cuyo nombre contenga el texto (sin importar mayúsculas/minúsculas)
        return productoRepository.findByNombreProductoContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // OBTENER UN PRODUCTO POR ID
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        // Buscar producto por ID
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return convertirADTO(producto);
    }

    // ========================================================================
    //CREATE, UPDATE, DELETE - MODIFICA BD
    // ========================================================================

    // CREAR NUEVO PRODUCTO
    @Transactional  // Transacción = o se guarda TODO o NO se guarda nada
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        log.info("Creando nuevo producto: {}", productoDTO.getNombre());
        
        // Crear objeto Producto vacío
        Producto producto = new Producto();
        
        // Asignar datos del DTO al producto (DTO es lo que viene del cliente)
        producto.setNombreProducto(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecioBase(productoDTO.getPrecio());
        producto.setIdCategoria(productoDTO.getIdCategoria());
        producto.setImagenUrl(productoDTO.getImagen());
        
        // Convertir disponible (true/false) a número (1/0)
        producto.setDisponible(productoDTO.getDisponible() != null && productoDTO.getDisponible() ? 1 : 0);

        // GUARDAR EN BD ORACLE
        Producto productoGuardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", productoGuardado.getIdProducto());
        
        // Convertir a DTO y retornar
        return convertirADTO(productoGuardado);
    }

    // ACTUALIZAR PRODUCTO EXISTENTE
    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
        log.info("Actualizando producto ID: {}", id);
        
        // PASO 1: Buscar el producto en BD
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // PASO 2: Actualizar los datos
        producto.setNombreProducto(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecioBase(productoDTO.getPrecio());
        producto.setIdCategoria(productoDTO.getIdCategoria());
        
        // Solo actualizar imagen si viene en la solicitud
        if (productoDTO.getImagen() != null && !productoDTO.getImagen().isEmpty()) {
            producto.setImagenUrl(productoDTO.getImagen());
        }
        
        producto.setDisponible(productoDTO.getDisponible() != null && productoDTO.getDisponible() ? 1 : 0);

        // PASO 3: Guardar cambios en BD
        Producto productoActualizado = productoRepository.save(producto);
        log.info("Producto actualizado exitosamente: {}", productoActualizado.getIdProducto());
        
        return convertirADTO(productoActualizado);
    }

    // ELIMINAR PRODUCTO
    @Transactional
    public void eliminarProducto(Long id) {
        log.info("Eliminando producto ID: {}", id);
        
        // Buscar producto
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Si tiene imagen, eliminarla de Firebase también
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
            try {
                firebaseStorageService.eliminarImagen(producto.getImagenUrl());
            } catch (Exception e) {
                log.warn("No se pudo eliminar la imagen del producto: {}", e.getMessage());
            }
        }

        // Eliminar de BD Oracle
        productoRepository.delete(producto);
        log.info("Producto eliminado exitosamente: {}", id);
    }

    // CAMBIAR DISPONIBILIDAD DEL PRODUCTO
    @Transactional
    public void cambiarDisponibilidad(Long id, Boolean disponible) {
        log.info("Cambiando disponibilidad del producto ID: {} a {}", id, disponible);
        
        // Buscar producto
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Cambiar el estado (1 = disponible, 0 = no disponible)
        producto.setDisponible(disponible ? 1 : 0);
        productoRepository.save(producto);
        
        log.info("Disponibilidad actualizada exitosamente");
    }

    // ========================================================================
    // SUBIR IMAGEN A FIREBASE Y A BD
    // ========================================================================
    
    //  FLUJO:
    //
    // 1. Usuario sube archivo en Swagger
    //    ↓
    // 2. El Controller recibe MultipartFile
    //    ↓
    // 3. El Controller llama a este método
    //    ↓
    // 4. ESTE MÉTODO:
    //    a) Busca el producto en BD Oracle
    //    b) Sube la imagen a Firebase Storage
    //    c) Obtiene la URL de Firebase
    //    d) Guarda la URL en BD Oracle
    //    e) Retorna el Producto COMPLETO con la imagen
    //
    @Transactional  
    public ProductoDTO subirImagenProducto(Long id, MultipartFile file) throws IOException {
        log.info("Subiendo imagen para producto ID: {}", id);
        
        // PASO 1: Buscar el producto en BD Oracle
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // PASO 2: Si ya tiene una imagen antigua, eliminarla de Firebase
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
            try {
                firebaseStorageService.eliminarImagen(producto.getImagenUrl());
                log.info("Imagen anterior eliminada de Firebase");
            } catch (Exception e) {
                log.warn("No se pudo eliminar la imagen anterior: {}", e.getMessage());
            }
        }
        
        // PASO 3: SUBIR NUEVA IMAGEN A FIREBASE STORAGE
        String imageUrl = firebaseStorageService.subirImagen(file);
        log.info("Imagen subida a Firebase: {}", imageUrl);
        
        // PASO 4: GUARDAR LA URL EN EL OBJETO PRODUCTO
        producto.setImagenUrl(imageUrl);
        
        // PASO 5: GUARDAR EL PRODUCTO EN BD ORACLE
        // Aquí es donde la transacción @Transactional GUARDA los cambios en BD
        Producto productoActualizado = productoRepository.save(producto);
        log.info("URL guardada en BD Oracle");
        
        // PASO 6: RETORNAR EL PRODUCTO COMPLETO (con la imagen)
        // El Controller extrae la URL de aquí y la retorna al cliente
        return convertirADTO(productoActualizado);
    }

    // ========================================================================
    // OPERACIONES CON CATEGORÍAS
    // ========================================================================

    @Transactional(readOnly = true)
    public List<CategoriaDTO> obtenerTodasCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirCategoriaADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtenerCategoriaPorId(Long id) {
        CategoriaProducto categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return convertirCategoriaADTO(categoria);
    }

    @Transactional
    public CategoriaDTO crearCategoria(CategoriaDTO categoriaDTO) {
        log.info("Creando nueva categoría: {}", categoriaDTO.getNombre());
        
        // Verificar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombreCategoria(categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        CategoriaProducto categoria = new CategoriaProducto();
        categoria.setNombreCategoria(categoriaDTO.getNombre());
        categoria.setDescripcion(categoriaDTO.getDescripcion());

        CategoriaProducto categoriaGuardada = categoriaRepository.save(categoria);
        log.info("Categoría creada exitosamente con ID: {}", categoriaGuardada.getIdCategoria());
        
        return convertirCategoriaADTO(categoriaGuardada);
    }

    @Transactional
    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoriaDTO) {
        log.info("Actualizando categoría ID: {}", id);
        
        CategoriaProducto categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Verificar que otro no tenga el mismo nombre
        if (!categoria.getNombreCategoria().equals(categoriaDTO.getNombre()) &&
            categoriaRepository.existsByNombreCategoria(categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        categoria.setNombreCategoria(categoriaDTO.getNombre());
        categoria.setDescripcion(categoriaDTO.getDescripcion());

        CategoriaProducto categoriaActualizada = categoriaRepository.save(categoria);
        log.info("Categoría actualizada exitosamente");
        
        return convertirCategoriaADTO(categoriaActualizada);
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        log.info("Eliminando categoría ID: {}", id);
        
        CategoriaProducto categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Verificar que no haya productos en esta categoría
        List<Producto> productos = productoRepository.findByIdCategoria(id);
        if (!productos.isEmpty()) {
            throw new RuntimeException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoriaRepository.delete(categoria);
        log.info("Categoría eliminada exitosamente");
    }

    // ========================================================================
    // MÉTODOS AUXILIARES - CONVERSIÓN ENTRE ENTIDADES Y DTOs
    // ========================================================================
    // Se usa para:
    // No enviar la ENTIDAD completa al cliente (con datos sensibles)
    // Independizar el cliente de los cambios en la entidad
    //
    // Ejemplo: Si la entidad Producto tiene un campo "costo" (costo de producción),
    // el DTO ProductoDTO NO lo incluye (es información confidencial)
    //

    // CONVERTIR Producto (entidad) → ProductoDTO (para enviar al cliente)
    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getIdProducto());
        dto.setNombre(producto.getNombreProducto());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecioBase());
        dto.setIdCategoria(producto.getIdCategoria());
        dto.setCategoria(CATEGORIAS.get(producto.getIdCategoria()));  // Obtener nombre de categoría del mapa
        dto.setImagen(producto.getImagenUrl());
        dto.setDisponible(producto.getDisponible() == 1);  // Convertir 1/0 a true/false
        return dto;
    }

    // CONVERTIR CategoriaProducto (entidad) → CategoriaDTO (para enviar al cliente)
    private CategoriaDTO convertirCategoriaADTO(CategoriaProducto categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombreCategoria());
        dto.setDescripcion(categoria.getDescripcion());
        return dto;
    }
}