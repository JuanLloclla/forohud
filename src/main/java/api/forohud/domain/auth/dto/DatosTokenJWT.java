package api.forohud.domain.auth.dto;

public record DatosTokenJWT(
        String accessToken,
        String refreshToken
) {
}
