package com.lazo.couriers.app.balance.service;

import com.lazo.couriers.app.accounting.domains.DebtsDomain;
import com.lazo.couriers.app.accounting.repository.CourierFeesRepository;
import com.lazo.couriers.app.accounting.repository.DebtsRepository;
import com.lazo.couriers.app.balance.domains.UserCardsDomain;
import com.lazo.couriers.app.balance.models.BalanceStatisticsModel;
import com.lazo.couriers.app.balance.models.CardCredentialsModel;
import com.lazo.couriers.app.balance.repository.UserCardsRepository;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.repository.UsersRepository;
import com.lazo.couriers.utils.LazoUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.List;

import static com.lazo.couriers.utils.LazoDateUtil.stringToLocalDateTime;
import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2022-02-10
 */

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    HttpHeaders headers = new HttpHeaders();

    private final UsersRepository usersRepository;
    private final OrdersRepository ordersRepository;
    private final CourierFeesRepository courierFeesRepository;
    private final DebtsRepository debtsRepository;
    private final UserCardsRepository userCardsRepository;

    @Override
    public ResponseEntity<BalanceStatisticsModel> getBalanceStatistics(String selectedDateFrom, String selectedDateTo) {

        var userId = getCurrentApplicationUserId();
        if (userId ==null || StringUtils.isEmpty(selectedDateFrom) || StringUtils.isEmpty(selectedDateTo))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var selectedDateFromLocalDate = stringToLocalDateTime(selectedDateFrom);
        var selectedDateToLocalDate = stringToLocalDateTime(selectedDateTo);

        var ordersCourierIsUser = ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("handedOverCourierUserId"), userId));
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("changeDate"), selectedDateFromLocalDate));
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("changeDate"), selectedDateToLocalDate));
            return predicate;
        });
        var parcelsAsCourier = ordersCourierIsUser.size();
        var serviceParcelPriceCourier = 0.0;
        for (var o : ordersCourierIsUser) {
            if (o.getServiceParcelPrice() !=null)
                serviceParcelPriceCourier += o.getServiceParcelPrice();

        }

        var ordersSenderIsUser = ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), userId));
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("changeDate"), selectedDateFromLocalDate));
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("changeDate"), selectedDateToLocalDate));
            return predicate;
        });
        var parcelsAsSender = ordersSenderIsUser.size();
        var servicePrice = 0.0;
        var serviceParcelPriceSender = 0.0;
        for (var o : ordersSenderIsUser) {
            if (o.getServicePrice() !=null)
                servicePrice += o.getServicePrice();

            if (o.getServiceParcelPrice() !=null)
                serviceParcelPriceSender += o.getServiceParcelPrice();

        }

        var courierFees = courierFeesRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("userId"), userId));
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("addDate"), selectedDateFromLocalDate));
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("addDate"), selectedDateToLocalDate));
            return predicate;
        });
        var ourShare = 0.0;
        for (var c : courierFees) {
            ourShare += c.getCourierFee();
        }

        var cardDebt = debtsRepository.cardDebtsList(Long.valueOf(userId), false);
        var cardDebtCount = 0.0;
        for (var c: cardDebt) {
            if (c !=null)
                cardDebtCount += c;
        }

        var depositDebt = debtsRepository.depositDebtList(Long.valueOf(userId), false);
        var depositDebtCount = 0.0;
        for (var d: depositDebt) {
            if (d !=null)
                depositDebtCount += d;
        }

        var parcelsAsCourierStr = String.valueOf(parcelsAsCourier);
        var parcelsAsSenderStr = String.valueOf(parcelsAsSender);

        var serviceParcelPriceSenderStr = String.valueOf(serviceParcelPriceSender);
        var serviceParcelPriceCourierStr = String.valueOf(serviceParcelPriceCourier);


        var servicePriceStr = String.valueOf(servicePrice);
        var ourShareStr = String.valueOf(ourShare);
        var cardDebtStr = String.valueOf(cardDebtCount);
        var depositDebtStr = String.valueOf(depositDebtCount);

        var balanceStatistics = new BalanceStatisticsModel(
                parcelsAsCourierStr, parcelsAsSenderStr, servicePriceStr, serviceParcelPriceSenderStr, serviceParcelPriceCourierStr, ourShareStr, cardDebtStr, depositDebtStr);


        return new ResponseEntity<>(balanceStatistics, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getDeposit() {

        var userId = getCurrentApplicationUserId();

        if (userId ==null)
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

        var deposit = usersRepository.depositByUserId(Long.valueOf(userId));

        if (deposit ==null)
            deposit = 0.0;

        return new ResponseEntity<>(deposit.toString(), headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<List<DebtsDomain>> getCardDebts(Integer pageKey, Integer pageSize, Boolean card, Boolean deposit) {

        var userId = getCurrentApplicationUserId();
        if(pageKey ==null || pageSize ==null || userId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page = debtsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("userId"), userId));
            predicate = builder.and(predicate, builder.equal(root.get("paid"), false));

            if (card) {
                predicate = builder.and(predicate, root.get("cardDebt").isNotNull());
            } else if (deposit) {
                predicate = builder.and(predicate, root.get("depositDebt").isNotNull());
            }


            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("debtId")));


        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> payDepositDebt() {

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>("UNEXPECTED_ERROR", headers, HttpStatus.OK);

        var user0 = usersRepository.findById(Long.valueOf(userId));
        if (user0.isEmpty())
            return new ResponseEntity<>("UNEXPECTED_ERROR", headers, HttpStatus.OK);

        var user = user0.get();

        var depositDebt = debtsRepository.depositDebtList(Long.valueOf(userId), false);
        var depositDebtCount = 0.0;
        for (var d: depositDebt) {
            if (d !=null)
                depositDebtCount += d;
        }

        if (depositDebtCount ==0.0)
            return new ResponseEntity<>("YOU_DID_NOT_HAVE_DEPOSIT_DEBTS", headers, HttpStatus.OK);

        var deposit = user.getDeposit();
        if (deposit ==null)
            return new ResponseEntity<>("UNEXPECTED_ERROR", headers, HttpStatus.OK);

        if (depositDebtCount > deposit)
            return new ResponseEntity<>("DEPOSIT_BALANCE_IS_NOT_ENOUGH", headers, HttpStatus.OK);

        user.setDeposit(deposit - depositDebtCount);
        usersRepository.save(user);
        return new ResponseEntity<>("DEBT_IS_PAID", headers, HttpStatus.OK);
    }

    //  TODO : ვალის გადახდა ბარათით
    @Override
    public ResponseEntity<String> payCardDebt() {
        var debt = debtsRepository.cardDebtsListObj(Long.valueOf(getCurrentApplicationUserId()), false);
        debtsRepository.deleteAll(debt);
        return new ResponseEntity<>("DEBT_IS_PAID", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> setCardCredentials(CardCredentialsModel model) {

        if (StringUtils.isEmpty(model.getCardNumber()) ||
                StringUtils.isEmpty(model.getExpiryDate()) ||
                StringUtils.isEmpty(model.getCardHolderName()) ||
                StringUtils.isEmpty(model.getCvvCode())) {
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);
        }

        userCardsRepository.save(new UserCardsDomain(model));
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<UserCardsDomain>> getUserCardCredentials() {
        var ans = userCardsRepository.findAllByUserId(Long.valueOf(getCurrentApplicationUserId()));
        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

}
