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

% not strained
[Si_suscdata,Si_xyspace,Si_zspace] = matsusc(Si_mixture2, 0);

% strained
[SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);

%zspace = [5.67, 5.6532];
d = [657; 0]*1e-10;
zspace = [SiGe_zspace; Si_zspace];
suscdata = [SiGe_suscdata; Si_suscdata];

theta_B = asin(lambda./(2*zspace)); % Bragg's law. Order of reflection already included in zspace
theta_Bs = theta_B(end);
theta = linspace(theta_Bs-3600*pi/180/3600, theta_Bs+1800*pi/180/3600, 500);

stddevrad = pi/180*0.013;

%theta_Bs*180/pi


meas = XRDCurve(lambda, d, zspace, suscdata, theta - 0.01*pi/180, stddevrad);
meas = (meas+3e-7) * 8e7;


% poissrnd is slow for large values

% This is for Matlab and new Octave
%meas = poissrnd(meas .* (meas<100)) + (randn(size(meas)) .* sqrt(meas) + meas) .* (meas>=100);
% This is for old Octave
meas = poisson_rnd(meas .* (meas<100)) + (randn(size(meas)) .* sqrt(meas) + meas) .* (meas>=100);

meas = meas / 8e7;
meas = tanh(20*meas)/20; % detector saturation


stddevrad = pi/180*0.010; % slightly different than in meas


d_min = [500, 600]*1e-10;
d_max = [800, 600]*1e-10;
d = [600, 600]*1e-10;
x_min = [0.00, 0];
x_max = [0.30, 0];
x = [0.15, 0];
%indices = [1,2,1,2,1,2,1,2,1,2];
indices = [1,2];

%d = [657, 0]*1e-10;
%d_min = d;
%d_max = d;
%x = [0.092, 0];
%x_min = x;
%x_max = x;



prodfactor_min = -2;
prodfactor = -0.4;
prodfactor_max = 1.3;

sumterm_min = -90;
sumterm = -66;
sumterm_max = -60;
thetaoffset_min = -0.05 * pi / 180;
thetaoffset_max = 0.05 * pi / 180;
thetaoffset = 0;

rel = [0, 1]; % actually the degree of relaxation of substrate doesn't matter, since it's always fully relaxed
rel_min = [0, 1];
rel_max = [0, 1];

mixtures{1} = SiGe_mixture2;
mixtures{2} = Si_mixture2;

ctx = fitDE_initXRD(theta, meas, mixtures, lambda, stddevrad, rel_min, rel, rel_max, d_min, d, d_max, x_min, x, x_max, prodfactor_min, prodfactor, prodfactor_max, sumterm_min, sumterm, sumterm_max, thetaoffset_min, thetaoffset, thetaoffset_max, indices, 'CovDE', 40);

for iter=1:80
    ctx = fitDE(ctx);
    y = fitDE_best(ctx);
    bestfitness = fitDE_best_fitness(ctx);
    medianfitness = fitDE_median_fitness(ctx);

    iter,bestfitness, medianfitness

    prodfactor = y(1);
    sumterm = y(2);
    thetaoffset = y(3);
    y2 = y(4:end);

    d = y2(1:end/3);
    x = y2(end/3+1:2*end/3);
    rel = y2(2*end/3+1:end);

    prodfactor,sumterm,thetaoffset,x,d

    simul = XRDindividuals(y,ctx.q);

    plot(theta, log(meas), theta, log(simul*10^(prodfactor/10) + 10^(sumterm/10)));
end

%% Calculate the curve without optimization to ensure we get the same result
%for k = 1:length(x)
%    mixtures{k}.x = x(k);
%    [suscdata_k, xyspace_k, zspace_k] = matsusc(mixtures{k}, forcexyspace(k));
%    suscdata(k,:) = suscdata_k;
%    zspace(k) = zspace_k;
%end

simul = XRDindividuals(y,ctx.q);

plot(theta, log(meas), theta, log(simul*10^(prodfactor/10) + 10^(sumterm/10)));
pause;


% xlabel('Deviation from Bragg''s angle (seconds)');
% ylabel('Reflectivity (dB)');
% legend('65.7 nm SiGe on Si');
% pause;
% 
% title('(400) reflection of 65.7 nm strained layer of SiGe on a Si substrate');
% plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, stddevrad))/log(10));
% hold('on');
% 
% 
% d = [700; 0]*1e-10;
% plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, stddevrad))/log(10));
% 
% d = [657; 0]*1e-10;
% SiGe_mixture2.x = 0.105;
% [SiGe_suscdata,SiGe_xyspace,SiGe_zspace] = matsusc(SiGe_mixture2, Si_xyspace);
% zspace = [SiGe_zspace; Si_zspace];
% suscdata = [SiGe_suscdata; Si_suscdata];
% plot(180*3600*(theta-theta_Bs)/pi, 10*log(XRDCurve(lambda, d, zspace, suscdata, theta, stddevrad))/log(10));
% 
% xlabel('Deviation from Bragg''s angle (seconds)');
% ylabel('Reflectivity (dB)');
% legend('65.7 nm SiGe on Si','70 nm SiGe on Si','65.7 nm SiGe with a different amount of Si');
% pause;
