/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.in.http.ActuatorApi;
import com.signomix.in.http.IntegrationApi;
import com.signomix.in.http.KpnApi;
import com.signomix.in.http.LoRaApi;
import com.signomix.in.http.TtnApi;
import com.signomix.iot.IotEvent;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.db.IotDataStorageIface;
import com.signomix.out.db.IotDatabaseIface;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import java.util.HashMap;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.in.http.EchoHttpAdapterIface;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import com.signomix.out.iot.ThingsDataIface;
import org.cricketmsf.microsite.out.queue.QueueAdapterIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.out.log.LoggerAdapterIface;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ActuatorDataIface;
import com.signomix.out.iot.VirtualStackIface;
import com.signomix.out.notification.EmailSenderIface;
import com.signomix.out.notification.NotificationIface;
import com.signomix.out.script.ScriptingAdapterIface;
import java.util.List;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.microsite.auth.AuthBusinessLogic;
import org.cricketmsf.microsite.in.http.ContentRequestProcessor;
import org.cricketmsf.microsite.out.queue.QueueException;
import org.cricketmsf.microsite.user.UserEvent;

/**
 * EchoService
 *
 * @author greg
 */
public class Service extends Kernel {

    //service parameters
    Invariants invariants = null;

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    LoggerAdapterIface gdprLogger = null;
    EchoHttpAdapterIface echoAdapter = null;
    KeyValueDBIface database = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface fileReader = null;

    // optional
    // we don't need to register input adapters:
    // UserApi, AuthApi and other input adapter if we not need to acces them directly from the service
    //CM module
    KeyValueDBIface cmsDatabase = null;
    FileReaderAdapterIface cmsFileReader = null;
    CmsIface cms = null;
    //user module
    KeyValueDBIface userDB = null;
    UserAdapterIface userAdapter = null;
    //auth module
    KeyValueDBIface authDB = null;
    AuthAdapterIface authAdapter = null;
    //event broker client
    QueueAdapterIface queueAdapter = null;
    KeyValueDBIface queueDB = null;
    // IoT
    ThingsDataIface thingsAdapter = null;
    DashboardAdapterIface dashboardAdapter = null;
    ActuatorCommandsDBIface actuatorCommandsDB = null;
    ActuatorApi actuatorApi = null;
    ActuatorDataIface actuatorAdapter = null;
    //WidgetAdapterIface widgetAdapter = null;
    IotDatabaseIface thingsDB = null;
    IotDataStorageIface iotDataDB = null;

    ScriptingAdapterIface scriptingAdapter = null;
    VirtualStackIface virtualStackAdapter = null;
    //notifications and emails
    NotificationIface smtpNotification = null;
    NotificationIface smsNotification = null;
    NotificationIface pushoverNotification = null;
    NotificationIface slackNotification = null;
    EmailSenderIface emailSender = null;

    //Integration services
    IntegrationApi integrationService = null;
    LoRaApi loraUplinkService = null;
    TtnApi ttnIntegrationService = null;
    KpnApi kpnUplinkService = null;

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        logAdapter = (LoggerAdapterIface) getRegistered("logger");
        gdprLogger = (LoggerAdapterIface) getRegistered("GdprLogger");
        echoAdapter = (EchoHttpAdapterIface) getRegistered("echo"); //TODO: remove?
        database = (KeyValueDBIface) getRegistered("database");
        scheduler = (SchedulerIface) getRegistered("scheduler");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        fileReader = (FileReaderAdapterIface) getRegistered("FileReader");
        //cms
        cmsFileReader = (FileReaderAdapterIface) getRegistered("CmsFileReader");
        cmsDatabase = (KeyValueDBIface) getRegistered("cmsDB");
        cms = (CmsIface) getRegistered("cms");
        //user
        userAdapter = (UserAdapterIface) getRegistered("userAdapter");
        userDB = (KeyValueDBIface) getRegistered("userDB");
        //auth
        authAdapter = (AuthAdapterIface) getRegistered("authAdapter");
        authDB = (KeyValueDBIface) getRegistered("authDB");
        //queue
        queueDB = (KeyValueDBIface) getRegistered("queueDB");
        queueAdapter = (QueueAdapterIface) getRegistered("queueAdapter");
        //IoT
        thingsAdapter = (ThingsDataIface) getRegistered("iotAdapter");
        thingsDB = (IotDatabaseIface) getRegistered("iotDB");
        iotDataDB = (IotDataStorageIface) getRegistered("iotDataDB");
        dashboardAdapter = (DashboardAdapterIface) getRegistered("dashboardAdapter");
        actuatorCommandsDB = (ActuatorCommandsDBIface) getRegistered("actuatorCommandsDB");
        actuatorApi = (ActuatorApi) getRegistered("ActuatorService");
        actuatorAdapter = (ActuatorDataIface) getRegistered("actuatorAdapter");
        //widgetAdapter = (WidgetAdapterIface) getRegistered("widgetAdapter");
        scriptingAdapter = (ScriptingAdapterIface) getRegistered("scriptingAdapter");
        virtualStackAdapter = (VirtualStackIface) getRegistered("virtualStack");
        //notifications
        smtpNotification = (NotificationIface) getRegistered("smtpNotification");
        smsNotification = (NotificationIface) getRegistered("smsNotification");
        pushoverNotification = (NotificationIface) getRegistered("pushoverNotification");
        slackNotification = (NotificationIface) getRegistered("slackNotification");
        emailSender = (EmailSenderIface) getRegistered("emailSender");

