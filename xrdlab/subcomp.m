matZ = matdb;

lambda=1.540562e-10;
stddevrad=0;
thetaoffset=0;

GaAs = Zmat2mat(matZ.GaAs_004, lambda);

GaAs_mixture1 = {1.0, GaAs};

GaAs_mixture2.first = GaAs_mixture1;
GaAs_mixture2.second = GaAs_mixture1;
GaAs_mixture2.x = 0;

[GaAs_susc,GaAs_xyspace,GaAs_zspace] = matsusc(GaAs_mixture2, 0);

zspace = [GaAs_zspace];
suscdata = [GaAs_susc];
theta=linspace(33.026-60/3600,33.026+60/3600,1901)*pi/180;

d = [0];
mixing = [1];

% GID_SL
%suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i; -.29E-04-0.13E-05i, 0.17E-04-0.11E-05i, 0.17E-04-0.11E-05i; -.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];

suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];
suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];
%suscdata2=[-.29E-04-1.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];

%suscdata2 *= 1.135;

%suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i; -.29E-04-0.13E-05i, 0.17E-04-0.11E-05i, 0.17E-04-0.11E-05i; -.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];

%suscdata2 = suscdata;

sub_simulation = subdat;
sub_simulation = sub_simulation/(1.282875);
%sub_simulation = sub_simulation/(1.21796745590958);
%mean(sub_simulation)
%mean(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:),  theta, stddevrad, thetaoffset))

% ???

zspace = zspace*1.000008;

plot(theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)),theta,log(sub_simulation(:,2)'));

pause;

plot(theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_bartels(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)));
legend('XRDCurve','XRDCurve_bartels');

pause;
