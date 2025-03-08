package com.eventstec.api.controllers;

import com.eventstec.api.domain.coupon.Coupon;
import com.eventstec.api.domain.coupon.dtos.CouponRequestDTO;
import com.eventstec.api.services.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("event/{eventId}")
    public ResponseEntity<Coupon> addCouponToEvent(@PathVariable UUID eventId, @RequestBody CouponRequestDTO couponRequestDTO) {
        Coupon coupon = couponService.addCouponToEvent(eventId, couponRequestDTO);

        return ResponseEntity.ok(coupon);
    }

}
