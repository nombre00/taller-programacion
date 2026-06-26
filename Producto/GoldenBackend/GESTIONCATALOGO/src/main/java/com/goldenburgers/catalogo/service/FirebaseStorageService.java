package com.goldenburgers.catalogo.service;

//import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

// este service se encarga de todo lo relacionado con Firebase Storage
// es decir, subir archivos, eliminar archivos, etc
@Slf4j
@Service
public class FirebaseStorageService {

    // nombre del bucket donde guardamos las imágenes
    // un bucket es como una carpeta en la nube de Firebase
    private static final String BUCKET_NAME = "goldenburgers-60680.firebasestorage.app";

    // nombre de la carpeta dentro del bucket donde guardamos
    // así todas las imágenes van en /img/
    private static final String FOLDER_NAME = "img/";

    // el cliente de Firebase Storage
    // esto lo da Spring automáticamente (inyección de dependencias)
    private final Storage storage;

    // constructor donde recibimos el Storage
    // Spring nos lo proporciona automáticamente
    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    // ===================================================================
    // SUBIR IMAGEN A FIREBASE STORAGE
    // ===================================================================

    // este método recibe un archivo que sube el usuario
    // y lo guarda en Firebase Storage
    public String subirImagen(MultipartFile file) throws IOException {
        try {
            // crea un nombre único para el archivo
            // usamos UUID para que nunca haya dos nombres iguales
            // ejemplo: img/a1b2c3d4-e5f6-7890-abcd-ef1234567890_ranchera.webp
            String fileName = FOLDER_NAME + UUID.randomUUID() + "_" + file.getOriginalFilename();

            // crea el identificador del blob (archivo)
            // un blob es un archivo en Firebase Storage
            BlobId blobId = BlobId.of(BUCKET_NAME, fileName);

            // creaa la información del blob
            // le decimos qué tipo de contenido es (image/jpeg, image/png, etc)
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // se sube el archivo a Firebase
            // file.getBytes() convierte el archivo a bytes (datos crudos)
            storage.create(blobInfo, file.getBytes());

            // crea la URL pública del archivo
            // esta URL es la que guardamos en la BD
            // ejemplo:
            // https://firebasestorage.googleapis.com/v0/b/goldenburgers-60680.firebasestorage.app/o/img%2Fa1b2c3d4...?alt=media
            String imageUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    BUCKET_NAME, fileName.replace("/", "%2F"));

            // paso 6: registrar en los logs que se subió correctamente
            log.info("Imagen subida exitosamente: {}", imageUrl);

            // paso 7: retornar la URL al caller (al CatalogoService)
            return imageUrl;

        } catch (Exception e) {
            // si algo falla, registrar el error en los logs
            log.error("Error al subir imagen a Firebase Storage: {}", e.getMessage());
            e.printStackTrace();
            // y lanzar una excepción para que el que llama sepa que hubo error
            throw new IOException("Error al subir imagen: " + e.getMessage());
        }
    }

    // ===================================================================
    // ELIMINAR IMAGEN DE FIREBASE STORAGE
    // ===================================================================

    // este método recibe una URL de una imagen
    // extrae el nombre del archivo y lo elimina de Firebase
    public void eliminarImagen(String imageUrl) {
        try {
            // extrae el nombre del archivo de la URL
            // ejemplo: si la URL es "https://...o/img%2Fa1b2c3d4...?alt=media"
            // extrae "img/a1b2c3d4..."
            String fileName = extractFileNameFromUrl(imageUrl);

            // si se pudo extrae el nombre, y lo elimina
            if (fileName != null) {
                // crea el identificador del archivo
                BlobId blobId = BlobId.of(BUCKET_NAME, fileName);

                // elimina el archivo de Firebase
                boolean deleted = storage.delete(blobId);

                // verifica si se eliminó correctamente
                if (deleted) {
                    log.info("Imagen eliminada exitosamente: {}", fileName);
                } else {
                    log.warn("No se pudo eliminar la imagen: {}", fileName);
                }
            }
        } catch (Exception e) {
            // si hay error, registrarlo pero no lanzar excepción
            // porque no queremos que la app se caiga si falla la eliminación
            log.error("Error al eliminar imagen de Firebase Storage: {}", e.getMessage());
        }
    }

    // ===================================================================
    // EXTRAER NOMBRE DE ARCHIVO DE LA URL
    // ===================================================================

    // este método ayuda a sacar el nombre del archivo de una URL
    // es útil para poder eliminarlo después
    private String extractFileNameFromUrl(String url) {
        try {

            // encuentra donde empieza el nombre (/o/ es donde empieza)
            int start = url.indexOf("/o/") + 3;

            // encuentra donde termina el nombre (?alt=media es donde termina)
            int end = url.indexOf("?alt=media");

            // si los índices son válidos, extraer el nombre
            if (start > 2 && end > start) {
                // substring extrae la parte de la string entre dos posiciones
                String fileName = url.substring(start, end);

                // convierte %2F de vuelta a / (porque %2F es "/" codificado en URL)
                return fileName.replace("%2F", "/");
            }
        } catch (Exception e) {
            // si hay error, registrarlo
            log.error("Error al extraer nombre de archivo de URL: {}", e.getMessage());
        }

        // si no se pudo extraer, retornar null
        return null;
    }
}