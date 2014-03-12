lambda = 1.54056e-10;

matZ = matdb;
GaAs = Zmat2mat(matZ.GaAs_004,lambda);
AlAs = Zmat2mat(matZ.AlAs_004,lambda);
InAs = Zmat2mat(matZ.InAs_004,lambda);


GaAs_mixture = {1.0, GaAs};
GaAs_mixture2.first = GaAs_mixture;
GaAs_mixture2.second = GaAs_mixture;
GaAs_mixture2.x = 0;

InAs_mixture = {1.0, InAs};
InAs_mixture2.first = InAs_mixture;
InAs_mixture2.second = InAs_mixture;
InAs_mixture2.x = 0;

AlAs_mixture = {1.0, AlAs};
AlAs_mixture2.first = AlAs_mixture;
AlAs_mixture2.second = AlAs_mixture;
AlAs_mixture2.x = 0;

AlGaAs_mixture2.first = AlAs_mixture;
AlGaAs_mixture2.second = GaAs_mixture;
AlGaAs_mixture2.x = 0;







% not strained
[GaAs_suscdata,GaAs_xyspace,GaAs_zspace] = matsusc(GaAs_mixture2, 0);

% strained
AlGaAs_mixture2.x = 0.0;
[AlAs_suscdata,AlAs_xyspace,AlAs_zspace] = matsusc(AlGaAs_mixture2, GaAs_xyspace);
AlGaAs_mixture2.x = 0.5;
[AlGaAs_suscdata,AlGaAs_xyspace,AlGaAs_zspace] = matsusc(AlGaAs_mixture2, GaAs_xyspace);

%AlAs_suscdata
%AlAs_suscdata2
%AlAs_xyspace == AlAs_xyspace2
%AlAs_zspace == AlAs_zspace2

InGaAs_mixture2.first = InAs_mixture;
InGaAs_mixture2.second = GaAs_mixture;
InGaAs_mixture2.x = 0.9;
[In1Ga9As10_suscdata,In1Ga9As10_xyspace,In1Ga9As10_zspace] = matsusc(InGaAs_mixture2, GaAs_xyspace);
InGaAs_mixture2.x = 0.8;
[In2Ga8As10_suscdata,In2Ga8As10_xyspace,In2Ga8As10_zspace] = matsusc(InGaAs_mixture2, GaAs_xyspace);




%zspace = [5.67, 5.6532];
d = [2000; 0]*1e-10;
zspace = [AlAs_zspace; GaAs_zspace];
suscdata = [AlAs_suscdata; GaAs_suscdata];
pol = 1;

theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace
theta_Bs = theta_B(end);
theta = linspace(theta_Bs-2000*pi/180/3600, theta_Bs+500*pi/180/3600, 500);



% t, x, chi0, chih, chihinv, chi0, chih, chihinv

% datatype: susclist
%[chi0, chih, chihinv
% ...]

% datatype: poisson, column vector

% datatype: spacing, column vector

% datatype: isstrained, column vector of booleans (0, 1)

% (new - old)/old



%zspace
%d
%suscdata



title('(400) reflection of 200 nm strained layer of AlAs on a GaAs substrate');
plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
xlabel('Deviation from Bragg''s angle (seconds)');
ylabel('Reflectivity (dB)');
legend('200 nm AlAs on GaAs');
pause;


title('(400) reflection of 200 nm strained layer of AlAs on a GaAs substrate');
plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
hold('on');

zspace = [AlGaAs_zspace; GaAs_zspace];
suscdata = [AlGaAs_suscdata; GaAs_suscdata];

plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
hold('on');


% various InGaAs mixtures
zspace = [In1Ga9As10_zspace; GaAs_zspace];
suscdata = [In1Ga9As10_suscdata; GaAs_suscdata];

plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
hold('on');

zspace = [In2Ga8As10_zspace; GaAs_zspace];
suscdata = [In2Ga8As10_suscdata; GaAs_suscdata];

plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
hold('on');




zspace = [GaAs_zspace];
d = [0];
suscdata = [GaAs_suscdata];


plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
xlabel('Deviation from Bragg''s angle (seconds)');
ylabel('Reflectivity (dB)');

legend('200 nm AlAs on GaAs', '200 nm Al0.5Ga0.5As on GaAs', '200 nm In0.1Ga0.9As on GaAs', '200 nm In0.2Ga0.8As on GaAs', 'GaAs substrate');
pause;

hold('off');




% TODO: radius of curvature



% References:
% M. Wormington, C. Panaccione, K. M. Matney, and D. K. Bowen, Phil. Trans. R. Soc. Lond. A (1999) 357, 2827-2848
% H. Wang, S-J. Xu, Q. Li, and S-L. Feng, Chin. Phys. Lett. Vol. 18, No. 6 (2001) 810

% Two excellent articles. Not available on the net, but can be read in TKK library:
% M. A. G. Halliwell, M. H. Lyons, and M. J. Hill, J. Cryst. Growth (1984) 68, 523-531
% W. J. Bartels, J. Hornstra, and D. J. W. Lobeek, Acta. Crystallogr. A (1986) 42, 539-545

% to-read:
% D. K. Bowen, L. Loxley, B. K. Tanner, M. Cooke, and M. A. Capano, Mater. Res. Soc. Symp. Proc. (1991) 208, 113-118
% and the original Takagi-Taupin articles (TODO: find references!)










