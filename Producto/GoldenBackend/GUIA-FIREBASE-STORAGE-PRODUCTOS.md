# 🔥 Guía: Integración Firebase Storage para Productos

**Proyecto:** Golden Burgers - GESTIONCATALOGO  
**Objetivo:** Subir imágenes de productos a Firebase Storage y guardar URL en base de datos  
**Fecha:** 9 de noviembre de 2025

---

## ✅ Verificación Previa (YA COMPLETADO)

- ✅ Cloud Storage API habilitada en Google Cloud
- ✅ Service Account con rol "Administrador de almacenamiento"
- ✅ Bucket creado: `goldenburgers-60680.firebasestorage.app`
- ✅ firebase-credentials.json disponible en API-GATEWAY

---

## 📋 PASO 1: Agregar Dependencias al pom.xml

**Archivo:** `GESTIONCATALOGO/pom.xml`

```xml
<!-- Firebase Admin SDK para Storage -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.4.2</version>
</dependency>

<!-- Apache Commons IO para manejo de archivos -->
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.11.0</version>
</dependency>
```

**Agregar después de las dependencias de Spring Boot.**

---

## 📋 PASO 2: Copiar firebase-credentials.json

**Comando desde la raíz del proyecto:**

```bash
cp API-GATEWAY/src/main/resources/firebase-credentials.json \
   GESTIONCATALOGO/src/main/resources/firebase-credentials.json
```

**O manualmente:**
1. Copiar archivo de: `API-GATEWAY/src/main/resources/firebase-credentials.json`
2. Pegar en: `GESTIONCATALOGO/src/main/resources/firebase-credentials.json`

---

## 📋 PASO 3: Configurar application.properties

**Archivo:** `GESTIONCATALOGO/src/main/resources/application.properties`

```properties
spring.application.name=gestionCatalogo

# ====================================================================
# SERVIDOR
# ====================================================================
server.port=8084

# ====================================================================
# FIREBASE STORAGE
# ====================================================================
firebase.credentials.path=classpath:firebase-credentials.json
firebase.storage.bucket=goldenburgers-60680.firebasestorage.app

# ====================================================================
# CONFIGURACION DE ARCHIVOS
# ====================================================================
# Tamaño máximo de archivo (5MB)
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# ====================================================================
# JWT - VALIDACION DE TOKENS INTERNOS
# ====================================================================
jwt.secret=${JWT_SECRET:TU_CLAVE_SECRETA_SUPER_SEGURA_MINIMO_256_BITS_CAMBIAR_EN_PRODUCCION}

# ====================================================================
# BASE DE DATOS ORACLE
# ====================================================================
spring.datasource.url=${DB_URL:jdbc:oracle:thin:@localhost:1521:XE}
spring.datasource.username=${DB_USERNAME:system}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect
spring.jpa.properties.hibernate.format_sql=true

# ====================================================================
# LOGGING
# ====================================================================
logging.level.root=INFO
logging.level.com.example.gestioncatalogo=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## 📋 PASO 4: Actualizar modelo Producto

**Archivo:** `Producto.java` (en el paquete model)

```java
package com.example.gestioncatalogo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "productos")
@Data
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private Double precio;
    
    private String descripcion;
    
    private String categoria;
    
    private Integer stock;
    
    // ⭐ NUEVA COLUMNA: URL de la imagen en Firebase Storage
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
}
```

**IMPORTANTE:** Agregar columna en la base de datos:

```sql
ALTER TABLE productos ADD imagen_url VARCHAR2(500);
```

---

## 📋 PASO 5: Crear FirebaseStorageService

**Archivo:** `GESTIONCATALOGO/src/main/java/.../service/FirebaseStorageService.java`

```java
package com.example.gestioncatalogo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    private Bucket bucket;

    // Tipos de archivos permitidos
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    // Tamaño máximo: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostConstruct
    public void initialize() throws IOException {
        // Inicializar Firebase Admin SDK
        InputStream serviceAccount = new ClassPathResource(
            credentialsPath.replace("classpath:", "")
        ).getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(bucketName)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        // Obtener referencia al bucket
        this.bucket = StorageClient.getInstance().bucket();
    }

    /**
     * Sube una imagen al Firebase Storage y retorna la URL pública
     * 
     * @param file Archivo MultipartFile del frontend
     * @param folder Carpeta dentro del bucket (ej: "productos", "categorias")
     * @return URL pública de la imagen
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // Validar archivo vacío
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Archivo muy grande. Máximo 5MB");
        }

        // Validar tipo de imagen
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Solo se permiten imágenes (JPG, PNG, WEBP, GIF)");
        }

        // Generar nombre único para evitar sobrescrituras
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = folder + "/" + UUID.randomUUID() + extension;

        // Subir archivo
        Blob blob = bucket.create(fileName, file.getInputStream(), contentType);

        // Hacer el archivo público (lectura para todos)
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        // Retornar URL pública
        return String.format(
            "https://storage.googleapis.com/%s/%s",
            bucketName,
            fileName
        );
    }

    /**
     * Elimina una imagen del Storage
     * 
     * @param imageUrl URL completa de la imagen
     */
    public void deleteImage(String imageUrl) {
        try {
            // Extraer nombre del archivo de la URL
            String fileName = imageUrl.substring(
                imageUrl.indexOf(bucketName) + bucketName.length() + 1
            );
            
            BlobId blobId = BlobId.of(bucketName, fileName);
            Blob blob = bucket.get(blobId.getName());
            
            if (blob != null) {
                blob.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar imagen: " + e.getMessage());
        }
    }

    /**
     * Actualiza una imagen: elimina la anterior y sube la nueva
     */
    public String updateImage(String oldImageUrl, MultipartFile newFile, String folder) throws IOException {
        // Eliminar imagen anterior si existe y no es placeholder
        if (oldImageUrl != null && !oldImageUrl.contains("placeholder")) {
            deleteImage(oldImageUrl);
        }
        
        // Subir nueva imagen
        return uploadImage(newFile, folder);
    }
}
```

---

## 📋 PASO 6: Actualizar ProductoController

**Archivo:** `ProductoController.java`

```java
package com.example.gestioncatalogo.controller;

