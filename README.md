# About

Inferno is a browser for Android based off of Mozilla's geckoView for Android, Mozilla's Android
Components, and built from Mozilla's Android Fenix browser. We're not a firefox fork like most other
browsers, but instead a firefox-based browser using Mozilla's Android Components. Although this
means more code to write initially, it allows for more customizability while maintaining similarity
to firefox, and less maintenance in the long term since we don't have to merge with changes made to
firefox for Android, which is nice.

# Features

- tabs (finally for firefox-mobile based apps)
- color customization (may be buggy)
- enhanced privacy (mozilla telemetry has been removed)

# Upcoming features:

- privacy
    - mozilla telemetry disabled
- theme (slick ui with lots of preference options)
- lots of customizability in general

that's it for now, hopefully this project doesn't die

# Under Construction

- [ ] color customization
    - [ ] preset themes and custom themes
- [ ] more customization settings
    - [ ] enable / disable tabs
    - [ ] customize toolbar items
    - [ ] toolbar and tabs position (top / bottom for each, which is above and below in case both on
      same side)
- [ ] tabs
    - [ ] set bottom bar and top bar height to 0 always (makes sites slow when variable)
    - [ ] make bottom bar semi transparent (apply 0.5 alpha to background color for all bottom
      components, eg: toolbar, tabs, readerview controls, fip bar, etc.)
    - [ ] use icons storage for favicon square (persisted)
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

# Future Features

- [ ] more toolbar items
    - [ ] go to passwords page (key with bottom right profile view) (requires auth, consider setting
      a timeout like bitwarden does)
    - [ ] 
- [ ] more customization settings
    - [ ] if horizontal, add option to show tabs on right or left, make resizable by sliding
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