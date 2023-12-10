package com.lazo.couriers.app.orders.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by Lazo on 2021-04-12
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "orders", name = "order_type")
public class OrderServiceTypeDomain {

    @Id
    @Column(name = "service_type_id")
    @SequenceGenerator(name = "order_type_service_type_id_seq", sequenceName = "orders.order_type_service_type_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_type_service_type_id_seq")
    private Long serviceTypeId;

    @Column(name = "service_price_or_ratio")
    private String serviceCostOrRatio;

    @Column(name = "description")
    private String description;

}
