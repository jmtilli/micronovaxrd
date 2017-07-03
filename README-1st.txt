INSTALLATION

This programs needs Java 1.5 to run. This program has been tested on Windows
and Linux, but it should work on any platform for which Java is available.

First, you need to install Java Runtime Environment from
http://www.java.com/en/download/. You can test the installation by trying to
start xrd.jar. On Windows it can be started by double-clicking it. It should
start properly. If you have Java already installed and the program doesn't
start after double-clicking xrd.jar, try reinstalling Java.



IMPORTANT NOTE:

If you are running on a really old computer with either Java 1.5 or a gigabyte
of memory or less, you should use run.bat (for Windows) or run.sh (for
Unix-like operating systems) to start the program. Starting the program by
double-clicking xrd.jar limits the maximum memory allocation to a value too
small. Hitting the memory limit will result in unpredictable behavior. This
note does not apply to modern computers with both a Java version greater than
1.5 and 2 gigabytes of memory or more. With modern computers, you may start
the program by double-clicking xrd.jar.



DEFAULT LAYER MODEL:

The layer model contains all the other information of the measurement setup
except the actual measured intensities. The wavelength and instrument
convolution width (specified by FWHM) are therefore part of the layer model.
Usually these values are constant for a given diffractometer, so the same
values of wavelength and FWHM are used in most setups.

If you want to have a specific layer model loaded automatically when the
program starts, save the layer model to a file named "default.layers". On Unix
systems, the name is case sensitive. The file should be saved to the directory
that contains the files atomic_masses.txt and octave_path.txt.

You might want to set the wavelength and FWHM to correct values and add a
substrate layer to the default layer model.

An example default layer model is provided with the program. Because of this,
if you modify this default layer model or install your own, you must be aware
that installing a new version of this program will overwrite your default layer
model. If you don't want your default model overwritten when updating the
program, you must keep a backup copy of your own default.layers.
