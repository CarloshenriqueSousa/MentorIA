package com.mentoria.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
<<<<<<< Updated upstream
=======
import jakarta.validation.constraints.Size;
>>>>>>> Stashed changes
import lombok.Data;

@Data
public class UpdateProfileRequest {
<<<<<<< Updated upstream
    @NotBlank(message = "Nome é obrigatório")
=======

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
>>>>>>> Stashed changes
    private String name;
}
