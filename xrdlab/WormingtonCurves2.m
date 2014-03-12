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
AlGaAs_mixture2.x = 1-0.35;

InGaAs_mixture2.first = InAs_mixture;
InGaAs_mixture2.second = GaAs_mixture;
InGaAs_mixture2.x = 1-0.214;



% not strained
[GaAs_suscdata,GaAs_xyspace,GaAs_zspace] = matsusc(GaAs_mixture2, 0);

% strained
[AlGaAs_suscdata,AlGaAs_xyspace,AlGaAs_zspace] = matsusc(AlGaAs_mixture2, GaAs_xyspace);
[InGaAs_suscdata,InGaAs_xyspace,InGaAs_zspace] = matsusc(InGaAs_mixture2, GaAs_xyspace);

d = [389; 253; 118; 0]*1e-10;
zspace = [GaAs_zspace; AlGaAs_zspace; InGaAs_zspace; GaAs_zspace];
suscdata = [GaAs_suscdata; AlGaAs_suscdata; InGaAs_suscdata; GaAs_suscdata];
pol = 1;

theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace
theta_Bs = theta_B(end);
theta = linspace(theta_Bs-2.5*pi/180, theta_Bs+1.00*pi/180, 500);



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



title('(400) reflection of 487 nm strained layer of AlGaAs on a GaAs substrate');


plot(180*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0))/log(10));
hold('on');
pause;
