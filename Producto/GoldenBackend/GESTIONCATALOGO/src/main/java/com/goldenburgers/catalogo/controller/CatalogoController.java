package com.goldenburgers.catalogo.controller;

import com.goldenburgers.catalogo.dto.CategoriaDTO;
import com.goldenburgers.catalogo.dto.ProductoDTO;
import com.goldenburgers.catalogo.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// este es el controlador, recibe las peticiones del cliente (Swagger)
// y las envía al service para que las procese
@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "API de gestión completa de catálogo de productos")
public class CatalogoController {

    // inyectamos el service, así podemos llamar a sus métodos
    private final CatalogoService catalogoService;

    // ===================================================================
    // consultas de productos - GET
    // ===================================================================

    @GetMapping("/productos")
    @Operation(summary = "Obtener todos los productos disponibles")
    public ResponseEntity<List<ProductoDTO>> obtenerProductos() {
        return ResponseEntity.ok(catalogoService.obtenerDisponibles());
    }

    @GetMapping("/productos/todos")
    @Operation(summary = "Obtener todos los productos (incluidos no disponibles)")
    public ResponseEntity<List<ProductoDTO>> obtenerTodosProductos() {
        return ResponseEntity.ok(catalogoService.obtenerTodos());
    }

    @GetMapping("/productos/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerPorId(id));
    }

    @GetMapping("/productos/categoria/{idCategoria}")
    @Operation(summary = "Obtener productos por categoría")
    public ResponseEntity<List<ProductoDTO>> obtenerPorCategoria(
            @Parameter(description = "ID de la categoría") @PathVariable Long idCategoria) {
        return ResponseEntity.ok(catalogoService.obtenerPorCategoria(idCategoria));
    }

    @GetMapping("/productos/buscar")
    @Operation(summary = "Buscar productos por nombre")
    public ResponseEntity<List<ProductoDTO>> buscarPorNombre(
            @Parameter(description = "Nombre del producto a buscar") @RequestParam String nombre) {
        return ResponseEntity.ok(catalogoService.buscarPorNombre(nombre));
    }

    // ===================================================================
    // crud de productos - CREATE, UPDATE, DELETE
    // ===================================================================

    // crear un producto nuevo - SOLO ADMIN
    @PostMapping("/productos")
    @Operation(summary = "Crear nuevo producto (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDTO> crearProducto(
            @Valid @RequestBody ProductoDTO productoDTO) {
        // llamamos al service para crear
        ProductoDTO nuevoProducto = catalogoService.crearProducto(productoDTO);
        // retornamos 201 (CREATED) porque se creó un nuevo recurso
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // actualizar un producto existente - TRABAJADOR o ADMIN
    @PutMapping("/productos/{id}")
    @Operation(summary = "Actualizar producto existente (ADMIN/TRABAJADOR)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoActualizado = catalogoService.actualizarProducto(id, productoDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    // eliminar un producto - SOLO ADMIN
    @DeleteMapping("/productos/{id}")
    @Operation(summary = "Eliminar producto (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        catalogoService.eliminarProducto(id);
        // retornamos 204 (NO CONTENT) porque se eliminó correctamente
        return ResponseEntity.noContent().build();
    }

    // cambiar si un producto está disponible o no - TRABAJADOR o ADMIN
    @PatchMapping("/productos/{id}/disponibilidad")
    @Operation(summary = "Cambiar disponibilidad del producto (ADMIN/TRABAJADOR)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<Void> cambiarDisponibilidad(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Parameter(description = "Nueva disponibilidad (true/false)") @RequestParam Boolean disponible) {
        catalogoService.cambiarDisponibilidad(id, disponible);
        return ResponseEntity.ok().build();
    }

    // ===================================================================
    // subir imagen del producto 
    // ===================================================================

    // este es el endpoint más importante
    // aqui es donde el usuario sube la imagen en Swagger - TRABAJADOR o ADMIN
    @PostMapping(value = "/productos/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen del producto (ADMIN/TRABAJADOR)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<Map<String, String>> subirImagenProducto(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Parameter(description = "Archivo de imagen") @RequestParam("imagen") MultipartFile file) {
        try {
            // validar que el archivo no esté vacío
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // validar que sea una imagen (jpg, png, webp, etc)
            // contentType es el tipo de archivo (image/jpeg, image/png, etc)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            // llamar al service para subir la imagen
            // el service hace todo el trabajo:
            // 1. busca el producto en BD
            // 2. sube la imagen a Firebase
            // 3. guarda la URL en la BD
            // 4. retorna el producto completo con la imagen
            ProductoDTO productoActualizado = catalogoService.subirImagenProducto(id, file);
            
            // extraer la URL de la imagen del producto retornado
            // el ProductoDTO ya tiene la imagen guardada
            String imageUrl = productoActualizado.getImagen();
            
            // crear la respuesta JSON que enviamos al cliente
            // la respuesta tiene dos campos: imageUrl y mensaje
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("mensaje", "imagen subida a firebase y guardada en base de datos");
            
            // retornar la respuesta al cliente
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // si hay error al leer el archivo, retornar error 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================================================================
    // consultas de categorías get
    // ===================================================================

    @GetMapping("/categorias")
    @Operation(summary = "Obtener todas las categorías")
    public ResponseEntity<List<CategoriaDTO>> obtenerCategorias() {
        return ResponseEntity.ok(catalogoService.obtenerTodasCategorias());
    }

    @GetMapping("/categorias/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoriaDTO> obtenerCategoriaPorId(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerCategoriaPorId(id));
    }

    // ===================================================================
    // crud de categorías 
    // ===================================================================

    // crear una categoría nueva - SOLO ADMIN
    @PostMapping("/categorias")
    @Operation(summary = "Crear nueva categoría (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaDTO> crearCategoria(
            @Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO nuevaCategoria = catalogoService.crearCategoria(categoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }

    // actualizar una categoría - TRABAJADOR o ADMIN
    @PutMapping("/categorias/{id}")
    @Operation(summary = "Actualizar categoría existente (ADMIN/TRABAJADOR)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(
            @Parameter(description = "ID de la categoría") @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoriaActualizada = catalogoService.actualizarCategoria(id, categoriaDTO);
        return ResponseEntity.ok(categoriaActualizada);
    }

    // eliminar una categoría - SOLO ADMIN
    @DeleteMapping("/categorias/{id}")
    @Operation(summary = "Eliminar categoría (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        catalogoService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // ===================================================================
    // verificar que el servicio esté funcionando
    // ===================================================================

    @GetMapping("/health")
    @Operation(summary = "Verificar estado del servicio")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Servicio de Catálogo funcionando correctamente");
    }
}