package com.lazo.couriers.app.mini_menu.service;

import com.lazo.couriers.app.mini_menu.models.AllUserModel;
import com.lazo.couriers.app.mini_menu.models.FavUserStatisticsModel;
import com.lazo.couriers.app.mini_menu.models.ProfileModel;
import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.repository.OrderJobsRepository;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.domains.AppUser;
import com.lazo.couriers.app.user.domains.NotificationsDomain;
import com.lazo.couriers.app.user.domains.UsersFavoriteUsersDomain;
import com.lazo.couriers.app.user.repository.NotificationsRepository;
import com.lazo.couriers.app.user.repository.UserRepository;
import com.lazo.couriers.app.user.repository.UsersFavoriteUsersRepository;
import com.lazo.couriers.app.user.repository.UsersRepository;
import com.lazo.couriers.utils.JwtUtils;
import com.lazo.couriers.utils.LazoUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2021-03-30
 */

@Service
@RequiredArgsConstructor
public class MiniMenuServiceImpl implements MiniMenuService{

    HttpHeaders headers = new HttpHeaders();

    private final UserRepository userRepository;

    private final UsersFavoriteUsersRepository usersFavoriteUsersRepository;

    private final JwtUtils jwtTokenUtils;

    private final OrdersRepository ordersRepository;

    private final OrderJobsRepository orderJobsRepository;

    private final UsersRepository usersRepository;

    private final NotificationsRepository notificationsRepository;

