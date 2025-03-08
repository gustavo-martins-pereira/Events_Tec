package com.eventstec.api.services;

import com.eventstec.api.domain.address.Address;
import com.eventstec.api.domain.event.Event;
import com.eventstec.api.domain.event.dtos.EventRequestDTO;
import com.eventstec.api.repositories.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Transactional
    public void createAddress(EventRequestDTO eventRequestDTO, Event event) {
        Address address = new Address();
        address.setCity(eventRequestDTO.city());
        address.setUf(eventRequestDTO.state());
        address.setEvent(event);

        addressRepository.save(address);
    }

}
