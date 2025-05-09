package com.pluxity.facility.strategy;

import com.pluxity.facility.entity.Facility;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FacilityStrategy<REQ, RES> {
    <T extends Facility> void save(T facility, REQ data);
    <T extends Facility> RES findByFacility(T facility);
    <T extends Facility> List<RES> findAllByFacility(T facility);
    <T extends Facility> void update(T facility, REQ data);
    <T extends Facility> void delete(T facility);
}
