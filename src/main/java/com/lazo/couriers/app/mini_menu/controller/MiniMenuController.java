package com.lazo.couriers.app.mini_menu.controller;

import com.lazo.couriers.app.mini_menu.models.AllUserModel;
import com.lazo.couriers.app.mini_menu.models.FavUserStatisticsModel;
import com.lazo.couriers.app.mini_menu.models.ProfileModel;
import com.lazo.couriers.app.mini_menu.service.MiniMenuService;
import com.lazo.couriers.app.user.domains.AppUser;
import com.lazo.couriers.app.user.domains.NotificationsDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Lazo on 2021-03-30
 */

@RestController
@RequestMapping("miniMenu")
@RequiredArgsConstructor
public class MiniMenuController {

    private final MiniMenuService miniMenuService;

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
	@RequestMapping({ "/get_profile_data" })
	public ResponseEntity<AppUser> getProfileData(@RequestHeader("Authorization") String token) {
        return miniMenuService.getProfileData(token);
	}

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_rating" })
    public ResponseEntity<String> getRating(@RequestHeader("Authorization") String token) {
        return miniMenuService.getRating(token);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/update_profile" })
    public ResponseEntity<Boolean> updateProfile(@RequestHeader("Authorization") String token, ProfileModel model) {
        return miniMenuService.updateProfile(token, model);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/all_users" })
    public ResponseEntity<List<AllUserModel>> allUsers(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize, Boolean searchOnlyFavorite) {
        return miniMenuService.allUsers(token, pageKey, pageSize, searchOnlyFavorite);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/add_user_in_favorites" })
    public ResponseEntity<Boolean> addUserInFavorites(@RequestHeader("Authorization") String token, Integer favoriteUserId) {
        return miniMenuService.addUserInFavorites(token, favoriteUserId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/remove_favorite" })
    public ResponseEntity<Boolean> removeFavorite(@RequestHeader("Authorization") String token, Integer favoriteUserId) {
        return miniMenuService.removeFavorite(token, favoriteUserId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/update_nickname" })
    public ResponseEntity<Boolean> updateNickname(@RequestHeader("Authorization") String token, Long favUserId, String nickname) {
        return miniMenuService.updateNickname(token, favUserId, nickname);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/fav_user_statistics" })
    public ResponseEntity<FavUserStatisticsModel> favUserStatistics(Long favUserId) {
        return miniMenuService.favUserStatistics(favUserId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_user_by_user_id" })
    public ResponseEntity<AllUserModel> getUserByUserId(@RequestHeader("Authorization") String token, Long otherUserId) {
        return miniMenuService.getUserByUserId(token, otherUserId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_fav_courier_company_id" })
    public ResponseEntity<Integer> getFavCourierCompanyId() {
        return miniMenuService.getFavCourierCompanyId();
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/choose_fav_courier_company" })
    public ResponseEntity<Boolean> chooseFavCourierCompany(Integer favouriteCourierCompanyId) {
        return miniMenuService.chooseFavCourierCompany(favouriteCourierCompanyId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/get_notifications" })
    public ResponseEntity<List<NotificationsDomain>> getNotifications(Integer pageKey, Integer pageSize, Boolean statusId) {
        return miniMenuService.getNotifications(pageKey, pageSize, statusId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/set_notification_status" })
    public ResponseEntity<Boolean> setNotificationStatus(Long notificationId) {
        return miniMenuService.setNotificationStatus(notificationId);
    }

    @PreAuthorize("hasRole('ROLE_COURIERS_APP')")
    @RequestMapping({ "/rate_user" })
    public ResponseEntity<Boolean> rateUser(Double rating, Long userId) {
        return miniMenuService.rateUser(rating, userId);
    }

}