        integrationService = (IntegrationApi) getRegistered("IntegrationService");
        loraUplinkService = (LoRaApi) getRegistered("LoRaUplinkService");
        ttnIntegrationService = (TtnApi) getRegistered("TtnIntegrationService");
        kpnUplinkService = (KpnApi) getRegistered("KpnUplinkService");
    }

    @Override
    public void runInitTasks() {
        //read the OS variable to get the service URL
        String urlEnvName = (String) getProperties().get("SRVC_URL_ENV_VARIABLE");
        if (null != urlEnvName) {
            try {
                String url = System.getenv(urlEnvName);
                if (null != url) {
                    getProperties().put("serviceurl", url);
                }
            } catch (Exception e) {
            }
        }
        invariants = new Invariants();
        PlatformAdministrationModule.getInstance().initDatabases(database, userDB, authDB, thingsDB, iotDataDB, actuatorCommandsDB);
        //PlatformAdministrationModule.getInstance().readPlatformConfig(database);
        //TODO: use services monitoring
        PlatformAdministrationModule.getInstance().initScheduledTasks(scheduler);
        //TODO: na tym się potrafi zawiesić
        Kernel.getInstance().handleEvent(
                new Event(
                        this.getClass().getSimpleName(),
                        Event.CATEGORY_GENERIC,
                        "EMAIL_ADMIN",
                        "+1s",
                        "Signomix service has been started.")
        );

        //tasks
        /*
        Event ev1 = new Event(this.getName(), "CATEGORYX", "mytype", "*15s", "HELLO!");
        ev1.setName("EV1");
        if(!scheduler.isScheduled(ev1.getName())){
            handle(ev1);
        }
        Event ev2 = new Event(this.getName(), Event.CATEGORY_GENERIC, "STATUS", "*5s", "HELLO!");
        ev2.setName("EV2");
        if(!scheduler.isScheduled(ev2.getName())){
            handle(ev2);
        }
         */
    }

    @Override
    public void runFinalTasks() {
        /*
        // CLI adapter doesn't start automaticaly as other inbound adapters
        if (cli != null) {
            cli.start();
        }
         */
    }

    /**
     * Executed when the Service is started in "not service" mode
     */
    @Override
    public void runOnce() {
        super.runOnce();
        handleEvent(Event.logInfo("Service.runOnce()", "executed"));
    }

    @Override
    public void shutdown() {
        emailSender.send(
                (String) getProperties().getOrDefault("admin-notification-email", ""),
                "Signomix - shutdown", "Signomix service is going down."
        );
        super.shutdown();
    }

    /**
     * Event dispatcher method. Depending on the event category, Service and
     * QueueAdapte configurations dispatches event to Scheduler, QueAdapter or
     * Kernel handler method.
     *
     * @param event Event object to dispatch
     * @return
     */
    @Override
    public Object handleEvent(Event event) {
        if (queueAdapter != null && queueAdapter.isHandling(event.getCategory())) {
            try {
                queueAdapter.send(event);
            } catch (QueueException ex) {
                handleEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
            }
            return null;
        }
        if (scheduler != null && event.getTimePoint() != null) {
            scheduler.handleEvent(event);
            return null;
        }
        return super.handleEvent(event);
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "WwwService", requestMethod = "GET")
    public Object wwwGet(Event event) {
        //TODO: to nie jest optymalne rozwiązanie
        handle(Event.logFinest(this.getClass().getSimpleName(), event.getRequest().uri));
        String language = (String) event.getRequest().parameters.get("language");
        if (language == null || language.isEmpty()) {
            language = "en";
        } else if (language.endsWith("/")) {
            language = language.substring(0, language.length() - 1);
        }
        ParameterMapResult result = null;
        try {
            String cacheName = "webcache_" + language;
            result = (ParameterMapResult) cms
                    .getFile(event.getRequest(), htmlAdapter.useCache() ? database : null, cacheName, language);
            if (result.getCode() == HttpAdapter.SC_NOT_FOUND) {
                if (event.getRequest().pathExt.endsWith(".html")) {
                    //TODO: configurable index file params
                    //RequestObject request = processRequest(event.getRequest(), ".html", "index_pl.html");
                    RequestObject request = processRequest(event.getRequest(), ".html", "index.html");
                    result = (ParameterMapResult) fileReader
                            .getFile(request, htmlAdapter.useCache() ? database : null, "webcache");
                }
            }
            //((HashMap) result.getData()).put("serviceurl", getProperties().get("serviceurl"));
            HashMap rd = (HashMap) result.getData();
            rd.put("serviceurl", getProperties().get("serviceurl"));
            rd.put("defaultLanguage", getProperties().get("default-language"));
            rd.put("token", event.getRequestParameter("tid"));  // fake tokens doesn't pass SecurityFilter
            rd.put("shared", event.getRequestParameter("tid"));  // niepusty tid może być permanentnym tokenem ale może też być fałszywy
            rd.put("user", event.getRequest().headers.getFirst("X-user-id"));
            rd.put("environmentName", getName());
            rd.put("distroType", (String) invariants.get("release"));
            List<String> roles = event.getRequest().headers.get("X-user-role");
            if (roles != null) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < roles.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("'").append(roles.get(i)).append("'");
                }
                sb.append("]");
                rd.put("roles", sb.toString());
            } else {
                rd.put("roles", "[]");
            }
            // TODO: caching policy 
            result.setMaxAge(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("HEAD".equalsIgnoreCase(event.getRequest().method)) {
            byte[] empty = {};
            result.setPayload(empty);
        }
        return result;
    }

    /**
     * Modify request pathExt basic on adapter configuration for CMS/Website
     * systems
     *
     * @param originalRequest
     * @param indexFileExt
     * @param indexFileName
     * @return
     */
    private RequestObject processRequest(RequestObject originalRequest, String indexFileExt, String indexFileName) {
        RequestObject request = originalRequest;
        String[] pathElements = request.uri.split("/");
        if (pathElements.length == 0) {
            return request;
        }
        StringBuilder sb = new StringBuilder();
        if (pathElements[pathElements.length - 1].endsWith(indexFileExt)) {
            if (!pathElements[pathElements.length - 1].equals(indexFileName)) {
                for (int i = 0; i < pathElements.length - 1; i++) {
                    sb.append(pathElements[i]).append("/");
                }
                request.pathExt = sb.toString();
            }
        }
        return request;
    }

    @HttpAdapterHook(adapterName = "AlertService", requestMethod = "OPTIONS")
    public Object alertCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "AlertService", requestMethod = "GET")
    public Object alertGet(Event event) {
        return AlertModule.getInstance().getAlerts(event, thingsAdapter);
    }

    @HttpAdapterHook(adapterName = "AlertService", requestMethod = "DELETE")
    public Object alertDelete(Event event) {
        return AlertModule.getInstance().removeAlert(event, thingsAdapter);
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "OPTIONS")
    public Object dashboardCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "*")
    public Object dashboardServiceHandle(Event event) {
        try {
            return new DashboardBusinessLogic().getInstance().processEvent(event, dashboardAdapter, thingsAdapter, authAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "ThingsService", requestMethod = "OPTIONS")
    public Object thingsCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ThingsService", requestMethod = "*")
    public Object thingsServiceHandle(Event event) {
        //System.out.println("QUERY BEFORE LOGIC=["+event.getRequestParameter("query")+"]");
        return new DeviceManagementModule().processEvent(event, thingsAdapter, userAdapter, PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "TtnIntegrationService", requestMethod = "OPTIONS")
    public Object ttnDataCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "TtnIntegrationService", requestMethod = "*")
    public Object ttnDataAdd(Event event) {
        try {
            return DeviceIntegrationModule.getInstance().processTtnRequest(event, thingsAdapter, userAdapter, scriptingAdapter, ttnIntegrationService);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "IntegrationService", requestMethod = "OPTIONS")
    public Object iotDataCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "IntegrationService", requestMethod = "*")
    public Object iotDataAdd(Event event) {
        StandardResult result;
        try {
            result = (StandardResult) DeviceIntegrationModule.getInstance().processIotRequest(event, thingsAdapter, userAdapter, scriptingAdapter, integrationService, actuatorCommandsDB);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //ActuatorModule.getInstance().getCommand(deviceEUI, actuatorCommandsDB);
        return result;
    }

    @HttpAdapterHook(adapterName = "ActuatorService", requestMethod = "OPTIONS")
    public Object actuatorCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ActuatorService", requestMethod = "*")
    public Object actuatorHandle(Event event) {
        return ActuatorModule.getInstance().processRequest(event, actuatorApi, thingsAdapter, actuatorCommandsDB, virtualStackAdapter, scriptingAdapter);
    }

    @HttpAdapterHook(adapterName = "LoRaUplinkService", requestMethod = "*")
    public Object LoRaUplinkHandle(Event event) {
        return DeviceIntegrationModule.getInstance().processLoRaRequest(event, thingsAdapter, userAdapter, scriptingAdapter, loraUplinkService);
    }

    @HttpAdapterHook(adapterName = "LoRaJoinService", requestMethod = "*")
    public Object LoRaJoinHandle(Event event) {
        return LoRaBusinessLogic.getInstance().processLoRaRequest(event);
    }

    @HttpAdapterHook(adapterName = "LoRaAckService", requestMethod = "*")
    public Object LoRaAckHandle(Event event) {
        return LoRaBusinessLogic.getInstance().processLoRaRequest(event);
    }

    @HttpAdapterHook(adapterName = "LoRaErrorService", requestMethod = "*")
    public Object LoRaErrorHandle(Event event) {
        return LoRaBusinessLogic.getInstance().processLoRaRequest(event);
    }

    @HttpAdapterHook(adapterName = "KpnUplinkService", requestMethod = "*")
    public Object KpnUplinkHandle(Event event) {
        return DeviceIntegrationModule.getInstance().processKpnRequest(event, thingsAdapter, userAdapter, scriptingAdapter, kpnUplinkService);
    }

    @HttpAdapterHook(adapterName = "RecoveryService", requestMethod = "OPTIONS")
    public Object recoveryCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "RecoveryService", requestMethod = "POST")
    public Object recoveryHandle(Event event) {
        String resetPassEmail = event.getRequestParameter("resetpass");
        String userName = event.getRequestParameter("name");
        return CustomerModule.getInstance().handleResetRequest(event, userName, resetPassEmail, userAdapter, authAdapter, emailSender);
    }

    @HttpAdapterHook(adapterName = "UserService", requestMethod = "OPTIONS")
    public Object userCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    /**
     * Return user data
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "GET")
    public Object userGet(Event event) {
        return UserModule.getInstance().handleGetRequest(event, userAdapter);
    }

    @HttpAdapterHook(adapterName = "UserService", requestMethod = "POST")
    public Object userAdd(Event event) {
        boolean withConfirmation = "true".equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleRegisterRequest(event, userAdapter, withConfirmation);
    }

    /**
     * Modify user data or sends password reset link
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "PUT")
    public Object userUpdate(Event event) {
        String resetPassEmail = event.getRequestParameter("resetpass");
        if (resetPassEmail == null || resetPassEmail.isEmpty()) {
            return UserModule.getInstance().handleUpdateRequest(event, userAdapter);
        } else {
            String userName = event.getRequestParameter("name");
            return CustomerModule.getInstance().handleResetRequest(event, userName, resetPassEmail, userAdapter, authAdapter, emailSender);
        }
    }

    /**
     * Set user as waiting for removal
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "DELETE")
    public Object userDelete(Event event) {
        boolean withConfirmation = "true".equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleDeleteRequest(event, userAdapter, withConfirmation);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "OPTIONS")
    public Object authCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "POST")
    public Object authLogin(Event event) {
        return AuthBusinessLogic.getInstance().login(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "DELETE")
    public Object authLogout(Event event) {
        return AuthBusinessLogic.getInstance().logout(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "GET")
    public Object authCheck(Event event) {
        return AuthBusinessLogic.getInstance().check(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "PUT")
    public Object authRefresh(Event event) {
        return AuthBusinessLogic.getInstance().refreshToken(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "ConfirmationService", requestMethod = "GET")
    public Object userConfirm(Event event) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        try {
            String key = event.getRequestParameter("key");
            try {
                //Token token = (Token) database.get("tokens", key);
                if (authAdapter.checkToken(key)) {
                    //if (token != null) {
                    //if (token.isValid()) {
                    //Token token=authAdapter.getUser(key)
                    //User user = userAdapter.get(token.getUid());
                    User user = authAdapter.getUser(key);
                    if (user.getStatus() == User.IS_REGISTERING && user.getConfirmString().equals(key)) {
                        user.setConfirmed(true);
                        userAdapter.modify(user);
                        result.setCode(200);
                        //TODO: build default html page or redirect
                        String pageContent
                                = "Registration confirmed.<br>You can go to <a href=/#login>login page</a> and sign in.";
                        result.setFileExtension("html");
                        result.setHeader("Content-type", "text/html");
                        //result.setData(pageContent);
                        result.setPayload(pageContent.getBytes());
                    }
                    //}
                    //TODO: remove token?
                    //if (!database.remove("tokens", key)) {
                    //    Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "unable to remove token: " + key));
                    //}
                } else {
                    result.setCode(401);
                    String pageContent
                            = "Oops, something has gone wrong: confirmation token not found . We cannot confirm your <a href=/#>Signomix</a> registration. Please contact support.";
                    result.setFileExtension("html");
                    result.setHeader("Content-type", "text/html");
                    //result.setData(pageContent);
                    result.setPayload(pageContent.getBytes());
                }
                //} catch (KeyValueDBException ex) {
                //    Kernel.handle(Event.logFine(this.getClass().getSimpleName(), "confirmation error: " + ex.getMessage()));
                //    if (ex.getCode() == AuthException.EXPIRED) {
                //        result.setCode(401);
                //    }
            } catch (UserException ex) {
                Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), "confirmation error " + ex.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @HttpAdapterHook(adapterName = "echo", requestMethod = "*")
    public Object doGetEcho(Event requestEvent) {
        return sendEcho(requestEvent.getRequest());
    }

    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "OPTIONS")
    public Object contentCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "GET")
    public Object contentGetPublished(Event event) {
        try {
            return new ContentRequestProcessor().processGetPublished(event, cms);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(HttpAdapter.SC_NOT_FOUND);
            return r;
        }
    }

    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "OPTIONS")
    public Object contentServiceCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "*")
    public Object contentServiceHandle(Event event) {
        return new ContentRequestProcessor().processRequest(event, cms);
    }

    @HttpAdapterHook(adapterName = "SystemService", requestMethod = "*")
    public Object systemServiceHandle(Event event) {
        return new PlatformAdministrationModule().handleRestEvent(event);
    }

    @EventHook(eventCategory = Event.CATEGORY_LOG)
    public void logEvent(Event event) {
        logAdapter.log(event);
        if (event.getType().equals(Event.LOG_SEVERE)) {
            emailSender.send((String) getProperties().getOrDefault("admin-notification-email", ""), "Signomix - error", event.toString());
        }
    }

    @EventHook(eventCategory = Event.CATEGORY_HTTP_LOG)
    public void logHttpEvent(Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = UserEvent.CATEGORY_USER)
    public void processUserEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
            return;
        }
        UserEventHandler.handleEvent(
                this,
                event,
                userAdapter,
                gdprLogger,
                authAdapter,
                thingsAdapter,
                dashboardAdapter,
                emailSender
        );
    }

    @EventHook(eventCategory = IotEvent.CATEGORY_IOT)
    public void processIotEvent(Event event) {
        IotEventHandler.handleEvent(
                this,
                event,
                scheduler,
                userAdapter,
                thingsAdapter,
                smtpNotification,
                smsNotification,
                pushoverNotification,
                slackNotification,
                dashboardAdapter,
                authAdapter,
                virtualStackAdapter,
                scriptingAdapter
        );

    }

    /**
     * Handles system events
     *
     * @param event event object to process
     */
    @EventHook(eventCategory = Event.CATEGORY_GENERIC)
    public void processSystemEvent(Event event) {
        SystemEventHandler.handleEvent(
                this,
                event,
                database,
                cmsDatabase,
                userAdapter,
                userDB,
                authAdapter,
                authDB,
                actuatorAdapter,
                actuatorCommandsDB,
                thingsAdapter,
                thingsDB,
                iotDataDB,
                dashboardAdapter,
                virtualStackAdapter,
                scriptingAdapter,
                emailSender
        );
        /*if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
            return;
        }
        switch (event.getType()) {
            case "SHUTDOWN":
                shutdown();
                break;
            case "EMAIL_ADMIN":
                emailSender.send(
                        (String) getProperties().getOrDefault("admin-notification-email", ""),
                        "Signomix - started", "" + event.getPayload()
                );
                break;
            case "CLEAR_DATA":
                try {
                    String payload = (String) event.getPayload();
                    String[] params = payload.split("|");
                    String dataCategory = "";
                    String userType = "";
                    if (params != null && params.length > 0) {
                        dataCategory = params[0];
                        if (params.length > 1) {
                            userType = params[1];
                        }
                    }
                    boolean demoMode = getName().toUpperCase().indexOf("DEMO") >= 0;
                    PlatformAdministrationModule.getInstance().clearData(
                            demoMode, dataCategory, userType, userAdapter, thingsAdapter, authAdapter, authDB, dashboardAdapter,
                            actuatorAdapter
                    );
                } catch (ClassCastException | IndexOutOfBoundsException ex) {
                    handleEvent(Event.logWarning(this, "Problem with clearing data parameters- " + ex.getMessage()));
                }
            case "CONTENT":
                try {
                    database.clear("webcache_pl");
                } catch (KeyValueDBException ex) {
                    dispatchEvent(Event.logWarning(this, "Problem while clearing web cache - " + ex.getMessage()));
                }
                try {
                    database.clear("webcache_en");
                } catch (KeyValueDBException ex) {
                    dispatchEvent(Event.logWarning(this, "Problem while clearing web cache - " + ex.getMessage()));
                }
                try {
                    database.clear("webcache_fr");
                } catch (KeyValueDBException ex) {
                    dispatchEvent(Event.logWarning(this, "Problem while clearing web cache - " + ex.getMessage()));
                }
                break;
            case "STATUS":
                System.out.println(printStatus());
                break;
            case "COMMAND":
                ActuatorModule.getInstance().processCommand(event, actuatorCommandsDB, virtualStackAdapter, thingsAdapter, scriptingAdapter);
                break;
            case "BACKUP":
                PlatformAdministrationModule.getInstance().backupDatabases(database, userDB, authDB, cmsDatabase, thingsDB, iotDataDB, actuatorCommandsDB);
                break;
            default:
                handleEvent(Event.logWarning("Don't know how to handle type " + event.getType(), event.getPayload().toString()));
        }
         */
    }

    /**
     * Handles all event categories not processed by other handler methods
     *
     * @param event event object to process
     */
    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            handleEvent(Event.logWarning("Don't know how to handle category " + event.getCategory(), event.getPayload().toString()));
        }
    }

    public Object sendEcho(RequestObject request) {
        StandardResult r = new StandardResult();
        r.setCode(HttpAdapter.SC_OK);
        try {
            if (!echoAdapter.isSilent()) {
                HashMap<String, Object> data = new HashMap<>(request.parameters);
                data.put("service.uuid", getUuid().toString());
                data.put("request.method", request.method);
                data.put("request.pathExt", request.pathExt);
                if (data.containsKey("error")) {
                    int errCode = HttpAdapter.SC_INTERNAL_SERVER_ERROR;
                    try {
                        errCode = Integer.parseInt((String) data.get("error"));
                    } catch (Exception e) {
                    }
                    r.setCode(errCode);
                    data.put("error", "error forced by request");
                }
                r.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
            r.setCode(500);
        }
        return r;
    }
}
