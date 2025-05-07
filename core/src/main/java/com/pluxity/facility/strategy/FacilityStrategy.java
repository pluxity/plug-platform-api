package com.pluxity.facility.strategy;

import com.pluxity.facility.entity.Facility;

import java.util.List;

public interface FacilityStrategy<REQ, RES> {
    void save(Facility facility, REQ data);
    
    List<RES> findAllByFacility(Facility facility);
    
    void update(Facility facility, REQ data);
    
    void delete(Facility facility);
}
