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
"  Juha-Matti Tilli <juha-matti.tilli@iki.fi>\n"+
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
"This program uses XChart, Copyright 2011-2015 Xeiam LLC (http://xeiam.com)\n"+
"and contributors, and Copyright 2015-2017 Knowm Inc. (http://knowm.org) and\n"+
"contributors. It is licensed under the Apache License, Version 2.0:\n"+
"http://www.apache.org/licenses/LICENSE-2.0";


    public XRDAboutDialog(Frame f)
    {
        super(f,title,text);
    }
    public XRDAboutDialog(Dialog d)
    {
        super(d,title,text);
    }
}
