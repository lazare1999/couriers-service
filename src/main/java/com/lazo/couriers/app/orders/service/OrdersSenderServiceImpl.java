package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.*;
import com.lazo.couriers.app.orders.repository.OrderJobsRepository;
import com.lazo.couriers.app.orders.repository.OrderServiceTypeRepository;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.domains.NotificationsDomain;
import com.lazo.couriers.app.user.repository.NotificationsRepository;
import com.lazo.couriers.app.user.repository.UserRepository;
import com.lazo.couriers.app.user.repository.UsersRepository;
import com.lazo.couriers.utils.JwtUtils;
import com.lazo.couriers.utils.LazoUtils;
import com.lazo.couriers.utils.MessagingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.lazo.couriers.app.orders.domain.OrderJobsDomain.OrderJobsStatus.ON_HOLD;
import static com.lazo.couriers.app.orders.domain.OrdersDomain.ParcelDeliveryType.*;
import static com.lazo.couriers.app.orders.domain.OrdersDomain.ParcelType.BIG;
import static com.lazo.couriers.utils.LazoDateUtil.stringToLocalDateTime2;
import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2021-04-13
 */

@Service
@RequiredArgsConstructor
public class OrdersSenderServiceImpl implements OrdersSenderService {

    HttpHeaders headers = new HttpHeaders();

    private final OrdersCommonFunctionsService ordersCommonFunctionsService;
    private final NotificationsRepository notificationsRepository;
    private final OrderServiceTypeRepository orderServiceTypeRepository;
    private final OrdersRepository ordersRepository;
    private final OrderJobsRepository orderJobsRepository;
    private final UserRepository userRepository;
    private final UsersRepository usersRepository;

    private final LazoUtils lazoUtils;

    private final JwtUtils jwtTokenUtils;

    private static final long EXPRESS_ANOTHER_COUNTRY = 1L;
    private static final long ANOTHER_COUNTRY = 2L;
    private static final long EXPRESS_ANOTHER_REGION = 3L;
    private static final long ANOTHER_REGION = 4L;
    private static final long EXPRESS = 5L;
    private static final long STANDARD = 6L;

    private static final Double EXPRESS_ANOTHER_COUNTRY_MINIMAL_PRICE = 200.0;
    private static final Double ANOTHER_COUNTRY_MINIMAL_PRICE = 100.0;
    private static final Double EXPRESS_ANOTHER_REGION_MINIMAL_PRICE = 15.0;
    private static final Double ANOTHER_REGION_MINIMAL_PRICE = 10.0;

    private Double reCalculateServicePrice(OrdersDomain order) {
        var m = new ServicePriceModel();

        if(StringUtils.isNotEmpty(order.getTotalDistance())) {
            m.setTotalDistance(Double.valueOf(order.getTotalDistance()));
        }
        m.setExpress(order.getExpress());
        m.setPickupAdminArea(order.getPickupAdminArea());
        m.setToBeDeliveredAdminArea(order.getToBeDeliveredAdminArea());
        m.setPickupCountryCode(order.getPickupCountryCode());
        m.setToBeDeliveredCountryCode(order.getToBeDeliveredCountryCode());
        m.setParcelType(order.getParcelType().ordinal());

        var recalculatedServicePrice = getServicePrice(m);
        if (recalculatedServicePrice.getStatusCode() == HttpStatus.BAD_REQUEST)
            return 0.0;

        var body = recalculatedServicePrice.getBody();
        if (StringUtils.isEmpty(body))
            return 0.0;

        String[] asd = body.split(" ");

        return Double.parseDouble(asd[0]);
    }

    private Double calculateServiceCostOrRatio(Long serviceTypeId) {

        if (serviceTypeId == null)
            return 0.0;

        var orderType0 = orderServiceTypeRepository.findById(serviceTypeId);

        if (orderType0.isEmpty())
            return 0.0;

        return Double.parseDouble(orderType0.get().getServiceCostOrRatio());
    }

    private HashMap<String, List<OrdersDomain>> generateOrdersHashMap(List<OrdersDomain> orders) {
        HashMap<String, List<OrdersDomain>> ordersHashMap = new HashMap<>();
        for (var o : orders) {
            if (!ordersHashMap.containsKey(o.getPickupAdminArea())) {
                List<OrdersDomain> list = new ArrayList<>();
                list.add(o);

                ordersHashMap.put(o.getPickupAdminArea(), list);
            } else {
                ordersHashMap.get(o.getPickupAdminArea()).add(o);
            }
        }

        return ordersHashMap;
    }

