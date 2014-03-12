matZ = matdb;

pol = 1; % s polarization

lambda=1.540562e-10;
stddevrad=0;
thetaoffset=0;

GaAs = Zmat2mat(matZ.GaAs_004, lambda);
InAs = Zmat2mat(matZ.InAs_004, lambda);

GaAs_mixture1 = {1.0, GaAs};
InAs_mixture1 = {1.0, InAs};

InGaAs_mixture2.first = GaAs_mixture1;
InGaAs_mixture2.second = InAs_mixture1;
InGaAs_mixture2.x = 0.2676;

GaAs_mixture2.first = GaAs_mixture1;
GaAs_mixture2.second = GaAs_mixture1;
GaAs_mixture2.x = 0;

[GaAs_susc,GaAs_xyspace,GaAs_zspace] = matsusc(GaAs_mixture2, 0);
[InGaAs_susc,InGaAs_xyspace,InGaAs_zspace] = matsusc(InGaAs_mixture2, GaAs_xyspace);

zspace = [GaAs_zspace; InGaAs_zspace; GaAs_zspace];
suscdata = [GaAs_susc; InGaAs_susc; GaAs_susc];
theta=linspace(33.026-7000/3600,33.026+2500/3600,1901)*pi/180;

d = [18.5e-9, 6.9e-9, 0.0];
mixing = [1,2,1,2,1,2,3];

% GID_SL
suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i; -.29E-04-0.13E-05i, -0.17E-04-0.11E-05i, -0.17E-04-0.11E-05i; -.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];

suscdata2 = conj(suscdata2);

gidsl_simulation = gidsldat_pol;

plot(theta,log(XRDCurve_pol(lambda, pol, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_pol(lambda, pol, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)),theta,log(gidsl_simulation(:,2)'));

pause;
