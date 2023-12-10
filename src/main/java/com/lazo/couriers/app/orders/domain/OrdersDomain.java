package com.lazo.couriers.app.orders.domain;

import com.lazo.couriers.app.orders.models.OrderModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import static com.lazo.couriers.app.orders.domain.OrdersDomain.ParcelDeliveryType.*;
import static com.lazo.couriers.app.orders.domain.OrdersDomain.ParcelType.*;
import static com.lazo.couriers.utils.LazoDateUtil.stringToLocalDateTime2;

/**
 * Created by Lazo on 2021-04-05
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "orders", name = "orders")
public class OrdersDomain {

    public enum ServicePaymentType {
        CARD, DELIVERY, TAKING
    }

    public enum ParcelDeliveryType {
        IN_REGION, OUT_OF_REGION, OUT_OF_COUNTRY
    }

    public enum OrderStatus {
        ACTIVE, DONE, UNSUCCESSFUL
    }

    public enum ParcelType {
        SMALL, BIG, FOOD
    }

    @Id
    @Column(name = "order_id")
    @SequenceGenerator(name = "orders_order_id_seq", sequenceName = "orders.orders_order_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_order_id_seq")
    private Long orderId;

    @Column(name = "sender_user_id")
    private Integer senderUserId;

    @Column(name = "column_31")
    private Integer courierCompanyUserId;

    @Column(name = "job_id")
    private Long jobId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "express")
    private Boolean express;

    @Column(name = "parcel_pickup_address_latitude")
    private String parcelPickupAddressLatitude;

    @Column(name = "parcel_pickup_address_longitude")
    private String parcelPickupAddressLongitude;

    @Column(name = "parcel_address_to_be_delivered_latitude")
    private String parcelAddressToBeDeliveredLatitude;

    @Column(name = "parcel_address_to_be_delivered_longitude")
    private String parcelAddressToBeDeliveredLongitude;

    @Column(name = "service_price")
    private Double servicePrice;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "service_payment_type")
    private ServicePaymentType servicePaymentType;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "service_date")
    private LocalDateTime serviceDate;

    @Column(name = "service_date_from")
    private LocalDateTime serviceDateFrom;

    @Column(name = "service_date_to")
    private LocalDateTime serviceDateTo;

    @Column(name = "service_parcel_price")
    private Double serviceParcelPrice;

    @Column(name = "service_parcel_identifiable")
    private String serviceParcelIdentifiable;

    @Column(name = "order_comment")
    private String orderComment;

    @Column(name = "pickup_admin_area")
    private String pickupAdminArea;

    @Column(name = "pickup_country_code")
    private String pickupCountryCode;

    @Column(name = "to_be_delivered_admin_area")
    private String toBeDeliveredAdminArea;

    @Column(name = "to_be_delivered_country_code")
    private String toBeDeliveredCountryCode;

    @Column(name = "total_distance")
    private String totalDistance;

    @Column(name = "viewer_phone")
    private String viewerPhone;

    @Column(name = "handed_over_courier_user_id")
    private Integer handedOverCourierUserId;

    @Column(name = "arrival_in_progress")
    private Boolean arrivalInProgress;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "parcel_delivery_type")
    private ParcelDeliveryType parcelDeliveryType;

    @Column(name = "currency")
    private String currency;

    @Column(name = "parcel_type")
    private ParcelType parcelType;

    @Column(name = "courier_has_parcel_money", insertable = false)
    private Boolean courierHasParcelMoney;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDate addDate;

    @Column(name = "change_date", insertable = false)
    private LocalDateTime changeDate;

    public OrdersDomain(OrderModel model, Integer senderUserId) {

        this.senderUserId = senderUserId;
        this.orderStatus = OrderStatus.ACTIVE;

        this.express = model.getExpress() != null && Boolean.parseBoolean(model.getExpress());
        this.parcelPickupAddressLatitude = model.getParcelPickupAddressLatitude();
        this.parcelPickupAddressLongitude = model.getParcelPickupAddressLongitude();
        this.parcelAddressToBeDeliveredLatitude = model.getParcelAddressToBeDeliveredLatitude();
        this.parcelAddressToBeDeliveredLongitude = model.getParcelAddressToBeDeliveredLongitude();

        String[] asd = model.getServicePrice().split(" ");
        this.servicePrice = Double.parseDouble(asd[0]);

        switch (model.getServicePaymentType()) {
            case "0" -> this.servicePaymentType = ServicePaymentType.CARD;
            case "1" -> this.servicePaymentType = ServicePaymentType.DELIVERY;
            case "2" -> this.servicePaymentType = ServicePaymentType.TAKING;
        }

        this.clientName = model.getClientName();
        this.serviceDate = StringUtils.isNotEmpty(model.getServiceDate()) ? stringToLocalDateTime2(model.getServiceDate()) : null;
        this.serviceDateFrom = StringUtils.isNotEmpty(model.getServiceDateFrom()) ? stringToLocalDateTime2(model.getServiceDateFrom()) : null;
        this.serviceDateTo = StringUtils.isNotEmpty(model.getServiceDateTo()) ? stringToLocalDateTime2(model.getServiceDateTo()) : null;
        this.serviceParcelPrice = StringUtils.isNotEmpty(model.getServiceParcelPrice()) ? Double.valueOf(model.getServiceParcelPrice()) : null;
        this.serviceParcelIdentifiable = model.getServiceParcelIdentifiable();
        this.orderComment = model.getOrderComment();
        this.pickupAdminArea = model.getPickupAdminArea();
        this.pickupCountryCode = model.getPickupCountryCode();
        this.toBeDeliveredAdminArea = model.getToBeDeliveredAdminArea();
        this.toBeDeliveredCountryCode = model.getToBeDeliveredCountryCode();
        this.totalDistance = model.getTotalDistance();
        this.viewerPhone = model.getViewerPhone();
        this.arrivalInProgress = false;

        if (!Objects.equals(model.getPickupCountryCode(), model.getToBeDeliveredCountryCode())) {
            this.parcelDeliveryType = OUT_OF_COUNTRY;
        } else if (!Objects.equals(model.getPickupAdminArea(), model.getToBeDeliveredAdminArea())) {
            this.parcelDeliveryType = OUT_OF_REGION;
        } else {
            this.parcelDeliveryType = IN_REGION;
        }

        this.currency = Currency.getInstance(new Locale("", model.getPickupCountryCode())).getCurrencyCode();

        var pType = model.getParcelType() ==null ? 0 : model.getParcelType();
        switch (pType) {
            case 1 -> this.parcelType = BIG;
            case 2 -> this.parcelType = FOOD;
            default -> this.parcelType = SMALL;
        }

        this.courierHasParcelMoney = model.getCourierHasParcelMoney();

    }
}
