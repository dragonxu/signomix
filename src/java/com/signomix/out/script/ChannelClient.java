/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.script;

import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ChannelClient {

    ThingsDataIface thingsAdapter;
    String userID;
    String deviceID;

    public ChannelClient(String userID, String deviceID, ThingsDataIface thingsAdapter) {
        this.thingsAdapter = thingsAdapter;
        this.userID = userID;
        this.deviceID = deviceID;
    }

    public ChannelData getLastData(String channel) {
        try {
            return thingsAdapter.getLastValue(userID, deviceID, channel);
        } catch (ThingsDataException ex) {
            return null;
        }
    }

}
