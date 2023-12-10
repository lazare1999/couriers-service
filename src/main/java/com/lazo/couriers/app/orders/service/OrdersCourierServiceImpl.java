package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.accounting.domains.CourierFeesDomain;
import com.lazo.couriers.app.accounting.domains.DebtsDomain;
import com.lazo.couriers.app.accounting.domains.DepositCashFlowDomain;
import com.lazo.couriers.app.accounting.repository.CourierFeesRepository;
import com.lazo.couriers.app.accounting.repository.DebtsRepository;
import com.lazo.couriers.app.accounting.repository.DepositCashFlowRepository;
import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.JobsModel;
import com.lazo.couriers.app.orders.repository.OrderJobsRepository;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.domains.NotificationsDomain;
import com.lazo.couriers.app.user.domains.UsersDomain;
import com.lazo.couriers.app.user.repository.NotificationsRepository;
import com.lazo.couriers.app.user.repository.UserRepository;
import com.lazo.couriers.app.user.repository.UsersRepository;
import com.lazo.couriers.utils.LazoUtils;
import com.lazo.couriers.utils.MessagingUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.lazo.couriers.app.orders.domain.OrderJobsDomain.OrderJobsStatus.ON_HOLD;
import static com.lazo.couriers.utils.LazoUtils.CheckIfUserIsVipAndThenCheckExpirationDateAnswers.USER_IS_VIP;
import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2021-10-08
 */


@Service
@RequiredArgsConstructor
public class OrdersCourierServiceImpl implements OrdersCourierService {


    @Value("${co.module.percentage_of_service}")
    public String PERCENTAGE_OF_SERVICE;

    HttpHeaders headers = new HttpHeaders();

    private final OrdersCommonFunctionsService ordersCommonFunctionsService;
    private final OrdersSenderService ordersSenderService;
    private final OrdersRepository ordersRepository;
    private final OrderJobsRepository orderJobsRepository;
    private final UserRepository userRepository;
    private final UsersRepository usersRepository;
    private final DebtsRepository debtsRepository;
    private final CourierFeesRepository courierFeesRepository;
    private final DepositCashFlowRepository depositCashFlowRepository;
    private final NotificationsRepository notificationsRepository;
    private final LazoUtils lazoUtils;

    private Page<OrderJobsDomain> getCourierJobs(Integer pageKey, Integer pageSize, OrderJobsDomain.OrderJobsStatus status, String token) {

        if (pageKey ==null || pageSize ==null || status ==null || StringUtils.isEmpty(token))
            return null;

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return null;

        return orderJobsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), status));
            predicate = builder.and(predicate, builder.equal(root.get("courierUserId"), userId));
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("orderCount")));
    }


    private void checkIfJobIsDoneAndConcludeRespectively(OrdersDomain order) {
        var jobId = order.getJobId();
        var job0 = orderJobsRepository.findById(jobId);
        if (job0.isEmpty())
            return;

        var job = job0.get();

        var countParcels = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("jobId"), jobId));
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            return predicate;
        });

        if (!Objects.equals(countParcels, 0L))
            return;

        notificationsRepository.save(new NotificationsDomain(job.getJobName(), MessagingUtils.JOBS_DONE, job.getSenderUserId().longValue(), true));
        MessagingUtils.sendCustomMessage(job.getJobName(), MessagingUtils.JOBS_DONE, job.getSenderUserId().toString());

        calculateDebts(jobId);

        job.setOrderJobStatus(OrderJobsDomain.OrderJobsStatus.DONE);
        orderJobsRepository.save(job);

    }


