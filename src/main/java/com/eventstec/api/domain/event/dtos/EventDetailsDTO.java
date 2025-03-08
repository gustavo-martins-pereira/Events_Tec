package com.eventstec.api.domain.event.dtos;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record EventDetailsDTO(UUID id, String title, String description, Date date, String city, String uf, String eventUrl, String imageUrl, List<CouponDTO> coupons) {

    public record CouponDTO(
            String code,
            Integer discount,
            Date valid) {
    }

}
