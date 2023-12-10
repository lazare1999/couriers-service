package com.lazo.couriers.app.main.service;

import com.lazo.couriers.app.main.models.AuthenticationRequest;
import com.lazo.couriers.app.main.models.AuthenticationResponse;
import com.lazo.couriers.app.main.models.RegisterModel;
import com.lazo.couriers.app.main.models.SmsOfficeResponseClass;
import com.lazo.couriers.app.user.domains.TemporaryCodesDomain;
import com.lazo.couriers.app.user.repository.TemporaryCodesRepository;
import com.lazo.couriers.app.user.repository.UserRepository;
import com.lazo.couriers.app.user.services.MyUserDetailsService;
import com.lazo.couriers.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.lazo.couriers.utils.EncryptUtils.encrypt;
import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2021-05-20
 */

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    HttpHeaders headers = new HttpHeaders();

    public static Map<String, String> forRegisterUserInfo = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).build();

    private final TemporaryCodesRepository temporaryCodesRepository;

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtTokenUtils;

    private final MyUserDetailsService userDetailsService;

    @Value("${co.module.api_key}")
    private String SMS_OFFICE_API_KEY;

    @Value("${co.module.sender}")
    private String SMS_OFFICE_SENDER;

    @Value("${co.module.salt}")
    private String SALT;

    @Override
    public ResponseEntity<Boolean> addRole(String token, Integer roleId) {
        if (roleId==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (!userDetailsService.checkIfRoleExists(roleId))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();

        if (userId ==null || Objects.equals(userId, 0))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (userDetailsService.roleIsAlreadyDefined(userId, roleId))
            return new ResponseEntity<>(true, headers, HttpStatus.OK);

        if (!userDetailsService.addRole(userId, roleId))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getUserName(String token) {
        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(userName, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> logout(String token) {
        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var td = temporaryCodesRepository.findByUserName(userName);
        td.ifPresent(temporaryCodesDomain -> temporaryCodesRepository.deleteById(temporaryCodesDomain.getTemporaryCodeId()));

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    //TODO: smsoffice
    private boolean smsOffice(String destination, String content) {
        String url = "http://smsoffice.ge/api/v2/send/";

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", SMS_OFFICE_API_KEY);
        params.add("destination", destination);
        params.add("sender", SMS_OFFICE_SENDER);
        params.add("content", content);
//			params.add("urgent", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        var restTemplate = new RestTemplate();

        try {
            var response = restTemplate.postForEntity(url, request, SmsOfficeResponseClass.class).getBody();

            return response != null && response.getSuccess() != null && response.getSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void generateTemporaryCodeForLogin(String username) {

        if (StringUtils.isEmpty(username))
            return;

//        String code = RandomStringUtils.random(6, false, true);
        String code = "123";

//        if (smsOffice(username, code)) {
            var userId = userRepository.findUserIdByUsername(username);
            if (userId ==null || Objects.equals(userId, 0L)) {
                return;
            }
            var td = temporaryCodesRepository.findByUserName(username);
            td.ifPresent(temporaryCodesDomain -> temporaryCodesRepository.deleteById(temporaryCodesDomain.getTemporaryCodeId()));
            temporaryCodesRepository.save(new TemporaryCodesDomain(username, encrypt(SALT, code)));
//        }
    }

    @Override
    public void generateTemporaryCodeForRegister(String username) {

        if (StringUtils.isEmpty(username))
            return;

//        String code = RandomStringUtils.random(6, false, true);
        String code = "123";

//        if (smsOffice(username, code)) {
            forRegisterUserInfo.remove(username);
            forRegisterUserInfo.put(username, encrypt(SALT, code));
//        }
    }

    @Override
    public ResponseEntity<?> createAuthenticationToken(AuthenticationRequest autRequest) throws Exception  {
        if (StringUtils.isEmpty(autRequest.getUsername()) || StringUtils.isEmpty(autRequest.getPassword()))
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        var td = temporaryCodesRepository.findByUserName(autRequest.getUsername());
        if (td.isEmpty() || td.get().getCode() == null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        var newUser = userDetailsService.authenticateJwt(autRequest.getUsername(), encrypt(SALT, autRequest.getPassword()), Objects.equals(encrypt(SALT, autRequest.getPassword()), td.get().getCode()));
        if (newUser ==null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        try {
            authenticationManager.authenticate(newUser);
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or" +
                    " password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(autRequest.getUsername());

        final AuthenticationResponse jwt = jwtTokenUtils.generateToken(userDetails);

        return new ResponseEntity<>(jwt, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> jwtViaRefreshToken(String refreshToken) {
        String userName = null;
        if (StringUtils.isNotEmpty(refreshToken)) {
            try {
                if (jwtTokenUtils.extractAccessTokenStatus(refreshToken))
                    return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);
                userName = jwtTokenUtils.extractUsername(refreshToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (userName == null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        var td = temporaryCodesRepository.findByUserName(userName);
        if (td.isEmpty() || td.get().getCode() == null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

        final AuthenticationResponse jwt = jwtTokenUtils.generateToken(userDetails);

        return new ResponseEntity<>(jwt, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> register(RegisterModel model) {

        if (StringUtils.isEmpty(model.getCode()))
            return new ResponseEntity<>("temporary_code_empty", headers, HttpStatus.BAD_REQUEST);

        var username = model.getPhoneNumber();

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>("phone_number_empty", headers, HttpStatus.BAD_REQUEST);

        if (forRegisterUserInfo.get(username) == null)
            return new ResponseEntity<>("temporary_code_not_exists", headers, HttpStatus.BAD_REQUEST);

        var userId = userRepository.findUserIdByUsername(username);
        if (userId !=null && userId !=0)
            return new ResponseEntity<>("user_already_defined", headers, HttpStatus.OK);

        if (!Objects.equals(encrypt(SALT, model.getCode()), forRegisterUserInfo.get(username)))
            return new ResponseEntity<>("temporary_code_incorrect", headers, HttpStatus.BAD_REQUEST);

        if (StringUtils.isEmpty(model.getNickname()) && (StringUtils.isEmpty(model.getFirstName()) || StringUtils.isEmpty(model.getLastName()))) {
            return new ResponseEntity<>("fill_blanks", headers, HttpStatus.BAD_REQUEST);
        }

        userDetailsService.register(model);

        forRegisterUserInfo.remove(username);
        return new ResponseEntity<>("success", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getPhone(Long senderUserId) {

        if (senderUserId == null)
            return new ResponseEntity<>("error", headers, HttpStatus.BAD_REQUEST);


        return new ResponseEntity<>(userRepository.findUsernameByUserId(senderUserId), headers, HttpStatus.OK);
    }

}
