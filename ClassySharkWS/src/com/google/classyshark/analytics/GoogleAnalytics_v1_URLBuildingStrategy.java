/*
 * Copyright 2015 Siddique Hameed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.analytics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

/**
 * URL building logic for the earlier versions of google analytics (urchin.js)
 *
 * @author : Siddique Hameed
 * @version : 0.1
 */

public class GoogleAnalytics_v1_URLBuildingStrategy implements URLBuildingStrategy {
    private FocusPoint appFocusPoint;
    private String googleAnalyticsTrackingCode;
    private String refererURL = "http://www.BoxySystems.com";

    private static final String TRACKING_URL_Prefix = "http://www.google-analytics.com/__utm.gif";

    private static final Random random = new Random();
    private static String hostName = "localhost";

    static {
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            //ignore this
        }
    }


    public GoogleAnalytics_v1_URLBuildingStrategy(String appName, String googleAnalyticsTrackingCode) {
        this.googleAnalyticsTrackingCode = googleAnalyticsTrackingCode;
        this.appFocusPoint = new FocusPoint(appName);
    }

    public GoogleAnalytics_v1_URLBuildingStrategy(String appName, String appVersion, String googleAnalyticsTrackingCode) {
        this.googleAnalyticsTrackingCode = googleAnalyticsTrackingCode;
        this.appFocusPoint = new FocusPoint(appVersion, new FocusPoint(appName));
    }


    public String buildURL(FocusPoint focusPoint) {

        int cookie = random.nextInt();
        int randomValue = random.nextInt(2147483647) - 1;
        long now = new Date().getTime();

//    String $urchinUrl="http://www.google-analytics.com/__utm.gif?utmwv=1&utmn='.$var_utmn.'&utmsr=-&utmsc=-&utmul=-&utmje=0&utmfl=-&utmdt=-&utmhn='.$var_utmhn.'&utmr='.$var_referer.'&utmp='.$var_utmp." +
//      "'&utmac='.$var_utmac.'" +
//      "&utmcc=__utma%3D'.$var_cookie.'.'.$var_random.'.'.$var_today.'.'.$var_today.'.'.$var_today.'.2%3B%2B__utmb%3D'.$var_cookie.'%3B%2B__utmc%3D'.$var_cookie.'%3B%2B__utmz%3D'.$var_cookie.'.'.$var_today.'.2.2.utmccn%3D(direct)%7Cutmcsr%3D(direct)%7Cutmcmd%3D(none)%3B%2B__utmv%3D'.$var_cookie.'.'.$var_uservar.'%3B";


        focusPoint.setParentTrackPoint(appFocusPoint);
        StringBuffer url = new StringBuffer(TRACKING_URL_Prefix);
        url.append("?utmwv=1"); //Urchin/Analytics version
        url.append("&utmn=" + random.nextInt());
        url.append("&utmcs=UTF-8"); //document encoding
        url.append("&utmsr=1440x900"); //screen resolution
        url.append("&utmsc=32-bit"); //color depth
        url.append("&utmul=en-us"); //user language
        url.append("&utmje=1"); //java enabled
        url.append("&utmfl=9.0%20%20r28"); //flash
        url.append("&utmcr=1"); //carriage return
        url.append("&utmdt=" + focusPoint.getContentTitle()); //The optimum keyword density //document title
        url.append("&utmhn=" + hostName);//document hostname
        url.append("&utmr=" + refererURL); //referer URL
        url.append("&utmp=" + focusPoint.getContentURI());//document page URL
        url.append("&utmac=" + googleAnalyticsTrackingCode);//Google Analytics account
        url.append("&utmcc=__utma%3D'" + cookie + "." + randomValue + "." + now + "." + now + "." + now + ".2%3B%2B__utmb%3D" + cookie + "%3B%2B__utmc%3D" + cookie + "%3B%2B__utmz%3D" + cookie + "." + now + ".2.2.utmccn%3D(direct)%7Cutmcsr%3D(direct)%7Cutmcmd%3D(none)%3B%2B__utmv%3D" + cookie);
        return url.toString();
    }

    public void setRefererURL(String refererURL) {
        this.refererURL = refererURL;
    }
}