import com.example.gestioncatalogo.model.Producto;
import com.example.gestioncatalogo.service.ProductoService;
import com.example.gestioncatalogo.service.FirebaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    /**
     * Crear producto CON imagen
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> crearProducto(
        @RequestParam("nombre") String nombre,
        @RequestParam("precio") Double precio,
        @RequestParam("descripcion") String descripcion,
        @RequestParam("categoria") String categoria,
        @RequestParam("stock") Integer stock,
        @RequestParam("imagen") MultipartFile imagen
    ) {
        try {
            // 1. Subir imagen a Firebase Storage
            String imageUrl = firebaseStorageService.uploadImage(imagen, "productos");

            // 2. Crear objeto Producto
            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setDescripcion(descripcion);
            producto.setCategoria(categoria);
            producto.setStock(stock);
            producto.setImagenUrl(imageUrl);

            // 3. Guardar en base de datos
            Producto nuevoProducto = productoService.guardar(producto);

            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear producto: " + e.getMessage()));
        }
    }

    /**
     * Listar todos los productos
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    /**
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        try {
            Producto producto = productoService.obtenerPorId(id);
            return ResponseEntity.ok(producto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualizar solo la imagen de un producto
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}/imagen")
    public ResponseEntity<?> actualizarImagen(
        @PathVariable Long id,
        @RequestParam("imagen") MultipartFile imagen
    ) {
        try {
            Producto producto = productoService.obtenerPorId(id);

            // Actualizar imagen (elimina anterior, sube nueva)
            String nuevaImagenUrl = firebaseStorageService.updateImage(
                producto.getImagenUrl(), 
                imagen, 
                "productos"
            );
            
            producto.setImagenUrl(nuevaImagenUrl);
            Producto productoActualizado = productoService.guardar(producto);
            
            return ResponseEntity.ok(productoActualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar producto completo
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
        @PathVariable Long id,
        @RequestBody Producto producto
    ) {
        try {
            Producto productoExistente = productoService.obtenerPorId(id);
            
            productoExistente.setNombre(producto.getNombre());
            productoExistente.setPrecio(producto.getPrecio());
            productoExistente.setDescripcion(producto.getDescripcion());
            productoExistente.setCategoria(producto.getCategoria());
            productoExistente.setStock(producto.getStock());
            // imagenUrl NO se actualiza aquí, usar endpoint /imagen
            
            Producto actualizado = productoService.guardar(productoExistente);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Eliminar producto (también elimina la imagen)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            Producto producto = productoService.obtenerPorId(id);

            // Eliminar imagen de Firebase Storage
            if (producto.getImagenUrl() != null && !producto.getImagenUrl().contains("placeholder")) {
                firebaseStorageService.deleteImage(producto.getImagenUrl());
            }

            // Eliminar producto de BD
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
```

---

## 🧪 PASO 7: Probar con Postman

### **Test 1: Crear producto con imagen**

```
POST http://localhost:8084/api/productos
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data

Body (form-data):
- nombre: "Hamburguesa Clásica"
- precio: 5990
- descripcion: "Deliciosa hamburguesa con queso"
- categoria: "Hamburguesas"
- stock: 50
- imagen: [Seleccionar archivo .jpg/.png]
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "nombre": "Hamburguesa Clásica",
  "precio": 5990,
  "descripcion": "Deliciosa hamburguesa con queso",
  "categoria": "Hamburguesas",
  "stock": 50,
  "imagenUrl": "https://storage.googleapis.com/goldenburgers-60680.firebasestorage.app/productos/abc123.jpg"
}
```

### **Test 2: Listar productos**

```
GET http://localhost:8084/api/productos
```

### **Test 3: Actualizar solo imagen**

```
PUT http://localhost:8084/api/productos/1/imagen
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data

Body (form-data):
- imagen: [Nueva imagen]
```

---

## 📂 Estructura del Storage

Después de subir imágenes, la estructura en Firebase Storage será:

```
goldenburgers-60680.firebasestorage.app/
├── productos/
│   ├── abc123-uuid.jpg
│   ├── def456-uuid.png
│   └── ...
```

---

## ⚠️ Errores Comunes y Soluciones

### Error 1: "Permission denied"
```
Causa: Service account sin permisos
Solución: Ya verificado ✅ (tienes "Administrador de almacenamiento")
```

### Error 2: "Bucket does not exist"
```
Causa: Nombre de bucket incorrecto en application.properties
Solución: Verificar que sea: goldenburgers-60680.firebasestorage.app
```

### Error 3: "File too large"
```
Causa: Archivo mayor a 5MB
Solución: Redimensionar imagen antes de subir
```

### Error 4: "Invalid file type"
```
Causa: Intentando subir archivo que no es imagen
Solución: Solo permitir JPG, PNG, WEBP, GIF
```

---

## 🔒 Seguridad Adicional (Opcional)

### Reglas de Firebase Storage

En Firebase Console > Storage > Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Solo lectura pública
    match /{allPaths=**} {
      allow read: if true;
      allow write: if false; // Solo backend puede escribir
    }
  }
}
```

---

## ✅ Checklist Final

```
□ Dependencias agregadas al pom.xml
□ firebase-credentials.json copiado
□ application.properties configurado
□ Columna imagen_url agregada en BD
□ Modelo Producto actualizado
□ FirebaseStorageService creado
□ ProductoController actualizado
□ Compilar proyecto: mvn clean compile
□ Probar endpoints con Postman
□ Verificar imágenes en Firebase Storage
```

---

## 📞 Soporte

Si tienes problemas:
1. Verificar logs del backend
2. Verificar Firebase Console > Storage
3. Verificar permisos en Google Cloud Console > IAM

---

**¡Todo listo para subir imágenes de productos! 🚀**
