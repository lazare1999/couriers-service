package com.lazo.couriers.app.accounting.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Lazo on 2022-01-24
 */

//    TODO : დასამატებელია სერვერის ბაზაშიც

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "accounting", name = "debts")
public class DebtsDomain {

    @Id
    @Column(name = "debt_id")
    @SequenceGenerator(name = "debts_debt_id_seq", sequenceName = "accounting.debts_debt_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debts_debt_id_seq")
    private Long debtId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "parcel_id")
    private Long parcelId;

    @Column(name = "card_debt")
    private Double cardDebt;

    @Column(name = "deposit_debt")
    private Double depositDebt;

    @Column(name = "paid")
    private Boolean paid;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDate addDate;

    @Column(name = "change_date", insertable = false)
    private LocalDateTime changeDate;

    public DebtsDomain(Integer userId, Long jobId, Long parcelId, Double cardDebt, Double depositDebt) {
        this.userId = Long.valueOf(userId);
        this.jobId = jobId;
        this.parcelId = parcelId;
        this.cardDebt = cardDebt;
        this.depositDebt = depositDebt;
        this.paid = false;
    }


}
