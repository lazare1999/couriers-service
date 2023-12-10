package com.lazo.couriers.app.accounting.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Created by Lazo on 2022-03-02
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "accounting", name = "courier_fees")
public class CourierFeesDomain {

    @Id
    @Column(name = "courier_fee_id")
    @SequenceGenerator(name = "courier_fees_courier_fee_id_seq", sequenceName = "accounting.courier_fees_courier_fee_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "courier_fees_courier_fee_id_seq")
    private Long courierFeeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "courier_fee")
    private Double courierFee;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDate addDate;

    public CourierFeesDomain(Long userId, Long orderId, Double courierFee) {
        this.userId = userId;
        this.orderId = orderId;
        this.courierFee = courierFee;
    }


}
