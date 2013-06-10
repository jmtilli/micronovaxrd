# You almost certainly have to modify these
MATLAB = /p/appl/math/matlab
JAVA = /usr/lib/jvm/java-1.5.0-sun

# You might need to modify these
MLIB = -L$(MATLAB)/bin/glnx86 -Wl,-rpath,$(MATLAB)/bin/glnx86
MLIBS = -leng -lmx
MINCLUDE = -I$(MATLAB)/extern/include
JINCLUDE = -I$(JAVA)/include -I$(JAVA)/include/linux

CFLAGS = ${JINCLUDE} ${MINCLUDE} -fPIC
LDFLAGS = -shared -fPIC
LINKER = ${CC}

all: libJMatLink.so

libJMatLink.so: JMatLink.o
	${LINKER} ${MLIB} ${LDFLAGS} ${MLIBS} -o libJMatLink.so JMatLink.o

JMatLink.o: JMatLink.c
	${CC} ${CFLAGS} -c -o JMatLink.o JMatLink.c

clean:
	${RM} JMatLink.o