    private List<CreateJobAnswerModel> createNewJobOrJobs(List<OrdersDomain> orders, String token, OrdersDomain.ParcelDeliveryType type, OrderJobsDomain.OrderJobsStatus jobStatus) {
        if (orders.isEmpty() || StringUtils.isEmpty(token))
            return null;

        if (orders.get(0).getExpress() ==null ||
                orders.get(0).getParcelPickupAddressLatitude() ==null ||
                orders.get(0).getParcelPickupAddressLongitude() ==null ||
                orders.get(0).getPickupAdminArea() ==null)
            return null;


        var user0 = userRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return null;

        var user = user0.get();

        var jobName = lazoUtils.getNickname(user);

        List<CreateJobAnswerModel> ans = new ArrayList<>();
        var userName = jwtTokenUtils.extractUsername(token.substring(7));

        if (Objects.equals(type, IN_REGION)) {

            var newInRegionJob = new OrderJobsDomain(jobStatus, getCurrentApplicationUserId(), userName, orders.size(), false, false, lazoUtils.calculateJobContainsOrNotExpress(orders), jobName);

            if (lazoUtils.setJobIdsToOrders(orders, orderJobsRepository.saveAndFlush(newInRegionJob).getOrderJobId()))
                return null;

            ans.add(new CreateJobAnswerModel("in_region", String.valueOf(orders.size())));
            return ans;
        } else if (Objects.equals(type, OUT_OF_REGION)) {
            var outOfRegionOrdersHashMap = generateOrdersHashMap(orders);

            if (outOfRegionOrdersHashMap.isEmpty())
                return null;

            OrderJobsDomain newOutOfRegionJob;
            List<OrdersDomain> outOfRegionValue;
            for(Map.Entry<String, List<OrdersDomain>> entry : outOfRegionOrdersHashMap.entrySet()) {

                outOfRegionValue = entry.getValue();

                newOutOfRegionJob = new OrderJobsDomain(jobStatus, getCurrentApplicationUserId(), userName, outOfRegionValue.size(), false, true, lazoUtils.calculateJobContainsOrNotExpress(outOfRegionValue), jobName);

                if (lazoUtils.setJobIdsToOrders(outOfRegionValue, orderJobsRepository.saveAndFlush(newOutOfRegionJob).getOrderJobId()))
                    return null;

                ans.add(new CreateJobAnswerModel("out_of_region", String.valueOf(entry.getValue().size())));
            }

            return ans;

        } else if (Objects.equals(type, OUT_OF_COUNTRY)) {

            var outOfCountryOrdersHashMap = generateOrdersHashMap(orders);

            if (outOfCountryOrdersHashMap.isEmpty())
                return null;

            OrderJobsDomain newOutOfCountryJob;
            List<OrdersDomain> outOfCountryValue;
            for(Map.Entry<String, List<OrdersDomain>> entry : outOfCountryOrdersHashMap.entrySet()) {

                outOfCountryValue = entry.getValue();

                newOutOfCountryJob = new OrderJobsDomain(jobStatus, getCurrentApplicationUserId(), userName, outOfCountryValue.size(), true, false, lazoUtils.calculateJobContainsOrNotExpress(outOfCountryValue), jobName);

                if (lazoUtils.setJobIdsToOrders(outOfCountryValue, orderJobsRepository.saveAndFlush(newOutOfCountryJob).getOrderJobId()))
                    return null;

                ans.add(new CreateJobAnswerModel("out_of_country", String.valueOf(entry.getValue().size())));
            }

            return ans;
        } else {
            var containsOutOfCountry = false;
            var containsOutOfRegion = false;

            for (var o : orders) {
                if (Objects.equals(o.getParcelDeliveryType(), OUT_OF_COUNTRY))
                    containsOutOfCountry = true;

                if (Objects.equals(o.getParcelDeliveryType(), OUT_OF_REGION))
                    containsOutOfRegion = true;
            }

            var newCustom = new OrderJobsDomain(jobStatus, getCurrentApplicationUserId(), userName, orders.size(), containsOutOfCountry, containsOutOfRegion, lazoUtils.calculateJobContainsOrNotExpress(orders), jobName);

            if (lazoUtils.setJobIdsToOrders(orders, orderJobsRepository.saveAndFlush(newCustom).getOrderJobId()))
                return null;

            ans.add(new CreateJobAnswerModel("custom", String.valueOf(orders.size())));
            return ans;
        }
    }


    private OrderJobsDomain updateJobCommonValues(Optional<OrderJobsDomain> job0, List<OrdersDomain> otherParcelsInJob) {
        if (job0.isEmpty())
            return null;

        var job = job0.get();
        if (!Objects.equals(job.getOrderJobStatus(), ON_HOLD))
            return null;

        if (otherParcelsInJob.isEmpty()) {
            job.setContainsDeliveryTypeOutOfRegion(false);
            job.setContainsDeliveryTypeOutOfCountry(false);
            job.setContainsExpressOrder(false);
        } else {
            var containsOutOfRegion = false;
            var containsOutOfCountry = false;
            for (var oJ : otherParcelsInJob) {
                if (Objects.equals(oJ.getParcelDeliveryType(), OUT_OF_REGION))
                    containsOutOfRegion = true;
                if (Objects.equals(oJ.getParcelDeliveryType(), OUT_OF_COUNTRY))
                    containsOutOfCountry = true;
            }

            job.setContainsDeliveryTypeOutOfRegion(containsOutOfRegion);
            job.setContainsDeliveryTypeOutOfCountry(containsOutOfCountry);
            job.setContainsExpressOrder(lazoUtils.calculateJobContainsOrNotExpress(otherParcelsInJob));
        }


        return job;
    }

