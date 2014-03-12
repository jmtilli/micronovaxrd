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

% strained
[SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);


%zspace = [5.67, 5.6532];
d = [200; 200; 200; 00; 200; 200; 00; 200; 200; 0]*1e-10;
zspace = [SiGe_zspace; SiGe_zspace; SiGe_zspace; SiGe_zspace; SiGe_zspace; SiGe_zspace; SiGe_zspace; SiGe_zspace; SiGe_zspace;  Si_zspace];
suscdata = [SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; SiGe_suscdata; Si_suscdata];
pol = 1;

theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace
theta_Bs = theta_B(end);
theta = linspace(theta_Bs-3600*pi/180/3600, theta_Bs+1800*pi/180/3600, 1500);

time1 = time;
for k=1:100
    XRDCurve(lambda, d, zspace, suscdata, theta, 0);
end
time2 = time;
time2-time1
