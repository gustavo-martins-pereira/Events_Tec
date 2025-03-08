package com.eventstec.api.domain.coupon.dtos;

public record CouponRequestDTO(String code, Integer discount, Long valid) {
}
