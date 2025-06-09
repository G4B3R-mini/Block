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

## Massiv Bugs

- [ ] BrowserComponent, custom sesh
    - [ ] show toolbar menu icon that pops up menu, but only show options available for custom tab,
      also add some options for custom tabs (open in browser, etc)
- [ ] MozEngineView not working properly when go to new compose page then return (most likely has to
  do with lifecycle management, when return to page need to reset/relink)
- [ ] theme settings
    - [ ] sometimes current theme selected when edit custom theme
- [ ] proto settings
    - [ ] currently default instance loaded first, fix so custom instance is first instance
      loaded, make sure to use runBlocking {} properly (was blocking app
      last time I tried it)

## IntentProcessor and ExternalAppBrowser

- [ ] pending
    - [ ] external toolbar visibility depending on tab manifest
    - [ ] show loading progressbar
- [ ] new structure
    - [ ] link custom tab session to activity BrowserComponentState
    - [ ] make BrowserComponentState parcelable and store in activity
    - [ ] there can be multiple instances of HomeActivity
    - [ ] figure out how to set flags for each activity / task
    - [ ] there can only be one instance of: browser task, external app task, or web app task, each
      with one instance allowed to be saved in recents screen
    - [ ] each possible instance is different, but they're all instances of HomeActivity, this is
      why when creating a new activity from intent receiver, flag must be set, and existing ones
      must be checked in order to determine if should create new instance or override currently
      existing one
    - [ ] launchMode standard allows multiple top-level instances, set flag and check if existing to
      know if should create or override existing instance, dont want to fill up recents screen with
      tons of instances
- [ ] for navigation, back pressed handler
    - [ ] if tab can go back, then go back, if cannot go back anymore, call system pop and return to
      app that requested browser, and custom tab is closed
- [ ] slightly less buggy now
- [ ] make browser components use custom tab if not setup already (web prompter, find in page,
  permissions, download feature)
- [ ] current strategy for custom tab manifest:
    - [ ] move browserComponentState to parent activity, there call start() and stop() accordingly,
      this also means access to biometric setup and activity callbacks (FileManager and other stuff
      for web prompter) will be much easier, no need for complex fragment wrapper implementation
    - [ ] funs in ExternalAppBrowserFragment will be moved to BrowserComponentState, listeners will
      be added/removed, depending on if custom tab exists

## InfernoHistoryPage

- [ ] offset wonky, works pretty well thou, check how implemented in moz pager
- [ ] delete time range (2 hours, 2 days, everything), copy moz implementation
- [ ] reference DefaultPagedHistoryProvider
- [ ] also add time range deletion,
  copy [R.layout.delete_history_time_range_dialog](./app/src/main/res/layout/delete_history_time_range_dialog.xml)

## TabTray

- [ ] missing functionality (synced tabs, tab grid layout, maybe some menu options)

### Steps:

- [ ] rename HomeActivity to BrowserActivity, or just keep it sounds nice

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