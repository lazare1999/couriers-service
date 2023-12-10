package com.lazo.couriers.utils;

import com.lazo.couriers.app.accounting.repository.DebtsRepository;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.domains.AppUser;
import com.lazo.couriers.app.user.repository.UsersRepository;
import com.lazo.couriers.security.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by Lazo on 2021-04-13
 */

@Service
@RequiredArgsConstructor
public class LazoUtils {

    private final UsersRepository usersRepository;
    private final DebtsRepository debtsRepository;
    private final OrdersRepository ordersRepository;

    public static <T> T mostCommonListValue(List<T> list) {
        if (list.isEmpty())
            return null;

        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max != null ? max.getKey() : null;
    }

    public static ApplicationUser getCurrentApplicationUser() {
        return (ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Integer getCurrentApplicationUserId() {
        return ((ApplicationUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }

    @NotNull
    public static Sort getSortAsc(String s) {
        return Sort.by(new Sort.Order(Sort.Direction.ASC, s));
    }

    @NotNull
    public static Sort getSortDesc(String s) {
        return Sort.by(new Sort.Order(Sort.Direction.DESC, s));
    }


    public enum CheckIfUserIsVipAndThenCheckExpirationDateAnswers {
        USER_IS_NULL, USER_IS_NOT_VIP, VIP_EXPIRATION_DATE_TIME_IS_NULL, VIP_STATUS_EXPIRED, USER_IS_VIP
    }

    public CheckIfUserIsVipAndThenCheckExpirationDateAnswers checkIfUserIsVipAndThenCheckExpirationDate(Integer userId) {

        var user0 = usersRepository.findById(Long.valueOf(userId));

        if (user0.isEmpty())
            return CheckIfUserIsVipAndThenCheckExpirationDateAnswers.USER_IS_NULL;

        var user = user0.get();

        if (!Objects.equals(user.getIsVip(), true))
            return CheckIfUserIsVipAndThenCheckExpirationDateAnswers.USER_IS_NOT_VIP;

        if (user.getVipExpirationDateTime() ==null)
            return CheckIfUserIsVipAndThenCheckExpirationDateAnswers.VIP_EXPIRATION_DATE_TIME_IS_NULL;

        if (user.getVipExpirationDateTime().isAfter(LocalDateTime.now()))
            return CheckIfUserIsVipAndThenCheckExpirationDateAnswers.VIP_STATUS_EXPIRED;


        return CheckIfUserIsVipAndThenCheckExpirationDateAnswers.USER_IS_VIP;
    }

    public long userDebtCount() {
        return debtsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("userId"), getCurrentApplicationUserId()));
            return predicate;
        });
    }

    public String getNickname(AppUser user) {
        var si = new StringBuilder();
        if (StringUtils.isNotEmpty(user.getNickname())) {
            si.append(user.getNickname()).append(" - ");
        } else {
            if (StringUtils.isNotEmpty(user.getFirstName())) {
                si.append(user.getFirstName()).append(" ");
            }

            if (StringUtils.isNotEmpty(user.getLastName())) {
                si.append(user.getLastName()).append(" ");
            }
        }
        return String.valueOf(si);
    }

    public Boolean calculateJobContainsOrNotExpress(List<OrdersDomain> orders) {
        if (orders.isEmpty())
            return false;

        var express = false;

        for (var i : orders) {
            if (i.getExpress()) {
                express = true;
                break;
            }
        }
        return express;
    }

    public boolean setJobIdsToOrders(List<OrdersDomain> orders, Long jobId) {

        if (orders.isEmpty() || jobId ==null) {
            return true;
        }

        Optional<OrdersDomain> order;
        for (var or : orders) {
            order = ordersRepository.findById(or.getOrderId());
            order.ifPresent(ordersDomain -> {
                ordersDomain.setJobId(jobId);
                ordersRepository.save(ordersDomain);
            });
        }
        return false;
    }

}
