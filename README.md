# About

Inferno Browser is a mobile browser for Android which mainly focuses on usability, privacy, and
relevant features. It's built mainly off of Mozilla's Fenix browser, also making use of the android
gecko engine. It is almost ready for beta launch on the Google play store

# Nice Features

- tab bar
- toolbar customization (modify items in toolbar)
- color customization / themes
    - preset themes and custom themes
- enhanced privacy (mozilla telemetry has been removed)

that's it for now, hopefully this project doesn't die

# Important Stuff

## Search Engines

- Engines to add as defaults:
    - May be able to add at least one SearXNG, or a couple default instances

## Toolbar Settings

- [ ] Reflect toolbar and tabbar positioning, also apply tabbar enabled/disabled
    - [ ] update toolbar height calculation and positioning (add if else tree in compose for order
      and position)
- [ ] Add support for dynamic / non-dynamic toolbar
    - [ ] GeckoView toolbar offset is always 0 (set when parent AndroidView created and on update)
    - [ ] If not dynamic, remove offset, ensure scaffold bottom/top content padding equal to bar
      height
    - [ ] If dynamic, enable offset, ensure scaffold bottom/top content padding is always 0

## Not Implemented

- [ ] Pocket (does anyone even use this, no future plans to implement)

## Critical

`Errors that make something unusable, these should be fixed as soon as possible`

- [x] (Fixed as of latest release) Crash on Google Pixel devices on setup megazord in startup, might be related to proguard
  settings, check megazord init in case some code runs only on release mode
    - [x] test in Google Pixel 6 emulator (check crashlytics for specific model & android version)

`Nothing critical wooooooo`

## Massiv Bugs

- [ ] AppRequestInterceptor
    - [ ] currently using default interceptor, new one will compare apps that can open current uri
      with list of apps that can open a generic uri. If list is different, then forward request, if
      same apps in both then don't show open in dialog
- [ ] AccountView in Settings Page
    - [ ] When first opened with wifi off, exit page, turn on wifi, and return, stays as loading
- [ ] Homepage
    - [ ] top sites buggy (2 default pages repeated, add some more defaults)
    - [ ] persist homepage state, pass that to homepage so doesnt rebuild
- [ ] BrowserComponent
- [ ] Accessibility Settings
    - [ ] custom sizing not doing anything, most likely has to do with default settings being loaded
      first, setting up a settings loader with runBlocking is critical (so selected settings are
      loaded from start)
- [ ] theme settings
    - [ ] sometimes current theme selected when edit custom theme
- [ ] proto settings
    - [ ] currently default instance loaded first, fix so custom instance is first instance
      loaded, make sure to use runBlocking {} properly (was blocking app
      last time I tried it)
- [ ] Extensions
    - [ ] Decentraleyes settings page not loading correctly, has to do with in-browser settings
      page (in fenix, separate page that implements EngineView and a couple features, in inferno,
      goes to new custom tab)
- [ ] Splash Screen
    - [ ] make custom splashscreen activity (check guide url somewhere around here)

## IntentProcessor and ExternalAppBrowser

- [ ] make browser components use custom tab if not setup already (find in page, permissions)
- [ ] current strategy for custom tab manifest:
    - [ ] move browserComponentState to parent activity, there call start() and stop() accordingly,
      this also means access to biometric setup and activity callbacks (FileManager and other stuff
      for web prompter) will be much easier, no need for complex fragment wrapper implementation

## InfernoHistoryPage

- [ ] offset wonky, switch to moz pager implementation instead of custom
- [ ] delete time range (2 hours, 2 days, everything), copy moz implementation
- [ ] reference DefaultPagedHistoryProvider
- [ ] also add time range deletion,
  copy [R.layout.delete_history_time_range_dialog](./app/src/main/res/layout/delete_history_time_range_dialog.xml)

## TabTray

- [ ] missing functionality (synced tabs, tab grid layout, maybe some menu options)

## Settings Pages Status

- [ ] Account Item & Account Pages
    - [ ] not implemented, also account item not visible
- [x] Toolbar (Working)
- [x] Tabs (Working)
    - [ ] side note: enable / disable tabbar not implemented
    - [ ] side note: top / bottom position not implemented (only bottom)
    - [ ] side note: tab tray grid not implemented
