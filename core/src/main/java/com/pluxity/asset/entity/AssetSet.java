package com.pluxity.asset.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asset_set")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "assetSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "asset_set_id")
    private final List<Asset> assets = new ArrayList<>();
}
