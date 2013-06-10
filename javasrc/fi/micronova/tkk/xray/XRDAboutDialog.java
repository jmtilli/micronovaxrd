package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import fi.micronova.tkk.xray.dialogs.*;





public class XRDAboutDialog extends GenericAboutDialog {

    private static final String title = "About XRD";
    private static final String text =

"Micronova XRD fitting software, version 0.1\n"+
"\n"+
"IMPORTANT NOTICE: This is a beta version which is NOT released\n"+
"under the following license:\n"+
"\n"+
"Copyright (c) 2007 Micronova, Helsinki University of Technology\n"+
"All rights reserved.\n"+
"\n"+
"Author:\n"+
"  Juha-Matti Tilli <juha-matti.tilli@tkk.fi>\n"+
"\n"+
"Redistribution and use in source and binary forms, with or without\n"+
"modification, are permitted provided that the following conditions\n"+
"are met:\n"+
"1. Redistributions of source code must retain the above copyright\n"+
"   notice, the above list of authors, this list of conditions and\n"+
"   the following disclaimer.\n"+
"2. Redistributions in binary form must reproduce the above copyright\n"+
"   notice, the above list of authors, this list of conditions and\n"+
"   the following disclaimer in the documentation and/or other materials\n"+
"   provided with the distribution.\n"+
"3. All advertising materials mentioning features or use of this software\n"+
"   must display the following acknowledgement:\n"+
"     This product includes software developed by Micronova, Helsinki\n"+
"     University of Technology\n"+
/*
"4. All scientific articles making use of this software must cite the\n"+
"   following article:\n"+
"     Tiilikainen J, Tilli J-M, Bosund V, Mattila M, Hakkarainen T,\n"+
"     Airaksinen V-M and Lipsanen H 2007 J. Phys D: Appl. Phys. 40 215-8\n"+
*/
"\n"+
"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS\n"+
"``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED\n"+
"TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR\n"+
"PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR CONTRIBUTORS\n"+
"BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR\n"+
"CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF\n"+
"SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS\n"+
"INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN\n"+
"CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\n"+
"ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE\n"+
"POSSIBILITY OF SUCH DAMAGE.\n"+
"\n"+
"This program uses JFreeChart, Copyright 2000-2006, by Object Refinery\n"+
"Limited and Contributors. It is distributed under the GNU Lesser General\n"+
"Public License: http://www.gnu.org/licenses/lgpl.html\n"+
"\n"+
"This program uses modified version of JMatLink, which is distributed under\n"+
"the following license:\n"+
"\n"+
"Copyright (c) 1999-2005 Stefan Mueller (stefan@held-mueller.de)\n"+
"All rights reserved.\n"+
"\n"+
"Redistribution and use in source and binary forms, with or without\n"+
"modification, are permitted provided that the following conditions\n"+
"are met:\n"+
"\n"+
"1. This software is free of ANY charges. It may be used in any\n"+
"   private, educational, governmental or commercial software\n"+
"   product.\n"+
"\n"+
"2. Redistributions of source code must retain the above copyright\n"+
"   notice, this list of conditions and the following disclaimer. \n"+
"\n"+
"3. Redistributions in binary form must reproduce the above copyright\n"+
"   notice, this list of conditions and the following disclaimer in\n"+
"   the documentation and/or other materials provided with the\n"+
"   distribution.\n"+
"\n"+
"5. Products derived from this software may not be called \"JMatLink\"\n"+
"   nor may \"JMatLink\" appear in their names without prior written\n"+
"   permission of the author.\n"+
"\n"+
"6. Redistributions of any form whatsoever must retain a link to \n"+
"   the homepage of JMatLink:\n"+
"   http://www.held-mueller.de/JMatLink/\n"+
"   or\n"+
"   http://jmatlink.sourceforge.net/\n"+
"\n"+
"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY\n"+
"EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n"+
"IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR\n"+
"PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR\n"+
"ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,\n"+
"SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"+
"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\n"+
"LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)\n"+
"HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,\n"+
"STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\n"+
"ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED\n"+
"OF THE POSSIBILITY OF SUCH DAMAGE.\n"+
"====================================================================\n"+
"THIS SOFTWARE USES MATLAB/SIMULINK TO DO EXTENSIVE CALCULATIONS ON THE \n"+
"LOCAL SYSTEM WHERE IT IS INSTALLED. SINCE MATLAB/SIMULINK ALLOWS ACCESS \n"+
"TO THE LOCAL DISCS AND FILE SYSTEMS MALLICIOUS OR BUGGY MATLAB M- OR \n"+
"MEX- FILES AND NATIVE AND EXTERNAL PROGRAMS CAN DESTROY OR MANIPULATE \n"+
"ALL DATA ON YOUR SYSTEM.\n"+
"IF THIS SOFTWARE IS USED IN AN INTERNET APPLICATION IT MIGHT BE \n"+
"POSSIBLE TO ACCESS AND MANIPULATE \"ALL\" DATA ON THE COMPUTER WHICH\n"+
"IS RUNNING JMATLINK/MATLAB. BE VERY CAREFUL TO OPENING YOUR MACHINE \n"+
"TO OTHER USERS OVER THE INTERNET. \n"+
"====================================================================\n"+
"\n"+
"Matlab is a trademark of TheMathworks, Inc.\n"+
"Java is a trademark of Sun, Inc.\n";


    public XRDAboutDialog(Frame f)
    {
        super(f,title,text);
    }
    public XRDAboutDialog(Dialog d)
    {
        super(d,title,text);
    }
}
