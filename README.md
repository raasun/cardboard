#Google Cardboard VR Toolkit Library Project

**CAUTION**: This is decompiled from JAR using JD-GUI. No guarantees.  

At present cardboard.jar is provided as binary along with treasure hunt cardboard sample at https://github.com/googlesamples/cardboard/

Google (+Christian Plagemann) replied on Google+ Cardboard & VR Developers Community promising the sources.
> Sources are coming. cardboard.jar just contains the binaries at this point. 

In the meantime, these sources are to serve the enthusiasts. Personally am taking the NDK route, minimizing Java in my code, using these Java sources as reference for C/C++ NativeActivity. 

The toolkit (library) simplifies many common VR development tasks, including:
* Lens distortion correction.
* Head tracking.
* 3D calibration.
* Side-by-side rendering.
* Stereo geometry configuration.
* User input event handling.

Internal classes handle math for sensor input to orientation: 
* vector
* matrix
* filtering

Very few modifications were done after decompiling cardboard.jar using JD-GUI tool. 
* The decompiler has replaced several named constants with the respective values. Left it so. 
* Decompiler didn’t bother with proper line wrapping, nor did I.
* Just one line class description comments for public classes/interfaces were added. 
* For full comments, refer to Google's public documentation https://developers.google.com/cardboard/api/reference/packages

Made a minor change to let volume buttons cause the same effect as magnet pull on cardboard.

Usage:
* Import this project as a library project. 
* Use it along with treasure hunt sample (also provided from the same github handle as a different project)
