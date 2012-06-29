# Google Safe Browsing 2 Finagle Service

Wraps [Google Safebrowsing2 Scala API](https://github.com/snopoke/google-safebrowsing2) in a [Finagle](http://twitter.github.com/finagle/)
service.

## Features
* Maintain local blacklist database according to [Google Safe Browsing API v2](https://developers.google.com/safe-browsing/)
* Expose HTTP service that implements the [Google Safebrowsing Lookup API](https://developers.google.com/safe-browsing/lookup_guide)

# Use cases
This service can be used to maintain a local blacklist database that can then be queried directly by using this 
[Google Safebrowsing2](https://github.com/snopoke/google-safebrowsing2) library.

The service can also be used to offer a local version of the Lookup API. This might be useful when you have a number of other 
application that need to do local lookup but don't want each one to have to maintain its own database.