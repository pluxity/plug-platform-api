package com.pluxity.station;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineRepository extends JpaRepository<Line, Long> {
    Optional<Line> findByName(
            @NotBlank(message = "노선 이름은 필수입니다") @Size(max = 50, message = "노선 이름은 50자 이하여야 합니다")
                    String name);
}