//           TODO : გატესტე
    private void calculateDebts(Long jobId) {

        if (jobId ==null)
            return;

        var parcelsInTheJob = ordersRepository.findAllByJobId(jobId);

        if (parcelsInTheJob.isEmpty())
            return;


        for (var p : parcelsInTheJob) {

            switch (p.getServicePaymentType()) {
                case CARD -> {
                    takeOurShareFromCourier(p);
                    debtsRepository.save(new DebtsDomain(p.getSenderUserId(), jobId, p.getOrderId(), p.getServicePrice(), 0.0));
                }
                case DELIVERY -> {

                    takeOurShareFromCourierDelivery(p);

                    if (Objects.equals(p.getCourierHasParcelMoney(), true))
                        courierHasParcelMoney(jobId, p);

                }
                case TAKING -> {
                    takeOurShareFromCourier(p);
                    debtsRepository.save(new DebtsDomain(p.getSenderUserId(), jobId, p.getOrderId(), p.getServicePrice(), 0.0));

                    if (Objects.equals(p.getCourierHasParcelMoney(), true))
                        courierHasParcelMoney(jobId, p);

                }

            }

        }

    }

    private void courierHasParcelMoney(Long jobId, OrdersDomain p) {

        debtsRepository.save(new DebtsDomain(p.getHandedOverCourierUserId(), jobId, p.getOrderId(), 0.0, p.getServiceParcelPrice()));

        var user0 = usersRepository.findById(Long.valueOf(p.getSenderUserId()));
        if (user0.isEmpty())
            return;

        var user = user0.get();

        depositCashFlowRepository.save(new DepositCashFlowDomain(user.getUserId(), p.getServiceParcelPrice(), 0.0, user.getDeposit()));
        user.setDeposit(user.getDeposit() + p.getServiceParcelPrice());
        usersRepository.save(user);
    }

    private UsersDomain checkUserWhileTakingOurShare(OrdersDomain p) {
        var courierUserId = p.getHandedOverCourierUserId();

        if (courierUserId ==null)
            return null;

        var courierIsVip = Objects.equals(lazoUtils.checkIfUserIsVipAndThenCheckExpirationDate(courierUserId), USER_IS_VIP);
        if (courierIsVip)
            return null;

        var user0 = usersRepository.findById(Long.valueOf(courierUserId));
        if (user0.isEmpty())
            return null;

        return user0.get();
    }

    private void takeOurShareFromCourier(OrdersDomain p) {

        var servicePrice = p.getServicePrice();
        var user = checkUserWhileTakingOurShare(p);
        if (servicePrice ==null || user ==null)
            return;

//        TODO : შეიძლება ცხრილში გატანა 'PERCENTAGE_OF_SERVICE'-ის
        double courierShareRatio = 100.0 - Double.parseDouble(PERCENTAGE_OF_SERVICE);
        double couriersShare = (servicePrice * courierShareRatio) / 100;

        depositCashFlowRepository.save(new DepositCashFlowDomain(user.getUserId(), couriersShare, 0.0, user.getDeposit()));
        user.setDeposit(user.getDeposit() + couriersShare);
        courierFeesRepository.save(new CourierFeesDomain(user.getUserId(), p.getOrderId(), servicePrice - couriersShare));
        usersRepository.save(user);
    }

    private void takeOurShareFromCourierDelivery(OrdersDomain p) {

        var servicePrice = p.getServicePrice();
        var user = checkUserWhileTakingOurShare(p);
        if (servicePrice ==null || user ==null)
            return;

//        TODO : შეიძლება ცხრილში გატანა 'PERCENTAGE_OF_SERVICE'-ის
        double ourShareRatio = Double.parseDouble(PERCENTAGE_OF_SERVICE);
        double ourShare = (servicePrice * ourShareRatio) / 100;

        depositCashFlowRepository.save(new DepositCashFlowDomain(user.getUserId(), 0.0, ourShare, user.getDeposit()));
        user.setDeposit(user.getDeposit() - ourShare);
        courierFeesRepository.save(new CourierFeesDomain(user.getUserId(), p.getOrderId(), ourShare));
        usersRepository.save(user);
    }


    @Override
    public ResponseEntity<JobsModel> getCourierJobs(String token) {
        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        JobsModel m = new JobsModel();

        m.setHandedOverJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), ON_HOLD));
            predicate = builder.and(predicate, builder.equal(root.get("courierUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            return predicate;
        }));
        m.setActiveJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            predicate = builder.and(predicate, builder.equal(root.get("courierUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            return predicate;
        }));
        m.setDoneJobs(orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("courierUserId"), userId));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            return predicate;
        }));

        return new ResponseEntity<>(m, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getActiveCourierJobs(String token, Integer pageKey, Integer pageSize) {

        var page = getCourierJobs(pageKey, pageSize, OrderJobsDomain.OrderJobsStatus.ACTIVE, token);
        List<OrderJobsDomain> ans = new ArrayList<>();

        if (page !=null)
            ans = page.toList();

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getDoneCourierJobs(String token, Integer pageKey, Integer pageSize) {

        var page = getCourierJobs(pageKey, pageSize, OrderJobsDomain.OrderJobsStatus.DONE, token);
        List<OrderJobsDomain> ans = new ArrayList<>();

        if (page !=null)
            ans = page.toList();

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getHandedOverJobsCourier(String token, Integer pageKey, Integer pageSize) {
        if (pageKey ==null || pageSize ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page = orderJobsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), ON_HOLD));
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            predicate = builder.and(predicate, builder.equal(root.get("courierUserId"), userId));
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("orderCount")));

        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> handedOverJobDispenserForCourier(Long orderJobId, Boolean accept) {
        var job = ordersCommonFunctionsService.validateJob(orderJobId);
        if (job ==null)
            return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_FOR_COURIER_ERROR_0", headers, HttpStatus.BAD_REQUEST);

        if (accept) {

            switch (lazoUtils.checkIfUserIsVipAndThenCheckExpirationDate(getCurrentApplicationUserId())) {
                case USER_IS_NULL, VIP_EXPIRATION_DATE_TIME_IS_NULL -> {
                    return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_FOR_COURIER_UNEXPECTED_ERROR", headers, HttpStatus.BAD_REQUEST);
                }
                case VIP_STATUS_EXPIRED -> {
                    return new ResponseEntity<>("VIP_STATUS_EXPIRED", headers, HttpStatus.OK);
                }
                case USER_IS_NOT_VIP, USER_IS_VIP -> {
                    if (lazoUtils.userDebtCount() >0)
                        return new ResponseEntity<>("MUST_PAY_DEBT", headers, HttpStatus.OK);
                }
            }

            job.setOrderJobStatus(OrderJobsDomain.OrderJobsStatus.ACTIVE);
        } else {
            job.setCourierPhone(null);
            job.setCourierUserId(null);
        }
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);

        return new ResponseEntity<>("HANDED_OVER_JOB_DISPENSER_FOR_COURIER_SUCCESS", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrdersDomain>> getParcels(String token, boolean courierParcelsOnly) {
        var userId = getCurrentApplicationUserId();
        if (userId==null && courierParcelsOnly)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        List<Long> courierJobsIds;
        if (courierParcelsOnly) {
            courierJobsIds = orderJobsRepository.getOrderJobsIdsViaCourierUserId(userId);
        } else {
            courierJobsIds = orderJobsRepository.getOrderJobsIdsWhereCourierIdIsNull();
        }

        List<OrdersDomain> parcels = new ArrayList<>();
        if (courierJobsIds.isEmpty())
            return new ResponseEntity<>(parcels, headers, HttpStatus.OK);

        parcels = ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, builder.in(root.get("jobId")).value(courierJobsIds));
            return predicate;
        }, Sort.by(new Sort.Order(Sort.Direction.ASC, "arrivalInProgress")));

        return new ResponseEntity<>(parcels, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrdersDomain>> getParcelsByJob(Integer pageKey, Integer pageSize, Long orderJobId) {
        if (pageKey ==null || pageSize==null|| orderJobId==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page =  ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("jobId"), orderJobId));
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("express")));


        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> approveJob(Long orderJobId) {

        switch (lazoUtils.checkIfUserIsVipAndThenCheckExpirationDate(getCurrentApplicationUserId())) {
            case USER_IS_NULL, VIP_EXPIRATION_DATE_TIME_IS_NULL -> {
                return new ResponseEntity<>("APPROVE_JOB_UNEXPECTED_ERROR", headers, HttpStatus.BAD_REQUEST);
            }
            case VIP_STATUS_EXPIRED -> {
                return new ResponseEntity<>("VIP_STATUS_EXPIRED", headers, HttpStatus.OK);
            }
            case USER_IS_NOT_VIP, USER_IS_VIP -> {
                if (lazoUtils.userDebtCount() >0)
                    return new ResponseEntity<>("MUST_PAY_DEBT", headers, HttpStatus.OK);
            }
        }


        if (orderJobId==null)
            return new ResponseEntity<>("APPROVE_JOB_ERROR_0", headers, HttpStatus.BAD_REQUEST);

        var courierUserId = getCurrentApplicationUserId();
        if (courierUserId ==null)
            return new ResponseEntity<>("APPROVE_JOB_ERROR_1", headers, HttpStatus.BAD_REQUEST);

        var user = userRepository.findById(courierUserId.longValue());
        if (user.isEmpty())
            return new ResponseEntity<>("APPROVE_JOB_ERROR_2", headers, HttpStatus.BAD_REQUEST);

        var job = orderJobsRepository.getFreeToTakeOrderByOrderId(orderJobId);

        if (job ==null)
            return new ResponseEntity<>("ALREADY_TAKEN", headers, HttpStatus.OK);

        var courierUser = user.get().getUsername();
        if (StringUtils.isEmpty(courierUser))
            return new ResponseEntity<>("APPROVE_JOB_ERROR_3", headers, HttpStatus.BAD_REQUEST);

        job.setCourierUserId(courierUserId);
        job.setCourierPhone(courierUser);
        job.setChangeDate(LocalDateTime.now());
        orderJobsRepository.save(job);

        notificationsRepository.save(new NotificationsDomain(job.getJobName(), MessagingUtils.COURIER_APPROVED_JOB, job.getSenderUserId().longValue(), false));
        MessagingUtils.sendCustomMessage(job.getJobName(), MessagingUtils.COURIER_APPROVED_JOB, String.valueOf(job.getSenderUserId()));

        return new ResponseEntity<>("APPROVE_JOB_SUCCESS", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<OrderJobsDomain>> getVacantJobs() {

        var ans = orderJobsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, root.get("courierUserId").isNull());
            predicate = builder.and(predicate, root.get("courierPhone").isNull());
            predicate = builder.and(predicate, root.get("rewriterUserId").isNull());
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.ACTIVE));
            return predicate;
        });

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> setParcelAsUnsuccessful(Long orderId) {
        var order = ordersCommonFunctionsService.getOrder(orderId);
        if (order ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (Objects.equals(order.getExpress(), true) &&
                order.getServiceDate() ==null &&
                order.getServiceDateFrom() ==null &&
                order.getServiceDateTo() ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        order.setOrderStatus(OrdersDomain.OrderStatus.UNSUCCESSFUL);
        order.setArrivalInProgress(false);
        order.setChangeDate(LocalDateTime.now());
        ordersRepository.saveAndFlush(order);
        checkIfJobIsDoneAndConcludeRespectively(order);

        notificationsRepository.save(new NotificationsDomain(order.getServiceParcelIdentifiable(), MessagingUtils.PARCEL_UNSUCCESSFUL, order.getSenderUserId().longValue(), true));
        MessagingUtils.sendCustomMessage(order.getServiceParcelIdentifiable(), MessagingUtils.PARCEL_UNSUCCESSFUL, String.valueOf(order.getSenderUserId()));

        return new ResponseEntity<>(checkExpressDates(orderId, order), headers, HttpStatus.OK);

    }

    private boolean checkExpressDates(Long orderId, OrdersDomain order) {
        if (order.getExpress()) {

            if (order.getServiceDate() !=null && LocalDateTime.now().isAfter(order.getServiceDate())) {
                ordersSenderService.removeStatusExpress(orderId);
                return false;
            }

            if (order.getServiceDateTo() !=null && LocalDateTime.now().isAfter(order.getServiceDateTo())) {
                ordersSenderService.removeStatusExpress(orderId);
                return false;
            }

        }
        return true;
    }

    @Override
    public ResponseEntity<Boolean> takeParcel(Long orderId) {
        var order = ordersCommonFunctionsService.getOrder(orderId);
        if (order ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        order.setArrivalInProgress(true);
        order.setChangeDate(LocalDateTime.now());
        ordersRepository.save(order);

        notificationsRepository.save(new NotificationsDomain(order.getServiceParcelIdentifiable(), MessagingUtils.COURIER_TOOK_PARCEL, order.getSenderUserId().longValue(), false));
        MessagingUtils.sendCustomMessage(order.getServiceParcelIdentifiable(), MessagingUtils.COURIER_TOOK_PARCEL, String.valueOf(order.getSenderUserId()));

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> jobsDone(Long orderId) {
        var courierUserId = getCurrentApplicationUserId();
        if (courierUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var order = ordersCommonFunctionsService.getOrder(orderId);

        if (order ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (order.getServiceDate() ==null && order.getServiceDateFrom() ==null && order.getServiceDateTo() ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        order.setHandedOverCourierUserId(courierUserId);
        order.setOrderStatus(OrdersDomain.OrderStatus.DONE);
        order.setArrivalInProgress(false);
        order.setChangeDate(LocalDateTime.now());
        ordersRepository.saveAndFlush(order);

        notificationsRepository.save(new NotificationsDomain(order.getServiceParcelIdentifiable(), MessagingUtils.JOBS_DONE, order.getSenderUserId().longValue(), true));
        MessagingUtils.sendCustomMessage(order.getServiceParcelIdentifiable(), MessagingUtils.JOBS_DONE, String.valueOf(order.getSenderUserId()));

        checkIfJobIsDoneAndConcludeRespectively(order);
        return new ResponseEntity<>(checkExpressDates(orderId, order), headers, HttpStatus.OK);
    }

}