- [ ] Search (Buggy)
    - [ ] add browser not working (probably problem with dialog)
- [x] Theme (Working)
    - [ ] side note: sometimes not applied, requires app restart
    - [ ] side note: sometimes current theme is used when edit theme selected

- [x] Gestures (Working)
    - [ ] side note: not implemented in respective components
- [x] Homepage (Working)
- [x] OnQuit (Working)
    - [ ] side note: not reflected when quit, need to add to callback when app closed
- [ ] Passwords (Buggy)
    - [ ] side note: exceptions not tested
    - [ ] side note: getting this error message:

```
Autofill popup isn't shown because autofill is not available.
                                                                                                    
Did you set up autofill?
1. Go to Settings > System > Languages&input > Advanced > Autofill Service
2. Pick a service

Did you add an account?
1. Go to Settings > System > Languages&input > Advanced
2. Click on the settings icon next to the Autofill Service
3. Add your account
```

- [x] Autofill (Working)

- [x] Site Permissions (Working)
    - [ ] side note: exceptions not tested
    - [ ] side note: prefs not tested (access/set in engine)
- [x] Accessibility (Working)
    - [ ] side note: some settings not applied
    - [ ] side note: slide bar looks weird (no thumb) and factor may be wrong (try 0.5-2 instead of
      50-200)
- [x] Language (Working)
- [x] Translations (Working)
    - [ ] side note: exceptions page (never translate) not tested yet

- [x] Privacy and Security (Working)

- [ ] Other stuff
    - [ ] make sure to check setting usage, still have not completed migration from android prefs to
      datastore

## Toolbar

- [ ] add keyboard listener to toolbar, if hidden, remove focus, if pops up, request focus
- [ ] add toolbar menu item editor to toolbar settings page
    - [ ] make toolbar items and toolbar menu items expandable, both can be collapsed at the same
      time, but only one can be expanded at any time, depending on that modify logic to determine
      which settings to edit, and which items can be moved
    - [ ] only items that cannot be in toolbar menu items are origin and menu item, origin mini is
      ok
    - [ ] allowed so user can edit which items are visible and their order, only obligatory is
      settings item (cannot be removed)
    - [ ] add menu settings listener to get items to be displayed

## Pending addition

`Features that will be added soon, priority is in the order listed`

- [ ] AuthIntentReceiverActivity
    - [ ] Instead of going to HomeActivity, go to AuthCustomTabActivity which overrides
      HomeActivity, once auth setup pop activity
- [ ] Toolbar & Toolbar Menu Icons
    - [ ] Account icon, add account state to icon, show icon & description depending on state
- [ ] Qr code scanner
    - [ ] Login (add in AccountSettingsPage for SignedOutOptions)
    - [ ] Homepage / toolbar menu icon for opening website
- [ ] more toolbar items
    - [ ] print page
    - [ ] scrolling screenshot

