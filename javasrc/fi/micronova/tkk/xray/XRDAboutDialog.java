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

"Micronova XRD fitting software, version 1.1\n"+
"\n"+
"Copyright 2006-2014 Aalto University\n"+
"Copyright 2006-2019 Juha-Matti Tilli\n"+
"\n"+
"Authors:\n"+
"  Juha-Matti Tilli <juha-matti.tilli@iki.fi>\n"+
"\n"+
"Permission is hereby granted, free of charge, to any person obtaining a copy of\n"+
"this software and associated documentation files (the \"Software\"), to deal in \n"+
"the Software without restriction, including without limitation the rights to\n"+
"use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies\n"+
"of the Software, and to permit persons to whom the Software is furnished to do\n"+
"so, subject to the following conditions:\n"+
"\n"+
"The above copyright notice and this permission notice shall be included in all \n"+
"copies or substantial portions of the Software.\n"+
"\n"+
"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR \n"+
"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n"+
"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n"+
"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n"+
"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n"+
"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n"+
"SOFTWARE.\n"+
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
