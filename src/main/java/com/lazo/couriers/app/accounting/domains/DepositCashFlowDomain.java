package com.lazo.couriers.app.accounting.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Created by Lazo on 2022-03-04
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "accounting", name = "deposit_cash_flow")
public class DepositCashFlowDomain {

    @Id
    @Column(name = "deposit_cash_flow_id")
    @SequenceGenerator(name = "deposit_cash_flow_deposit_cash_flow_id_seq", sequenceName = "accounting.deposit_cash_flow_deposit_cash_flow_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deposit_cash_flow_deposit_cash_flow_id_seq")
    private Long courierFeeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "flow_in")
    private Double flowIn;

    @Column(name = "flow_out")
    private Double flowOut;

    @Column(name = "current_deposit_balance")
    private Double currentDepositBalance;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDate addDate;

    public DepositCashFlowDomain(Long userId, Double flowIn, Double flowOut, Double currentDepositBalance) {
        this.userId = userId;
        this.flowIn = flowIn;
        this.flowOut = flowOut;
        this.currentDepositBalance = currentDepositBalance;
    }

}
