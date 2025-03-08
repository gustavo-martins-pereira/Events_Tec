package com.eventstec.api.services;

import com.eventstec.api.domain.coupon.Coupon;
import com.eventstec.api.domain.event.dtos.EventDetailsDTO;
import com.eventstec.api.domain.event.dtos.EventRequestDTO;
import com.eventstec.api.domain.event.Event;
import com.eventstec.api.domain.event.dtos.EventResponseDTO;
import com.eventstec.api.repositories.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventService {

    @Value("${aws.endpoint}")
    private String endpoint;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CouponService couponService;

    @Transactional
    public Event createEvent(EventRequestDTO eventRequestDTO) {
        String imgUrl = null;

        if(eventRequestDTO.image() != null) {
            imgUrl = this.uploadImg(eventRequestDTO.image());
        }

        Event newEvent = new Event();
        newEvent.setTitle(eventRequestDTO.title());
        newEvent.setDescription(eventRequestDTO.description());
        newEvent.setDate(new Date(eventRequestDTO.date()));
        newEvent.setEventUrl(eventRequestDTO.eventUrl());
        newEvent.setRemote(eventRequestDTO.remote());
        newEvent.setImageUrl(imgUrl);

        Event savedEvent = eventRepository.save(newEvent);

        if(!eventRequestDTO.remote()) {
            this.addressService.createAddress(eventRequestDTO, savedEvent);
        }

        return savedEvent;
    }

    public List<EventResponseDTO> getEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = this.eventRepository.findAll(pageable);

        return eventsPage.map(event -> new EventResponseDTO(event.getId(), event.getTitle(), event.getDescription(), event.getDate(), "", "", event.getRemote(), event.getEventUrl(), event.getImageUrl()))
                .stream().toList();
    }

    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = this.eventRepository.findAllUpcomingEvents(new Date(), pageable);

        return eventsPage.map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        event.getAddress() != null ? event.getAddress().getCity() : "",
                        event.getAddress() != null ?event.getAddress().getUf() : "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImageUrl())
                )
                .stream().toList();
    }

    public EventDetailsDTO getEventDetailsById(UUID eventId) {
        Event event = this.eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<Coupon> coupons = this.couponService.consultCoupons(eventId, new Date());

        List<EventDetailsDTO.CouponDTO> couponDTOS = coupons.stream()
                .map(coupon -> new EventDetailsDTO.CouponDTO(
                        coupon.getCode(),
                        coupon.getDiscount(),
                        coupon.getValid()))
                .toList();

        return new EventDetailsDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() : "",
                event.getEventUrl(),
                event.getImageUrl(),
                couponDTOS
        );
    }

    public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf, Date startDate, Date endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = this.eventRepository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);

        return eventsPage.map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        event.getAddress() != null ? event.getAddress().getCity() : "",
                        event.getAddress() != null ?event.getAddress().getUf() : "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImageUrl())
                )
                .stream().toList();
    }

    private String uploadImg(MultipartFile multipartFile) {
        String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        try {
            File file = this.convertMultipartToFile(multipartFile);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            s3Client.putObject(objectRequest, file.toPath());

            file.delete();

            return s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build()).toString();
        } catch (Exception e) {
            System.out.println("Error on upload the file");

            return null;
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(multipartFile.getBytes());
        fos.close();

        return convertedFile;
    }

}
