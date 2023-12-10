package com.lazo.couriers.app.orders.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Lazo on 2021-04-08
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "orders", name = "order_jobs")
public class OrderJobsDomain {

    public enum OrderJobsStatus {
        ACTIVE, DONE, ON_HOLD
    }

    @Id
    @Column(name = "job_id")
    @SequenceGenerator(name = "order_jobs_job_id_seq", sequenceName = "orders.order_jobs_job_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_jobs_job_id_seq")
    private Long orderJobId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "order_job_status")
    private OrderJobsStatus orderJobStatus;

    @Column(name = "sender_user_id")
    private Integer senderUserId;

    @Column(name = "rewriter_user_id")
    private Integer rewriterUserId;

    @Column(name = "sender_phone")
    private String senderPhone;

    @Column(name = "courier_user_id")
    private Integer courierUserId;

    @Column(name = "courier_phone")
    private String courierPhone;

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "contains_delivery_type_out_of_country")
    private Boolean containsDeliveryTypeOutOfCountry;

    @Column(name = "contains_delivery_type_out_of_region")
    private Boolean containsDeliveryTypeOutOfRegion;

    @Column(name = "contains_express_order")
    private Boolean containsExpressOrder;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDate addDate;

    @Column(name = "change_date", insertable = false)
    private LocalDateTime changeDate;

    public OrderJobsDomain(OrderJobsStatus jobStatus, Integer senderUserId, String senderPhone, int orderCount,
                           boolean containsDeliveryTypeOutOfCountry, boolean containsDeliveryTypeOutOfRegion,
                           boolean containsExpressOrder, String jobName) {

        this.orderJobStatus = jobStatus ==null ? OrderJobsStatus.ACTIVE: jobStatus;
        this.senderUserId = senderUserId;
        this.senderPhone = senderPhone;
        this.orderCount = orderCount;
        this.containsDeliveryTypeOutOfCountry = containsDeliveryTypeOutOfCountry;
        this.containsDeliveryTypeOutOfRegion = containsDeliveryTypeOutOfRegion;
        this.containsExpressOrder = containsExpressOrder;
        this.jobName = jobName;

    }

    public OrderJobsDomain(OrderJobsStatus jobStatus, Integer senderUserId, String senderPhone, int orderCount,
                           boolean containsDeliveryTypeOutOfCountry, boolean containsDeliveryTypeOutOfRegion,
                           boolean containsExpressOrder, String jobName, Integer courierUserId, String courierPhone) {

        this.orderJobStatus = jobStatus ==null ? OrderJobsStatus.ACTIVE: jobStatus;
        this.senderUserId = senderUserId;
        this.senderPhone = senderPhone;
        this.orderCount = orderCount;
        this.containsDeliveryTypeOutOfCountry = containsDeliveryTypeOutOfCountry;
        this.containsDeliveryTypeOutOfRegion = containsDeliveryTypeOutOfRegion;
        this.containsExpressOrder = containsExpressOrder;
        this.jobName = jobName;
        this.courierUserId = courierUserId;
        this.courierPhone = courierPhone;

    }

}
