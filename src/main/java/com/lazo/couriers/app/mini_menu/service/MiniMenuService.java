package com.lazo.couriers.app.mini_menu.service;

import com.lazo.couriers.app.mini_menu.models.AllUserModel;
import com.lazo.couriers.app.mini_menu.models.FavUserStatisticsModel;
import com.lazo.couriers.app.mini_menu.models.ProfileModel;
import com.lazo.couriers.app.user.domains.AppUser;
import com.lazo.couriers.app.user.domains.NotificationsDomain;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2021-03-30
 */

public interface MiniMenuService {

    ResponseEntity<AppUser> getProfileData(String token);

    ResponseEntity<String> getRating(String token);

    ResponseEntity<Boolean> updateProfile(String token, ProfileModel model);

    ResponseEntity<List<AllUserModel>> allUsers(String token, Integer pageKey, Integer pageSize, Boolean searchOnlyFavorite);

    ResponseEntity<Boolean> addUserInFavorites(String token, Integer favoriteUserId);

    ResponseEntity<Boolean> removeFavorite(String token, Integer favoriteUserId);

    ResponseEntity<Boolean> updateNickname(String token, Long favUserId, String nickname);

    ResponseEntity<FavUserStatisticsModel> favUserStatistics(Long favUserId);

    ResponseEntity<AllUserModel> getUserByUserId(String token, Long otherUserId);

    ResponseEntity<Integer> getFavCourierCompanyId();

    ResponseEntity<Boolean> chooseFavCourierCompany(Integer favouriteCourierCompanyId);

    ResponseEntity<List<NotificationsDomain>> getNotifications(Integer pageKey, Integer pageSize, Boolean statusId);

    ResponseEntity<Boolean> setNotificationStatus(Long notificationId);

    ResponseEntity<Boolean> rateUser(Double rating, Long userId);
}
