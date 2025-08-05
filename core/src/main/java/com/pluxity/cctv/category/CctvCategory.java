package com.pluxity.cctv.category;

import com.pluxity.category.entity.Category;
import com.pluxity.cctv.Cctv;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CctvCategory extends Category<CctvCategory> {
    @OneToMany(mappedBy = "category")
    private final List<Cctv> cctvs = new ArrayList<>();

    @Builder
    public CctvCategory(String name) {
        this.name = name;
    }
}
