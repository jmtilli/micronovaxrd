lambda = 1.54056e-10;

matZ = matdb;

Si = Zmat2mat(matZ.Si_004, lambda);
Ge = Zmat2mat(matZ.Ge_004, lambda);

Si_mixture = {1.0, Si};
Ge_mixture = {1.0, Ge};

Si_mixture2.first = Si_mixture;
Si_mixture2.second = Si_mixture;
Si_mixture2.x = 0;

SiGe_mixture2.first = Si_mixture;
SiGe_mixture2.second = Ge_mixture;
SiGe_mixture2.x = 0.092;
%SiGe_mixture2.x = 0.15;

% not strained
[Si_suscdata,Si_xyspace,Si_zspace] = matsusc(Si_mixture2, 0);

Si_suscdata

% strained
[SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);


%zspace = [5.67, 5.6532];
d = [657; 0]*1e-10;
zspace = [SiGe_zspace; Si_zspace];
suscdata = [SiGe_suscdata; Si_suscdata];

theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace
theta_Bs = theta_B(end);
theta = linspace(theta_Bs-3600*pi/180/3600, theta_Bs+1800*pi/180/3600, 500);


title('(400) reflection of 65.7 nm strained layer of SiGe on a Si substrate');
plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0, 0))/log(10));


xlabel('Deviation from Bragg''s angle (seconds)');
ylabel('Reflectivity (dB)');
legend('65.7 nm SiGe on Si');
pause;

title('(400) reflection of 65.7 nm strained layer of SiGe on a Si substrate');
plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0, 0))/log(10));
hold('on');


d = [700; 0]*1e-10;
plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0, 0))/log(10));

d = [657; 0]*1e-10;
SiGe_mixture2.x = 0.105;
[SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);
zspace = [SiGe_zspace; Si_zspace];
suscdata = [SiGe_suscdata; Si_suscdata];
plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, 0, 0))/log(10));

xlabel('Deviation from Bragg''s angle (seconds)');
ylabel('Reflectivity (dB)');
legend('65.7 nm SiGe on Si','70 nm SiGe on Si','65.7 nm SiGe with a different amount of Si');
pause;
