INSTALLATION


This programs needs Java 1.5 and a Matlab-compatible programming language to
run. The Matlab-compatible languages currently supported are Octave with
octave-forge and Matlab. The supported versions of Octave are 2.7 and 2.9. 2.9
is an unstable development version, so 2.7 should be used until 3.0 is
released. Other versions of Octave might work. This program has been tested on
Windows and Linux, but it should work on any platform for which Java and
Octave or Matlab are available.



First, you need to install Java Runtime Environment from
http://www.java.com/en/download/. You can test the installation by trying to
start xrd.jar. On Windows it can be started by double-clicking it. It should
start properly or complain about not being able to use Matlab. If you have
Java already installed and the program doesn't start after double-clicking
xrd.jar, try reinstalling Java.

Should you choose to use Octave instead of Matlab, you now have to install
Octave. An installer for Windows is available from
http://sourceforge.net/project/showfiles.php?group_id=2888

You can choose to use the stable version 2.1.73 or the development version
2.9.13. 2.1.73 is recommended.



Installing Octave on Windows is a bit tricky. It must be installed in such a
way that it can be started directly from command line. The installer will not
change the PATH environment variables. If Octave is installed to "C:\Octave",
you must add the following directories to PATH:

C:\octave\bin
C:\octave\lib\lapack

The directories are separated by semicolon. For example, if the original PATH
was:

c:\windows\system32;c:\windows

you would have to change it to:

c:\windows\system32;c:\windows;C:\octave\bin;C:\octave\lib\lapack

On Windows XP PATH may be changed from
Control Panel -> System -> Advanced -> Environment variables -> System variables

If you are unable to change system variables, you can add a user variable
instead. The effective PATH is a combination of directories from the user
variable and the system variable, so you won't need to add all the directories
from the system variable to the user variable.

Octave may be tested by typing the command to start Octave to command line. It
is "octave-2.1.73" for the older, stable version. If octave
starts, try the command

quad("cos",-pi/2,pi/2)

the resuld should be 2.



After Java and Octave are installed, you have to create a text file named
"octave_path.txt", which will contain the command to start Octave and the
switch "-q". The file must be in the directory containing xrd.jar. On Windows
you have to add the ".exe" extension, so if you start Octave with the command
"octave-2.1.73", you have to add the following line to octave_path.txt:

octave-2.1.73.exe -q

On Unix systems the command is usually "octave" with no extensions, so you can
just add the line

octave -q

to octave_path.txt.

The "-q" switch is mandatory!

If the file named "octave_path.txt" doesn't exist or is empty, Matlab will be
used instead of Octave. On Windows, some versions of Matlab might require
administrative privileges to run. If Matlab doesn't work on Windows, try
reinstalling it. On Unix systems, you have to compile a library which is in
the directory nativelib and install it on a platform-defined manner.
Generally, the library should be in one of the directories libraries are
searched from. If you are unable to install the library there, you have to
adjust LD_LIBRARY_PATH. An example Makefile is included, which works on the
Linux computers of TKK's computing centre. You will almost certainly need to
adjust this file.


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
except the actual measured intensities. The wavelength and instrument convolution
width (specified by FWHM) are therefore part of the layer model. Usually these
values are constant for a given diffractometer, so the same values of wavelength
and FWHM are used in most setups.

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
