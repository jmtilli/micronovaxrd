matZ = matdb;

lambda=1.540562e-10;
stddevrad=0;
thetaoffset=0;

GaAs = Zmat2mat(matZ.GaAs, lambda);
AlAs = Zmat2mat(matZ.AlAs, lambda);

GaAs_mixture1 = {1.0, GaAs};
AlAs_mixture1 = {1.0, AlAs};

AlAs_mixture2.first = AlAs_mixture1;
AlAs_mixture2.second = AlAs_mixture1;
AlAs_mixture2.x = 0;

GaAs_mixture2.first = GaAs_mixture1;
GaAs_mixture2.second = GaAs_mixture1;
GaAs_mixture2.x = 0;

[GaAs_susc,GaAs_xyspace,GaAs_zspace] = matsusc(GaAs_mixture2, 0);
[AlAs_susc,AlAs_xyspace,AlAs_zspace] = matsusc(AlAs_mixture2, GaAs_xyspace);

zspace = [AlAs_zspace; GaAs_zspace];
suscdata = [AlAs_susc; GaAs_susc];
theta=linspace(33.026-1000/3600,33.026+500/3600,1901)*pi/180;

d = [200e-9, 0.0];
mixing = [1,2];

%suscdata
%chirh = (suscdata(:,2) + conj(suscdata(:,3)))/2
%chiih = (suscdata(:,2) - conj(suscdata(:,3)))/(2*i)
%chirhabs = abs(chirh)
%chiihabs = abs(chiih)
%phase = -(angle(chirh)-angle(chiih))/pi + 1
%exit;

% GID_SL
%suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i; -.29E-04-0.13E-05i, 0.17E-04-0.11E-05i, 0.17E-04-0.11E-05i; -.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];
suscdata2=[-.21E-04-0.59E-06i, -0.12E-04-0.54E-06i, -0.12E-04-0.54E-06i;
           -.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];
%suscdata2 *= 1.135;

%suscdata2=[-.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i; -.29E-04-0.13E-05i, 0.17E-04-0.11E-05i, 0.17E-04-0.11E-05i; -.29E-04-0.84E-06i, -0.17E-04-0.78E-06i, -0.17E-04-0.78E-06i];

%suscdata2 = suscdata;

alas_simulation = alasdat;

% ???

%plot(theta,log(1.25*XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(1.25*XRDCurve(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)),theta,log(alas_simulation(:,2)'));

%pause;

plot(theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)));
pause;

suscdata2 = suscdata;
suscdata2(:,3) = 0.5*suscdata2(:,3);

plot(theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)));
%legend('XRDCurve','XRDCurve_simplematrix');

pause;

'simple matrix'
plot(theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)));
%legend('XRDCurve','XRDCurve_simplematrix');

pause;

'bartels'
plot(theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)));
%legend('XRDCurve','XRDCurve_simplematrix');

pause;
