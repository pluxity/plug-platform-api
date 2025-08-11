package com.pluxity.cctv.repository;

import com.pluxity.cctv.entity.DeviceCctv;
import com.pluxity.device.entity.Device;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceCctvRepository extends JpaRepository<DeviceCctv, Long> {

    @EntityGraph(attributePaths = {"cctv", "cctv.category", "cctv.feature", "cctv.feature.facility"})
    List<DeviceCctv> findByDevice(Device device);

    @Modifying
    @Query("delete from DeviceCctv d where d.cctv.id in :ids")
    void deleteByCctvIdIn(@Param("ids") List<String> ids);
}