    @Override
    public ResponseEntity<AppUser> getProfileData(String token) {
        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(new AppUser(), headers, HttpStatus.BAD_REQUEST);

        var username = jwtTokenUtils.extractUsername(token.substring(7));

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>(new AppUser(), headers, HttpStatus.BAD_REQUEST);


        AppUser user = userRepository.findByUsername(username);

        return new ResponseEntity<>(user, headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<String> getRating(String token) {
        var username = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

        var user = userRepository.findByUsername(username);
        var rating = user.getRating();

        if (rating ==null)
            return new ResponseEntity<>("0.0", headers, HttpStatus.OK);

        return new ResponseEntity<>(String.valueOf(rating), headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Boolean> updateProfile(String token, ProfileModel model) {
        var username = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var user = userRepository.findByUsername(username);
        user.setFirstName(model.getFirstName());
        user.setLastName(model.getLastName());
        user.setNickname(model.getNickname());
        user.setEmail(model.getEmail());
        userRepository.save(user);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AllUserModel>> allUsers(String token, Integer pageKey, Integer pageSize, Boolean searchOnlyFavorite) {
        List<AllUserModel> ans = new ArrayList<>();
        if (StringUtils.isEmpty(token) || pageKey ==null || pageSize ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var favUsers = usersFavoriteUsersRepository.findAllByUserId(userId.longValue());
        var favUsersIds = new ArrayList<>();
        favUsersIds.add(-1L);
        for (var f : favUsers) {
            favUsersIds.add(f.getFavoriteUserId());
        }

        var users =  userRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if (searchOnlyFavorite) {
                predicate = builder.and(predicate, builder.in(root.get("userId")).value(favUsersIds));
            } else {
                predicate = builder.and(predicate, builder.not(builder.in(root.get("userId")).value(favUsersIds)));
            }

            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("rating")));

        for (var u : users) {
            String nName = "";
            var isF = false;
            if (favUsersIds.contains(u.getUserId())) {
                isF = true;
                nName = usersFavoriteUsersRepository.nickname(userId.longValue(), u.getUserId());
            }

            var m = new AllUserModel(u, nName, isF);
            ans.add(m);
        }

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> addUserInFavorites(String token, Integer favoriteUserId) {
        if (StringUtils.isEmpty(token) || favoriteUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var newFav = new UsersFavoriteUsersDomain();
        newFav.setUserId(userId.longValue());
        newFav.setFavoriteUserId(favoriteUserId.longValue());
        usersFavoriteUsersRepository.save(newFav);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Boolean> removeFavorite(String token, Integer favoriteUserId) {
        if (StringUtils.isEmpty(token) || favoriteUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        usersFavoriteUsersRepository.deleteByUserIdAndFavoriteUserId(userId.longValue(), favoriteUserId.longValue());
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> updateNickname(String token, Long favUserId, String nickname) {
        if (StringUtils.isEmpty(token) || favUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var favUser0 = usersFavoriteUsersRepository.findByUserIdAndFavoriteUserId(userId.longValue(), favUserId);

        if (favUser0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var favUser = favUser0.get();
        favUser.setFavoriteNickname(nickname);
        usersFavoriteUsersRepository.save(favUser);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FavUserStatisticsModel> favUserStatistics(Long favUserId) {
        if (favUserId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userDeliveredParcelsCount = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("handedOverCourierUserId"), favUserId));
            return predicate;
        });

        var userSuccessfullyCompletedJobsCount = orderJobsRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderJobStatus"), OrderJobsDomain.OrderJobsStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("senderUserId"), favUserId));
            return predicate;
        });

        var m = new FavUserStatisticsModel(userDeliveredParcelsCount, userSuccessfullyCompletedJobsCount);
        return new ResponseEntity<>(m, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AllUserModel> getUserByUserId(String token, Long otherUserId) {
        var ans = new AllUserModel();
        if (otherUserId ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var user0 = userRepository.findById(otherUserId);
        if (user0.isEmpty())
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var user = user0.get();

        var favUsers = usersFavoriteUsersRepository.findAllByUserId(Long.valueOf(userId));
        var favUsersIds = new ArrayList<>();
        for (var f : favUsers) {
            favUsersIds.add(f.getFavoriteUserId());
        }

        if (favUsersIds.contains(otherUserId)) {
            ans.setNickname(usersFavoriteUsersRepository.nickname(userId.longValue(), user.getUserId()));
        }

        if (StringUtils.isEmpty(ans.getNickname())) {
            if (StringUtils.isNotEmpty(user.getUsername()))
                ans.setUsername(user.getUsername());

            if (StringUtils.isNotEmpty(user.getFirstName()))
                ans.setFirstName(user.getFirstName());

            if (StringUtils.isNotEmpty(user.getLastName()))
                ans.setLastName(user.getLastName());

            if (StringUtils.isNotEmpty(user.getNickname()))
                ans.setMainNickname(user.getNickname());
        }


        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getFavCourierCompanyId() {
        var user0 = usersRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(user0.get().getFavouriteCourierCompanyId(), headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Boolean> chooseFavCourierCompany(Integer favouriteCourierCompanyId) {
        if (favouriteCourierCompanyId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var user0 = usersRepository.findById(Long.valueOf(getCurrentApplicationUserId()));
        if (user0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var user = user0.get();
        user.setFavouriteCourierCompanyId(favouriteCourierCompanyId);
        usersRepository.save(user);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<NotificationsDomain>> getNotifications(Integer pageKey, Integer pageSize, Boolean statusId) {

        if (pageKey ==null || pageSize ==null || statusId ==null)
            return null;

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return null;

        var page = notificationsRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("statusId"), statusId ? 0 : 1));
            predicate = builder.and(predicate, builder.equal(root.get("userId"), userId));
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortAsc("notificationId")));


        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Boolean> setNotificationStatus(Long notificationId) {

        if (notificationId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var n0 = notificationsRepository.findById(notificationId);

        if (n0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.OK);

        var n = n0.get();
        n.setStatusId(1);
        notificationsRepository.save(n);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> rateUser(Double rating, Long userId) {

        if (rating ==null || userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var u0 = userRepository.findById(userId);

        if (u0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.OK);

        var u = u0.get();
        u.setRating((u.getRating() + rating)/2);
        userRepository.save(u);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

}