[//]: # (    - [ ] keeping this here in case I come up with more, currently all good)

- [ ] BrowserComponent
    - [ ] persist component states, or move to state variables to manage visual bloating, pending:
        - [ ] TabsTray
        - [ ] ToolbarMenu
        - [ ] DownloadComponent

# Future Features

`Features that will probably eventually be added`

- [ ] app shortcuts
    - [ ] custom tabs that have launcher shortcuts
    - [ ] have gesture for accessing settings from here since bottom bar will be hidden
    - [ ] also have gesture for toolbar, quick settings for changing shortcut name (works above
      android
      7.0, shouldnt be a problem) and editing other manifest details manually, see if can store
      variable in app manifest to edit manually or update automatically
- [ ] workspaces
    - Requires storing multiple engines probably, one for each workspace. Should check zen browser
      implementation for ideas.
- [ ] synced workspaces
    - [ ] will require a workaround, since vanilla Firefox doesn't even support workspaces currently
        - [ ] Could store / encode in bookmarks folder, there is a max of 5000 however. If
          implemented this way, bookmarks shown would have to hide special storage folders, and they
          would be visible if accessed from a Firefox browser. This would be VERY hacky, but there
          are a lot of fields to store different types of data, and it could be done as cleanly as
          possible, maybe storing everything in a base folder called "_". It would also need to be
          implemented in a way that if edited externally, it doesn't just completely break.
        - [ ] if implemented this way, it would also allow just pinned tabs to be synced, etc
- [ ] bug reporting system / feedback page
    - [ ] for bug reporting show select in settings: (disabled, send report auto, or ask to send
      report)
- [ ] history page
    - [ ] search feature
- [ ] login manager (settings)
    - [ ] add search function to filter logins, implemented by moz in [SavedLoginsFragment]
    - [ ] add login sorting and save selected in prefs
    - [ ] find duplicates? not sure how necessary
    - [ ] add exceptions page
- [ ] site permissions settings page
    - [ ] exceptions, reference moz implementation in `SitePermissionsExceptionsFragment` (
      for clearing site permissions) and `SitePermissionsDetailsExceptionsFragment` (for
      setting individual site permissions)
    - [ ] in exceptions fragment show each site as expandable item, when expanded shows individual
      settings and clear permissions on this site, clear permissions for all sites is at the bottom
      and requires dialog to confirm
- [ ] enhanced tracking protection
    - [ ] exceptions
- [ ] crash reporting
    - [ ] crash reporting setting will have 3 options: Disabled, Automatic, and Manual
        - [ ] if automatic or manual show additional settings for data to send selected by default
          in checkboxes (device model, time error occurred, email for contact -> data ordered from
          less sensitive to most sensitive)
    - [ ] accompanying crashes page that shows all errors (only store 5 latest)
        - [ ] if disabled, show all errors
        - [ ] if manual, show all unsent errors, once sent remove persisted entry
        - [ ] if automatic, show all unsent errors, send error one day after occurs
    - [ ] when error occurs, show crash notification (inferno browser just crashed, click to see
      error), which takes user to individual crash page
    - [ ] crash page when individual error clicked from crashes
        - [ ] show log (first 10 lines, code format with different background), optional data with
          options selected according to settings, description, and send button. When crash sent
          delete from persisted entries.
    - [ ] this _could_ be done with crashlytics, would be very hacky. Before sending a crash, clear
      all crashes, then attach data as messages, add error, and send data
    - [ ] If implemented this way also add crash reporting quick access (bug icon) and add to
      settings at bottom, only if proceed

# Current Structures

- Intent Receiver
    - All intents received go to HomeActivity, only important param passed is customTabId, the rest
      is handled by BrowserComponent
    - HomeActivity Manifest launchMode is set to "standard", this means multiple tasks with
      HomeActivity can exist at the top level
        - Only one instance of each type of HomeActivity can exist, possible instances are normal
          browsing, custom tab (opened by another app), or custom tab opened from shortcut
        - This has not been tested with shortcut apps however, if not working there is a mechanism
          in mind: in IntentReceiver, specify intent extra for browser type, depending on this
          check existing instances and decide if need to create new instance or use existing
- Browser State
    - Browser State is stored in compose in BrowserComponentState, by means of
      rememberBrowserComponentState.
    - BrowserComponentState listens to tab flow and updates accordingly
    - if a CustomTabSessionId is specified in intent that created the activity, it's applied
      accordingly in BrowserComponentState; the custom tab is selected and a CustomTabManager is
      created, which takes care of changes specific to this CustomTabSession (window sizing)
- Features
    - All features (DownloadComponent, InfernoPromptFeature, etc.) are either compose adaptations or
      direct usage of Mozilla's features
    - These listen to browser state changes and apply changes accordingly
    - In the case of compose adaptations, all mozilla logic is moved / adapted to a State instance
      that is remembered in compose and passed to an accompanying ui composable component
- Crash Reporting
    - Automatic crashlytics crash reporting, enabled for com.shmibblez.inferno and
      com.shmibblez.inferno.beta builds (com.shmibblez.inferno.debug ignored)
    - Can only be enabled or disabled, crash page is not possible since no way to get and send
      individual reports with crashlytics, no way to attach optional data
    - Currently `google-services.json` is public since _allegedly_ all api keys are public, and can
      be extracted from apk so y not lol
- Built Apks
    - Code is not obfuscated, only minified so if anyone wishes to verify that code reflects repo
      code they definitely can