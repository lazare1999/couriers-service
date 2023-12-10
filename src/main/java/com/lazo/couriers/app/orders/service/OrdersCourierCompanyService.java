package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.CourierCompanyNumbersModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2022-06-23
 */

public interface OrdersCourierCompanyService {

    ResponseEntity<List<OrdersDomain>> getActiveParcelsNotInJobForCourierCompany(Integer pageKey, Integer pageSize);

    ResponseEntity<String> handOverJobToCompanyCourier(String token, String checkedParcels, Integer courierUserId);

    ResponseEntity<CourierCompanyNumbersModel> getCourierCompanyNumbers();

}
