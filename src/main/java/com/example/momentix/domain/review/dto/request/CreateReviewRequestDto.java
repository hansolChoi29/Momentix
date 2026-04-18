package com.example.momentix.domain.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateReviewRequestDto {
    @NotBlank
    @Size(max = 500)
    private String contents;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("5.0")
    private Double rating;
}