package com.gidp.sure3odds.service.users;

import com.gidp.sure3odds.entity.games.Status;
import com.gidp.sure3odds.entity.payments.Payments;
import com.gidp.sure3odds.entity.payments.PlanTypes;
import com.gidp.sure3odds.entity.payments.Plans;
import com.gidp.sure3odds.entity.response.BaseResponse;
import com.gidp.sure3odds.entity.users.NewUser;
import com.gidp.sure3odds.entity.users.Parameters;
import com.gidp.sure3odds.entity.users.UserTypes;
import com.gidp.sure3odds.entity.users.Users;
import com.gidp.sure3odds.helper.AppHelper;
import com.gidp.sure3odds.repository.games.StatusRepository;
import com.gidp.sure3odds.repository.payments.PaymentsRepository;
import com.gidp.sure3odds.repository.payments.PlanTypesRepository;
import com.gidp.sure3odds.repository.payments.PlansRepository;
import com.gidp.sure3odds.repository.users.ParametersRepository;
import com.gidp.sure3odds.repository.users.UserTypesRepository;
import com.gidp.sure3odds.repository.users.UsersRepository;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UsersService {
    CloseableHttpClient httpClient = HttpClients.createDefault();

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    UserTypesRepository userTypesRepository;

    @Autowired
    PlanTypesRepository planTypesRepository;

    @Autowired
    PlansRepository plansRepository;

    @Autowired
    PaymentsRepository paymentsRepository;

    @Autowired
    ParametersRepository parametersRepository;

    AppHelper appHelper = new AppHelper();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    EmailService emailService;

    public BaseResponse CreateNewUser(NewUser newUser) throws IOException {
        BaseResponse response = new BaseResponse();
        if (appHelper.isValidEmail(newUser.getEmail())) {
            if (!checkEmailAddressOrPhoneNumberExist(newUser.getEmail(), newUser.getPhone())) {
                response = RegisterUser(newUser);
            } else {
                response.setDescription("User is already registered");
                response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.setDescription("Please enter a valid email");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;
    }

    public BaseResponse RegisterUser(NewUser newUser) throws IOException {
        BaseResponse response = new BaseResponse();
        String validationResult = "failed";
        if (newUser.getPlatform().equals("Android")) {
            validationResult = AndroidPaymentValidation(newUser.getReferencecode());
        } else if (newUser.getPlatform().equals("iOS")) {
            validationResult = iOSPaymentValidation(newUser.getReferencecode());
        }

        if (validationResult.equals("success")) {
            Long usertypeid = newUser.getUserTypes().getId();
            Optional<UserTypes> usertype = userTypesRepository.findById(usertypeid);
            if (usertype.isPresent()) {
                Long planttypeid = newUser.getPlantype().getId();
                Optional<PlanTypes> plantype = planTypesRepository.findById(planttypeid);
                if (plantype.isPresent()) {
                    Optional<Status> status = statusRepository.findByName("Active");
                    String password = passwordEncoder.encode(newUser.getPassword());
                    LocalDate CurrentDate = LocalDate.now();
                    Users user = new Users(newUser.getEmail(), newUser.getPhone(), password, newUser.getFirstname(), newUser.getLastname(), CurrentDate,
                            "Pending");
                    user.setUsertype(usertype.get());
                    user.setStatus(status.get());
                    Users saved_user = usersRepository.save(user);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String CurrentDateString = CurrentDate.format(formatter);
                    LocalDate eDate = LocalDate.parse(CurrentDateString);
                    LocalDate endDate = eDate.plusMonths(1);
                    Plans plan = new Plans(CurrentDate, endDate);
                    plan.setUser(saved_user);
                    plan.setPlantype(plantype.get());
                    Plans saved_plan = plansRepository.save(plan);
                    Payments payment = new Payments(CurrentDate, "Registration",
                            newUser.getPlatform(), newUser.getReferencecode());
                    payment.setUser(saved_user);
                    payment.setPlantype(plantype.get());
                    Payments saved_payment = paymentsRepository.save(payment);
                    response.setData(saved_user);
                    String userName = newUser.getFirstname() + " " + newUser.getLastname();
                    response.setDescription("user created successfully");
                    response.setStatusCode(HttpServletResponse.SC_OK);
                    emailService.sendEmail(user.getEmail(), userName, plantype.get().getName());
                } else {
                    response.setDescription("Please, select a plan type found.");
                    response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                response.setDescription("Please, select a usertype");
                response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.setDescription("Please, select a Usertype");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;
    }

    public BaseResponse GetUsersByUserTypID(long userTypeID) {
        BaseResponse response = new BaseResponse();
        List<Users> users = usersRepository.findUsersByUsertype(userTypeID);
        if (!users.isEmpty()) {
            response.setData(users);
            response.setDescription("Users by user type found successfully.");
            response.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            response.setDescription("No results found.");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;

    }

    public String iOSPaymentValidation(String receiptData) throws IOException {
        String result = "success";
//        try {
////	        this.http.post( 'https://buy.itunes.apple.com/verifyReceipt',
//            HttpPost request = new HttpPost("https://sandbox.itunes.apple.com/verifyReceipt");
//            JSONObject requestData = new JSONObject();
//            try {
//                requestData = new JSONObject();
//                requestData.put("receipt-data", receiptData);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            StringEntity requestEntity = new StringEntity(requestData.toString());
//            request.addHeader("content-type", "application/x-www-form-urlencoded");
//            request.setEntity((HttpEntity) requestEntity);
//            CloseableHttpResponse response = httpClient.execute(request);
//            HttpEntity entity = response.getEntity();
//            StringBuilder Sbuilder = new StringBuilder();
//            if (entity != null) {
//                try {
//                    BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
//                    String line;
//                    while ((line = rd.readLine()) != null) {
//                        Sbuilder.append(line);
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//
//            } else {
//                throw new Exception("Error Occured while connecting to paystack url");
//            }
//            result = Sbuilder.toString();
//        } catch ( Exception ex) {
//            throw new RuntimeException(ex);
//        } finally {
//            httpClient.close();
//        }
        return result;

    }

    public String AndroidPaymentValidation(String ReferenceCode) throws IOException {

        String result = "success";
        Parameters parameters = parametersRepository.findById(2l).get();
        String SecretKey = parameters.getValue();
        try {
            HttpGet newRequest = new HttpGet("https://api.paystack.co/transaction/verify/" + ReferenceCode);
            newRequest.addHeader("Content-type", "application/json");
            newRequest.addHeader("Authorization", "Bearer " + SecretKey);
            newRequest.addHeader("Cache-Control", "no-cache");
            CloseableHttpResponse response = httpClient.execute(newRequest);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String responseBody = EntityUtils.toString(entity);
                    System.out.println(responseBody);
                    if (response.getStatusLine().getReasonPhrase().toString() == "OK") {
                        return result = "success";
                    }
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            httpClient.close();
        }
        return result;
    }

    public BaseResponse CreateSubAdmin(Users newUser) {
        BaseResponse response = new BaseResponse();
        Long usertypeid = newUser.getUsertype().getId();
        Optional<UserTypes> usertype = userTypesRepository.findById(usertypeid);
        if (usertype.isPresent()) {
            Optional<Status> status = statusRepository.findByName("Active");
            String password = passwordEncoder.encode(newUser.getPassword());
            newUser.setStatus(status.get());
            newUser.setDevice_token("Pending");
            Users user = new Users(newUser.getEmail(), newUser.getPhone(), password,
                    newUser.getFirstname(), newUser.getLastname(), newUser.getDatejoined(),
                    newUser.getDevice_token());
            user.setUsertype(usertype.get());

            Users saved_user = usersRepository.save(user);
            response.setData(saved_user);
            response.setDescription("user created successfully");
            response.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            response.setDescription("No results found.");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }

        return response;

    }


    public BaseResponse searchByFirstNameOrLastName(Long usertypeid, String searchValue) {
        BaseResponse response = new BaseResponse();
        List<Users> users = usersRepository.findUsersByFirstnameOrLastnameContaining(searchValue, searchValue);
        ArrayList<Object> data = new ArrayList<>();
        if (!users.isEmpty()) {
            for (Users user : users) {
                Long usertypeID = user.getUsertype().getId();
                if (usertypeID == usertypeid) {
                    data.add(user);
                }
            }
            if (!data.isEmpty()) {
                response.setData(users);
                response.setDescription("Usertypes found successfully.");
                response.setStatusCode(HttpServletResponse.SC_OK);
            } else {
                response.setDescription("No results found.");
                response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.setDescription("No results found.");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;

    }

    public BaseResponse GetUserDetailsByID(Long userid) {
        BaseResponse response = new BaseResponse();
        Optional<Users> users = usersRepository.findById(userid);
        if (users != null) {
            response.setData(users);
            response.setDescription("User found successfully.");
            response.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            response.setDescription("No results found.");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;

    }

    public String GetUserTypeNameByUserID(Long userid) {
        Users users = usersRepository.findById(userid).get();
        String usertypeName = users.getUsertype().getName();
        return usertypeName;
    }

    public String GetUserName(Long userid) {
        Users users = usersRepository.findById(userid).get();
        String userName = users.getLastname() + " " + users.getFirstname();
        return userName;
    }

    public String GetUserEmail(Long userid) {
        Users users = usersRepository.findById(userid).get();
        String userEmail = users.getEmail();
        return userEmail;
    }

    public String GetUserPhone(Long userid) {
        Users users = usersRepository.findById(userid).get();
        String userPhone = users.getPhone();
        return userPhone;
    }

    public String GetUserPassWord(Long userid) {
        Users users = usersRepository.findById(userid).get();
        String userPassword = users.getPassword();
        return userPassword;
    }

    public BaseResponse UpdateUser(Users users) {
        BaseResponse response = new BaseResponse();
        if (appHelper.isValidEmail(users.getEmail())) {
            Long userid = users.getId();
            String userEmail = GetUserEmail(userid);
            String userPhone = GetUserPhone(userid);
            if (checkEmailAddressOrPhoneNumberExist(users.getEmail(), users.getPhone())) {
                if (userEmail.equals(users.getEmail()) || userPhone.equals(users.getPhone())) {
                    users.setPassword(GetUserPassWord(userid));
                    Users updatedusers = usersRepository.save(users);
                    if (updatedusers != null) {
                        response.setData(updatedusers);
                        response.setDescription("User has been updated succesfully.");
                        response.setStatusCode(HttpServletResponse.SC_OK);
                    } else {
                        response.setDescription("User was not updated.");
                        response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    response.setDescription("Email or phone number provided is in use by another user.");
                    response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                users.setPassword(GetUserPassWord(userid));
                Users updatedusers = usersRepository.save(users);
                if (updatedusers != null) {
                    response.setData(updatedusers);
                    response.setDescription("User has been updated succesfully.");
                    response.setStatusCode(HttpServletResponse.SC_OK);
                } else {
                    response.setDescription("User was not updated.");
                    response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        } else {
            response.setDescription("please enter a valid email");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;
    }

    public boolean checkEmailAddressOrPhoneNumberExist(String email, String phoneNumber) {
        boolean result = false;
        Optional<Users> user = usersRepository.findUsersByEmailOrPhoneContaining(email, phoneNumber);
        if (user.isPresent()) {
            result = true;
        }
        return result;
    }

    public boolean IsUserActive(long UserID) {
        boolean result = false;
        Optional<Users> user = usersRepository.findById(UserID);
        if (user.isPresent()) {
            if (UserID == 1l) {
                result = true;
            } else {
                if (user.get().getUsertype().getId() == 2l) {//members
                    Plans plans = plansRepository.findPlansByUser(user.get());
                    LocalDate dueDate = plans.getEndDate();
                    LocalDate currentDate = LocalDate.now();
                    Status status = statusRepository.findByName("Inactive").get();
                    if (currentDate.isAfter(dueDate)) {
                        user.get().setStatus(status);
                        usersRepository.save(user.get());
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }
        }
        return result;
    }


    public BaseResponse GetAppReports() {
        BaseResponse response = new BaseResponse();
        HashMap<String, Object> result = new HashMap<String, Object>();

        Optional<UserTypes> userTypes = userTypesRepository.findById(2l);
        List<Users> allUser = usersRepository.findUsersByUsertypeEquals(userTypes.get());
        result.put("totalusers", allUser.size());

        List<Users> allActiveUsers = usersRepository.findUsersByStatusEqualsAndUsertypeEquals("Active", userTypes.get());
        result.put("totalactiveusers", allActiveUsers.size());

        List<Users> allInActiveUsers = usersRepository.findUsersByStatusEqualsAndUsertypeEquals("Inactive", userTypes.get());
        result.put("totalinactiveusers", allInActiveUsers.size());

        Optional<PlanTypes> planTypes = planTypesRepository.findById(1l);
        List<Payments> planTypes1Users = paymentsRepository.findPaymentsByPlantypeEquals(planTypes.get());
        result.put("totalvvipusers", planTypes1Users.size());

        Optional<PlanTypes> planTypes2 = planTypesRepository.findById(2l);
        List<Payments> allPlanTypes2Users = paymentsRepository.findPaymentsByPlantypeEquals(planTypes2.get());
        result.put("totalvipusers", allPlanTypes2Users.size());

        BigDecimal planType1Income = BigDecimal.ZERO;
        planType1Income = planTypes.get().getAmount().multiply(new BigDecimal(planTypes1Users.size()));
        result.put("totalvvipincome", planType1Income);


        BigDecimal planType2Income = BigDecimal.ZERO;
        planType2Income = planTypes2.get().getAmount().multiply(new BigDecimal(allPlanTypes2Users.size()));
        result.put("totalvipincome", planType2Income);

        BigDecimal totalincome = planType1Income.add(planType2Income);
        result.put("totalincome", totalincome);

        if (result != null) {
            response.setData(result);
            response.setDescription("report found successfully.");
            response.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            response.setDescription("No results found.");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;

    }

    public String ValidateAllUsersPaymentDueDate() {
        String result = "failed";
        List<Users> users = usersRepository.findAll();
        for (Users user : users) {
            long userid = user.getId();
            IsUserActive(userid);
        }
        return result;
    }


    public BaseResponse GetMonthlyReports(Date startDate) {
        BaseResponse response = new BaseResponse();
        HashMap<String, Object> result = new HashMap<String, Object>();

        LocalDate convertedDate = appHelper.convertToLocalDateViaInstant(startDate);
        convertedDate = convertedDate.withDayOfMonth(convertedDate.getMonth().length(convertedDate.isLeapYear()));

        Date endDate = appHelper.convertToDateViaInstant(convertedDate);
        Optional<UserTypes> userTypes = userTypesRepository.findById(2l);
        List<Users> allUser = usersRepository.findUsersByDatejoinedBetweenAndUsertypeEquals(startDate, endDate, userTypes.get());
        result.put("totalusers", allUser.size());

        List<Users> allActiveUsers = usersRepository.findUsersByDatejoinedBetweenAndStatusEqualsAndUsertypeEquals(startDate, endDate, "Active", userTypes.get());
        result.put("totalactiveusers", allActiveUsers.size());

        List<Users> allInActiveUsers = usersRepository.findUsersByDatejoinedBetweenAndStatusEqualsAndUsertypeEquals(startDate, endDate, "Inactive", userTypes.get());
        result.put("totalinactiveusers", allInActiveUsers.size());

        Optional<PlanTypes> planTypes = planTypesRepository.findById(1l);
        List<Payments> planTypes1Users = paymentsRepository.findPaymentsByPaymentdateBetweenAndPlantypeEquals(startDate, endDate, planTypes.get());
        result.put("totalvvipusers", planTypes1Users.size());

        Optional<PlanTypes> planTypes2 = planTypesRepository.findById(2l);
        List<Payments> allPlanTypes2Users = paymentsRepository.findPaymentsByPaymentdateBetweenAndPlantypeEquals(startDate, endDate, planTypes2.get());
        result.put("totalvipusers", allPlanTypes2Users.size());

        BigDecimal planType1Income = BigDecimal.ZERO;
        planType1Income = planTypes.get().getAmount().multiply(new BigDecimal(planTypes1Users.size()));
        result.put("totalvvipincome", planType1Income);


        BigDecimal planType2Income = BigDecimal.ZERO;
        planType2Income = planTypes2.get().getAmount().multiply(new BigDecimal(allPlanTypes2Users.size()));
        result.put("totalvipincome", planType2Income);

        BigDecimal totalincome = planType1Income.add(planType2Income);
        result.put("totalincome", totalincome);

        if (result != null) {
            response.setData(result);
            response.setDescription("report found succesfully.");
            response.setStatusCode(HttpServletResponse.SC_OK);
        } else {
            response.setDescription("No result found.");
            response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return response;

    }


}
