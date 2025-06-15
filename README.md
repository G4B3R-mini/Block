# About

Inferno Browser is a mobile browser for Android which mainly focuses on usability, privacy, and
relevant features. It's built mainly off of Mozilla's Fenix browser, also making use of the android
gecko engine. It is almost ready for beta launch on the Google play store

# Next Steps

Before launching however, there are 4 main things that have to be fixed, these are:

- access from external apps (external app browser)
- extension access (settings)
- some websites crashing unexpectedly (does not happen on Firefox)
- crash reporting system

In order for this app to be launched for actual use, these problems must be fixed. Not to shadow the
fact that there are some other things that are a bit hacky or clunky, but they are functional and
can be progressively improved; the 4 points above are crucial and currently lacking. With this in
mind, the app IS currently in a functional state, and is actually very usable this is my main
browser lol, collaboration is welcome you can email me ideas or if you want some pointers on what
needs work right now.

# Nice Features

- tab bar
- color customization / themes
    - preset themes and custom themes
- enhanced privacy (mozilla telemetry has been removed)
- toolbar customization (modify items in toolbar)

that's it for now, hopefully this project doesn't die

# Currently Under Development

## Pending Launch

- [x] Autofill Settings Page
    - [x] add biometric, fix padding (copy fixes to passwords page)
- [ ] Web Prompter
    - [ ] AndroidPhotoPicker not working, activity callbacks murky (may have to add something
      similar to biometric prompt with listener and callback structure)
- [ ] Home Page
    - [ ] override actions (currently functions do nothing)
    - [ ] remove sponsored top sites
- [ ] Crash Reporter
    - [ ] In onboarding, show quick settings on second page (crash reporting
      checked by default, show message saying this helps development of app, but can be disabled
      right there), first page is welcome page, third page is theme
    - [ ] Add crash reporting quick access (bug icon) and add to settings at bottom
        - [ ] Crash reporting page will have a list of crashes with X on the right, when one clicked
          goes to crash page
            - [ ] crash page shows error log (# of lines then 3 dots) and optional checkbox for info
              like device type, time error occurred, android version, and other relevant info, and
              at the bottom cancel button and send

## Critical

`Errors that make something unusable, these should be fixed as soon as possible`

- [ ] Settings Page
    - [ ] when back pressed, nothing happens (does not go back to browser)
        - [ ] check browser component back handlers, something may be interfering there

[//]: # (`Nothing critical wooooooo`)

## Massiv Bugs

- [ ] Homepage
    - [ ] remove sponsored top sites
- [ ] BrowserComponent
    - [ ] every time google search is performed, open in app requested
    - [ ] load page, select, and return to browser not working properly (from extensions page links)
        - [ ] find more addons link (page stops working), extension settings page (very buggy)
- [ ] MozEngineView not working properly when go to new compose page then return (most likely has to
  do with lifecycle management, when return to page need to reset/relink)
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

## IntentProcessor and ExternalAppBrowser

- [ ] make browser components use custom tab if not setup already (find in page, permissions)
- [ ] current strategy for custom tab manifest:
    - [ ] move browserComponentState to parent activity, there call start() and stop() accordingly,
      this also means access to biometric setup and activity callbacks (FileManager and other stuff
      for web prompter) will be much easier, no need for complex fragment wrapper implementation

## InfernoPromptFeatureState

- [ ] add filePicker param and move creation to base activity, this avoids having to create a custom
  FilePicker for use in compose, and assures all logic is handled correctly

## InfernoHistoryPage

- [ ] offset wonky, works pretty well thou, check how implemented in moz pager
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
    - [ ] side note: ui good, passwords not saving when added though
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

## Under Construction

- [ ] biometric
    - [ ] for components that depend on biometric/auth, you could create a fragment in
      compose (as shown [here](https://stackoverflow.com/a/71480760/14642303)), that listens for
      biometric success/fail callbacks, and takes composables as children. Put composables that
      require biometric here with callbacks for biometric/auth events. This removes the need to
      create deeply nested callbacks
    - [ ] so far components that depend on biometric/pin auth are:
        - [ ] dialog feature in browser component
        - [ ] creditCardManager in settings
        - [ ] loginManager in settings
- [ ] autofill settings page
    - [ ] show some settings depending on login state (sync cards, sync logins, etc.)
    - [ ] use authentication when click on manage cards
    - [ ] use authentication when click on manage logins

## Pending addition

`Features that will be added eventually, but are currently not a priority`

- [ ] more toolbar items
    - [ ] go to passwords page (key with bottom right profile view) (requires auth, consider setting
      a timeout like bitwarden does)
    - [ ] extensions settings direct access

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