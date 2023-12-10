package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.CourierCompanyNumbersModel;
import com.lazo.couriers.app.orders.repository.OrderJobsRepository;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.repository.UserRepository;
import com.lazo.couriers.app.user.repository.UsersRepository;
import com.lazo.couriers.utils.JwtUtils;
import com.lazo.couriers.utils.LazoUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.lazo.couriers.app.orders.domain.OrderJobsDomain.OrderJobsStatus.ON_HOLD;
import static com.lazo.couriers.app.orders.domain.OrdersDomain.ParcelDeliveryType.OUT_OF_COUNTRY;
import static com.lazo.couriers.app.orders.domain.OrdersDomain.ParcelDeliveryType.OUT_OF_REGION;
import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2022-06-23
 */

@Service
@RequiredArgsConstructor
public class OrdersCourierCompanyServiceImpl implements OrdersCourierCompanyService {

    HttpHeaders headers = new HttpHeaders();

    private final OrdersRepository ordersRepository;

    private final OrderJobsRepository orderJobsRepository;

    private final UserRepository userRepository;

    private final LazoUtils lazoUtils;

    private final JwtUtils jwtTokenUtils;

    @Override
    public ResponseEntity<List<OrdersDomain>> getActiveParcelsNotInJobForCourierCompany(Integer pageKey, Integer pageSize) {

        if (pageKey ==null || pageSize==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page =  ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("courierCompanyUserId"), userId));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("jobId").isNull());
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortAsc("toBeDeliveredAdminArea")));


        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> handOverJobToCompanyCourier(String token, String checkedParcels, Integer courierUserId) {

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(checkedParcels) || courierUserId ==null)
            return new ResponseEntity<>("1", headers, HttpStatus.BAD_REQUEST);

        var listString = checkedParcels.replace("[","").replace("]","");

        String[] strList = listString.split(",");
        List<Long> lngList = new ArrayList<>();
        for(String s : strList) lngList.add(Long.valueOf(s.trim()));

        var orders = ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            predicate = builder.and(predicate, builder.in(root.get("orderId")).value(lngList));

            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));

            predicate = builder.and(predicate, root.get("jobId").isNull());

            return predicate;
        });

        if (orders.isEmpty())
            return new ResponseEntity<>("2", headers, HttpStatus.OK);

        var containsOutOfCountry = false;
        var containsOutOfRegion = false;

        for (var o : orders) {
            if (Objects.equals(o.getParcelDeliveryType(), OUT_OF_COUNTRY))
                containsOutOfCountry = true;

            if (Objects.equals(o.getParcelDeliveryType(), OUT_OF_REGION))
                containsOutOfRegion = true;
        }

        var user0 = userRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return null;

        var user = user0.get();

        var jobName = lazoUtils.getNickname(user);

        var userName = jwtTokenUtils.extractUsername(token.substring(7));

        var courierUser = userRepository.findById(courierUserId.longValue());
        if (courierUser.isEmpty())
            return new ResponseEntity<>("3", headers, HttpStatus.BAD_REQUEST);

        var courierUserName = courierUser.get().getUsername();
        if (StringUtils.isEmpty(courierUserName))
            return new ResponseEntity<>("4", headers, HttpStatus.BAD_REQUEST);

        var newCustom = new OrderJobsDomain(ON_HOLD, getCurrentApplicationUserId(), userName,
                orders.size(), containsOutOfCountry,
                containsOutOfRegion, lazoUtils.calculateJobContainsOrNotExpress(orders), jobName,
                courierUserId, courierUserName);

        if (lazoUtils.setJobIdsToOrders(orders, orderJobsRepository.saveAndFlush(newCustom).getOrderJobId()))
            return new ResponseEntity<>("5", headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>("success", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CourierCompanyNumbersModel> getCourierCompanyNumbers() {
        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        CourierCompanyNumbersModel m = new CourierCompanyNumbersModel();

        m.setDistributable(ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, builder.equal(root.get("courierCompanyUserId"), userId));
            predicate = builder.and(predicate, root.get("jobId").isNull());
            return predicate;
        }));

        m.setActiveJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            return predicate;
        }));

        return new ResponseEntity<>(m, headers, HttpStatus.OK);
    }

}
