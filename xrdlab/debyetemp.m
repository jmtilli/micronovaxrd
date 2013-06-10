% A script to generate B_table2.txt


% the following should be added: B, N, P, S, O, I, Br
% there might be more that are missing


Td = Inf*ones(1,92);

% Debye temperatures
% Source: Kittel, Introduction to Solid State Physics, 7th Ed., Wiley, (1996)
Td(3) = 344;
Td(4) = 1440;
Td(6) = 2230;
Td(10) = 74;
Td(11) = 158;
Td(12) = 400;
Td(13) = 428;
Td(14) = 645;
Td(18) = 92;
Td(19) = 91;
Td(20) = 230;
Td(21) = 360;
Td(22) = 420;
Td(23) = 380;
Td(24) = 630;
Td(25) = 410;
Td(26) = 470;
Td(27) = 445;
Td(28) = 450;
Td(29) = 343;
Td(30) = 327;
Td(31) = 320;
Td(32) = 374;
Td(33) = 282;
Td(34) = 90;
Td(36) = 72;
Td(37) = 56;
Td(38) = 147;
Td(39) = 280;
Td(40) = 291;
Td(41) = 275;
Td(42) = 450;
Td(44) = 600;
Td(45) = 480;
Td(46) = 274;
Td(47) = 225;
Td(48) = 209;
Td(49) = 108;
Td(50) = 200;
Td(51) = 211;
Td(52) = 153;
Td(54) = 64;
Td(55) = 38;
Td(56) = 110;
Td(57) = 142;
Td(64) = 200;
Td(66) = 210;
Td(70) = 120;
Td(71) = 210;
Td(72) = 252;
Td(73) = 240;
Td(74) = 400;
Td(75) = 430;
Td(76) = 500;
Td(77) = 420;
Td(78) = 240;
Td(79) = 165;
Td(80) = 71.9;
Td(81) = 78.5;
Td(82) = 105;
Td(83) = 119;
Td(86) = 64;
Td(90) = 163;
Td(92) = 207;




% Debye temperatures




B_table = zeros(1,92);
B_table(symbol2element('Li')) = 4.60;
B_table(symbol2element('Be')) = 0.40;
B_table(symbol2element('C')) = 0.14;
B_table(symbol2element('Na')) = 6.36;
B_table(symbol2element('Mg')) = 1.73;
B_table(symbol2element('Al')) = 0.75;
B_table(symbol2element('Si')) = 0.46;
B_table(symbol2element('K')) = 10.24;
B_table(symbol2element('Ca')) = (1.91+2.48)/2;
B_table(symbol2element('Sc')) = 0.71;
B_table(symbol2element('Ti')) = 0.49;
B_table(symbol2element('V')) = 0.55;
B_table(symbol2element('Cr')) = 0.24;
B_table(symbol2element('Fe')) = (0.31+0.53)/2;
B_table(symbol2element('Ni')) = 0.34;
B_table(symbol2element('Cu')) = 0.53;
B_table(symbol2element('Zn')) = 1.09;
B_table(symbol2element('Ge')) = 0.57;
%B_table(symbol2element('Kr')) = 3.63; % 40 K
B_table(symbol2element('Rb')) = 12.60;
B_table(symbol2element('Sr')) = 3.62;
B_table(symbol2element('Y')) = 0.82;
B_table(symbol2element('Zr')) = 0.54;
B_table(symbol2element('Nb')) = 0.43;
B_table(symbol2element('Mo')) = 0.21;
B_table(symbol2element('Pd')) = 0.43;
B_table(symbol2element('Ag')) = 0.70;
B_table(symbol2element('Sn')) = 1.08;
%B_table(symbol2element('Xe')) = 4.86; % 160 K
B_table(symbol2element('Cs')) = 16.77;
B_table(symbol2element('Ba')) = 2.93;
B_table(symbol2element('La')) = 1.77;
B_table(symbol2element('Tb')) = 0.97;
B_table(symbol2element('Ho')) = 0.80;
B_table(symbol2element('Ta')) = 0.31;
B_table(symbol2element('W')) = 0.15;
B_table(symbol2element('Pt')) = 0.36;
B_table(symbol2element('Au')) = 0.59;
B_table(symbol2element('Pb')) = 2.03;
B_table(symbol2element('Th')) = 0.70;

B_table = B_table / 1e20;




weights = atomweight(1:92);
T = 280; % largest temperature in the article
B_Td = calcB(T,Td,weights);


% B_table is the primary source, use B_Td only for
% elements unavailable in B_table
B_table2 = B_table + B_Td .* (B_table == 0);

fd = fopen('B_table2.txt','w');
fprintf(fd,'%g ',B_table2);
fclose(fd);