    private Page<OrderJobsDomain> getJobs(Integer pageKey, Integer pageSize, OrderJobsDomain.OrderJobsStatus status, String token, boolean courierUserIdIsNull) {

        if (pageKey ==null || pageSize ==null || status ==null || StringUtils.isEmpty(token))
            return null;

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return null;

        return orderJobsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), status));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            if (courierUserIdIsNull)
                predicate = builder.and(predicate, root.get("courierUserId").isNull());

            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("orderCount")));
    }

    private boolean checkIfUserOwnsJob(Integer userId, Long jobId) {

        if (jobId ==null || userId ==null)
            return true;

        var j0 = orderJobsRepository.findById(jobId);

        if (j0.isEmpty())
            return true;

        return !Objects.equals(j0.get().getSenderUserId(), userId);
    }

    private OrdersDomain checkOrderAndApproveThatUserOwnsOrdersJob(Long orderId) {
        var order0 = ordersRepository.findById(orderId);
        if (order0.isEmpty())
            return null;

        var order = order0.get();

        if (checkIfUserOwnsJob(getCurrentApplicationUserId(), order.getJobId()))
            return null;
        return order;
    }

    @Override
    public ResponseEntity<String> getServicePrice(ServicePriceModel model) {

        OrdersDomain.ParcelDeliveryType parcelDeliveryType;
        double price;

        if (StringUtils.isEmpty(model.getPickupAdminArea()) ||
                StringUtils.isEmpty(model.getToBeDeliveredAdminArea()) ||
                StringUtils.isEmpty(model.getPickupCountryCode()) ||
                StringUtils.isEmpty(model.getToBeDeliveredCountryCode())
        ) {
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);
        }


        if (!Objects.equals(model.getPickupCountryCode(), model.getToBeDeliveredCountryCode())) {

            if (model.getTotalDistance() ==null || Objects.equals(model.getTotalDistance(), 0.0))
                return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

            parcelDeliveryType = OUT_OF_COUNTRY;

        } else if (!Objects.equals(model.getPickupAdminArea(), model.getToBeDeliveredAdminArea())) {

            if (model.getTotalDistance() ==null || Objects.equals(model.getTotalDistance(), 0.0))
                return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

            parcelDeliveryType = OUT_OF_REGION;

        } else {
            parcelDeliveryType = IN_REGION;
        }


        if (parcelDeliveryType.equals(OUT_OF_COUNTRY)) {

            if (Objects.equals(model.getExpress(), true)) {
                price = model.getTotalDistance() * calculateServiceCostOrRatio(EXPRESS_ANOTHER_COUNTRY);
                if (price != 0.0 && price < EXPRESS_ANOTHER_COUNTRY_MINIMAL_PRICE) {
                    price = 200.0;
                }
            } else {
                price = model.getTotalDistance() * calculateServiceCostOrRatio(ANOTHER_COUNTRY);
                if (price != 0.0 && price < ANOTHER_COUNTRY_MINIMAL_PRICE) {
                    price = 100.0;
                }
            }

        } else if (parcelDeliveryType.equals(OUT_OF_REGION)) {

            if (Objects.equals(model.getExpress(), true)) {
                price = model.getTotalDistance() * calculateServiceCostOrRatio(EXPRESS_ANOTHER_REGION);
                if (price != 0.0 && price < EXPRESS_ANOTHER_REGION_MINIMAL_PRICE) {
                    price = 15.0;
                }

            } else {
                price = model.getTotalDistance() * calculateServiceCostOrRatio(ANOTHER_REGION);
                if (price != 0.0 && price < ANOTHER_REGION_MINIMAL_PRICE) {
                    price = 10.0;
                }
            }

        } else {

            if (Objects.equals(model.getExpress(), true)) {
                price = calculateServiceCostOrRatio(EXPRESS);
            } else {
                price = calculateServiceCostOrRatio(STANDARD);
            }

        }

        if (Objects.equals(price, 0.0))
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);



        if (Objects.equals(model.getParcelType(), BIG.ordinal()))
            price *= 10;


        return new ResponseEntity<>(price + " " + Currency.getInstance(new Locale("", model.getPickupCountryCode())).getCurrencyCode(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> placeAnOrder(OrderModel model) {

        switch (lazoUtils.checkIfUserIsVipAndThenCheckExpirationDate(getCurrentApplicationUserId())) {
            case USER_IS_NULL, VIP_EXPIRATION_DATE_TIME_IS_NULL -> {
                return new ResponseEntity<>("PLACE_AN_ORDER_ERROR_UNEXPECTED_ERROR", headers, HttpStatus.BAD_REQUEST);
            }
            case VIP_STATUS_EXPIRED -> {
                return new ResponseEntity<>("VIP_STATUS_EXPIRED", headers, HttpStatus.OK);
            }
            case USER_IS_NOT_VIP, USER_IS_VIP -> {
                if (lazoUtils.userDebtCount() >0)
                    return new ResponseEntity<>("MUST_PAY_DEBT", headers, HttpStatus.OK);
            }
        }

        if (StringUtils.isEmpty(model.getParcelPickupAddressLatitude()) ||
                StringUtils.isEmpty(model.getParcelPickupAddressLongitude()) ||
                StringUtils.isEmpty(model.getParcelAddressToBeDeliveredLatitude()) ||
                StringUtils.isEmpty(model.getParcelAddressToBeDeliveredLongitude()) ||
                StringUtils.isEmpty(model.getServicePrice()) ||
                StringUtils.isEmpty(model.getServicePaymentType()) ||
                StringUtils.isEmpty(model.getClientName()) ||
                StringUtils.isEmpty(model.getServiceParcelIdentifiable()) ||
                StringUtils.isEmpty(model.getPickupCountryCode()) ||
                StringUtils.isEmpty(model.getPickupAdminArea()) ||
                StringUtils.isEmpty(model.getToBeDeliveredCountryCode()) ||
                StringUtils.isEmpty(model.getToBeDeliveredAdminArea()) ||
                StringUtils.isEmpty(model.getViewerPhone())) {
            return new ResponseEntity<>("PLACE_AN_ORDER_ERROR_0", headers, HttpStatus.BAD_REQUEST);
        }


        var senderUserId = getCurrentApplicationUserId();
        if (senderUserId ==null)
            return new ResponseEntity<>("PLACE_AN_ORDER_ERROR_1", headers, HttpStatus.BAD_REQUEST);

        if (model.getCourierHasParcelMoney() && StringUtils.isEmpty(model.getServiceParcelPrice()))
            return new ResponseEntity<>("PLACE_AN_ORDER_ERROR_2", headers, HttpStatus.BAD_REQUEST);


        var user0 = userRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return new ResponseEntity<>("PLACE_AN_ORDER_ERROR_3", headers, HttpStatus.BAD_REQUEST);

        var user = user0.get();

        var si = lazoUtils.getNickname(user);

        model.setServiceParcelIdentifiable(StringUtils.isEmpty(si) ? model.getServiceParcelIdentifiable() : si + model.getServiceParcelIdentifiable());

        var o = ordersRepository.saveAndFlush(new OrdersDomain(model, senderUserId));

        return new ResponseEntity<>(o.getOrderId().toString(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> createJob(String token, String checkedParcels, Boolean active, Boolean hold) {

        if (StringUtils.isEmpty(token))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var jobStatus = OrderJobsDomain.OrderJobsStatus.ACTIVE;
        if (!active && hold)
            jobStatus = ON_HOLD;

        HashMap<String, List<CreateJobAnswerModel>> jobsInfo = new HashMap<>();
        List<OrdersDomain> inRegion = new ArrayList<>();
        List<OrdersDomain> outOfRegion = new ArrayList<>();
        List<OrdersDomain> outOfCountry = new ArrayList<>();

        List<OrdersDomain> orders;

        var listString = checkedParcels.replace("[","").replace("]","");

        if (StringUtils.isEmpty(listString)) {
            orders = ordersRepository.findAllBySenderUserIdAndOrderStatusAndJobIdOrderByOrderId(userId, OrdersDomain.OrderStatus.ACTIVE, null);

            if (orders.isEmpty())
                return new ResponseEntity<>(null, headers, HttpStatus.OK);

            for (var o : orders) {
                switch (o.getParcelDeliveryType()) {
                    case IN_REGION -> inRegion.add(o);
                    case OUT_OF_REGION -> outOfRegion.add(o);
                    case OUT_OF_COUNTRY -> outOfCountry.add(o);
                }
            }

            if (inRegion.size() >0) {
                var inRegions = createNewJobOrJobs(inRegion, token, IN_REGION, jobStatus);
                if (inRegions !=null) {
                    jobsInfo.put("IN_REGION", inRegions);
                }
            }

            if (outOfRegion.size() >0) {
                var outOfRegions = createNewJobOrJobs(outOfRegion, token, OUT_OF_REGION, jobStatus);
                if (outOfRegions !=null) {
                    jobsInfo.put("OUT_OF_REGION", outOfRegions);
                }
            }

            if (outOfCountry.size() >0) {
                var outOfCountries = createNewJobOrJobs(outOfCountry, token, OUT_OF_COUNTRY, jobStatus);
                if (outOfCountries !=null) {
                    jobsInfo.put("OUT_OF_COUNTRY", outOfCountries);
                }
            }

        } else {

            String[] strList = listString.split(",");
            List<Long> lngList = new ArrayList<>();
            for(String s : strList) lngList.add(Long.valueOf(s.trim()));

            orders = ordersRepository.findAll((root, query, builder) -> {
                Predicate predicate = builder.conjunction();

                predicate = builder.and(predicate, builder.in(root.get("orderId")).value(lngList));

                predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));

                predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));

                predicate = builder.and(predicate, root.get("jobId").isNull());

                return predicate;
            });

            if (orders.isEmpty())
                return new ResponseEntity<>(null, headers, HttpStatus.OK);

            var custom = createNewJobOrJobs(orders, token, null, jobStatus);
            if (custom !=null) {
                jobsInfo.put("CUSTOM", custom);
            }
        }

        List<CreateJobAnswerModel> answer = new ArrayList<>();

        for(Map.Entry<String, List<CreateJobAnswerModel>> entry : jobsInfo.entrySet()) {
            String key = entry.getKey();
            List<CreateJobAnswerModel> value = entry.getValue();

            if (Objects.equals(key, "IN_REGION") || Objects.equals(key, "CUSTOM")) {
                answer.add(new CreateJobAnswerModel(value.get(0).getAnswerVariable(), value.get(0).getOrderCount()));
            } else {
                for (var v : value) {
                    answer.add(new CreateJobAnswerModel(v.getAnswerVariable(), v.getOrderCount()));
                }
            }
        }

        return new ResponseEntity<>(answer, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ParcelAndJobInfoModel> getParcelAndJobInfo(String token) {

        if (StringUtils.isEmpty(token))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var senderUserId = getCurrentApplicationUserId();
        if (senderUserId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);


        var todayParcels = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("addDate"), LocalDate.now()));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("jobId").isNotNull());
            return predicate;
        });

        var inActiveParcels = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.equal(root.get("arrivalInProgress"), false));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("jobId").isNotNull());
            return predicate;
        });

        var parcelsWithOutJob = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, root.get("jobId").isNull());
            return predicate;
        });

        var madeParcels = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.equal(root.get("arrivalInProgress"), false));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.DONE));
            predicate = builder.and(predicate, root.get("jobId").isNotNull());
            return predicate;
        });

        var activeParcels = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.equal(root.get("arrivalInProgress"), true));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("jobId").isNotNull());
            return predicate;
        });

        var allParcels = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            return predicate;
        });


        var todayJobs = orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("addDate"), LocalDate.now()));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            return predicate;
        });

        var activeJobs = orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            return predicate;
        });

        var onHoldJobs = orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), ON_HOLD));
            return predicate;
        });

        var doneJobs = orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.DONE));
            return predicate;
        });

        var allJobs = orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), senderUserId));
            return predicate;
        });

        var answer = new ParcelAndJobInfoModel(todayParcels, inActiveParcels, parcelsWithOutJob, madeParcels, activeParcels, allParcels,
                todayJobs, activeJobs, onHoldJobs, doneJobs, allJobs
        );

        return new ResponseEntity<>(answer, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrdersDomain>> getActiveParcelsNotInJob(String token, Integer pageKey, Integer pageSize) {

        if (StringUtils.isEmpty(token) || pageKey ==null || pageSize==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page =  ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("jobId").isNull());
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortAsc("orderId")));


        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Long> userOrdersCount(String token) {
        if (StringUtils.isEmpty(token))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var ordersCount = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            return predicate;
        });

        return new ResponseEntity<>(ordersCount, headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Boolean> removeParcel(Long orderId) {
        if (orderId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order = ordersRepository.findById(orderId);
        if (order.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var job = orderJobsRepository.findById(order.get().getJobId());
        if (job.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);
        job.get().setOrderCount(job.get().getOrderCount()-1);

        ordersRepository.deleteById(orderId);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<JobsModel> getJobs(String token) {
        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        JobsModel m = new JobsModel();

        m.setActiveJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            return predicate;
        }));
        m.setDoneJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            return predicate;
        }));
        m.setOnHoldJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), ON_HOLD));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            predicate = builder.and(predicate, root.get("courierUserId").isNull());
            return predicate;
        }));
        m.setHandedOverJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), ON_HOLD));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNotNull());
            predicate = builder.and(predicate, root.get("courierUserId").isNull());
            return predicate;
        }));


        return new ResponseEntity<>(m, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getActiveJobs(String token, Integer pageKey, Integer pageSize) {

        var page = getJobs(pageKey, pageSize, OrderJobsDomain.OrderJobsStatus.ACTIVE, token, false);
        List<OrderJobsDomain> ans = new ArrayList<>();

        if (page !=null)
            ans = page.toList();

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getOnHoldJobs(String token, Integer pageKey, Integer pageSize) {

        var page = getJobs(pageKey, pageSize, ON_HOLD, token, true);
        List<OrderJobsDomain> ans = new ArrayList<>();

        if (page !=null)
            ans = page.toList();

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getDoneJobs(String token, Integer pageKey, Integer pageSize) {

        var page = getJobs(pageKey, pageSize, OrderJobsDomain.OrderJobsStatus.DONE, token, false);
        List<OrderJobsDomain> ans = new ArrayList<>();

        if (page !=null)
            ans = page.toList();

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getHandedOverJobs(String token, Integer pageKey, Integer pageSize) {
        if (pageKey ==null || pageSize ==null || StringUtils.isEmpty(token))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page = orderJobsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, root.get("rewriterUserId").isNotNull());
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), ON_HOLD));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("orderCount")));

        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Boolean> removeJob(Long orderJobId) {
        if (orderJobId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        ordersRepository.deleteByOrderJobId(orderJobId);

        if (orderJobsRepository.findById(orderJobId).isEmpty())
            return new ResponseEntity<>(true, headers, HttpStatus.OK);

        orderJobsRepository.deleteById(orderJobId);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> activateJob(Long orderJobId) {

        var job = ordersCommonFunctionsService.validateJob(orderJobId);
        if (job ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (Objects.equals(job.getOrderJobStatus(), OrderJobsDomain.OrderJobsStatus.ACTIVE))
            return new ResponseEntity<>(true, headers, HttpStatus.OK);

        if (job.getRewriterUserId() !=null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        job.setOrderJobStatus(OrderJobsDomain.OrderJobsStatus.ACTIVE);
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> holdJob(Long orderJobId) {
        var job0 = orderJobsRepository.findById(orderJobId);
        if (job0.isEmpty())
            return new ResponseEntity<>("", headers, HttpStatus.OK);

        var job = job0.get();

        if (job.getCourierUserId() !=null)
            return new ResponseEntity<>("job_can_not_be_hold", headers, HttpStatus.OK);

        job.setOrderJobStatus(ON_HOLD);
        job.setCourierPhone(null);
        job.setCourierUserId(null);
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.saveAndFlush(job);
        return new ResponseEntity<>("success", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrdersDomain>> ordersByJob(Long orderJobId, Integer pageKey, Integer pageSize) {

        if (orderJobId ==null || pageKey ==null || pageSize==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page =  ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("jobId"), orderJobId));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortAsc("orderId")));


        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> removeStatusExpress(Long orderId) {
        if (orderId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order0 = ordersRepository.findById(orderId);
        if (order0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order = order0.get();
        order.setExpress(false);
        order.setServiceDate(null);
        order.setServiceDateFrom(null);
        order.setServiceDateTo(null);
        order.setServicePrice(reCalculateServicePrice(order));
        ordersRepository.saveAndFlush(order);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> addStatusExpress(Long orderId, String serviceDateFrom, String serviceDateTo, String serviceDate) {
        if (orderId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (StringUtils.isEmpty(serviceDateFrom) && StringUtils.isEmpty(serviceDateTo) && StringUtils.isEmpty(serviceDate))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order0 = ordersRepository.findById(orderId);

        if (order0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order = order0.get();
        order.setExpress(true);
        order.setServiceDate(StringUtils.isNotEmpty(serviceDate) ? stringToLocalDateTime2(serviceDate) : null);
        order.setServiceDateFrom(StringUtils.isNotEmpty(serviceDateFrom) ? stringToLocalDateTime2(serviceDateFrom) : null);
        order.setServiceDateTo(StringUtils.isNotEmpty(serviceDateTo) ? stringToLocalDateTime2(serviceDateTo) : null);
        order.setServicePrice(reCalculateServicePrice(order));
        ordersRepository.save(order);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrdersCouriersInfoModel>> ordersCouriersInfo(String token) {

        if (StringUtils.isEmpty(token))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        List<OrdersCouriersInfoModel> ans = new ArrayList<>();

        var jobs = orderJobsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            predicate = builder.and(predicate, root.get("courierUserId").isNotNull());
            return predicate;
        });

        if (jobs.isEmpty())
            return new ResponseEntity<>(ans, headers, HttpStatus.OK);

        List<Long> jobIdS = new ArrayList<>();

        for (var j : jobs) {
            jobIdS.add(j.getOrderJobId());
        }

        var orders = ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, builder.equal(root.get("arrivalInProgress"), true));
            predicate = builder.and(predicate, builder.in(root.get("jobId")).value(jobIdS));
            return predicate;
        });

        if (orders.isEmpty())
            return new ResponseEntity<>(ans, headers, HttpStatus.OK);

        for (var o : orders) {
            var m = new OrdersCouriersInfoModel();
            m.update(o, orderJobsRepository.getCourierPhone(o.getJobId()));
            ans.add(m);
        }

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> handedOverJobDispenser(Long orderJobId, Boolean accept) {
        var job = ordersCommonFunctionsService.validateJob(orderJobId);
        if (job ==null)
            return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_ERROR_0", headers, HttpStatus.BAD_REQUEST);

        if (accept) {
            var user = userRepository.findById(job.getSenderUserId().longValue());
            if (user.isEmpty())
                return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_ERROR_1", headers, HttpStatus.BAD_REQUEST);

            switch (lazoUtils.checkIfUserIsVipAndThenCheckExpirationDate(getCurrentApplicationUserId())) {
                case USER_IS_NULL, VIP_EXPIRATION_DATE_TIME_IS_NULL -> {
                    return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_UNEXPECTED_ERROR", headers, HttpStatus.BAD_REQUEST);
                }
                case VIP_STATUS_EXPIRED -> {
                    return new ResponseEntity<>("VIP_STATUS_EXPIRED", headers, HttpStatus.OK);
                }
                case USER_IS_NOT_VIP, USER_IS_VIP -> {
                    if (lazoUtils.userDebtCount() >0)
                        return new ResponseEntity<>("MUST_PAY_DEBT", headers, HttpStatus.OK);
                }
            }

            job.setSenderPhone(user.get().getUsername());
        } else {
            job.setSenderUserId(job.getRewriterUserId());
        }
        job.setRewriterUserId(null);
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);

        return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_SUCCESS", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> handOverJob(Long orderJobId, Integer newSenderUserId) {

        if (orderJobId == null || newSenderUserId == null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (getCurrentApplicationUserId() ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var job0 = orderJobsRepository.findById(orderJobId);

        if (job0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var job = job0.get();
        job.setSenderUserId(newSenderUserId);
        job.setRewriterUserId(getCurrentApplicationUserId());
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);

        notificationsRepository.save(new NotificationsDomain(job.getJobName(), MessagingUtils.HAND_OVER_JOB, newSenderUserId.longValue(), false));
        MessagingUtils.sendCustomMessage(job.getJobName(), MessagingUtils.HAND_OVER_JOB, String.valueOf(newSenderUserId));

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> handOverJobs(String checkedJobs, Integer newSenderUserId) {

        if (StringUtils.isEmpty(checkedJobs) || newSenderUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var listString = checkedJobs.replace("[","").replace("]","");

        if (StringUtils.isEmpty(listString))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        String[] strList = listString.split(",");
        List<Long> lngList = new ArrayList<>();
        for(String s : strList) lngList.add(Long.valueOf(s.trim()));

        for (var l : lngList) {
            if (Boolean.FALSE.equals(handOverJob(l, newSenderUserId).getBody())) {
                return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(true, headers, HttpStatus.OK);

    }

    private Boolean handOverJobToFavCourierCompany(Long orderJobId, Integer newCourierCompanyUserId) {
        if (orderJobId == null || newCourierCompanyUserId == null)
            return false;

        if (getCurrentApplicationUserId() ==null)
            return false;

        var job0 = orderJobsRepository.findById(orderJobId);

        if (job0.isEmpty())
            return false;

        var orders = ordersRepository.findAllByJobId(orderJobId);

        for (var o : orders) {
            o.setCourierCompanyUserId(newCourierCompanyUserId);
            o.setJobId(null);
            ordersRepository.save(o);
        }

        var job = job0.get();

        orderJobsRepository.deleteById(orderJobId);

        notificationsRepository.save(new NotificationsDomain(job.getJobName(), MessagingUtils.HAND_OVER_JOB, newCourierCompanyUserId.longValue(), false));
        MessagingUtils.sendCustomMessage(job.getJobName(), MessagingUtils.HAND_OVER_JOB, String.valueOf(newCourierCompanyUserId));

        return true;
    }

    @Override
    public ResponseEntity<String> handOverJobsToFavCourierCompany(String checkedJobs) {

        if (StringUtils.isEmpty(checkedJobs))
            return new ResponseEntity<>("check_jobs", headers, HttpStatus.OK);

        var listString = checkedJobs.replace("[","").replace("]","");

        if (StringUtils.isEmpty(listString))
            return new ResponseEntity<>("check_jobs", headers, HttpStatus.OK);

        var user0 = usersRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return new ResponseEntity<>("ERROR1", headers, HttpStatus.BAD_REQUEST);

        var user = user0.get();

        var newSenderUserId = user.getFavouriteCourierCompanyId();

        if (newSenderUserId == null)
            return new ResponseEntity<>("choose_fav_courier_company", headers, HttpStatus.OK);

        String[] strList = listString.split(",");
        List<Long> lngList = new ArrayList<>();
        for(String s : strList) lngList.add(Long.valueOf(s.trim()));

        for (var l : lngList) {
            if (Boolean.FALSE.equals(handOverJobToFavCourierCompany(l, newSenderUserId))) {
                return new ResponseEntity<>("ERROR2", headers, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("SUCCESS", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> handOverOneOrderToFavCourierCompany(Long orderId) {

        if (orderId == null)
            return new ResponseEntity<>("orderId is null", headers, HttpStatus.BAD_REQUEST);

        var user0 = usersRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return new ResponseEntity<>("ERROR1", headers, HttpStatus.BAD_REQUEST);

        var user = user0.get();

        var order0 = ordersRepository.findById(orderId);

        if (order0.isEmpty())
            return new ResponseEntity<>("order is empty", headers, HttpStatus.BAD_REQUEST);

        var order = order0.get();
        order.setCourierCompanyUserId(user.getFavouriteCourierCompanyId());
        order.setJobId(null);
        ordersRepository.save(order);

        return new ResponseEntity<>("SUCCESS", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> handOverOneOrderToCourier(Long orderId, Long courierId) {

        if (orderId ==null || courierId ==null)
            return new ResponseEntity<>("1", headers, HttpStatus.BAD_REQUEST);


        var order0 = ordersRepository.findById(orderId);
        if (order0.isEmpty())
            return new ResponseEntity<>("2", headers, HttpStatus.OK);

        var order = order0.get();

        var containsOutOfCountry = false;
        var containsOutOfRegion = false;

        if (Objects.equals(order.getParcelDeliveryType(), OUT_OF_COUNTRY))
            containsOutOfCountry = true;

        if (Objects.equals(order.getParcelDeliveryType(), OUT_OF_REGION))
            containsOutOfRegion = true;

        var user0 = userRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return null;

        var user = user0.get();

        var jobName = lazoUtils.getNickname(user);

        var courierUser = userRepository.findById(courierId);
        if (courierUser.isEmpty())
            return new ResponseEntity<>("3", headers, HttpStatus.BAD_REQUEST);

        var courierUserName = courierUser.get().getUsername();
        if (StringUtils.isEmpty(courierUserName))
            return new ResponseEntity<>("4", headers, HttpStatus.BAD_REQUEST);

        var newCustom = new OrderJobsDomain(ON_HOLD, getCurrentApplicationUserId(), user.getUsername(),
                1, containsOutOfCountry,
                containsOutOfRegion, order.getExpress(), jobName,
                Integer.valueOf(String.valueOf(courierId)), courierUserName);

        order.setJobId(orderJobsRepository.saveAndFlush(newCustom).getOrderJobId());
        ordersRepository.save(order);

        return new ResponseEntity<>("success", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> handOverJobToCourier(String token, Long orderJobId, Integer courierUserId) {
        if (StringUtils.isEmpty(token) || orderJobId == null || courierUserId == null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var job = ordersCommonFunctionsService.validateJob(orderJobId);
        if (job ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var user = userRepository.findById(job.getSenderUserId().longValue());
        if (user.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        job.setCourierUserId(courierUserId);
        job.setCourierPhone(user.get().getUsername());
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Boolean> removeOrderFromJob(String token, Long orderId) {
        if (StringUtils.isEmpty(token) || orderId == null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order = checkOrderAndApproveThatUserOwnsOrdersJob(orderId);
        if (order ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var jobOrders = ordersRepository.findAllByJobId(order.getJobId());
        jobOrders.remove(order);

        var job = updateJobCommonValues(orderJobsRepository.findById(order.getJobId()), jobOrders);
        if (job ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        order.setJobId(null);
        order.setChangeDate(LocalDateTime.now());
        ordersRepository.save(order);

        if (job.getOrderCount() >0)
            job.setOrderCount(job.getOrderCount() -1);
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> sentOrderToAnotherJob(String token, Long orderId, Long newJobId) {
        if (orderId == null || newJobId ==null || StringUtils.isEmpty(token) || checkIfUserOwnsJob(getCurrentApplicationUserId(), newJobId))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order = checkOrderAndApproveThatUserOwnsOrdersJob(orderId);
        if (order ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var OldJobId = order.getJobId();

        //გაანახლებს იმ შეკვეთას საიდანაც გადავიდა ამანათი
        var oldJobOrders = ordersRepository.findAllByJobId(OldJobId);
        oldJobOrders.remove(order);

        var oldJob = updateJobCommonValues(orderJobsRepository.findById(OldJobId), oldJobOrders);
        if (oldJob ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);
        //გაანახლებს იმ შეკვეთას საიდანაც გადავიდა ამანათი


        //გაანახლებს იმ შეკვეთას სადაც გადავიდა ამანათი
        var newJobOrders = ordersRepository.findAllByJobId(newJobId);
        newJobOrders.add(order);

        var newJob = updateJobCommonValues(orderJobsRepository.findById(newJobId), newJobOrders);
        if (newJob ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);
        //გაანახლებს იმ შეკვეთას სადაც გადავიდა ამანათი


        order.setJobId(newJobId);
        order.setChangeDate(LocalDateTime.now());
        ordersRepository.save(order);

        if (oldJob.getOrderCount() >0)
            oldJob.setOrderCount(oldJob.getOrderCount() -1);
        oldJob.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(oldJob);

        newJob.setOrderCount(newJob.getOrderCount() +1);
        newJob.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(newJob);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> updateJobName(Long orderJobId, String jobName) {
        var job = ordersCommonFunctionsService.validateJob(orderJobId);
        if (job ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        job.setJobName(job.getOrderJobId().toString() + ". " + jobName);
        orderJobsRepository.save(job);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

}
