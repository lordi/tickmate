# Tickmate

### About

Tickmate is basically a one bit journal. For each day, you can specify whether something has occurred or not. The occurrences can be arbitrary things, like flossing your teeth or having a drink. You are able to quantify your habits and the accumulated data can be displayed for the past weeks or months to track changes over time. This way, it may help you to get over your old habits or embody new ones.

Tickmate is an Android App and is available from the [Play Store](https://play.google.com/store/apps/details?id=de.smasi.tickmate) and [F-Droid](https://f-droid.org/repository/browse/?fdid=de.smasi.tickmate).

![Screenshot](artwork/screenshot.png)

### Android Wear

The Android Wear app lets you add and remove Ticks from your wrist and shows your weekly stats.
It uses the Android Wear [MessageApi](http://developer.android.com/training/wearables/data-layer/messages.html) for reading/writing data, so the app needs an active connection to the phone in order to work.

![Android Wear: Setting Tick](artwork/android_wear/wear_tick.png)
![Android Wear: Setting Multitick](artwork/android_wear/wear_multitick.png)
![Android Wear: Weekly stats](artwork/android_wear/wear_stats.png)

### Analysis

These section contains development ideas for the analysis of Tickmate data. In
future, those ideas might be integrated into the app or available as a Web
service.

Plot multiple tracks:

![Analysis: Plot](analysis/monthly_example.png)

Find correlations between your tracks:

![Analysis: Correlation](analysis/corr_example.png)

### Notes

![Build status](https://travis-ci.org/lordi/tickmate.svg?branch=master)

Contains icons from the [Glyphicons FREE icon set](http://glyphicons.com/) (CC-BY-3.0)

Bitcoin donations are happily accepted at [18tub3juj26zyGwdpmGDLgtLEpfFf2Nvhu](http://blockchain.info/de/address/18tub3juj26zyGwdpmGDLgtLEpfFf2Nvhu).

[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=&url=https://github.com/lordi/tickmate&title=Tickmate&language=&tags=github&category=software)

[Translations](LOCALIZATION.md) are gratefully accepted as well.
