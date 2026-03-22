package api.forohud.domain.topico;

import api.forohud.domain.topico.dto.DatosActualizacionTopico;
import api.forohud.domain.topico.dto.DatosDetalleTopico;
import api.forohud.domain.topico.dto.DatosListaTopico;
import api.forohud.domain.topico.dto.DatosRegistroTopico;
import api.forohud.domain.topico.validacion.ValidadorTopicoDuplicado;
import api.forohud.domain.usuario.Usuario;
import api.forohud.infra.exceptions.AccesoDenegadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/topicos")
@SecurityRequirement(name = "bearer-key")
@Tag(
        name = "Tópicos",
        description = """
                CRUD de tópicos del foro.
                
                **Requiere autenticación:** todos los endpoints necesitan el header **Authorization: Bearer <accessToken>**.
                
                **Reglas de negocio:**
                - No se permiten tópicos duplicados (mismo título y mensaje)
                - Solo el autor puede editar su propio tópico
                - Cualquier usuario autenticado puede eliminar cualquier tópico
                - Los tópicos se crean con status **ABIERTO** por defecto
                """
)
public class TopicoController {

    @Autowired
    private TopicoRepository repository;

    @Autowired
    private ValidadorTopicoDuplicado validador;

    @Operation(
            summary = "Crear tópico",
            description = """
                    Registra un nuevo tópico en el foro asociado al usuario autenticado.
                    
                    Valida que no exista un tópico con el mismo `titulo` y `mensaje` — si es así devuelve `409`.
                    """
    )
    @Transactional
    @PostMapping
    public ResponseEntity registrar(@RequestBody @Valid DatosRegistroTopico datos, UriComponentsBuilder uriComponentsBuilder,
                                    Authentication authentication){
        validador.validar(datos);

        var usuario = (Usuario) authentication.getPrincipal();
        var topico = new Topico(datos, usuario);
        repository.save(topico);

        var uri = uriComponentsBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DatosDetalleTopico(topico));
    }

    @Operation(
            summary = "Listar tópicos",
            description = """
                    Devuelve una lista paginada de todos los tópicos, ordenados por fecha descendente por defecto.
                    
                    **Parámetros de paginación:**
                    - **page** — número de página (empieza en 0)
                    - **size** — cantidad por página (default: 10)
                    - **sort** — campo de ordenamiento (default: **fecha,desc**)
                    """
    )
    @GetMapping
    public ResponseEntity<Page<DatosListaTopico>> listar(@ParameterObject @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable paginacion){
        var page = repository.findAll(paginacion).map(DatosListaTopico::new);

        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Actualizar tópico",
            description = """
                    Actualiza los datos de un tópico existente.
                    
                    **Solo el autor del tópico puede editarlo.** Si otro usuario intenta modificarlo, se devuelve `403`.
                    """
    )
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity actualizar(@PathVariable Long id, @RequestBody @Valid DatosActualizacionTopico datos, Authentication authentication){
        var optionalTopico = repository.findById(id);
        if (optionalTopico.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        var topico = optionalTopico.get();

        var usuarioLogueado = (Usuario) authentication.getPrincipal();
        if (!topico.getAutor().getId().equals(usuarioLogueado.getId())){
            throw new AccesoDenegadoException("No tienes permiso para actualizar este tópico");
        }

        topico.actualizarDatos(datos);
        return ResponseEntity.ok(new DatosDetalleTopico(topico));
    }

    @Operation(
            summary = "Eliminar tópico",
            description = "Elimina permanentemente un tópico. Cualquier usuario autenticado puede eliminar cualquier tópico."
    )
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity eliminar(@PathVariable Long id){
        var optionalTopico = repository.findById(id);

        if (optionalTopico.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Obtener tópico por ID",
            description = "Devuelve el detalle completo de un tópico específico."
    )
    @GetMapping("/{id}")
    public ResponseEntity detallar(@PathVariable Long id){
        var topico = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return ResponseEntity.ok(new DatosDetalleTopico(topico));
//        return topico
//                .map(topicodetalle -> ResponseEntity.ok(new DatosDetalleTopico(topicodetalle)))
//                .orElseGet(()-> ResponseEntity.notFound().build());
    }

}
