/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import java.util.HashMap;
import java.util.Locale;
import org.cricketmsf.Kernel;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Invariants extends HashMap {

    /* 
    Configuration parameters:
    demo: [true|false]
    release: [standard|mini]
    webCacheSize: rozmiar cache (lczba dokumentów/plików w cache)
    
    maxUsers:
    maxDevices:
    responseLimit:
    demoCollectionLimit:
    demoDataRetention:
    demoDevicesLimit:
    demoNotifications: 
    freeCollectionLimit:
    freeDataRetention:
    freeDevicesLimit:
    freeNotifications: 
    primaryCollectionLimit:
    primaryDataRetention:
    primaryDevicesLimit:
    primaryNotifications: 
    */

    public Invariants() {
        super();

        put("release", "standard"); //this parameter is changed by the build script
        String releaseType = null;
        try {
            releaseType = (String) get("release");
        } catch (ClassCastException e) {
        }

        boolean demoVersion = Kernel.getInstance().getName().toLowerCase(Locale.getDefault()).contains("demo");
        put("demo", demoVersion);
        put("webCacheSize", 500);

        if ("mini".equalsIgnoreCase(releaseType)) {
            put("maxUsers", 10);
            put("maxDevices", 20); // no limit for total number of registered devices

            put("demoCollectionLimit", 100);
            put("demoDataRetention", 1); // days
            put("demoDevicesLimit", 3); // user devices
            put("demoNotifications", "SMTP");

            put("freeCollectionLimit", 100000);
            put("freeDataRetention", 30); // days
            put("freeDevicesLimit", 20); // user devices
            put("freeNotifications", "SMTP,SLACK");

            put("standardCollectionLimit", 100000);
            put("standardDataRetention", 30); // days
            put("standardDevicesLimit", 20); // user devices
            put("standardNotifications", "SMTP,SLACK");

            put("primaryCollectionLimit", 100000);
            put("primaryDataRetention", 30); // days
            put("primaryDevicesLimit", 20); // user devices
            put("primaryNotifications", "SMTP,SLACK");
        } else if (demoVersion) {
            put("maxUsers", 10);
            put("maxDevices", 0); // no limit for total number of registered devices

            put("demoCollectionLimit", 100);
            put("demoDataRetention", 1); // 1 day
            put("demoDevicesLimit", 3); // user devices
            put("demoNotifications", "SMTP,SLACK");

            put("freeCollectionLimit", 100);
            put("freeDataRetention", 1); // 1 day
            put("freeDevicesLimit", 3); // user devices
            put("freeNotifications", "SMTP,SLACK");

            put("standardCollectionLimit", 100);
            put("standardDataRetention", 1); // days
            put("standardDevicesLimit", 3); // user devices
            put("standardNotifications", "SMTP,SLACK");

            put("primaryCollectionLimit", 100);
            put("primaryDataRetention", 1); // 1 day
            put("primaryDevicesLimit", 3); // user devices
            put("primaryNotifications", "SMTP,SLACK");
        } else {
            put("maxUsers", 1000);
            put("maxDevices", 0); // no limit for total number of registered devices

            put("demoCollectionLimit", 100);
            put("demoDataRetention", 1); // 1 day
            put("demoDevicesLimit", 1); // user devices
            put("demoNotifications", "SMTP,SLACK");

            put("freeCollectionLimit", 1000);
            put("freeDataRetention", 7); // 1 day
            put("freeDevicesLimit", 5); // user devices
            put("freeNotifications", "SMTP,SLACK");

            put("standardCollectionLimit", 50000);
            put("standardDataRetention", 30); // days
            put("standardDevicesLimit", 10); // user devices
            put("standardNotifications", "SMTP,SLACK");

            put("primaryCollectionLimit", 100000);
            put("primaryDataRetention", 30); // 1 day
            put("primaryDevicesLimit", 50); // user devices
            put("primaryNotifications", "SMTP,PUSHOVER,SMS,SLACK");
        }
    }

}
