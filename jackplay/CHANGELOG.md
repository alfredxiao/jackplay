# Jackplay Change Logs

## To be released
- Can trace constructor?

## 0.9.3 (22-Aug-2016)
- bug fix where the URI for loading currentProgram has changed to be /info/currentProgram

## 0.9.2 (22-Aug-2016)
- Added a feature where a failure in adding new redefinition does not affect previously working redefinition

## 0.9.1 (22-Aug-2016)
- bug fix where redefinition can't be added again and again

## 0.9.0 (21-Aug-2016)
- Added unit and integration tests
- Refactoring around play process, now we clearly have a REHEARSAL and a STAGING phase, where in REHEARSAL phase the Java Instrumentation API is not involved, we can find compilation errors in this phase; in STAGING the transformation is applied by Instrumentation API and we can find errors like VerifyError
- Display timestamps in logging
- Added logFile option to specify a file path to log to
- Display exception logs in UI in red color

## 0.8.8 (18-Aug-2016)
- bug fix NullPointerException when removing a method from program

## 0.8.7 (17-Aug-2016)
- Apply same transformation to all Classes when multiple are found (loaded by different classloader)
- Refactoring and bug fix

## 0.8.6 (17-Aug-2016)
- Refactoring and bug fix

## 0.8.5 (16-Aug-2016)
- Added About/Info page to display information about this program
- Loads tracing rules from startup hence enabling tracing methods ran in application initialisation phase

## 0.8.4 (15-Aug-2016)
- Auto focus method lookup input box when page is opened
- Added threadId and threadName as part of traceLog
- Support for listening on HTTPS
- Display trace log counts
- Moved limit of autoSuggest to server side, such that it last during the entire session

## 0.8.3 (14-Aug-2016)
- Allows users to configure max number of suggestions and the limit of trace log entries
- Disable playing with abstract method
- Display '...more...' as last suggestion when not all suggestions are being shown
