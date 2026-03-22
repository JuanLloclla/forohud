package api.forohud.domain.topico.dto;

import api.forohud.domain.topico.Status;
import api.forohud.domain.topico.Topico;

import java.time.LocalDateTime;

public record DatosListaTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fecha,
        Status status,
        String autor,
        String curso
) {
    public DatosListaTopico(Topico topico){
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFecha(),
                topico.getStatus(),
                topico.getAutor().getUsername(),
                topico.getCurso()
        );
    }
}
