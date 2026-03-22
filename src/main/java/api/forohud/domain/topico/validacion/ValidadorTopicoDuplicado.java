package api.forohud.domain.topico.validacion;

import api.forohud.infra.exceptions.RecursoDuplicadoException;
import api.forohud.domain.topico.TopicoRepository;
import api.forohud.domain.topico.dto.DatosRegistroTopico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorTopicoDuplicado {

    @Autowired
    private TopicoRepository repository;

    public void validar(DatosRegistroTopico datos){
        var topicoduplicado = repository.existsByTituloAndMensaje(datos.titulo(), datos.mensaje());

        if (topicoduplicado){
            throw new RecursoDuplicadoException("Ya existe un tópico con el mismo titulo y mensaje");
        }
    }
}
