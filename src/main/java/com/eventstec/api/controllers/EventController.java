package com.eventstec.api.controllers;

import com.eventstec.api.domain.event.dtos.EventDetailsDTO;
import com.eventstec.api.domain.event.dtos.EventRequestDTO;
import com.eventstec.api.domain.event.Event;
import com.eventstec.api.domain.event.dtos.EventResponseDTO;
import com.eventstec.api.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Event> create(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("date") Long date,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("remote") Boolean remote,
            @RequestParam("eventUrl") String eventUrl,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        EventRequestDTO eventRequestDTO = new EventRequestDTO(title, description, date, city, state, remote, eventUrl, image);
        Event event = this.eventService.createEvent(eventRequestDTO);

        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        List<EventResponseDTO> events = eventService.getUpcomingEvents(page, size);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsDTO> getEventDetailsById(@PathVariable UUID eventId) {
        EventDetailsDTO eventDetails = eventService.getEventDetailsById(eventId);

        return ResponseEntity.ok(eventDetails);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<EventResponseDTO>> getFilteredEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "") String city,
            @RequestParam(required = false, defaultValue = "") String state,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate
    ) {
        Date effectiveStartDate = Optional.ofNullable(startDate).orElse(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date effectiveEndDate = Optional.ofNullable(endDate).orElse(calendar.getTime());

        List<EventResponseDTO> events = eventService.getFilteredEvents(page, size, title, city, state, effectiveStartDate, effectiveEndDate);

        return ResponseEntity.ok(events);
    }

}
