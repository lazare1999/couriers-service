package com.lazo.couriers.app.balance.domains;

import com.lazo.couriers.app.balance.models.CardCredentialsModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2022-04-06
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "user_cards")
public class UserCardsDomain {

    @Id
    @Column(name = "user_cards_id")
    @SequenceGenerator(name = "user_cards_user_cards_id_seq", sequenceName = "users.user_cards_user_cards_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_cards_user_cards_id_seq")
    private Long userCardsId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "valid_thru")
    private String validThru;

    @Column(name = "card_holder")
    private String cardHolder;

    @Column(name = "cvv")
    private String cvv;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDate addDate;

    public UserCardsDomain(CardCredentialsModel model) {
        this.userId = Long.valueOf(getCurrentApplicationUserId());
        this.cardNumber = model.getCardNumber();
        this.validThru = model.getExpiryDate();
        this.cardHolder = model.getCardHolderName();
        this.cvv = model.getCvvCode();
    }
}
