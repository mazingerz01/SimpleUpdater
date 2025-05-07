# A zero dependency library to download a new version of your program and to restart it.
### What it does
- Check a local version string of your application against a version string on an URL and tell if there is a newer version available
- Download and extract a zip file which contains your new version of your application, but you can also do it on your own in any other format.
- Shut down your application, and optionally start the new downloaded version
- Optionally clean up the old version 
### Limitations
- Currently Windows only - *if someone wants to add Unix support, please join.*

### Inside
The library mainly consists of a bunch of static methods which sufficient commentary to be used. You can build in any custom steps in between or instead these method calls, e.g. your download routine or your consistency check of your downloaded files.
