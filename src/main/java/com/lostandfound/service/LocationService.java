package com.lostandfound.service;

import com.lostandfound.model.Location;
import com.lostandfound.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    public Location createLocation(Location location) {
        if (locationRepository.existsByPlaceName(location.getPlaceName())) {
            throw new RuntimeException("Location already exists: " + location.getPlaceName());
        }
        return locationRepository.save(location);
    }

    public Location getLocationById(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
    }

    public Location getLocationByName(String name) {
        return locationRepository.findByPlaceNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Location not found: " + name));
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAllByOrderByPlaceNameAsc();
    }

    public Location updateLocation(Long locationId, Location updated) {
        Location location = getLocationById(locationId);
        location.setPlaceName(updated.getPlaceName());
        location.setDescription(updated.getDescription());
        location.setAddress(updated.getAddress());
        location.setCity(updated.getCity());
        location.setState(updated.getState());
        return locationRepository.save(location);
    }

    public void deleteLocation(Long locationId) {
        locationRepository.deleteById(locationId);
    }
}
