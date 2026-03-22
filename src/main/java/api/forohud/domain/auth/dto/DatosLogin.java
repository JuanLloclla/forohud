package api.forohud.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DatosLogin(
        @NotBlank String login,
        @NotBlank String contrasena
) {
}
