lambda = 1.54056e-10;

matZ = matdb;

Si = Zmat2mat(matZ.Si, lambda);
Ge = Zmat2mat(matZ.Ge, lambda);

Si_mixture = {1.0, Si};
Ge_mixture = {1.0, Ge};

Si_mixture2.first = Si_mixture;
Si_mixture2.second = Si_mixture;
Si_mixture2.x = 0;

SiGe_mixture2.first = Si_mixture;
SiGe_mixture2.second = Ge_mixture;
SiGe_mixture2.x = 0.092;

% not strained
[Si_suscdata,Si_xyspace,Si_zspace] = matsusc(Si_mixture2, 0);

% strained
[SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);


%zspace = [5.67, 5.6532];
d = [657; 0]*1e-10;
zspace = [SiGe_zspace; Si_zspace];
suscdata = [SiGe_suscdata; Si_suscdata];
pol = 1;

chi_0 = suscdata(:,1);

theta = linspace(0, 2*pi/180, 700);

title('XRR of 65.7 nm strained layer of SiGe on a Si substrate');
plot(180*theta/pi, 10*log(xrrCurve_s(theta, [0;d], [0;chi_0], [0;0*d], lambda, 0))/log(10));


xlabel('Grazing angle');
ylabel('Reflectivity (dB)');
legend('65.7 nm SiGe on Si');
pause;




SiGe_mixture2.x = 0.12;

% not strained
[Si_suscdata,Si_xyspace,Si_zspace] = matsusc(Si_mixture2, 0);

% strained
[SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);


%zspace = [5.67, 5.6532];
d = [657; 0]*1e-10;
zspace = [SiGe_zspace; Si_zspace];
suscdata = [SiGe_suscdata; Si_suscdata];
pol = 1;

chi_0 = suscdata(:,1);

theta = linspace(0, 2*pi/180, 700);

title('XRR of 65.7 nm strained layer of SiGe on a Si substrate');
plot(180*theta/pi, 10*log(xrrCurve_s(theta, [0;d], [0;chi_0], [0;0*d], lambda, 0))/log(10));


xlabel('Grazing angle');
ylabel('Reflectivity (dB)');
legend('65.7 nm SiGe on Si');
pause;
