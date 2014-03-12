matZ = matdb;

lambda=1.540562e-10;
stddevrad=0;
thetaoffset=0;

GaAs = Zmat2mat(matZ.GaAs_004, lambda);
AlAs = Zmat2mat(matZ.AlAs_004, lambda);

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
%mixing = [1,2,1,2,1,2,3];
mixing = [1,2];

submixing = [2];

% GID_SL

%gidsl_simulation = gidsldat;

%plot(theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata2(mixing,:), theta, stddevrad, thetaoffset)),theta,log(gidsl_simulation(:,2)'));

%pause;

plot(theta,log(XRDCurve(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_simplematrix(lambda, d(mixing), zspace(mixing), suscdata(mixing,:), theta, stddevrad, thetaoffset)));
hold('on');
plot(theta,log(XRDCurve(lambda, d(submixing), zspace(submixing), suscdata(submixing,:), theta, stddevrad, thetaoffset)),theta,log(XRDCurve_simplematrix(lambda, d(submixing), zspace(submixing), suscdata(submixing,:), theta, stddevrad, thetaoffset)));
legend('XRDCurve','XRDCurve_simplematrix','XRDCurve of substrate','XRDCurve_simplematrix of substrate');

pause;
