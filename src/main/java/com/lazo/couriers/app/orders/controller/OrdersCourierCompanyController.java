package com.lazo.couriers.app.orders.controller;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.CourierCompanyNumbersModel;
import com.lazo.couriers.app.orders.service.OrdersCourierCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Lazo on 2022-06-23
 */

@RestController
@RequestMapping("courier_company")
@RequiredArgsConstructor
public class OrdersCourierCompanyController {

    private final OrdersCourierCompanyService ordersCourierCompanyService;

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_active_parcels_not_in_job_for_courier_company" })
    public ResponseEntity<List<OrdersDomain>> getActiveParcelsNotInJobForCourierCompany(Integer pageKey, Integer pageSize) {
        return ordersCourierCompanyService.getActiveParcelsNotInJobForCourierCompany(pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/hand_over_job_to_company_courier" })
    public ResponseEntity<String> handOverJobToCompanyCourier(@RequestHeader("Authorization") String token, String checkedParcels, Integer courierUserId) {
        return ordersCourierCompanyService.handOverJobToCompanyCourier(token, checkedParcels, courierUserId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_courier_company_numbers" })
    public ResponseEntity<CourierCompanyNumbersModel> getCourierCompanyNumbers() {
        return ordersCourierCompanyService.getCourierCompanyNumbers();
    }


}
