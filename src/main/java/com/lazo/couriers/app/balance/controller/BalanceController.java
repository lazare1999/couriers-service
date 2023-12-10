package com.lazo.couriers.app.balance.controller;

import com.lazo.couriers.app.accounting.domains.DebtsDomain;
import com.lazo.couriers.app.balance.domains.UserCardsDomain;
import com.lazo.couriers.app.balance.models.BalanceStatisticsModel;
import com.lazo.couriers.app.balance.models.CardCredentialsModel;
import com.lazo.couriers.app.balance.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Lazo on 2022-02-10
 */

@RestController
@RequestMapping("balance")
@RequiredArgsConstructor
public class BalanceController {


    private final BalanceService balanceService;

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_balance_statistics" })
    public ResponseEntity<BalanceStatisticsModel> getBalanceStatistics(String selectedDateFrom, String selectedDateTo) {
        return balanceService.getBalanceStatistics(selectedDateFrom, selectedDateTo);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_deposit" })
    public ResponseEntity<String> getDeposit() {
        return balanceService.getDeposit();
    }


    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_card_debts" })
    public ResponseEntity<List<DebtsDomain>> getCardDebts(Integer pageKey, Integer pageSize, Boolean card, Boolean deposit) {
        return balanceService.getCardDebts(pageKey, pageSize, card, deposit);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/pay_deposit_debt" })
    public ResponseEntity<String> payDepositDebt() {
        return balanceService.payDepositDebt();
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/pay_card_debt" })
    public ResponseEntity<String> payCardDebt() {
        return balanceService.payCardDebt();
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/set_card_credentials" })
    public ResponseEntity<Boolean> setCardCredentials(CardCredentialsModel model) {
        return balanceService.setCardCredentials(model);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_user_card_credentials" })
    public ResponseEntity<List<UserCardsDomain>> getUserCardCredentials() {
        return balanceService.getUserCardCredentials();
    }

}
