# VoIP.ms SMS #

[![Android CI (primary)](https://github.com/michaelkourlas/voipms-sms-client/actions/workflows/android-primary.yml/badge.svg)](https://github.com/michaelkourlas/voipms-sms-client/actions/workflows/android-primary.yml) [![Android CI (F-Droid)](https://github.com/michaelkourlas/voipms-sms-client/actions/workflows/android-fdroid.yml/badge.svg)](https://github.com/michaelkourlas/voipms-sms-client/actions/workflows/android-fdroid.yml)

## Overview ##

VoIP.ms SMS is an Android messaging app for VoIP.ms that seeks to replicate the aesthetic of [Google's official SMS app](https://play.google.com/store/apps/details?id=com.google.android.apps.messaging).

[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
    alt="Get it on Google Play"
    height="50">](https://play.google.com/store/apps/details?id=net.kourlas.voipms_sms)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="50">](https://f-droid.org/packages/net.kourlas.voipms_sms)

## Features ##

* Material design
* Push notifications (if using the Google Play version of the app)
* Synchronization with device contacts
* Message search
* Comprehensive support for synchronization with VoIP.ms
* Completely free

## Rationale ##

A number of people use VoIP.ms as a cheaper alternative to subscribing to a voice plan for their mobile devices.

Unfortunately, this can make sending text messages rather difficult, as the [VoIP.ms SMS Message Center](https://voip.ms/m/sms.php) is clearly built as a diagnostic tool for use in desktop browsers, not as an easy way to send and receive messages on a mobile device.

VoIP.ms does provide a [mobile version](https://sms.voip.ms/) of this interface with an improved UI, but it still lacks important features that are only possible with a dedicated app.

## Installation ##

The Google Play version of the app uses closed-source Firebase libraries to
support push notifications. The F-Droid version of the application is completely
open source.

The Google Play version of the app can be downloaded from the [Releases section](https://github.com/michaelkourlas/voipms-sms-client/releases) of the GitHub repository.

## Documentation ##

The app's documentation is available in the [HELP.md file](https://github.com/michaelkourlas/voipms-sms-client/blob/master/HELP.md).

## License ##

VoIP.ms SMS is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Google Play and the Google Play logo are trademarks of Google LLC.
